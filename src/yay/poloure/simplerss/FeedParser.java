package yay.poloure.simplerss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

class FeedParser
{
   private static final SimpleDateFormat RSS_DATE           = new SimpleDateFormat(
         "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
   private static final SimpleDateFormat RFC3339            = new SimpleDateFormat(
         "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
   private static final Pattern          PATTERN_LTGT       = Pattern.compile("(&lt;).*?(&gt;)");
   private static final Pattern          PATTERN_CDATA      = Pattern.compile("\\<.*?\\>");
   private static final Pattern          PATTERN_WHITESPACE = Pattern.compile(
         "[\\t\\n\\x0B\\f\\r\\|]");
   private static final int              SCREEN_WIDTH       = (int) Math.round(
         Util.getScreenWidth() * 0.944);
   private static final String           DUMP_PATH          = "content.dump" + Constants.TXT;
   private static final String           URL_PATH           = "content.url" + Constants.TXT;
   private static       String[]         s_startTags        = {
         "<link>", "<published>", "<pubDate>", "<description>", "<title", "<content"
   };
   private static       String[]         s_endTags          = {
         "/link", "/publ", "/pubD", "/desc", "/titl", "/cont"
   };
   private static       String[]         s_allTags          = {
         "<link>",
         "<published>",
         "<pubDate>",
         "<description>",
         "<title",
         "<content",
         "</link>",
         "</published>",
         "</pubDate>",
         "</description>",
         "</title",
         "</content",
         "<entry",
         "<item",
         "</entry",
         "</item"
   };

   static
   void parseFeed(String feed)
   {
      String storeFile = Util.getStorage() + feed + Constants.STORE;
      String contentFile = Util.getPath(feed, Constants.CONTENT);
      String imageDir = Util.getPath(feed, Constants.IMAGES);
      String thumbnailDir = Util.getPath(feed, Constants.THUMBNAILS);
      String[] filters = Read.file(Constants.FILTER_LIST);

      Set<String> set = Read.set(contentFile);
      Time time = new Time();

      /* TODO Replace reader method with Read.file, char-by-char. */
      BufferedReader reader = null;
      try
      {
         reader = Util.isUsingSd() ? Read.reader(storeFile) : Read.reader(storeFile, Read.UTF8);
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }

      StringBuilder line = new StringBuilder();

      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;

      int test = 0;
      boolean writeMode = false;
      boolean cdataMode = false;
      String tempLine;
      while(-1 != test)
      {
         try
         {
            reader.reset();
         }
         catch(IOException e)
         {
            e.printStackTrace();
         }

         String currentTag;
         try
         {
            currentTag = getNextTag(reader, s_allTags);
         }
         catch(IOException e)
         {
            e.printStackTrace();
            currentTag = "";
         }

         if(currentTag.contains("<entry") || currentTag.contains("<item"))
         {
            /// Add line to set and reset the line.
            if(1 < line.length() && writeMode)
            {
               tempLine = line.toString();
               if(!tempLine.contains("published|") &&
                     !tempLine.contains("pubDate|") &&
                     !set.contains(tempLine))
               {
                  tempLine = tempLine + "pubDate|" + RFC3339.format(new Date()) + '|';
               }
               set.add(tempLine);
            }
            line.setLength(0);
            writeMode = true;
         }
         else if(currentTag.contains("</entry") || currentTag.contains("</item"))
         {
            String image = saveImageName();
            if(0 != image.length())
            {
               line.append(Constants.IMAGE).append(image).append('|');
               String imageName = image.substring(image.lastIndexOf(Constants.SEPAR) + 1,
                     image.length());

               boolean success = true;
               /* If the image does not exist, try to download it from the internet. */
               if(Util.isNonExisting(imageDir + imageName))
               {
                  success = !Write.download(image, imageDir + imageName);
               }

               /* If the image failed to download. */
               if(success)
               {
                  Write.log("Failed to download image " + image);
               }

               /* If the image downloaded fine and a thumbnail does not exist. */
               else if(Util.isNonExisting(thumbnailDir + imageName))
               {
                  compressImage(Util.getStorage() + imageDir, Util.getStorage() + thumbnailDir,
                        imageName);
               }

               /* ISSUE #194 */
               BitmapFactory.decodeFile(Util.getStorage() + thumbnailDir + imageName, options);
               if(0 == options.outWidth)
               {
                  writeMode = false;
               }
               line.append(Constants.WIDTH)
                     .append(options.outWidth)
                     .append('|')
                     .append(Constants.HEIGHT)
                     .append(options.outHeight)
                     .append('|');
            }
            line.append(saveUrl());
         }
         else
         {
            int startLength = s_startTags.length;
            for(int i = 0; i < startLength; i++)
            {
               if(currentTag.contains(s_startTags[i]))
               {
                  int desLength = 0;

                  if(currentTag.contains("<title"))
                  {
                     currentTag = "<title>";
                  }
                  else if(currentTag.contains("<description"))
                  {
                     /// remove content
                     if(6 == s_startTags.length)
                     {
                        s_startTags = Util.arrayRemove(s_startTags, 5);
                        s_endTags = Util.arrayRemove(s_endTags, 5);
                        s_allTags = Util.arrayRemove(s_allTags, 5);
                        s_allTags = Util.arrayRemove(s_allTags, 10);
                     }
                     desLength = -2048;
                  }
                  else if(currentTag.contains("<content"))
                  {
                     currentTag = "<description>";
                  }

                  /// Write description| to the line buffer.
                  line.append(currentTag.substring(1, currentTag.length() - 1)).append('|');

                  String content = getStringToTag(reader, s_endTags[i]).trim();
                  int contentLength = content.length();

                  /// remove <![CDATA[ if it exists.
                  if(10 < contentLength && "<![CDATA[".equals(content.substring(0, 9)))
                  {
                     content = content.substring(9, contentLength - 3);
                     cdataMode = true;
                  }

                  content = content.replace("&amp;", "&").replace("&quot;", "\"");

                  /// Save the image url from content.
                  if(currentTag.contains("<description"))
                  {
                     int tem = content.indexOf("img src=");
                     if(-1 != tem)
                     {
                        int tem2 = content.indexOf('\"', tem + 10);
                        if(-1 == tem2)
                        {
                           tem2 = content.indexOf('\'', tem + 10);
                        }
                        else
                        {
                           int tem3 = content.indexOf('\'', tem + 10);
                           if(-1 != tem3 && tem3 < tem2)
                           {
                              tem2 = tem3;
                           }
                        }
                        String imgstr = content.substring(tem + 9, tem2) + Constants.NL;
                        Write.single(DUMP_PATH, imgstr);
                     }
                  }
                  /* If it follows the rss 2.0 specification for rfc882. */
                  else if("<pubDate>".equals(currentTag))
                  {
                     try
                     {
                        content = RFC3339.format(RSS_DATE.parse(content));
                     }
                     catch(ParseException e)
                     {
                        e.printStackTrace();
                        Write.log("BUG : atom-3339, looks like: " + content);
                        content = RFC3339.format(new Date());
                     }

                     line.append(content).append('|');
                     break;
                  }
                  /* If it is an atom feed it will be one of four
                   * RFC3339 formats. */
                  else if("<published>".equals(currentTag))
                  {
                     try
                     {
                        time.parse3339(content);
                        content = time.format3339(false);
                     }
                     catch(RuntimeException e)
                     {
                        e.printStackTrace();
                        Write.log("BUG : atom-3339, looks like: " + content);
                        content = RFC3339.format(new Date());
                     }
                     line.append(content).append('|');
                     break;
                  }

                  /// Replace ALL_TAG <x> with nothing.
                  if(cdataMode)
                  {
                     content = PATTERN_CDATA.matcher(content).replaceAll("");
                     cdataMode = false;
                  }
                  else
                  {
                     content = PATTERN_LTGT.matcher(content).replaceAll("");
                  }
                  content = PATTERN_WHITESPACE.matcher(content).replaceAll(" ");

                  if(currentTag.contains("<title>"))
                  {
                     String cont2 = content.toLowerCase();
                     for(String filter : filters)
                     {
                        if(cont2.contains(filter.toLowerCase()))
                        {
                           writeMode = false;
                           break;
                        }
                     }
                  }

                  int take = desLength;
                  desLength += content.length();

                  if(512 < desLength && 512 > take)
                  {
                     line.append(content.substring(0, 512 - take));
                  }
                  else if(512 > desLength)
                  {
                     line.append(content);
                  }

                  line.append('|');
                  break;
               }
            }
         }
         try
         {
            reader.mark(2);
            test = reader.read();
         }
         catch(IOException e)
         {
            e.printStackTrace();
         }
      }

      /// Add the last line that has no <entry / <item after it.
      if(writeMode)
      {
         tempLine = line.toString();
         if(!tempLine.contains("published|") &&
               !tempLine.contains("pubDate|") &&
               !set.contains(tempLine))
         {
            tempLine += "pubDate|" + RFC3339.format(new Date()) + '|';
         }
         set.add(tempLine);
      }

      Util.remove(storeFile);
      /* Write the new content to the file. */
      Write.collection(contentFile, set);
   }

   private static
   String getStringToTag(BufferedReader reader, String tag)
   {
      /* </link> */
      StringBuilder cont = new StringBuilder();
      try
      {
         char[] buffer = new char[5];
         while(!new String(buffer).equals(tag))
         {
            cont.append(getStringToNextChar(reader, '<'));
            /* hello my name is a penguin< */
            reader.mark(6);
            reader.read(buffer, 0, 5);
            reader.reset();
         }
         /* hello my name is a penguin<link>blash stha */
         cont.deleteCharAt(cont.length() - 1);
      }
      catch(IOException e)
      {
         e.printStackTrace();
         return "";
      }

      return cont.toString();
   }

   private static
   String saveImageName()
   {
      String[] imageUrl = Read.file(DUMP_PATH);
      if(0 == imageUrl.length)
      {
         return "";
      }

      if(6 >= imageUrl[0].length())
      {
         imageUrl[0] = "";
      }

      Util.remove(DUMP_PATH);
      return imageUrl[0];
   }

   private static
   String saveUrl()
   {
      String[] url = Read.file(URL_PATH);
      if(0 == url.length)
      {
         return "";
      }

      url[0] = 6 < url[0].length() ? "link|" + url[0] + '|' : "";

      Util.remove(URL_PATH);
      return url[0];
   }

   private static
   String getNextTag(BufferedReader reader, String... types) throws IOException
   {
      boolean found = true;
      String tag = "";
      while(found)
      {
         char current = '\0';
         while('<' != current)
         {
            int eof = reader.read();
            if(-1 == eof)
            {
               return "eof";
            }
            else
            {
               current = (char) eof;
            }
         }

         tag = '<' + getStringToNextChar(reader, '>');

         int tem = tag.indexOf("img src=");
         int tem2;
         int tem3;
         if(-1 != tem)
         {
            tem2 = tag.indexOf('\"', tem + 10);
            if(-1 == tem2)
            {
               tem2 = tag.indexOf('\'', tem + 10);
            }
            else
            {
               tem3 = tag.indexOf('\'', tem + 10);
               if(-1 != tem3 && tem3 < tem2)
               {
                  tem2 = tem3;
               }
            }
            Write.single(DUMP_PATH, tag.substring(tem + 9, tem2) + Constants.NL);
         }

         if(tag.contains("type=\"text/html") || tag.contains("type=\'text/html"))
         {
            int i = tag.indexOf("href=");
            if(-1 != i)
            {
               tem2 = tag.indexOf('\"', i + 7);
               if(-1 == tem2)
               {
                  tem2 = tag.indexOf('\'', i + 7);
               }
               else
               {
                  tem3 = tag.indexOf('\'', i + 7);
                  if(-1 != tem3 && tem3 < tem2)
                  {
                     tem2 = tem3;
                  }
               }
               Write.single(URL_PATH, tag.substring(i + 6, tem2) + Constants.NL);
            }
         }
         else if(tag.contains("type=\"image/jpeg") || tag.contains("type=\'image/jpeg"))
         {
            int i = tag.indexOf("href=");
            if(-1 != i)
            {
               tem2 = tag.indexOf('\"', i + 7);
               if(-1 == tem2)
               {
                  tem2 = tag.indexOf('\'', i + 7);
               }
               else
               {
                  tem3 = tag.indexOf('\'', i + 7);
                  if(-1 != tem3 && tem3 < tem2)
                  {
                     tem2 = tem3;
                  }
               }
               Util.remove(DUMP_PATH);
               Write.single(DUMP_PATH, tag.substring(i + 6, tem2) + Constants.NL);
            }
         }

         for(String type : types)
         {
            if(tag.contains(type))
            {
               found = false;
            }
         }
      }
      return tag;
   }

   private static
   String getStringToNextChar(BufferedReader reader, char next)
   {
      try
      {
         char[] build = new char[4096];
         int i = 0;
         do
         {
            build[i] = (char) reader.read();
            i++;
         }
         while(build[i] != next && 4094 != i);

         build[i] = next;

         return new String(build, 0, i + 1);
      }
      catch(IOException e)
      {
         e.printStackTrace();
         return "";
      }
   }

   private static
   void compressImage(String imageDir, String thumbnailDir, String img)
   {

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(imageDir + img, o);

      float widthTmp = o.outWidth;

      float insample = widthTmp > SCREEN_WIDTH ? Math.round(widthTmp / SCREEN_WIDTH) : 1;

      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = Math.round(insample);
      Bitmap bitmap = BitmapFactory.decodeFile(imageDir + img, o2);

      try
      {
         FileOutputStream out = new FileOutputStream(thumbnailDir + img);
         bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
   }
}
