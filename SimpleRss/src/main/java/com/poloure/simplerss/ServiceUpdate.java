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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public
class ServiceUpdate extends IntentService
{
   static final         String           ITEM_LIST                   = "item_list.txt";
   static final         String           CONTENT                     = "content.txt";
   /* Folders */
   static final         String           THUMBNAIL_DIR               = "thumbnails" +
         File.separatorChar;
   /* Parser saves */
   private static final String           INDEX_IMAGE                 = "image|";
   private static final String           INDEX_TIME                  = "pubDate|";
   private static final String           INDEX_HEIGHT                = "height|";
   private static final String           INDEX_WIDTH                 = "width|";
   private static final SimpleDateFormat RSS_DATE                    = new SimpleDateFormat(
         "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
   @SuppressWarnings("HardcodedLineSeparator")
   private static final Pattern          PATTERN_WHITESPACE          = Pattern.compile(
         "[\\t\\n\\x0B\\f\\r\\|]");
   private static final String[]         DESIRED_TAGS                = {
         "link", "published", "pubDate", "description", "title", "content", "entry", "item"
   };
   private static final int              FEED_ITEM_INITIAL_CAPACITY  = 200;
   private static final double           AMOUNT_OF_SCREEN_IMAGE_USES = 0.944;
   private static final char             ITEM_SEPARATOR              = '|';

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

      int page = intent.getIntExtra("GROUP_NUMBER", 0);
      String applicationFolder = FeedsActivity.getApplicationFolder(this);
      String allTag = getString(R.string.all_tag);

      Set<String> tagSet = PagerAdapterFeeds.getTagsFromDisk(applicationFolder, allTag);
      int tagSize = tagSet.size();
      String tag = tagSet.toArray(new String[tagSize])[page];

      String[][] content = Read.indexFile(applicationFolder);
      String[] names = content[0];
      String[] urls = content[1];
      String[] tags = content[2];

      boolean isAllTag = tag.equals(allTag);

      /* Download and parse each feed in the index. */
      int namesLength = names.length;
      for(int i = 0; i < namesLength; i++)
      {
         if(isAllTag || tags[i].contains(tag))
         {
            String path1 = applicationFolder + names[i] +
                  File.separatorChar +
                  THUMBNAIL_DIR;
            File folder = new File(path1);
            folder.mkdirs();

            try
            {
               parseFeed(urls[i], names[i], applicationFolder);
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

      /* TODO GET UNREAD WITHOUT CONTEXT. int[] unreadCounts = FeedsActivity.getUnreadCounts
      (this); */

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

   private
   void parseFeed(String urlString, String feed, String applicationFolder)
         throws XmlPullParserException, IOException
   {
      String feedFolder = feed + File.separatorChar;
      String contentFileName = feedFolder + CONTENT;
      String longFileName = feedFolder + ITEM_LIST;
      String thumbnailDir = feedFolder + THUMBNAIL_DIR;
      /*String[] filters = Read.file(Constants.FILTER_LIST);*/

      /* Load the previously saved items to a map. */
      Set<String> set = fileToSet(contentFileName, applicationFolder);
      Set<Long> longSet = Read.longSet(longFileName, applicationFolder);

      int setSize = set.size();
      String[] lines = set.toArray(new String[setSize]);

      int longSize = longSet.size();
      Long[] longs = longSet.toArray(new Long[longSize]);

      Comparator<Long> longComparator = Collections.reverseOrder();
      Map<Long, String> map = new TreeMap<Long, String>(longComparator);

      for(int i = 0; i < setSize; i++)
      {
         map.put(longs[i], lines[i]);
      }

      Time time = new Time();

      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser parser = factory.newPullParser();

      URL url = new URL(urlString);
      InputStream inputStream = url.openStream();
      parser.setInput(inputStream, null);

      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;

      StringBuilder feedItemBuilder = new StringBuilder(FEED_ITEM_INITIAL_CAPACITY);

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

      long timeLong = 0L;

      while(true)
      {
         int eventType = parser.getEventType();

         if(XmlPullParser.START_TAG == eventType)
         {
            String tag = parser.getName();

            int index = index(DESIRED_TAGS, tag);
            String timeString;

            /* "entry", "item" */
            if(5 < index)
            {
               feedItemBuilder.setLength(0);
               timeLong = 0L;
            }
            else if(0 == index)
            {
               String link = parser.getAttributeValue(null, "href");
               if(null == link)
               {
                  parser.next();
                  link = parser.getText();
               }
               feedItemBuilder.append("link|");
               feedItemBuilder.append(link);
               feedItemBuilder.append(ITEM_SEPARATOR);
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
                  Write.toLogFile("BUG : RFC3339, looks like: " + contentText, applicationFolder);
                  time.setToNow();
               }

               timeLong = time.toMillis(true);

               timeString = Long.toString(timeLong);
               feedItemBuilder.append(INDEX_TIME);
               feedItemBuilder.append(timeString);
               feedItemBuilder.append(ITEM_SEPARATOR);
            }

            /* "pubDate" - It follows the rss 2.0 specification for rfc882. */
            else if(2 == index)
            {
               parser.next();
               String contentText = parser.getText();

               try
               {
                  Calendar calendar = Calendar.getInstance();
                  Date date = RSS_DATE.parse(contentText);
                  calendar.setTime(date);
                  timeLong = calendar.getTimeInMillis();
               }
               catch(Exception ignored)
               {
                  Write.toLogFile("BUG : rfc882, looks like: " + contentText, applicationFolder);
                  time.setToNow();
                  timeLong = time.toMillis(true);
               }

               timeString = Long.toString(timeLong);
               feedItemBuilder.append(INDEX_TIME);
               feedItemBuilder.append(timeString);
               feedItemBuilder.append(ITEM_SEPARATOR);
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
                  feedItemBuilder.append(INDEX_IMAGE);
                  feedItemBuilder.append(imgLink);
                  feedItemBuilder.append(ITEM_SEPARATOR);

                  int lastSlash = imgLink.lastIndexOf('/') + 1;
                  String imgName = imgLink.substring(lastSlash);

                  String thumbnailPath = applicationFolder + thumbnailDir + imgName;

                  File file = new File(thumbnailPath);
                  if(!file.exists())
                  {
                     compressImage(thumbnailDir, imgLink, imgName, this);
                  }

                  /* ISSUE #194 */
                  BitmapFactory.decodeFile(thumbnailPath, options);

                  feedItemBuilder.append(INDEX_WIDTH);
                  feedItemBuilder.append(options.outWidth);
                  feedItemBuilder.append(ITEM_SEPARATOR);
                  feedItemBuilder.append(INDEX_HEIGHT);
                  feedItemBuilder.append(options.outHeight);
                  feedItemBuilder.append(ITEM_SEPARATOR);
               }

               String tagToAppend = DESIRED_TAGS[5].equals(tag) ? DESIRED_TAGS[3] : tag;

               feedItemBuilder.append(tagToAppend);
               feedItemBuilder.append(ITEM_SEPARATOR);
               feedItemBuilder.append(content);
               feedItemBuilder.append(ITEM_SEPARATOR);
            }
         }
         else if(XmlPullParser.END_TAG == eventType)
         {
            String tag = parser.getName();

            /* "entry", "item" */
            if(DESIRED_TAGS[6].equals(tag) || DESIRED_TAGS[7].equals(tag))
            {
               String finalLine = feedItemBuilder.toString();
               map.put(timeLong, finalLine);
            }
         }
         else if(XmlPullParser.END_DOCUMENT == eventType)
         {
            Resources resources = getResources();
            String[] settingTitles = resources.getStringArray(R.array.settings_function_titles);
            String settingName = FeedsActivity.SETTINGS_DIR + settingTitles[5] + ".txt";
            String[] check = Read.file(settingName, applicationFolder);
            String setting = 0 == check.length ? "" : check[0];

            int saveSize = 0 == setting.length() ? 100000 : Integer.parseInt(setting);
            int mapSize = map.size();

            saveSize = saveSize >= mapSize ? mapSize : saveSize;

            Long[] mapKeys = new Long[saveSize];
            String[] mapValues = new String[saveSize];

            Set<Long> finalLongSet = map.keySet();
            Collection<String> finalSet = map.values();

            Long[] longArray = finalLongSet.toArray(new Long[saveSize]);
            String[] stringArray = finalSet.toArray(new String[saveSize]);

            for(int i = 0; i < saveSize; i++)
            {
               mapKeys[i] = longArray[i];
               mapValues[i] = stringArray[i];
            }

            List<String> valueList = Arrays.asList(mapValues);
            List<Long> keyList = Arrays.asList(mapKeys);

            Write.collection(contentFileName, valueList, applicationFolder);
            Write.longSet(longFileName, keyList, applicationFolder);

            return;
         }
         parser.next();
      }
   }

   /* index throws an ArrayOutOfBoundsException if not handled. */
   static
   <T> int index(T[] array, T value)
   {
      if(null == array)
      {
         return -1;
      }

      int arrayLength = array.length;
      for(int i = 0; i < arrayLength; i++)
      {
         if(array[i].equals(value))
         {
            return i;
         }
      }
      return -1;
   }

   private static
   Set<String> fileToSet(String fileName, String fileFolder)
   {
      Set<String> set = new LinkedHashSet<String>();

      if(Read.isUnmounted())
      {
         return set;
      }

      String[] lines = Read.file(fileName, fileFolder);
      Collections.addAll(set, lines);

      return set;
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
      catch(MalformedURLException ignored)
      {
         return;
      }
      catch(IOException ignored)
      {
         return;
      }

      BitmapFactory.decodeStream(inputStream, null, o);

      float widthTmp = o.outWidth;

      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      int screenWidth = (int) Math.round(displayMetrics.widthPixels * AMOUNT_OF_SCREEN_IMAGE_USES);
      float inSample = widthTmp > screenWidth ? Math.round(widthTmp / screenWidth) : 1;

      try
      {
         inputStream.close();
         URL imageUrl = new URL(imgLink);
         inputStream = imageUrl.openStream();
      }
      catch(MalformedURLException ignored)
      {
         return;
      }
      catch(IOException ignored)
      {
         return;
      }

      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = Math.round(inSample);
      Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);

      String storage = FeedsActivity.getApplicationFolder(context);
      try
      {
         FileOutputStream out = new FileOutputStream(storage + thumbnailDir + imgName);
         bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
   }
}
