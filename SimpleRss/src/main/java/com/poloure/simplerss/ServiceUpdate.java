/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.DisplayMetrics;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
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
   static final String ITEM_LIST = "item_list.txt";
   static final String CONTENT = "content.txt";
   /* Folders */
   static final String THUMBNAIL_DIR = "thumbnails" + File.separatorChar;
   private static final char ITEM_SEPARATOR = '|';
   /* Parser saves */
   private static final String INDEX_IMAGE = "image|";
   private static final String INDEX_TITLE = "title|";
   private static final String INDEX_TIME = "pubDate|";
   private static final String INDEX_LINK = "link|";
   private static final String INDEX_LINK_TRIMMED = "blink|";
   private static final String INDEX_MIME = "mime|";
   private static final String[] INDEX_DES = {"x", "y", "z"};
   private static final String MIME_GIF = "image/gif";
   private static final int MIN_IMAGE_WIDTH = 64;
   private static final Pattern PATTERN_WHITESPACE = Pattern.compile("[\\t\\n\\x0B\\f\\r\\|]");
   private static final Pattern PATTERN_CDATA = Pattern.compile("\\<.*?\\>");
   private static final String[] DESIRED_TAGS = {
         "link", "published", "pubDate", "description", "title", "content", "entry", "item"
   };
   private static final int FEED_ITEM_INITIAL_CAPACITY = 200;
   private static final int DEFAULT_MAX_HISTORY = 10000;

   public
   ServiceUpdate()
   {
      super("Service");
   }

   private static
   void collection(String fileName, Iterable<?> content, String fileFolder)
   {
      if(Read.isUnmounted())
      {
         return;
      }

      BufferedWriter out = Write.open(fileFolder + fileName, false);

      try
      {
         for(Object item : content)
         {
            out.write(item + Write.NEW_LINE);
         }
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      finally
      {
         Read.close(out);
      }
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

      /* Make a new paint object so we can break texts. */
      new ViewCustom(this, 0);

      /* Get the tagList (from disk if it is empty). */
      List<String> tagList = PagerAdapterFeeds.TAG_LIST;
      if(tagList.isEmpty())
      {
         Set<String> set = PagerAdapterFeeds.getTagsFromDisk(applicationFolder, this);
         tagList.addAll(set);
      }

      String tag = tagList.get(page);

      String[][] content = Read.csvFile(Read.INDEX, applicationFolder, 'f', 'u', 't');
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
            catch(IOException e)
            {
               e.printStackTrace();
            }
         }
      }

      /* If action_bar_menu is running. */
      if(null != FeedsActivity.s_serviceHandler)
      {
         Message message = new Message();
         Bundle bundle = new Bundle();
         bundle.putInt("page_number", page);
         message.setData(bundle);
         FeedsActivity.s_serviceHandler.sendMessage(message);
      }

      wakeLock.release();
      stopSelf();
   }

   private
   void parseFeed(String urlString, String feed, String applicationFolder) throws
         XmlPullParserException, IOException
   {
      String feedFolder = feed + File.separatorChar;
      String contentFileName = feedFolder + CONTENT;
      String longFileName = feedFolder + ITEM_LIST;
      String thumbnailDir = feedFolder + THUMBNAIL_DIR;

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

      Resources resources = getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      int screenWidth = metrics.widthPixels;

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

      /* Create the date format. */
      SimpleDateFormat rssDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

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
               feedItemBuilder.append(ITEM_SEPARATOR);
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

               feedItemBuilder.append(INDEX_LINK);
               feedItemBuilder.append(link);
               feedItemBuilder.append(ITEM_SEPARATOR);

               feedItemBuilder.append(INDEX_LINK_TRIMMED);
               feedItemBuilder = appendFitToScreen(feedItemBuilder, link, screenWidth, 1);
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
               catch(RuntimeException ignored)
               {
                  System.out.println("BUG : RFC3339, looks like: " + contentText);
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
                  Date date = rssDate.parse(contentText);
                  calendar.setTime(date);
                  timeLong = calendar.getTimeInMillis();
               }
               catch(ParseException ignored)
               {
                  System.out.println("BUG : rfc882, looks like: " + contentText);
                  time.setToNow();
                  timeLong = time.toMillis(true);
               }

               timeString = Long.toString(timeLong);
               feedItemBuilder.append(INDEX_TIME);
               feedItemBuilder.append(timeString);
               feedItemBuilder.append(ITEM_SEPARATOR);
            }

            /* Title. */
            else if(4 == index)
            {
               parser.next();
               String content = parser.getText();
               content = null != content ? PATTERN_WHITESPACE.matcher(content).replaceAll(" ") : "";

               /* "title|". */
               feedItemBuilder.append(INDEX_TITLE);
               feedItemBuilder = appendFitToScreen(feedItemBuilder, content, screenWidth, 0);
               feedItemBuilder.append(ITEM_SEPARATOR);
            }

            /* Content, description. */
            else if(-1 != index)
            {
               parser.next();
               String content = parser.getText();
               content = null != content ? PATTERN_WHITESPACE.matcher(content).replaceAll(" ") : "";

               int imgPosition = content.indexOf("img");
               int srcPosition = content.indexOf("src", imgPosition);
               if(-1 != imgPosition && -1 != srcPosition)
               {
                  /* Find which mark is being used. */
                  int apostrophePosition = content.indexOf('\'', srcPosition);
                  int quotePosition = content.indexOf('\"', srcPosition);
                  int usedMarkPosition;

                  if(-1 != apostrophePosition && -1 != quotePosition)
                  {
                     usedMarkPosition = Math.min(apostrophePosition, quotePosition);
                  }
                  else
                  {
                     usedMarkPosition = -1 == quotePosition ? apostrophePosition : quotePosition;
                  }

                  char quote = content.charAt(usedMarkPosition);
                  int finalPosition = content.indexOf(quote, usedMarkPosition + 1);

                  String imgLink = content.substring(usedMarkPosition + 1, finalPosition);
                  feedItemBuilder.append(INDEX_IMAGE);
                  feedItemBuilder.append(imgLink);
                  feedItemBuilder.append(ITEM_SEPARATOR);

                  int lastSlash = imgLink.lastIndexOf('/') + 1;
                  String imgName = imgLink.substring(lastSlash);

                  String thumbnailPath = applicationFolder + thumbnailDir + imgName;

                  File file = new File(thumbnailPath);
                  if(!file.exists())
                  {
                     feedItemBuilder = compressImage(thumbnailDir, imgLink, imgName, this,
                           feedItemBuilder);
                  }
               }

               /* Replace ALL_TAG <x> with nothing. */
               content = PATTERN_CDATA.matcher(content).replaceAll("").trim();
               if(!content.isEmpty())
               {
                  String tagToAppend = DESIRED_TAGS[5].equals(tag) ? DESIRED_TAGS[3] : tag;

                  if(tagToAppend.equals(DESIRED_TAGS[3]))
                  {
                     feedItemBuilder = appendDesLines(feedItemBuilder, content, screenWidth);
                  }
                  else
                  {
                     feedItemBuilder.append(tagToAppend);
                     feedItemBuilder.append(ITEM_SEPARATOR);
                     feedItemBuilder.append(content);
                     feedItemBuilder.append(ITEM_SEPARATOR);
                  }
               }
            }
         }
         else if(XmlPullParser.END_TAG == eventType)
         {
            String tag = parser.getName();
            boolean newItem = !longSet.contains(timeLong);

            /* "entry", "item" */
            if(DESIRED_TAGS[6].equals(tag) || DESIRED_TAGS[7].equals(tag) && newItem)
            {
               String finalLine = feedItemBuilder.toString();
               map.put(timeLong, finalLine);
            }
         }
         else if(XmlPullParser.END_DOCUMENT == eventType)
         {
            /* TODO Get MAX_HISTORY setting */
            int saveSize = DEFAULT_MAX_HISTORY;
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

            collection(contentFileName, valueList, applicationFolder);
            Write.longSet(longFileName, keyList, applicationFolder);

            return;
         }
         parser.next();
      }
   }

   private static
   StringBuilder appendDesLines(StringBuilder builder, String content, int screenWidth)
   {
      String contentCopy = content;
      for(int x = 0; 3 > x; x++)
      {
         int desChars = ViewCustom.PAINTS[2]
               .breakText(contentCopy, true, screenWidth - 40.0F, null);
         int desSpace = contentCopy.lastIndexOf(' ', desChars);
         desChars = -1 == desSpace ? desChars : desSpace + 1;

         builder.append(INDEX_DES[x]);
         builder.append(ITEM_SEPARATOR);
         builder.append(contentCopy.substring(0, desChars));
         builder.append(ITEM_SEPARATOR);

         contentCopy = contentCopy.substring(desChars);
      }
      return builder;
   }

   private static
   StringBuilder appendFitToScreen(StringBuilder builder, String content, int screenWidth, int ind)
   {
      int chars = ViewCustom.PAINTS[ind].breakText(content, true, screenWidth - 40.0F, null);

      int space = content.lastIndexOf(' ', chars);

      String trimmed = content.substring(0, -1 == space ? chars : space);
      builder.append(trimmed);

      return builder;
   }

   private static
   Set<String> fileToSet(String fileName, String fileFolder)
   {
      Set<String> set = new LinkedHashSet<String>(64);

      if(Read.isUnmounted())
      {
         return set;
      }

      String[] lines = Read.file(fileName, fileFolder);
      Collections.addAll(set, lines);

      return set;
   }

   private static
   StringBuilder compressImage(String thumbnailDir, String imgLink, String imgName, Context context,
         StringBuilder builder)
   {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      InputStream inputStream;
      try
      {
         URL imageUrl = new URL(imgLink);
         inputStream = imageUrl.openStream();
      }
      catch(MalformedURLException ignored)
      {
         return builder;
      }
      catch(IOException ignored)
      {
         return builder;
      }

      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      float screenWidth = displayMetrics.widthPixels;

      String applicationFolder = FeedsActivity.getApplicationFolder(context);
      BitmapFactory.decodeStream(inputStream, null, options);

      float imageWidth = options.outWidth;
      float imageHeight = options.outHeight;
      String mimeType = options.outMimeType;

      /* If the image is smaller than we care about, do not save it. */
      if(MIN_IMAGE_WIDTH > imageWidth)
      {
         return builder;
      }

      /* Save these details before we possible return. */
      builder.append(INDEX_MIME);
      builder.append(mimeType);
      builder.append(ITEM_SEPARATOR);

      /* If images are disabled. */
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      boolean imagesDisabled = !preferences.getBoolean("images_enabled", true);
      if(imagesDisabled)
      {
         return builder;
      }

      FileOutputStream out = null;

      if(MIME_GIF.equals(mimeType))
      {
         try
         {
            inputStream.close();
            URL imageUrl = new URL(imgLink);
            inputStream = imageUrl.openStream();
            out = new FileOutputStream(applicationFolder + thumbnailDir + imgName);

            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            while(-1 != bytesRead)
            {
               out.write(buffer, 0, bytesRead);
               bytesRead = inputStream.read(buffer);
            }
         }
         catch(IOException ignored)
         {
         }
         finally
         {
            Read.close(out);
         }
      }
      else
      {
         float inSample = imageWidth / screenWidth;
         int height = Math.round(imageHeight / inSample);

         try
         {
            inputStream.close();
            URL imageUrl = new URL(imgLink);
            inputStream = imageUrl.openStream();
         }
         catch(MalformedURLException ignored)
         {
            return builder;
         }
         catch(IOException ignored)
         {
            return builder;
         }

         BitmapFactory.Options o2 = new BitmapFactory.Options();
         o2.inSampleSize = 1;
         Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, o2);

         /* Scale it to the screen width. */
         bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(screenWidth), height, false);

         /* Shrink it to IMAGE_HEIGHT. */
         int newHeight = Math.min(bitmap.getHeight(), ViewCustom.IMAGE_HEIGHT);
         bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), newHeight);

         try
         {
            out = new FileOutputStream(applicationFolder + thumbnailDir + imgName);

            /* Get the quality from settings. */
            String qualityString = preferences.getString("thumbnail_quality", "75");
            int quality = Integer.parseInt(qualityString);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
         }
         catch(FileNotFoundException e)
         {
            e.printStackTrace();
         }
         finally
         {
            Read.close(out);
         }
      }

      return builder;
   }

   /* index throws an ArrayOutOfBoundsException if not handled. */
   private static
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
}
