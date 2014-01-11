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
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.List;
import java.util.Set;

class AsyncNavigationAdapter extends AsyncTask<String, Void, NavItem[]>
{
   private final Activity m_activity;
   private final int m_currentPage;

   private
   AsyncNavigationAdapter(Activity activity, int currentPage)
   {
      m_activity = activity;
      m_currentPage = currentPage;
   }

   static
   void newInstance(Activity activity, String applicationFolder, int currentPage)
   {
      AsyncTask<String, Void, NavItem[]> task = new AsyncNavigationAdapter(activity, currentPage);
      task.executeOnExecutor(THREAD_POOL_EXECUTOR, applicationFolder);
   }

   /* Get the unread counts for the tags. */
   @Override
   protected
   NavItem[] doInBackground(String... applicationFolder)
   {
      String appFolder = applicationFolder[0];

      /* If null was passed into the task, count the unread items. */
      List<String> currentTags = PagerAdapterFeeds.TAG_LIST;

      String[][] content = Read.csvFile(Read.INDEX, appFolder, 'f', 't');
      String[] indexNames = content[0];
      String[] indexTags = content[1];

      String append = File.separatorChar + ServiceUpdate.ITEM_LIST;
      int total = 0;
      int tagCount = currentTags.size();
      int indexTagsCount = indexTags.length;

      NavItem[] navItems = new NavItem[tagCount];

      /* TODO each longSet is read multiple times. */

      /* For each tag. */
      for(int i = 1; i < tagCount; i++)
      {
         navItems[i] = new NavItem(PagerAdapterFeeds.TAG_LIST.get(i), 0);
         /* For each index entry. */
         for(int j = 0; j < indexTagsCount; j++)
         {
            if(indexTags[j].contains(currentTags.get(i)))
            {
               String longFile = indexNames[j] + append;
               Set<Long> longSet = Read.longSet(longFile, appFolder);

               longSet.removeAll(AdapterTags.READ_ITEM_TIMES);
               navItems[i].m_count += longSet.size();
            }
         }
         total += navItems[i].m_count;
      }

      navItems[0] = new NavItem(PagerAdapterFeeds.TAG_LIST.get(0), total);

      return navItems;
   }

   @Override
   protected
   void onPostExecute(NavItem[] result)
   {
      /* Set the titles & counts arrays in this file and notify the adapter. */
      ListView navigationList = (ListView) m_activity.findViewById(R.id.navigation_list);
      ArrayAdapter<?> adapter = (ArrayAdapter<?>) navigationList.getAdapter();

      /* Update the data in the adapter. */
      adapter.clear();
      adapter.addAll(result);
      adapter.notifyDataSetChanged();

      /* Update the subtitle. */
      Utilities.updateSubtitleCount(m_activity, m_currentPage);
   }
}
