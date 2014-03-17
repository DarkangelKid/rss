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
import android.graphics.Paint;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.format.Time;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
   public static final String BROADCAST_ACTION = "com.poloure.serviceupdate.handle";

   private static
   class Tags
   {
      static final String LINK = "link";
      static final String PUBLISHED = "published";
      static final String PUB_DATE = "pubDate";
      static final String TITLE = "title";
      static final String DESCRIPTION = "description";
      static final String CONTENT = "content";
      static final String ENTRY = "entry";
      static final String ITEM = "item";
   }

   private static
   class Patterns
   {
      static final Pattern CDATA = Pattern.compile("\\<.*?\\>");
      static final Pattern IMG = Pattern.compile("(?i)<img([^>]+)/>");      static final Pattern LINE = Pattern.compile(NEWLINE);
      static final Pattern SRC = Pattern.compile("\\s*(?i)src\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
      static final Pattern APOSTROPHE = Pattern.compile("'");
      static final Pattern QUOT = Pattern.compile("\"");


   }

   static final String ITEM_LIST = "-item_list.txt";
   static final String CONTENT_FILE = "-content.txt";
   static final String[] FEED_FILES = {ITEM_LIST, CONTENT_FILE};
   static final String NEWLINE = System.getProperty("line.separator");
   private static final int MIN_IMAGE_WIDTH = 64;
   private static final float SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
   private static final float USABLE_WIDTH_TEXT = SCREEN_WIDTH - (Utilities.EIGHT_DP << 1);

   public
   ServiceUpdate()
   {
      super("Service");
   }

   private static
   void setDesLines(Resources resources, FeedItem feedItem, CharSequence content)
   {
      Paint paint = ViewFeedItem.configurePaint(resources, R.dimen.item_description_size, R.color.item_description_color);

      List<String> lines = new ArrayList<String>(Arrays.asList(Patterns.LINE.split(content)));

      int j = 0;

      for(int x = 0; 3 > x; x++)
      {
         /* Skip any empty lines. */
         while(null != lines && j < lines.size() && lines.get(j).trim().isEmpty())
         {
            j++;
         }
         if(j == lines.size())
         {
            break;
         }

         String currentLine = lines.get(j).trim();

         int index = paint.breakText(currentLine, true, USABLE_WIDTH_TEXT, null);

         if(currentLine.length() == index)
         {
            feedItem.m_desLines[x] = currentLine;
         }
         else
         {
            /* Break at the closest ' ' - 1 (some padding). */
            int space = currentLine.lastIndexOf(' ', index - 1);

            /* TODO if no space add a hyphen. */
            index = -1 == space ? index : space;

            feedItem.m_desLines[x] = currentLine.substring(0, index);

            /* Add the remaining to the next line. */
            if(j + 1 < lines.size())
            {
               lines.set(j + 1, currentLine.substring(index) + lines.get(j + 1));
            }
            else
            {
               lines.add(currentLine.substring(index));
            }
         }

         j++;
      }
   }

   private static
   String fitToScreen(Resources resources, String content, int ind, float extra)
   {
      /* ind == 0 is the title, ind == 1 is the link. */
      int size = 0 == ind ? R.dimen.item_title_size : R.dimen.item_link_size;
      int color = 0 == ind ? R.color.item_title_color : R.color.item_link_color;

      Paint paint = ViewFeedItem.configurePaint(resources, size, color);

      int chars = paint.breakText(content, true, USABLE_WIDTH_TEXT - extra, null);
      int space = content.lastIndexOf(' ', chars);

      return content.substring(0, -1 == space ? chars : space);
   }

   private static
   void getThumbnail(FeedItem item, String imageLink, Context context)
   {
      /* Find out if images are disabled. */
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      boolean imagesDisabled = !preferences.getBoolean("images_enabled", true);

      /* Produce the thumbnail path. */
      String imageFile = imageLink.substring(imageLink.lastIndexOf('/') + 1);

      /* If the image exists then it has previously passed the MIN_IMAGE_WIDTH condition. */
      if(imagesDisabled || fileExists(context, imageFile))
      {
         item.m_imageLink = imageLink;
         item.m_imageName = imageFile;
         return;
      }

      try
      {
         BufferedInputStream input = new BufferedInputStream(new URL(imageLink).openStream());
         BufferedOutputStream out = new BufferedOutputStream(context.openFileOutput(imageFile, MODE_PRIVATE));
         try
         {
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            /* If the image is smaller than we care about, do not save it. */
            if(MIN_IMAGE_WIDTH > bitmap.getWidth())
            {
               return;
            }

            item.m_imageLink = imageLink;
            item.m_imageName = imageFile;

            float scale = bitmap.getWidth() / SCREEN_WIDTH;
            int desiredHeight = Math.round(bitmap.getHeight() / scale);

            /* Scale it to the screen width. */
            bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(SCREEN_WIDTH), desiredHeight, false);

            /* Shrink it to VIEW_HEIGHT if that is more than the scaled height. */
            int maxHeight = Math.round(context.getResources()
                  .getDimension(R.dimen.max_image_height));
            int newHeight = Math.min(bitmap.getHeight(), maxHeight);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), newHeight);

            /* Get the quality from settings. */
            String qualityString = preferences.getString("thumbnail_quality", "75");
            int quality = Integer.parseInt(qualityString);

            bitmap.compress(Bitmap.CompressFormat.WEBP, quality, out);
         }
         finally
         {
            if(null != input)
            {
               input.close();
            }
            if(null != out)
            {
               out.close();
            }
         }
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   private static
   void setPublishedTime(FeedItem feedItem, String content, String tag)
   {
      Time time = new Time();
      try
      {
         /* <published> - It is an atom feed it will be one of four RFC3339 formats. */
         if(Tags.PUBLISHED.equals(tag))
         {
            time.parse3339(content);
            feedItem.m_time = time.toMillis(true);
         }
         /* <pubDate> - It follows the rss 2.0 specification for rfc882. */
         else
         {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat rssDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            Date date = rssDate.parse(content);
            calendar.setTime(date);
            feedItem.m_time = calendar.getTimeInMillis();
         }
      }
      catch(ParseException ignored)
      {
         System.out.println("BUG : Could not parse: " + content);
         time.setToNow();
         feedItem.m_time = time.toMillis(true);
      }
      catch(RuntimeException ignored)
      {
         System.out.println("BUG : Could not parse: " + content);
         time.setToNow();
         feedItem.m_time = time.toMillis(true);
      }
   }

   private static
   boolean fileExists(Context context, String fileName)
   {
      try
      {
         FileInputStream in = context.openFileInput(fileName);
         try
         {
         }
         finally
         {
            if(null != in)
            {
               in.close();
            }
         }
      }
      catch(IOException ignored)
      {
         return false;
      }
      return true;
   }

   private static
   String getContent(XmlPullParser parser)
   {
      try
      {
         parser.next();
      }
      catch(XmlPullParserException ignored)
      {
         return "";
      }
      catch(IOException ignored)
      {
         return "";
      }
      String content = parser.getText();
      return null == content ? "" : content;
   }

   private static
   void parseHtmlForImage(Context context, CharSequence html, FeedItem feedItem)
   {
      Matcher matcherImg = Patterns.IMG.matcher(html);

      if(matcherImg.find())
      {
         String src = matcherImg.group(1);
         Matcher matcherHref = Patterns.SRC.matcher(src);

         if(matcherHref.find())
         {
            /* If we get here, we have an image to download and save. */
            String imgLink = matcherHref.group(1);

            /* Get rid of any apostrophes and quotation marks in the link. */
            imgLink = Patterns.APOSTROPHE.matcher(imgLink).replaceAll("");
            imgLink = Patterns.QUOT.matcher(imgLink).replaceAll("");

            getThumbnail(feedItem, imgLink, context);
         }
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
      List<IndexItem> indexItems = Utilities.loadIndexList(this);

      /* Get the tagList (from disk if it is empty). */
      List<String> tagList = PagerAdapterTags.getTagsFromIndex(this, indexItems);

      String tag = tagList.get(page);

      /* Download and parse each feed in the index. */
      for(IndexItem indexItem : indexItems)
      {
         if(0 == page || Arrays.asList(indexItem.m_tags).contains(tag))
         {
            try
            {
               parseFeed(indexItem.m_url, indexItem.m_uid);
            }
            catch(IOException e)
            {
               e.printStackTrace();
            }
            catch(XmlPullParserException e)
            {
               e.printStackTrace();
            }
         }
      }

      /* Update the Activity. */
      Intent broadcast = new Intent(BROADCAST_ACTION);
      broadcast.getIntExtra("PAGE_NUMBER", page);
      sendBroadcast(broadcast);

      wakeLock.release();
      stopSelf();
   }

   private
   void parseFeed(CharSequence urlString, long uid) throws XmlPullParserException, IOException
   {
      String contentFile = uid + CONTENT_FILE;
      String longFile = uid + ITEM_LIST;

      /* Load the previously saved items to a map. */
      Set<Long> tempSet = (Set<Long>) Read.object(this, longFile);
      Set<Long> longSet = null == tempSet ? new HashSet<Long>(0) : tempSet;

      Map<Long, FeedItem> map = new TreeMap<Long, FeedItem>(Collections.reverseOrder());

      Map<Long, FeedItem> tempMap = (Map<Long, FeedItem>) Read.object(this, contentFile);
      if(null != tempMap)
      {
         map.putAll(tempMap);
      }

      /* Read a Map<Long, FeedItem> = TreeMap from file. */
      XmlPullParser parser = Utilities.createXmlParser(urlString);
      FeedItem feedItem = new FeedItem();
      Resources resources = getResources();
      float timeSpace = getResources().getDimension(R.dimen.reserved_time);

      /* Skip everything in the xml file until we arrive at an 'entry' or 'item' open tag. */
      int eventType;
      do
      {
         parser.next();
         eventType = parser.getEventType();
      }
      /* !(A && (B || C)) = (!A || !B) && (!A || !C). */
      while((XmlPullParser.START_TAG != eventType || !Tags.ENTRY.equals(parser.getName())) &&
            (XmlPullParser.START_TAG != eventType || !Tags.ITEM.equals(parser.getName())) &&
            XmlPullParser.END_DOCUMENT != eventType);

      /* This is the main part that parses for each feed item/entry. */
      while(XmlPullParser.END_DOCUMENT != eventType)
      {
         if(XmlPullParser.START_TAG == eventType)
         {
            String tag = parser.getName();

            if(tag.equals(Tags.ENTRY) || tag.equals(Tags.ITEM))
            {
               feedItem = new FeedItem();
            }
            else if(tag.equals(Tags.LINK))
            {
               String link = parser.getAttributeValue(null, "href");
               if(null == link)
               {
                  link = getContent(parser);
               }
               feedItem.m_url = link;
               feedItem.m_urlTrimmed = fitToScreen(resources, link, 1, 0.0F);
            }
            else if(tag.equals(Tags.PUBLISHED) || tag.equals(Tags.PUB_DATE))
            {
               setPublishedTime(feedItem, getContent(parser), tag);
            }
            else if(tag.equals(Tags.TITLE))
            {
               feedItem.m_title = fitToScreen(resources, getContent(parser).trim(), 0, timeSpace);
            }
            else if(tag.equals(Tags.CONTENT) || tag.equals(Tags.DESCRIPTION))
            {
               String content = getContent(parser);
               feedItem.m_content = content;

               parseHtmlForImage(this, content, feedItem);
               content = Patterns.CDATA.matcher(content).replaceAll("").trim();
               setDesLines(resources, feedItem, content);
            }
         }
         else if(XmlPullParser.END_TAG == eventType)
         {
            String tag = parser.getName();
            boolean newItem = !longSet.contains(feedItem.m_time);

            if(Tags.ENTRY.equals(tag) || Tags.ITEM.equals(tag) && newItem)
            {
               map.put(feedItem.m_time, feedItem);
            }
         }
         parser.next();
         eventType = parser.getEventType();
      }

      /* We have finished forming the sets and we can save the new files to disk. */
      Write.object(this, contentFile, map);

      /* Write the item list of longs. */
      Set<Long> set = new HashSet<Long>(map.keySet());
      Write.object(this, longFile, set);
   }
}
