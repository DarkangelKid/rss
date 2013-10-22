package com.poloure.simplerss;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<String, Void, String[]>
{
   private static final String  TAG_TITLE               = "<title";
   private static final String  END_TAG_TITLE           = "</title>";
   private static final Pattern ILLEGAL_FILE_CHARS      = Pattern.compile("[/\\?%*|<>:]");
   private static final Pattern SPLIT_SPACE             = Pattern.compile(" ");
   private static final int     FEED_STREAM_BYTE_BUFFER = 512;
   private static final int     TAG_INITIAL_CAPACITY    = 32;
   private final AlertDialog m_dialog;
   private final String      m_mode;
   private final String      m_title;
   private final Context     m_context;
   private       String      m_tag;
   private       boolean     m_isFeedNotReal;
   private       String      m_name;

   AsyncCheckFeed(AlertDialog dialog, String tag, String feedName, String mode, String currentTitle,
         Context context)
   {
      m_dialog = dialog;
      m_tag = tag;
      m_name = feedName;
      m_mode = mode;
      m_title = currentTitle;
      m_context = context;
      Button button = m_dialog.getButton(DialogInterface.BUTTON_POSITIVE);
      button.setEnabled(false);
   }

   @Override
   protected
   String[] doInBackground(String... url)
   {
      /* Capitalise each word. */
      String[] words = SPLIT_SPACE.split(m_tag);
      StringBuilder tagBuilder = new StringBuilder(TAG_INITIAL_CAPACITY);

      for(String word : words)
      {
         String firstLetter = word.substring(0, 1);
         String restOfWord = word.substring(1);

         Locale defaultLocale = Locale.getDefault();
         String firstLetterUpper = firstLetter.toUpperCase(defaultLocale);
         String restOfWordLower = restOfWord.toLowerCase(defaultLocale);

         tagBuilder.append(firstLetterUpper);
         tagBuilder.append(restOfWordLower);
         tagBuilder.append(' ');
      }

      int tagLength = tagBuilder.length();
      int lastChar = tagLength - 1;
      tagBuilder.delete(lastChar, tagLength);
      m_tag = tagBuilder.toString();

      String[] checkList = url[0].contains("http") ? new String[]{url[0]} : new String[]{
            "http://" + url[0], "https://" + url[0]
      };

      String feedUrl = "";
      String feedTitle = "";

      for(String check : checkList)
      {
         try
         {
            BufferedInputStream in = null;
            try
            {
               URL url1 = new URL(check);
               InputStream inputStream = url1.openStream();
               in = new BufferedInputStream(inputStream);
               byte[] data = new byte[FEED_STREAM_BYTE_BUFFER];
               in.read(data, 0, FEED_STREAM_BYTE_BUFFER);

               String line = new String(data);
               if(line.contains("rss") || line.contains("Atom") || line.contains("atom"))
               {
                  while(!line.contains(TAG_TITLE) && !line.contains(END_TAG_TITLE))
                  {
                     byte[] data2 = new byte[FEED_STREAM_BYTE_BUFFER];
                     in.read(data2, 0, FEED_STREAM_BYTE_BUFFER);
                     data = concat(data, data2);
                     line = new String(data);
                  }

                  int tagTitleIndex = line.indexOf(TAG_TITLE);
                  int moreIndex = line.indexOf('>', tagTitleIndex + 1);
                  int lessIndex = line.indexOf("</", moreIndex);

                  feedTitle = line.substring(moreIndex, lessIndex);
                  m_isFeedNotReal = false;
                  feedUrl = check;
               }
            }
            finally
            {
               if(null != in)
               {
                  in.close();
               }
            }
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
      return new String[]{feedUrl, feedTitle};
   }

   private static
   byte[] concat(byte[] first, byte... second)
   {
      if(null == first)
      {
         return second;
      }
      if(null == second)
      {
         return first;
      }
      byte[] result = new byte[first.length + second.length];
      System.arraycopy(first, 0, result, 0, first.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   @Override
   protected
   void onPostExecute(String[] result)
   {
      if(m_isFeedNotReal)
      {
         Button button = m_dialog.getButton(DialogInterface.BUTTON_POSITIVE);
         if(null != button)
         {
            button.setEnabled(true);
         }
         return;
      }

      if(0 == m_name.length())
      {
         m_name = result[1];
      }

      Matcher matcher = ILLEGAL_FILE_CHARS.matcher(m_name);
      m_name = matcher.replaceAll("");

      if(Constants.EDIT.equals(m_mode))
      {
         editFeed(m_title, m_name, result[0], m_tag, m_context);
      }
      else if(Constants.ADD.equals(m_mode))
      {

         /* Create the csv. */
         String feedInfo = String.format(Constants.INDEX_FORMAT, m_name, result[0], m_tag) +
               Constants.NL;

         /* Save the feed to the index. */
         Write.single(Constants.INDEX, feedInfo, m_context);

         /* TODO Update the manage ListViews with the new information. */
         /* if(null != PagerAdapterManage.MANAGE_FRAGMENTS[1].getListAdapter())
         {
            Update.manageFeeds();
         }
         if(null != PagerAdapterManage.MANAGE_FRAGMENTS[0].getListAdapter())
         {
            Update.manageTags();
         }*/
      }

      Util.updateTags((Activity) m_context);

      m_dialog.dismiss();
   }

   private static
   void editFeed(CharSequence oldFeed, String newFeed, String newUrl, String newTag,
         Context context)
   {

      String oldFeedFolder = oldFeed + File.separator;
      String newFeedFolder = newFeed + File.separator;

      if(!oldFeed.equals(newFeed))
      {
         Util.moveFile(oldFeedFolder, newFeedFolder, context);
      }

      /* Replace the all_tag file with the new image and data. */
      String index = Constants.INDEX;
      String entry = String.format(Constants.INDEX_FORMAT, newFeed, newUrl, newTag);

      Write.removeLine(index, oldFeed, true, context);
      Write.single(index, entry + Constants.NL, context);

      Util.updateTags((Activity) context);
      // TODO AsyncManageTagsRefresh(listView, listAdapter);
   }
}
