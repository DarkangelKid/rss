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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
   static final String ITEM_LIST = "-item_list.txt";
   static final String CONTENT_FILE = "-content.txt";
   static final String[] FEED_FILES = {ITEM_LIST, CONTENT_FILE};

   private static final char ITEM_SEPARATOR = '|';
   private static final int MIN_IMAGE_WIDTH = 64;
   private static final int FEED_ITEM_INITIAL_CAPACITY = 200;
   private long m_timeCurrentItem;

   private static final float SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
   private static final float USABLE_WIDTH_TEXT = SCREEN_WIDTH - 40.0F;
   private static final int INITIAL_TAG_SET_SIZE = 128;

   public static final String BROADCAST_ACTION = "com.poloure.serviceupdate.handle";

   private static
   class Index
   {
      static final String IMAGE = "image";
      static final String TITLE = "title";
      static final String TIME = "pubDate";
      static final String LINK = "link";
      static final String LINK_TRIMMED = "blink";
      static final String[] DES = {"x", "y", "z"};
   }

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
      static final Pattern WHITESPACE = Pattern.compile("[\\t\\n\\x0B\\f\\r\\|]");
      static final Pattern CDATA = Pattern.compile("\\<.*?\\>");
      static final Pattern IMG = Pattern.compile("(?i)<img([^>]+)/>");
      static final Pattern SRC = Pattern.compile("\\s*(?i)src\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
      static final Pattern APOSTROPHE = Pattern.compile("'");
      static final Pattern QUOT = Pattern.compile("\"");
   }

   public
   ServiceUpdate()
   {
      super("Service");
   }

   private static
   void writeCollection(Context context, String fileName, Iterable<?> content) throws IOException
   {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE)));
      try
      {
         for(Object item : content)
         {
            out.write(item + Write.NEW_LINE);
         }
      }
      finally
      {
         if(null != out)
         {
            out.close();
         }
      }
   }

   private
   StringBuilder appendDesLines(StringBuilder builder, String content)
   {
      String contentCopy = content;
      Paint paint = ViewFeedItem.configurePaint(getResources(), R.dimen.item_description_size, R.color.item_description_color);

      for(int x = 0; 3 > x; x++)
      {
         int desChars = paint.breakText(contentCopy, true, USABLE_WIDTH_TEXT, null);
         int desSpace = contentCopy.lastIndexOf(' ', desChars);
         desChars = -1 == desSpace ? desChars : desSpace + 1;

         appendItem(builder, Index.DES[x], contentCopy.substring(0, desChars));

         contentCopy = contentCopy.substring(desChars);
      }
      return builder;
   }

   private
   String fitToScreen(String content, int ind, float extra)
   {
      /* ind == 0 is the title, ind == 1 is the link. */
      int size = 0 == ind ? R.dimen.item_title_size : R.dimen.item_link_size;
      int color = 0 == ind ? R.color.item_title_color : R.color.item_link_color;

      Paint paint = ViewFeedItem.configurePaint(getResources(), size, color);

      int chars = paint.breakText(content, true, USABLE_WIDTH_TEXT - extra, null);
      int space = content.lastIndexOf(' ', chars);

      return content.substring(0, -1 == space ? chars : space);
   }

   private static
   Set<String> fileToSet(Context context, String fileName)
   {
      Set<String> set = new LinkedHashSet<String>(INITIAL_TAG_SET_SIZE);
      Collections.addAll(set, Read.file(context, fileName));
      return set;
   }

   private static
   void getThumbnail(StringBuilder build, String imgLink, Context context)
   {
      /* Find out if images are disabled. */
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      boolean imagesDisabled = !preferences.getBoolean("images_enabled", true);

      /* Produce the thumbnail path. */
      String imageFile = imgLink.substring(imgLink.lastIndexOf('/') + 1);

      /* If the image exists then it has previously passed the MIN_IMAGE_WIDTH condition. */
      if(imagesDisabled || Read.fileExists(context, imageFile))
      {
         appendItem(build, Index.IMAGE, imgLink);
         return;
      }

      try
      {
         BufferedInputStream input = new BufferedInputStream(new URL(imgLink).openStream());
         BufferedOutputStream out = new BufferedOutputStream(context.openFileOutput(imageFile, MODE_PRIVATE));
         try
         {
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            /* If the image is smaller than we care about, do not save it. */
            if(MIN_IMAGE_WIDTH > bitmap.getWidth())
            {
               return;
            }

            appendItem(build, Index.IMAGE, imgLink);

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
   void appendItem(StringBuilder builder, String tag, String content)
   {
      builder.append(tag).append(ITEM_SEPARATOR).append(content).append(ITEM_SEPARATOR);
   }

   private
   void appendPublishedTime(StringBuilder builder, String content, String tag)
   {
      Time time = new Time();

      try
      {
         /* <published> - It is an atom feed it will be one of four RFC3339 formats. */
         if(Tags.PUBLISHED.equals(tag))
         {
            time.parse3339(content);
            m_timeCurrentItem = time.toMillis(true);
         }
         /* <pubDate> - It follows the rss 2.0 specification for rfc882. */
         else
         {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat rssDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            Date date = rssDate.parse(content);
            calendar.setTime(date);
            m_timeCurrentItem = calendar.getTimeInMillis();
         }
      }
      catch(ParseException ignored)
      {
         System.out.println("BUG : Could not parse: " + content);
         time.setToNow();
         m_timeCurrentItem = time.toMillis(true);
      }
      catch(RuntimeException ignored)
      {
         System.out.println("BUG : Could not parse: " + content);
         time.setToNow();
         m_timeCurrentItem = time.toMillis(true);
      }

      appendItem(builder, Index.TIME, Long.toString(m_timeCurrentItem));
   }

   @Override
   protected
   void onHandleIntent(Intent intent)
   {
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SIMPLERSS");
      wakeLock.acquire();

      int page = intent.getIntExtra("GROUP_NUMBER", 0);

      /* Get the tagList (from disk if it is empty). */
      List<String> tagList = PagerAdapterTags.TAG_LIST;
      if(tagList.isEmpty())
      {
         tagList.addAll(PagerAdapterTags.getTagsFromDisk(this));
      }

      String tag = tagList.get(page);

      String[][] content = Read.csvFile(this, Read.INDEX, 'i', 'u', 't');

      /* Download and parse each feed in the index. */
      for(int i = 0; i < content[0].length; i++)
      {
         if(0 == page || Arrays.asList(PagerAdapterTags.SPLIT_COMMA.split(content[2][i]))
                               .contains(tag))
         {
            try
            {
               parseFeed(content[1][i], content[0][i]);
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
      return null == content ? "" : Patterns.WHITESPACE.matcher(content).replaceAll(" ");
   }

   private static
   void parseHtmlForImage(Context context, CharSequence html, StringBuilder build)
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

            getThumbnail(build, imgLink, context);
         }
      }
   }

   private
   void parseFeed(CharSequence urlString, String uid) throws XmlPullParserException, IOException
   {
      String contentFile = uid + CONTENT_FILE;
      String longFile = uid + ITEM_LIST;

      /* Load the previously saved items to a map. */
      Set<String> set = fileToSet(this, contentFile);
      Set<Long> longSet = Read.longSet(this, longFile);

      int setSize = set.size();
      int longSize = longSet.size();

      String[] lines = set.toArray(new String[setSize]);
      Long[] longs = longSet.toArray(new Long[longSize]);

      Map<Long, String> map = new TreeMap<Long, String>(Collections.reverseOrder());

      for(int i = 0; i < setSize; i++)
      {
         map.put(longs[i], lines[i]);
      }

      XmlPullParser parser = Utilities.createXmlParser(urlString);

      StringBuilder builder = new StringBuilder(FEED_ITEM_INITIAL_CAPACITY);

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
               builder.setLength(0);
               builder.append(ITEM_SEPARATOR);
               m_timeCurrentItem = 0L;
            }
            else if(tag.equals(Tags.LINK))
            {
               String link = parser.getAttributeValue(null, "href");
               if(null == link)
               {
                  link = getContent(parser);
               }
               appendItem(builder, Index.LINK, link);
               appendItem(builder, Index.LINK_TRIMMED, fitToScreen(link, 1, 0.0F));
            }
            else if(tag.equals(Tags.PUBLISHED) || tag.equals(Tags.PUB_DATE))
            {
               appendPublishedTime(builder, getContent(parser), tag);
            }
            else if(tag.equals(Tags.TITLE))
            {
               float timeSpace = getResources().getDimension(R.dimen.reserved_time);
               appendItem(builder, Index.TITLE, fitToScreen(getContent(parser), 0, timeSpace));
            }
            else if(tag.equals(Tags.CONTENT) || tag.equals(Tags.DESCRIPTION))
            {
               String content = getContent(parser);
               parseHtmlForImage(this, content, builder);
               content = Patterns.CDATA.matcher(content).replaceAll("").trim();
               builder = appendDesLines(builder, content);
            }
         }
         else if(XmlPullParser.END_TAG == eventType)
         {
            String tag = parser.getName();
            boolean newItem = !longSet.contains(m_timeCurrentItem);

            if(Tags.ENTRY.equals(tag) || Tags.ITEM.equals(tag) && newItem)
            {
               map.put(m_timeCurrentItem, builder.toString());
            }
         }
         parser.next();
         eventType = parser.getEventType();
      }

      /* We have finished forming the sets and we can save the new files to disk. */
      writeCollection(this, contentFile, map.values());
      Write.longSet(this, longFile, map.keySet());
   }
}
