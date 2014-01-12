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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
      String append = File.separatorChar + ServiceUpdate.ITEM_LIST;

      /* Read the index file for an array of feed names and feed tags. */
      String[][] content = Read.csvFile(Read.INDEX, appFolder, 'f', 't');
      String[] feedNames = content[0];
      String[] feedTags = content[1];

      /* Get the total number of tags and feeds that exist. */
      int tagTotal = PagerAdapterFeeds.TAG_LIST.size();
      int feedTotal = feedNames.length;

      /* Make a NavItem for each tag we will display in the navigation drawer. */
      NavItem[] navItems = new NavItem[tagTotal];

      /* This is a list of Sets each containing all of the feed's items. */
      List<Collection<Long>> feedItems = new ArrayList<>(feedTotal);
      for(String feedName : feedNames)
      {
         feedItems.add(Read.longSet(feedName + append, appFolder));
      }

      /* Create a temporary collection we will .clear() each iteration of the next for loop. */
      Collection<Long> itemsInTag = new HashSet<>(0);

      /* For each tag excluding the all tag. */
      for(int i = 0; tagTotal > i; i++)
      {
         String tag = PagerAdapterFeeds.TAG_LIST.get(i);

         /* For each feed, if the feed ∈ this tag, add the feed items to the tag collection. */
         for(int j = 0; j < feedTotal; j++)
         {
            /* If the feed's index entry (tag1, tag2, etc) contains this tag or is the all tag. */
            if(0 == i || feedTags[j].contains(tag))
            {
               itemsInTag.addAll(feedItems.get(j));
            }
         }
         itemsInTag.removeAll(AdapterTags.READ_ITEM_TIMES);
         navItems[i] = new NavItem(tag, itemsInTag.size());
         itemsInTag.clear();
      }

      return navItems;
   }

   @Override
   protected
   void onPostExecute(NavItem[] result)
   {
      /* Set the titles & counts arrays in this file and notify the adapter. */
      ListView navigationList = (ListView) m_activity.findViewById(R.id.navigation_list);
      ArrayAdapter<NavItem> adapter = (ArrayAdapter<NavItem>) navigationList.getAdapter();

      /* Update the data in the adapter. */
      adapter.clear();
      adapter.addAll(result);
      adapter.notifyDataSetChanged();

      /* Update the subtitle. */
      Utilities.updateSubtitle(m_activity, m_currentPage);
   }
}
