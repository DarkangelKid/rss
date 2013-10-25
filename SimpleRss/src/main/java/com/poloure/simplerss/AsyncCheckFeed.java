package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<Void, Void, String[]>
{
   /* Formats */
   private static final String  INDEX_FORMAT            = "feed|%s|url|%s|tag|%s|";
   private static final String  TAG_TITLE               = "<title";
   private static final String  END_TAG_TITLE           = "</title>";
   private static final Pattern ILLEGAL_FILE_CHARS      = Pattern.compile("[/\\?%*|<>:]");
   private static final Pattern SPLIT_SPACE             = Pattern.compile(" ");
   private static final int     FEED_STREAM_BYTE_BUFFER = 512;
   private final AlertDialog m_dialog;
   private final String      m_oldFeedName;
   private final String      m_applicationFolder;
   private final String      m_allTag;
   private       boolean     m_isFeedReal;

   AsyncCheckFeed(AlertDialog dialog, String currentTitle, String applicationFolder, String allTag)
   {
      m_dialog = dialog;
      m_oldFeedName = currentTitle;
      m_applicationFolder = applicationFolder;
      m_allTag = allTag;

      Button button = m_dialog.getButton(DialogInterface.BUTTON_POSITIVE);
      button.setEnabled(false);
   }

   static
   AsyncTask<Void, Void, String[]> newInstance(AlertDialog dialog, String oldFeedTitle,
         String applicationFolder, String allTag)
   {
      AsyncTask<Void, Void, String[]> task = new AsyncCheckFeed(dialog, oldFeedTitle,
            applicationFolder, allTag);

      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
      return task;
   }

   @Override
   protected
   String[] doInBackground(Void... nothing)
   {
      String userInputTags = ((TextView) m_dialog.findViewById(R.id.tag_edit)).getText().toString();
      Locale defaultLocale = Locale.getDefault();

      String initialTags = 0 == userInputTags.length()
            ? m_allTag
            : userInputTags.toLowerCase(defaultLocale);

      int tagInitialCapacity = initialTags.length();

      /* Capitalise each word. */
      String[] words = SPLIT_SPACE.split(initialTags);
      StringBuilder tagBuilder = new StringBuilder(tagInitialCapacity);

      for(String word : words)
      {
         String firstLetter = word.substring(0, 1);
         String restOfWord = word.substring(1);

         String firstLetterUpper = firstLetter.toUpperCase(defaultLocale);
         String restOfWordLower = restOfWord.toLowerCase(defaultLocale);

         tagBuilder.append(firstLetterUpper);
         tagBuilder.append(restOfWordLower);
         tagBuilder.append(' ');
      }

      int tagLength = tagBuilder.length();
      int lastChar = tagLength - 1;
      tagBuilder.delete(lastChar, tagLength);
      String finalTag = tagBuilder.toString();

      String userInputUrl = ((TextView) m_dialog.findViewById(R.id.feed_url_edit)).getText()
            .toString();

      String[] checkList = userInputUrl.contains("http")
            ? new String[]{userInputUrl}
            : new String[]{
                  "http://" + userInputUrl, "https://" + userInputUrl
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
                     data = joinByteArrays(data, data2);
                     line = new String(data);
                  }

                  int tagTitleIndex = line.indexOf(TAG_TITLE);
                  int moreIndex = line.indexOf('>', tagTitleIndex + 1);
                  int lessIndex = line.indexOf("</", moreIndex);

                  feedTitle = line.substring(moreIndex, lessIndex);
                  m_isFeedReal = true;
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
      return new String[]{feedUrl, feedTitle, finalTag};
   }

   @Override
   protected
   void onPostExecute(String[] result)
   {
      if(!m_isFeedReal)
      {
         Button button = m_dialog.getButton(DialogInterface.BUTTON_POSITIVE);
         if(null != button)
         {
            button.setEnabled(true);
         }
         return;
      }

      String feedUrlFromCheck = result[0];
      String feedTitleFromXml = result[1];
      String feedTag = result[2];

      String userInputName = ((TextView) m_dialog.findViewById(R.id.name_edit)).getText()
            .toString();

      /* Did the user enter a feed name? If not, use the feed title found from the check. */
      String finalName = 0 == userInputName.length() ? feedTitleFromXml : userInputName;

      /* Replace any characters that are not allowed in file names. */
      Matcher matcher = ILLEGAL_FILE_CHARS.matcher(finalName);
      finalName = matcher.replaceAll("");

      /* Create the csv. */
      String feedInfo = String.format(INDEX_FORMAT, finalName, feedUrlFromCheck, feedTag) +
            System.getProperty("line.separator");

      if(0 != m_oldFeedName.length())
      {
         editFeed(m_oldFeedName, finalName, m_applicationFolder);
      }

      /* Save the feed to the index. */
      Write.single(Read.INDEX, feedInfo, m_applicationFolder);

      /* Update the tags. */
      /* TODO updateTags((Activity) m_context); */

      /* Update the manage ListView adapters. */
      /*FragmentManager fragmentManager = ((FragmentActivity) m_context)
      .getSupportFragmentManager();

      String tagPrefix = "android:switcher:" + FragmentManage.VIEW_PAGER_ID + ':';
      String tagTag = tagPrefix + 0;
      String feedsTag = tagPrefix + 1;

      ListFragment tagFragment = (ListFragment) fragmentManager.findFragmentByTag(tagTag);
      ListFragment feedsFragment = (ListFragment) fragmentManager.findFragmentByTag(feedsTag);

      ListView tagListView = tagFragment.getListView();
      ListView feedsListView = feedsFragment.getListView();*/

      /* TODO AsyncManageTagsRefresh.newInstance(tagListView); */
      /* TODO AsyncManageFeedsRefresh.newInstance(feedsListView, m_context); */

      m_dialog.dismiss();
   }

   private
   void editFeed(CharSequence oldFeed, String newFeed, String applicationFolder)
   {
      /* Rename the folder if it is different. */
      String oldFeedFolder = oldFeed + File.separator;
      String newFeedFolder = newFeed + File.separatorChar;

      if(!oldFeed.equals(newFeed))
      {
         Write.moveFile(oldFeedFolder, newFeedFolder, applicationFolder);
      }

      /* Replace the all_tag file with the new image and data. */
      Write.removeLine(Read.INDEX, oldFeed, true, applicationFolder);

   }

   static
   void updateTags(PagerAdapter tagPagerAdapter, AdapterNavDrawer adapterNavDrawer,
         String applicationFolder, String allTag)
   {
      ((PagerAdapterFeeds) tagPagerAdapter).getTagsFromDisk(applicationFolder, allTag);
      tagPagerAdapter.notifyDataSetChanged();

      AsyncRefreshNavigationAdapter.newInstance(adapterNavDrawer, applicationFolder);
   }

   private static
   byte[] joinByteArrays(byte[] first, byte... second)
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
