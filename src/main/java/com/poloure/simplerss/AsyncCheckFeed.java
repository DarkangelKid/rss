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

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<Void, Void, String[]>
{
   /* Formats */
   private static final String INDEX_FORMAT = "|f|%s|u|%s|t|%s|";
   private static final String NEW_LINE = System.getProperty("line.separator");
   private static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[/\\?%*|<>:]");
   private static final Pattern SPLIT_SPACE = Pattern.compile(" ");
   private static final Pattern SPLIT_COMMA = Pattern.compile(",");
   private final Dialog m_dialog;
   private final String m_oldFeedName;
   private final String m_applicationFolder;
   private final Activity m_activity;

   private
   AsyncCheckFeed(Activity activity, Dialog dialog, String oldFeedName, String applicationFolder)
   {
      m_dialog = dialog;
      m_oldFeedName = oldFeedName;
      m_applicationFolder = applicationFolder;
      m_activity = activity;

      Button button = (Button) m_dialog.findViewById(DialogEditFeed.BUTTON_IDS[1]);
      button.setText(R.string.dialog_checking);
      button.setEnabled(false);
   }

   static
   void newInstance(Activity activity, Dialog dialog, String oldFeedTitle, String applicationFolder)
   {
      AsyncTask<Void, Void, String[]> task = new AsyncCheckFeed(activity, dialog, oldFeedTitle,
            applicationFolder);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR);
   }

   private static
   void moveFile(String originalName, String resultingName, String storage)
   {
      new File(storage + originalName).renameTo(new File(storage + resultingName));
   }

   /* Function should be safe, returns false if fails. */
   private static
   void AppendLineToIndex(String lineToAppend, String applicationFolder)
   {
      String filePath = applicationFolder + Read.INDEX;

      try(BufferedWriter out = new BufferedWriter(new FileWriter(filePath, true)))
      {
         out.write(lineToAppend);
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   @Override
   protected
   String[] doInBackground(Void... nothing)
   {
      /* Get the user's input. */
      CharSequence inputName = ((TextView) m_dialog.findViewById(DialogEditFeed.IDS[0])).getText();
      CharSequence inputTags = ((TextView) m_dialog.findViewById(DialogEditFeed.IDS[2])).getText();
      CharSequence inputUrl = ((TextView) m_dialog.findViewById(DialogEditFeed.IDS[1])).getText();

      inputUrl = null == inputUrl ? "" : inputUrl;
      inputName = null == inputName ? "" : inputName;

      /* Form the array of urls we will check the validity of. */
      CharSequence[] urlCheckList = {inputUrl, "https://" + inputUrl, "http://" + inputUrl};

      String url = "";
      String title = "";

      for(CharSequence urlToCheck : urlCheckList)
      {
         if(isValidFeed(urlToCheck))
         {
            /* Did the user enter a feed name? If not, use the feed title found from the check. */
            String tempTitle = 0 == inputName.length() ? getFeedTitle(urlToCheck)
                  : inputName.toString();

            /* Replace any characters that are not allowed in file names. */
            Matcher matcher = ILLEGAL_FILE_CHARS.matcher(tempTitle);
            title = matcher.replaceAll("");

            url = urlToCheck.toString();
            break;
         }
      }

      String tags = formatUserTagsInput(inputTags);

      return new String[]{url, title, tags};
   }

   private static
   String getFeedTitle(CharSequence url)
   {
      String feedTitle = "";
      try
      {
         XmlPullParser parser = Utilities.createXmlParser(url);
         int eventType;
         do
         {
            parser.next();
            eventType = parser.getEventType();
            if(XmlPullParser.START_TAG == eventType)
            {
               String tag = parser.getName();
               if("title".equals(tag))
               {
                  parser.next();
                  feedTitle = parser.getText();
               }
            }
         }
         while(feedTitle.isEmpty() && XmlPullParser.END_DOCUMENT != eventType);
      }
      catch(IOException | XmlPullParserException ignored)
      {
         feedTitle = "No Title - " + System.currentTimeMillis();
      }
      return feedTitle;
   }

   private static
   boolean isValidFeed(CharSequence urlString)
   {
      boolean isValid = false;
      try
      {
         XmlPullParser parser = Utilities.createXmlParser(urlString.toString());

         parser.next();
         int eventType = parser.getEventType();
         if(XmlPullParser.START_TAG == eventType)
         {
            String tag = parser.getName();
            if("rss".equals(tag) || "feed".equals(tag))
            {
               isValid = true;
            }
         }
      }
      catch(IOException | XmlPullParserException ignored)
      {
      }
      return isValid;
   }

   private
   String formatUserTagsInput(CharSequence userInputTags)
   {
      String inputTags = userInputTags.toString();
      String allTag = m_activity.getString(R.string.all_tag);

      if(inputTags.isEmpty())
      {
         return allTag;
      }

      String lowerTags = inputTags.toLowerCase();

      /* + 10 in case the user did not put spaces after the commas. */
      StringBuilder tagBuilder = new StringBuilder(lowerTags.length() + 10);
      String[] tags = SPLIT_COMMA.split(lowerTags);

      /* For each tag. */
      for(String tag : tags)
      {
         /* In case the tag is multiple words. */
         String[] words = SPLIT_SPACE.split(tag);

         /* The input tag is all lowercase. */
         for(String word : words)
         {
            if(!word.isEmpty())
            {
               char firstLetter = word.charAt(0);
               char firstLetterUpper = Character.toUpperCase(firstLetter);

               String restOfWord = word.substring(1);

               tagBuilder.append(firstLetterUpper);
               tagBuilder.append(restOfWord);
               tagBuilder.append(' ');
            }
         }
         /* Delete the last space. */
         int builderLength = tagBuilder.length();
         tagBuilder.deleteCharAt(builderLength - 1);

         tagBuilder.append(", ");
      }

      /* Delete the last comma and space. */
      int builderLength = tagBuilder.length();
      tagBuilder.setLength(builderLength - 2);

      return tagBuilder.toString();
   }

   @Override
   protected
   void onPostExecute(String[] result)
   {
      String feedUrlFromCheck = result[0];
      String finalTitle = result[1];
      String finalTag = result[2];

      boolean isFeedValid = !feedUrlFromCheck.isEmpty();
      boolean isExistingFeed = !m_oldFeedName.isEmpty();
      Context context = m_dialog.getContext();

      if(isFeedValid)
      {
         /* Create the csv. */
         String feedInfo = String.format(INDEX_FORMAT, finalTitle, feedUrlFromCheck, finalTag) +
                           NEW_LINE;

         if(isExistingFeed)
         {
            /* Rename the folder if it is different. */
            if(!m_oldFeedName.equals(finalTitle))
            {
               moveFile(m_oldFeedName, finalTitle, m_applicationFolder);
            }
            Write.editIndexLine(m_oldFeedName, m_applicationFolder, Write.MODE_REPLACE, feedInfo);
         }
         else
         {
            /* Save the feed to the index. */
            AppendLineToIndex(feedInfo, m_applicationFolder);
         }

         /* Update the PagerAdapter for the tag fragments. */
         ViewPager feedPager = (ViewPager) m_activity.findViewById(R.id.view_pager_tags);
         PagerAdapterFeeds pagerAdapterFeeds = (PagerAdapterFeeds) feedPager.getAdapter();
         pagerAdapterFeeds.updateTags(m_applicationFolder, context);

         /* Update the NavigationDrawer adapter. */
         AsyncNavigationAdapter.newInstance(m_activity, m_applicationFolder, -1);

         /* Get the manage ListView and update it. */
         String[] navTitles = m_activity.getResources().getStringArray(R.array.navigation_titles);
         Fragment fragment = m_activity.getFragmentManager().findFragmentByTag(navTitles[1]);

         if(null != fragment)
         {
            AsyncManage.newInstance(
                  (ArrayAdapter<Editable>) ((ListFragment) fragment).getListAdapter(),
                  context.getResources(), m_applicationFolder);
         }

         m_dialog.dismiss();
      }
      else
      {
         Button button = (Button) m_dialog.findViewById(DialogEditFeed.BUTTON_IDS[1]);
         button.setText(R.string.dialog_accept);
         button.setEnabled(true);
      }

      /* Show added feed toast notification. */
      String text = isFeedValid ? context.getString(R.string.toast_added_feed) + ' ' + finalTitle
            : context.getString(R.string.toast_invalid_feed);

      Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
      toast.show();
   }
}
