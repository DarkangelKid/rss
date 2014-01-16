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

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class AsyncManage extends AsyncTask<String, Editable[], Void>
{
   private static final AbsoluteSizeSpan TITLE_SIZE = new AbsoluteSizeSpan(14, true);
   private static final StyleSpan SPAN_BOLD = new StyleSpan(Typeface.BOLD);
   private final ArrayAdapter<Editable> m_manageAdapter;
   private final Resources m_resources;

   private
   AsyncManage(ArrayAdapter<Editable> manageAdapter, Resources resources)
   {
      m_resources = resources;
      m_manageAdapter = manageAdapter;
   }

   static
   void newInstance(ArrayAdapter<Editable> manageAdapter, Resources resources,
         String applicationFolder)
   {
      AsyncTask<String, Editable[], Void> task = new AsyncManage(manageAdapter, resources);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR, applicationFolder);
   }

   private static
   int count(String fileName, String applicationFolder)
   {
      int count = 0;
      try(BufferedReader read = new BufferedReader(new FileReader(applicationFolder + fileName)))
      {
         while(null != read.readLine())
         {
            count++;
         }
      }
      catch(IOException ignored)
      {
      }
      return count;
   }

   @Override
   protected
   Void doInBackground(String... applicationFolder)
   {
      String appFolder = applicationFolder[0];

      /* Read the index file for names, urls, and tags. */
      String[][] feedsIndex = Read.csvFile(Read.INDEX, appFolder, 'f', 'u', 't');
      String[] feedNames = feedsIndex[0];
      String[] feedUrls = feedsIndex[1];
      String[] feedTags = feedsIndex[2];

      boolean rtl = Utilities.isTextRtl(PagerAdapterFeeds.TAG_LIST.get(0));
      char direction = rtl ? (char) 0x200F : (char) 0x200E;

      int size = feedNames.length;
      Editable[] editables = new SpannableStringBuilder[size];

      for(int i = 0; i < size; i++)
      {
         /* New object here because we make it a reference in the array. */
         Editable editable = new SpannableStringBuilder();

         /* Append the feed name. */
         /* If this is a RTL language but the feed name is LTR, make it RTL.
            The first char must always be a LTR/RTL char for the app to work. */
         if(direction != feedNames[i].charAt(0))
         {
            editable.append(direction);
         }
         editable.append(feedNames[i]);
         editable.append("\n");

         /* Make the feed name size 16dip. */
         int titleLength = editable.length();
         editable.setSpan(TITLE_SIZE, 0, titleLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

         /* Form the path to the feed_content file. */
         String feedContentFileName = feedNames[i] + File.separatorChar +
                                      ServiceUpdate.CONTENT_FILE;
         int feedContentSize = count(feedContentFileName, appFolder);
         String contentSize = Utilities.getLocaleInt(feedContentSize);

         /* Append the url to the next line. */
         editable.append(direction);
         editable.append(feedUrls[i]);
         editable.append("\n");

         /* Append an bold "Items :" text. */
         int thirdLinePosition = editable.length();
         editable.append(m_resources.getString(R.string.manage_feed_item_count));
         editable.append(' ');
         editable.setSpan(SPAN_BOLD, thirdLinePosition, editable.length(),
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         editable.append(contentSize);

         editable.append(" · ");

         /* Append the tags in bold. */
         int currentPosition = editable.length();
         editable.append(feedTags[i]);
         editable.setSpan(SPAN_BOLD, currentPosition, editable.length(),
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         editables[i] = editable;
      }
      publishProgress(editables);
      return null;
   }

   @Override
   protected
   void onProgressUpdate(Editable[]... values)
   {
      m_manageAdapter.clear();
      m_manageAdapter.addAll(values[0]);
   }
}
