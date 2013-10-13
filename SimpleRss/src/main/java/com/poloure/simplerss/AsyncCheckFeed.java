package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<String, Void, String[]>
{
   private static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[/\\?%*|<>:]");
   private static final Pattern SPLIT_SPACE        = Pattern.compile(" ");
   private final AlertDialog m_dialog;
   private final String      m_mode;
   private final String      m_title;
   private final BaseAdapter m_navigationAdapter;
   private       String      m_tag;
   private       boolean     m_isFeedNotReal;
   private       String      name;
   private       boolean     m_newTag;
   private final Context     m_context;

   AsyncCheckFeed(AlertDialog dialog, String tag, String feedName, String mode, String currentTitle,
         BaseAdapter navigationAdapter, Context context)
   {
      m_dialog = dialog;
      m_tag = tag;
      name = feedName;
      m_mode = mode;
      m_title = currentTitle;
      m_newTag = true;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
      Button button = m_dialog.getButton(DialogInterface.BUTTON_POSITIVE);
      if(null != button)
      {
         button.setEnabled(false);
      }
   }

   @Override
   protected
   String[] doInBackground(String... url)
   {
      /* If the m_imageViewTag entry has text, check to see if it is an old m_imageViewTag
       * or if it is new. */

      /* Capitalise each word. */
      String[] words = SPLIT_SPACE.split(m_tag);
      m_tag = "";

      for(String word : words)
      {
         m_tag += word.substring(0, 1).toUpperCase(Constants.LOCALE) +
               word.substring(1).toLowerCase(Constants.LOCALE) + ' ';
      }
      m_tag = m_tag.substring(0, m_tag.length() - 1);

      /* Check to see if the tag already exists. */
      String[] currentTags = Read.file(Constants.TAG_LIST, m_context);
      if(-1 != Util.index(currentTags, m_tag))
      {
         m_newTag = false;
      }

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
               in = new BufferedInputStream(new URL(check).openStream());
               byte[] data = new byte[512];
               in.read(data, 0, 512);

               String line = new String(data);
               if(line.contains("rss") || line.contains("Atom") || line.contains("atom"))
               {
                  while(!line.contains(Constants.TAG_TITLE) &&
                        !line.contains(Constants.ENDTAG_TITLE))
                  {
                     byte[] data2 = new byte[512];
                     in.read(data2, 0, 512);
                     data = concat(data, data2);
                     line = new String(data);
                  }
                  int ind = line.indexOf('>', line.indexOf(Constants.TAG_TITLE) + 1);
                  feedTitle = line.substring(ind, line.indexOf("</", ind));
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

      if(m_newTag)
      {
         Write.single(Constants.TAG_LIST, m_tag + Constants.NL, m_context);
         Util.updateTags(m_navigationAdapter, m_context);
      }
      if(0 == name.length())
      {
         name = result[1];
      }

      name = ILLEGAL_FILE_CHARS.matcher(name).replaceAll("");

      if(Constants.EDIT.equals(m_mode))
      {
         editFeed(m_title, name, result[0], m_tag, m_context);
      }
      else if(Constants.ADD.equals(m_mode))
      {

   /* Create the csv. */
         String feedInfo = String.format(Constants.INDEX_FORMAT, name, result[0], m_tag) +
               Constants.NL;

   /* Save the feed to the index. */
         Write.single(Constants.INDEX, feedInfo, m_context);

   /* TODO Update the manage ListViews with the new information. */
         //if(null != PagerAdapterManage.MANAGE_FRAGMENTS[1].getListAdapter())
         {
            //  Update.manageFeeds();
         }
         // if(null != PagerAdapterManage.MANAGE_FRAGMENTS[0].getListAdapter())
         {
            //    Update.manageTags();
         }
      }

      m_dialog.dismiss();
   }

   private static
   void editFeed(String oldFeed, String newFeed, String newUrl, String newTag, Context context)
   {

      String oldFeedFolder = oldFeed + Constants.SEPAR + "";
      String newFeedFolder = newFeed + Constants.SEPAR + "";

      if(!oldFeed.equals(newFeed))
      {
         Util.move(oldFeedFolder, newFeedFolder, context);
      }

      /* Replace the all_tag file with the new image and data. */
      String index = Constants.INDEX;
      String entry = String.format(Constants.INDEX_FORMAT, newFeed, newUrl, newTag);

      int position = Write.removeLine(index, oldFeed, true, context);
      Write.single(index, entry + Constants.NL, context);

     /*TODO ListFragment fragmentManageFeeds = ?
      ListView listView = fragmentManageFeeds.getListView();
      ListAdapter listAdapter = fragmentManageFeeds.getListAdapter();

      String feedContentPath = Util.getPath(newFeed, Constants.CONTENT);
      int feedContentCount = Read.count(feedContentPath);
      String feedInfo = String.format(Constants.LOCALE, Constants.FEED_INFO, newUrl, newTag,
            feedContentCount);

      ((AdapterManageFeeds) listAdapter).setPosition(position, newFeed, feedInfo);

      /// To ManageFeedsRefresh the counts and the order of the tags.
      // TODO Util.updateTags();
      Update.AsyncCompatManageTagsRefresh(listView, listAdapter);*/
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
}
