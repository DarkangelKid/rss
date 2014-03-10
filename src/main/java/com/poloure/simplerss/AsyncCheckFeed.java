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
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<Void, Void, String[]>
{
   /* Formats */
   static final String INDEX_FORMAT = "|i|%s|u|%s|t|%s|";
   private static final Pattern SPLIT_SPACE = Pattern.compile(" ");
   private static final Pattern SPLIT_COMMA = Pattern.compile(",");
   private final Dialog m_dialog;
   private final String m_oldUid;
   private final String m_oldIndexLine;
   private final Activity m_activity;

   private
   AsyncCheckFeed(Activity activity, Dialog dialog, String oldIndexLine, String oldUid)
   {
      m_dialog = dialog;
      m_oldIndexLine = oldIndexLine;
      m_oldUid = oldUid;
      m_activity = activity;

      Button button = (Button) m_dialog.findViewById(R.id.dialog_button_positive);
      button.setText(R.string.dialog_checking);
      button.setEnabled(false);
   }

   static
   void newInstance(Activity activity, Dialog dialog, String oldIndexLine, String oldUid)
   {
      AsyncTask<Void, Void, String[]> task = new AsyncCheckFeed(activity, dialog, oldIndexLine, oldUid);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR);
   }

   /* Function should be safe, returns false if fails. */
   private static
   void AppendLineToIndex(Context context, String lineToAppend)
   {
      try
      {
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(Read.INDEX, Context.MODE_APPEND)));
         try
         {
            out.write(lineToAppend);
         }
         finally
         {
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

   @Override
   protected
   String[] doInBackground(Void... nothing)
   {
      /* Get the user's input. */
      CharSequence inputUrl = ((TextView) m_dialog.findViewById(R.id.dialog_url)).getText();
      CharSequence inputTags = ((TextView) m_dialog.findViewById(R.id.dialog_tags)).getText();

      inputUrl = null == inputUrl ? "" : inputUrl;

      /* Form the array of urls we will check the validity of. */
      CharSequence[] urlCheckList = {inputUrl, "https://" + inputUrl, "http://" + inputUrl};

      String url = "";
      String uid = "";

      for(CharSequence urlToCheck : urlCheckList)
      {
         if(isValidFeed(urlToCheck))
         {
            List<String> urls = Arrays.asList(Read.csvFile(m_activity, Read.INDEX, 'u')[0]);

            /* Make sure this url is not already in the index. */
            if(!urls.contains(urlToCheck.toString()))
            {
               /* This is not really in a loop so this is the only time this line runs. */
               uid = Long.toString(System.currentTimeMillis());
            }
            url = urlToCheck.toString();
            break;
         }
      }

      return new String[]{url, uid, formatUserTagsInput(inputTags)};
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
      catch(IOException ignored)
      {
      }
      catch(XmlPullParserException ignored)
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

      /* Delete the last space. */
      int builderLength = tagBuilder.length();
      tagBuilder.setLength(builderLength - 1);

      /* Remove all trailing commas. */
      while(',' == tagBuilder.charAt(tagBuilder.length() - 1))
      {
         tagBuilder.setLength(tagBuilder.length() - 1);
      }

      return tagBuilder.toString();
   }

   @Override
   protected
   void onPostExecute(String[] result)
   {
      String url = result[0];
      String uid = result[1];
      String tags = result[2];

      /* Valid feed if we set url. */
      boolean isFeedValid = !url.isEmpty();

      /* Existing feed if uid is empty. */
      boolean isExistingFeed = uid.isEmpty();

      Context context = m_dialog.getContext();

      if(isFeedValid)
      {
         /* Create the csv. */
         String newIndexLine = String.format(INDEX_FORMAT, uid.isEmpty() ? m_oldUid : uid, url, tags) + Write.NEW_LINE;

         if(isExistingFeed)
         {
            Write.editIndexLine(context, m_oldIndexLine, Write.MODE_REPLACE, newIndexLine);
         }
         else
         {
            /* Save the feed to the index. */
            AppendLineToIndex(context, newIndexLine);
         }

         /* Must update the tags first. */
         PagerAdapterTags.update(m_activity);
         AsyncNavigationAdapter.update(m_activity);
         AsyncManageAdapter.update(m_activity);

         String text = context.getString(R.string.toast_added_feed, url);
         Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

         m_dialog.dismiss();
      }
      else
      {
         Button button = (Button) m_dialog.findViewById(R.id.dialog_button_positive);
         button.setText(R.string.dialog_accept);
         button.setEnabled(true);

         Toast.makeText(context, R.string.toast_invalid_feed, Toast.LENGTH_SHORT).show();
      }
   }
}
