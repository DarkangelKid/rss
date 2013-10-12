package com.poloure.simplerss;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
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
   private static final int              SCREEN_WIDTH       = (int) Math.round(
         Util.getScreenWidth() * 0.944);
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
      Util.setContext(this);

      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SIMPLERSS");
      wakeLock.acquire();

      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

      int page = intent.getIntExtra("GROUP_NUMBER", 0);

      String tag = Read.file(Constants.TAG_LIST)[page];

      String[][] content = Read.csv();
      String[] names = content[0];
      String[] urls = content[1];
      String[] tags = content[2];

      /* Download and parse each feed in the index. */
      int namesLength = names.length;
      for(int i = 0; i < namesLength; i++)
      {
         if(tags[i].equals(tag) || tag.equals(Constants.ALL_TAG))
         {
            String feedPath = Util.getPath(names[i], "");
            if(Util.isNonExisting(feedPath))
            {
               Util.mkdir(feedPath + Constants.THUMBNAIL_DIR);
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

      int[] unreadCounts = Util.getUnreadCounts();

      /* If activity is running. */
      if(null != FeedsActivity.s_serviceHandler)
      {
         Message message = new Message();
         Bundle bundle = new Bundle();
         bundle.putInt("page_number", page);
         message.setData(bundle);
         FeedsActivity.s_serviceHandler.sendMessage(message);
      }
      else if(0 != unreadCounts[0] && intent.getBooleanExtra("NOTIFICATIONS", false))
      {
         /* Calculate the number of tags with new items. */
         int tagItems = 1;
         int sizes = unreadCounts.length;

         for(int i = 1; i < sizes; i++)
         {
            int count = unreadCounts[i];
            if(0 != count)
            {
               tagItems++;
            }
         }

         String titleSingle = Util.getString(R.string.notification_title_singular);
         String titlePlural = Util.getString(R.string.notification_title_plural);
         String contentSingleItem = Util.getString(R.string.notification_content_tag_item);
         String contentSingleTag = Util.getString(R.string.notification_content_tag_items);
         String contentPluralTag = Util.getString(R.string.notification_content_tags);

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

         Builder notificationBuilder = new Builder(this);
         notificationBuilder.setSmallIcon(R.drawable.rss_icon)
               .setContentTitle(notificationTitle)
               .setContentText(notificationContent)
               .setAutoCancel(true);

         Intent notificationIntent = new Intent(this, FeedsActivity.class);

         TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

         stackBuilder.addParentStack(FeedsActivity.class);
         stackBuilder.addNextIntent(notificationIntent);
         PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
               PendingIntent.FLAG_UPDATE_CURRENT);
         notificationBuilder.setContentIntent(pendingIntent);
         NotificationManager notificationManager = (NotificationManager) getSystemService(
               Context.NOTIFICATION_SERVICE);
         notificationManager.notify(1, notificationBuilder.build());
      }

      wakeLock.release();
      stopSelf();
   }

   private static
   void parseFeed(String urlString, String feed)
         throws XmlPullParserException, MalformedURLException, IOException
   {
      String contentFile = Util.getPath(feed, Constants.CONTENT);
      String thumbnailDir = Util.getPath(feed, Constants.THUMBNAILS);
      /*String[] filters = Read.file(Constants.FILTER_LIST);*/

      Set<String> set = Read.set(contentFile);

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
               catch(RuntimeException ignored)
               {
                  Write.log("BUG : RFC3339, looks like: " + contentText);
                  time.setToNow();
               }

               timeLong = time.toMillis(true);
               timeString = Long.toString(timeLong);
               stringBuilder.append("pubDate|").append(timeString).append('|');
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
               catch(ParseException ignored)
               {
                  Write.log("BUG : rfc882, looks like: " + contentText);
                  time.setToNow();
                  timeLong = time.toMillis(true);
               }

               timeString = Long.toString(timeLong);
               stringBuilder.append("pubDate|").append(timeString).append('|');
            }
            else if(-1 != index)
            {
               parser.next();
               String content = parser.getText();
               content = PATTERN_WHITESPACE.matcher(content).replaceAll(" ");

               int imgPosition = content.indexOf("img");
               if(-1 != imgPosition)
               {
                  int srcPosition = content.indexOf("src", imgPosition) + 4;
                  char quote = content.charAt(srcPosition);
                  int finalPosition = content.indexOf(quote, srcPosition + 1);

                  String imgLink = content.substring(srcPosition + 1, finalPosition);
                  stringBuilder.append("image|").append(imgLink).append('|');

                  int lastSlash = imgLink.lastIndexOf('/') + 1;
                  String imgName = imgLink.substring(lastSlash);

                  if(Util.isNonExisting(thumbnailDir + imgName))
                  {
                     compressImage(thumbnailDir, imgLink, imgName);
                  }

                  /* ISSUE #194 */
                  BitmapFactory.decodeFile(Util.getStorage() + thumbnailDir + imgName, options);

                  stringBuilder.append(Constants.WIDTH)
                        .append(options.outWidth)
                        .append('|')
                        .append(Constants.HEIGHT)
                        .append(options.outHeight)
                        .append('|');
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
            Write.collection(contentFile, set);
            return;
         }
         parser.next();
      }
   }

   private static
   void compressImage(String thumbnailDir, String imgLink, String imgName)
   {
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      InputStream inputStream;
      try
      {
         URL imageUrl = new URL(imgLink);
         inputStream = imageUrl.openStream();
      }
      catch(IOException e)
      {
         return;
      }

      BitmapFactory.decodeStream(inputStream, null, o);

      float widthTmp = o.outWidth;

      float inSample = widthTmp > SCREEN_WIDTH ? Math.round(widthTmp / SCREEN_WIDTH) : 1;

      try
      {
         inputStream.close();
         URL imageUrl = new URL(imgLink);
         inputStream = imageUrl.openStream();
      }
      catch(IOException e)
      {
         return;
      }

      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = Math.round(inSample);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);

      try
      {
         FileOutputStream out = new FileOutputStream(Util.getStorage() + thumbnailDir + imgName);
         bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
   }
}
