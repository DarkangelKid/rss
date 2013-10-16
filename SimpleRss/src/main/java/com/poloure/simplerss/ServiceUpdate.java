package com.poloure.simplerss;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.text.format.Time;
import android.util.DisplayMetrics;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public
class ServiceUpdate extends IntentService
{
   private static final SimpleDateFormat RSS_DATE           = new SimpleDateFormat(
         "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
   private static final Pattern          PATTERN_WHITESPACE = Pattern.compile(
         "[\\t\\n\\x0B\\f\\r\\|]");
   private static final String[]         DESIRED_TAGS       = {
         "link", "published", "pubDate", "description", "title", "content", "entry", "item"
   };

   public
   ServiceUpdate()
   {
      super("Service");
   }

   @Override
   protected
   void onHandleIntent(Intent intent)
   {
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SIMPLERSS");
      wakeLock.acquire();

      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

      int page = intent.getIntExtra("GROUP_NUMBER", 0);

      Set<String> tagSet = PagerAdapterFeeds.updateTags(this);
      int tagSize = tagSet.size();
      String tag = tagSet.toArray(new String[tagSize])[page];

      String[][] content = Read.csv(this);
      String[] names = content[0];
      String[] urls = content[1];
      String[] tags = content[2];

      String allTag = getString(R.string.all_tag);
      boolean isAllTag = tag.equals(allTag);

      /* Download and parse each feed in the index. */
      int namesLength = names.length;
      for(int i = 0; i < namesLength; i++)
      {
         if(isAllTag || tags[i].contains(tag))
         {
            String feedPath = names[i] + Constants.SEPAR;
            if(isNonExisting(feedPath, this))
            {
               String path1 = Util.getStorage(this) + feedPath + Constants.THUMBNAIL_DIR;
               File folder = new File(path1);
               folder.mkdirs();
            }

            try
            {
               parseFeed(urls[i], names[i]);
            }
            catch(XmlPullParserException e)
            {
               e.printStackTrace();
            }
            catch(MalformedURLException e)
            {
               e.printStackTrace();
            }
            catch(IOException e)
            {
               e.printStackTrace();
            }
         }
      }

      /* TODO GET UNREAD WITHOUT CONTEXT. int[] unreadCounts = Util.getUnreadCounts(this); */

      /* If activity is running. */
      if(null != FeedsActivity.s_serviceHandler)
      {
         Message message = new Message();
         Bundle bundle = new Bundle();
         bundle.putInt("page_number", page);
         message.setData(bundle);
         FeedsActivity.s_serviceHandler.sendMessage(message);
      }
      /*else if(0 != unreadCounts[0] && intent.getBooleanExtra("NOTIFICATIONS", false))
      {
         /* Calculate the number of tags with new items. */
         /*int tagItems = 1;
         int sizes = unreadCounts.length;

         for(int i = 1; i < sizes; i++)
         {
            int count = unreadCounts[i];
            if(0 != count)
            {
               tagItems++;
            }
         }

         String titleSingle = getString(R.string.notification_title_singular);
         String titlePlural = getString(R.string.notification_title_plural);
         String contentSingleItem = getString(R.string.notification_content_tag_item);
         String contentSingleTag = getString(R.string.notification_content_tag_items);
         String contentPluralTag = getString(R.string.notification_content_tags);

         String notificationTitle = 1 == unreadCounts[0]
               ? String.format(titleSingle, 1)
               : String.format(titlePlural, unreadCounts[0]);

         String notificationContent;
         if(1 == unreadCounts[0] && 1 == tagItems - 1)
         {
            notificationContent = contentSingleItem;
         }
         else
         {
            notificationContent = 1 < unreadCounts[0] && 1 == tagItems - 1
                  ? contentSingleTag
                  : String.format(contentPluralTag, tagItems - 1);
         }

         Intent notificationIntent = new Intent(this, FeedsActivity.class);

         TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

         stackBuilder.addParentStack(FeedsActivity.class);
         stackBuilder.addNextIntent(notificationIntent);
         PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
               PendingIntent.FLAG_UPDATE_CURRENT);

         Builder builder = new Builder(this);
         builder.setSmallIcon(R.drawable.rss_icon);
         builder.setContentTitle(notificationTitle);
         builder.setContentText(notificationContent);
         builder.setAutoCancel(true);
         builder.setContentIntent(pendingIntent);

         NotificationManager notificationManager = (NotificationManager) getSystemService(
               Context.NOTIFICATION_SERVICE);
         notificationManager.notify(1, builder.build());
      }*/

      wakeLock.release();
      stopSelf();
   }

   boolean isNonExisting(String path, Context context)
   {
      String path1 = Util.getStorage(context) + path;
      if(Util.isUsingSd() || path1.contains(Constants.THUMBNAIL_DIR))
      {
         File file = new File(path1);
         return !file.exists();
      }
      else
      {
         String internalPath = Util.getInternalPath(path1);
         File file = getFileStreamPath(internalPath);
         return !file.exists();
      }
   }

   private
   void parseFeed(String urlString, String feed)
         throws XmlPullParserException, MalformedURLException, IOException
   {
      String contentFile = feed + Constants.SEPAR + Constants.CONTENT;
      String longFile = feed + Constants.SEPAR + Constants.ITEM_LIST;
      String thumbnailDir = feed + Constants.SEPAR + Constants.THUMBNAIL_DIR;
      /*String[] filters = Read.file(Constants.FILTER_LIST);*/

      Set<String> set = Read.set(contentFile, this);
      Set<Long> longSet = Read.longSet(longFile, this);

      Time time = new Time();

      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser parser = factory.newPullParser();

      URL url = new URL(urlString);
      InputStream inputStream = url.openStream();
      parser.setInput(inputStream, null);

      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;

      StringBuilder stringBuilder = new StringBuilder(200);

      boolean preEntry = true;

      while(preEntry)
      {
         parser.next();
         int eventType = parser.getEventType();
         if(XmlPullParser.START_TAG == eventType)
         {
            String tag = parser.getName();
            if(DESIRED_TAGS[6].equals(tag) || DESIRED_TAGS[7].equals(tag))
            {
               preEntry = false;
            }
         }
         else if(XmlPullParser.END_DOCUMENT == eventType)
         {
            return;
         }
      }

      while(true)
      {
         int eventType = parser.getEventType();

         if(XmlPullParser.START_TAG == eventType)
         {
            String tag = parser.getName();

            int index = Util.index(DESIRED_TAGS, tag);
            long timeLong;
            String timeString;

            /* "entry", "item" */
            if(5 < index)
            {
               stringBuilder.setLength(0);
            }
            else if(0 == index)
            {
               String link = parser.getAttributeValue(null, "href");
               if(null == link)
               {
                  parser.next();
                  link = parser.getText();
               }
               stringBuilder.append("link|").append(link).append('|');
            }

            /* "published" - It is an atom feed it will be one of four RFC3339 formats. */
            else if(1 == index)
            {
               parser.next();
               String contentText = parser.getText();

               try
               {
                  time.parse3339(contentText);
               }
               catch(Exception ignored)
               {
                  Write.log("BUG : RFC3339, looks like: " + contentText, this);
                  time.setToNow();
               }

               timeLong = time.toMillis(true);
               longSet.add(timeLong);

               timeString = Long.toString(timeLong);
               stringBuilder.append(Constants.TIME).append(timeString).append('|');
            }

            /* "pubDate" - It follows the rss 2.0 specification for rfc882. */
            else if(2 == index)
            {
               parser.next();
               String contentText = parser.getText();

               try
               {
                  Calendar calendar = Calendar.getInstance();
                  calendar.setTime(RSS_DATE.parse(contentText));
                  timeLong = calendar.getTimeInMillis();
               }
               catch(Exception ignored)
               {
                  Write.log("BUG : rfc882, looks like: " + contentText, this);
                  time.setToNow();
                  timeLong = time.toMillis(true);
               }

               longSet.add(timeLong);
               timeString = Long.toString(timeLong);
               stringBuilder.append(Constants.TIME).append(timeString).append('|');
            }
            else if(-1 != index)
            {
               parser.next();
               String content = parser.getText();
               content = null != content ? PATTERN_WHITESPACE.matcher(content).replaceAll(" ") : "";

               int imgPosition = content.indexOf("img");
               if(-1 != imgPosition)
               {
                  int srcPosition = content.indexOf("src", imgPosition) + 4;
                  char quote = content.charAt(srcPosition);
                  int finalPosition = content.indexOf(quote, srcPosition + 1);

                  String imgLink = content.substring(srcPosition + 1, finalPosition);
                  stringBuilder.append(Constants.IMAGE).append(imgLink).append('|');

                  int lastSlash = imgLink.lastIndexOf('/') + 1;
                  String imgName = imgLink.substring(lastSlash);

                  if(isNonExisting(thumbnailDir + imgName, this))
                  {
                     compressImage(thumbnailDir, imgLink, imgName, this);
                  }

                  /* ISSUE #194 */
                  BitmapFactory.decodeFile(Util.getStorage(this) + thumbnailDir + imgName, options);

                  stringBuilder.append(Constants.WIDTH);
                  stringBuilder.append(options.outWidth);
                  stringBuilder.append('|');
                  stringBuilder.append(Constants.HEIGHT);
                  stringBuilder.append(options.outHeight);
                  stringBuilder.append('|');
               }

               String tagToAppend = DESIRED_TAGS[5].equals(tag) ? DESIRED_TAGS[3] : tag;

               stringBuilder.append(tagToAppend).append('|').append(content).append('|');
            }
         }
         else if(XmlPullParser.END_TAG == eventType)
         {
            String tag = parser.getName();

            /* "entry", "item" */
            if(DESIRED_TAGS[6].equals(tag) || DESIRED_TAGS[7].equals(tag))
            {
               String finalLine = stringBuilder.toString();
               set.add(finalLine);
            }
         }
         else if(XmlPullParser.END_DOCUMENT == eventType)
         {
            Write.collection(contentFile, set, this);
            Write.log("size = " + Integer.toString(longSet.size()), this);
            Write.longSet(longFile, longSet, this);
            return;
         }
         parser.next();
      }
   }

   private static
   void compressImage(String thumbnailDir, String imgLink, String imgName, Context context)
   {
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      InputStream inputStream;
      try
      {
         URL imageUrl = new URL(imgLink);
         inputStream = imageUrl.openStream();
      }
      catch(IOException ignored)
      {
         return;
      }

      BitmapFactory.decodeStream(inputStream, null, o);

      float widthTmp = o.outWidth;

      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      int screenWidth = (int) Math.round(displayMetrics.widthPixels * 0.944);
      float inSample = widthTmp > screenWidth ? Math.round(widthTmp / screenWidth) : 1;

      try
      {
         inputStream.close();
         URL imageUrl = new URL(imgLink);
         inputStream = imageUrl.openStream();
      }
      catch(IOException ignored)
      {
         return;
      }

      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = Math.round(inSample);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);

      try
      {
         FileOutputStream out = new FileOutputStream(
               Util.getStorage(context) + thumbnailDir + imgName);
         bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
   }
}
