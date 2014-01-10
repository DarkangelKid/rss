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
import java.util.regex.Matcher;
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
   private static final String INDEX_IMAGE = "image";
   private static final String INDEX_TITLE = "title";
   private static final String INDEX_TIME = "pubDate";
   private static final String INDEX_LINK = "link";
   private static final String INDEX_LINK_TRIMMED = "blink";
   private static final String INDEX_MIME = "mime";
   private static final String[] INDEX_DES = {"x", "y", "z"};
   private static final int MIN_IMAGE_WIDTH = 64;
   private static final Pattern PATTERN_WHITESPACE = Pattern.compile("[\\t\\n\\x0B\\f\\r\\|]");
   private static final Pattern PATTERN_CDATA = Pattern.compile("\\<.*?\\>");
   private static final Pattern PATTERN_IMG = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
   private static final Pattern PATTERN_HREF = Pattern
         .compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
   private static final Pattern PATTERN_APOS = Pattern.compile("'");
   private static final Pattern PATTERN_QUOT = Pattern.compile("\"");

   private static final String TAG_LINK = "link";
   private static final String TAG_PUBLISHED = "published";
   private static final String TAG_PUBDATE = "pubDate";
   private static final String TAG_DES = "description";
   private static final String TAG_TITLE = "title";
   private static final String TAG_CONTENT = "content";
   private static final String TAG_ENTRY = "entry";
   private static final String TAG_ITEM = "item";

   private static final int FEED_ITEM_INITIAL_CAPACITY = 200;
   private static final int DEFAULT_MAX_HISTORY = 10000;

   private long m_timeCurrentItem;

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

         builder = appendItem(builder, INDEX_DES[x], contentCopy.substring(0, desChars));

         contentCopy = contentCopy.substring(desChars);
      }
      return builder;
   }

   private static
   String fitToScreen(String content, int screenWidth, int ind)
   {
      int chars = ViewCustom.PAINTS[ind].breakText(content, true, screenWidth - 40.0F, null);
      int space = content.lastIndexOf(' ', chars);

      return content.substring(0, -1 == space ? chars : space);
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
      builder = appendItem(builder, INDEX_MIME, mimeType);

      /* If images are disabled. */
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      boolean imagesDisabled = !preferences.getBoolean("images_enabled", true);
      if(imagesDisabled)
      {
         return builder;
      }

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

      FileOutputStream out = null;

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


      return builder;
   }

   private static
   StringBuilder appendItem(StringBuilder builder, String tag, String content)
   {
      builder.append(tag);
      builder.append(ITEM_SEPARATOR);
      builder.append(content);
      builder.append(ITEM_SEPARATOR);
      return builder;
   }

   private
   StringBuilder appendPublishedTime(StringBuilder builder, String content, String tag)
   {
      Time time = new Time();

      try
      {
         /* <published> - It is an atom feed it will be one of four RFC3339 formats. */
         if(TAG_PUBLISHED.equals(tag))
         {
            time.parse3339(content);
            m_timeCurrentItem = time.toMillis(true);
         }
         /* <pubDate> - It follows the rss 2.0 specification for rfc882. */
         else
         {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat rssDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
                  Locale.ENGLISH);
            Date date = rssDate.parse(content);
            calendar.setTime(date);
            m_timeCurrentItem = calendar.getTimeInMillis();
         }
      }
      /* Exception here because we can not use multi catch ParseException & RuntimeException. */
      catch(Exception ignored)
      {
         System.out.println("BUG : Could not parse: " + content);
         time.setToNow();
         m_timeCurrentItem = time.toMillis(true);
      }

      return appendItem(builder, INDEX_TIME, Long.toString(m_timeCurrentItem));
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

      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser parser = factory.newPullParser();

      URL url = new URL(urlString);
      InputStream inputStream = url.openStream();
      parser.setInput(inputStream, null);

      StringBuilder builder = new StringBuilder(FEED_ITEM_INITIAL_CAPACITY);

      Resources resources = getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      int screenWidth = metrics.widthPixels;

      /* Skip everything in the xml file until we arrive at an 'entry' or 'item' open tag. */
      boolean preEntry = true;

      while(preEntry)
      {
         parser.next();
         int eventType = parser.getEventType();
         if(XmlPullParser.START_TAG == eventType)
         {
            String tag = parser.getName();
            if(TAG_ENTRY.equals(tag) || TAG_ITEM.equals(tag))
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

            if(TAG_ENTRY.equals(tag) || TAG_ITEM.equals(tag))
            {
               builder.setLength(0);
               builder.append(ITEM_SEPARATOR);
               m_timeCurrentItem = 0L;
            }
            else if(TAG_LINK.equals(tag))
            {
               String link = parser.getAttributeValue(null, "href");
               if(null == link)
               {
                  parser.next();
                  link = parser.getText();
               }

               builder = appendItem(builder, INDEX_LINK, link);
               builder = appendItem(builder, INDEX_LINK_TRIMMED, fitToScreen(link, screenWidth, 1));
            }
            else if(TAG_PUBLISHED.equals(tag) || TAG_PUBDATE.equals(tag))
            {
               parser.next();
               String content = parser.getText();

               builder = appendPublishedTime(builder, content, tag);
            }
            else if(TAG_TITLE.equals(tag))
            {
               parser.next();
               String content = parser.getText();
               content = null != content ? PATTERN_WHITESPACE.matcher(content).replaceAll(" ") : "";

               builder = appendItem(builder, INDEX_TITLE, fitToScreen(content, screenWidth, 0));
            }
            else if(TAG_CONTENT.equals(tag) || TAG_DES.equals(tag))
            {
               parser.next();
               String content = parser.getText();
               content = null != content ? PATTERN_WHITESPACE.matcher(content).replaceAll(" ") : "";

               /* Here we want to parse the html for the first image. */
               Matcher matcherImg = PATTERN_IMG.matcher(content);

               if(matcherImg.find())
               {
                  String href = matcherImg.group(1);
                  Matcher matcherHref = PATTERN_HREF.matcher(href);

                  if(matcherHref.find())
                  {
                     /* If we get here, we have an image to save. */
                     String imgLink = matcherHref.group(1);

                     /* Get rid of any apostrophes and quotation marks in the link. */
                     imgLink = PATTERN_APOS.matcher(imgLink).replaceAll("");
                     imgLink = PATTERN_QUOT.matcher(imgLink).replaceAll("");

                     builder = appendItem(builder, INDEX_IMAGE, imgLink);

                     int lastSlash = imgLink.lastIndexOf('/') + 1;
                     String imgName = imgLink.substring(lastSlash);

                     String thumbnailPath = applicationFolder + thumbnailDir + imgName;

                     File file = new File(thumbnailPath);
                     if(!file.exists())
                     {
                        builder = compressImage(thumbnailDir, imgLink, imgName, this, builder);
                     }
                  }
               }

               /* Replace all the html tags with nothing. */
               content = PATTERN_CDATA.matcher(content).replaceAll("").trim();
               if(!content.isEmpty())
               {
                  builder = appendDesLines(builder, content, screenWidth);
               }
            }
         }
         else if(XmlPullParser.END_TAG == eventType)
         {
            String tag = parser.getName();
            boolean newItem = !longSet.contains(m_timeCurrentItem);

            if(TAG_ENTRY.equals(tag) || TAG_ITEM.equals(tag) && newItem)
            {
               map.put(m_timeCurrentItem, builder.toString());
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
}
