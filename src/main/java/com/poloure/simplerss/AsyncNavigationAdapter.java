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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

class AsyncNavigationAdapter extends AsyncTask<String, Void, String[][]>
{
   private final Activity m_activity;

   private
   AsyncNavigationAdapter(Activity activity)
   {
      m_activity = activity;
   }

   static
   void update(Activity activity)
   {
      AsyncTask<String, Void, String[][]> task = new AsyncNavigationAdapter(activity);
      task.executeOnExecutor(THREAD_POOL_EXECUTOR);
   }

   /* Get the unread counts for the tags. */
   @Override
   protected
   String[][] doInBackground(String... applicationFolder)
   {
      /* Read the index file for an array of feed names and feed tags. */
      String[][] content = Read.csvFile(m_activity, Read.INDEX, 'i', 't');

      /* Get the total number of tags and feeds that exist. */
      int tagTotal = PagerAdapterTags.TAG_LIST.size();

      /* Make a NavItem for each tag we will display in the navigation drawer. */
      String[][] navItems = new String[tagTotal][2];

      /* This is a list of Sets each containing all of the feed's items. */
      List<Collection<Long>> feedItems = new ArrayList<Collection<Long>>(content[0].length);
      for(String feedUid : content[0])
      {
         feedItems.add(Read.longSet(m_activity, feedUid + ServiceUpdate.ITEM_LIST));
      }

      /* Create a temporary collection we will .clear() each iteration of the next for loop. */
      Collection<Long> itemsInTag = Collections.synchronizedCollection(new HashSet<Long>(0));

      /* For each tag excluding the all tag. */
      for(int i = 0; tagTotal > i; i++)
      {
         String tag = PagerAdapterTags.TAG_LIST.get(i);

         /* For each feed, if the feed ∈ this tag, add the feed items to the tag collection. */
         for(int j = 0; j < content[0].length; j++)
         {
            /* If the feed's index entry (tag1, tag2, etc) contains this tag or is the all tag. */
            if(0 == i || Arrays.asList(PagerAdapterTags.SPLIT_COMMA.split(content[1][j]))
                               .contains(tag))
            {
               itemsInTag.addAll(feedItems.get(j));
            }
         }
         itemsInTag.removeAll(AdapterTags.READ_ITEM_TIMES);

         int size = itemsInTag.size();
         navItems[i][0] = tag;
         navItems[i][1] = 0 == size ? "" : Utilities.NUMBER_FORMAT.format(size);
         itemsInTag.clear();
      }

      return navItems;
   }

   @Override
   protected
   void onPostExecute(String[][] result)
   {
      /* Set the titles & counts arrays in this file and notify the adapter. */
      ListView navigationList = (ListView) m_activity.findViewById(R.id.navigation_drawer);
      ArrayAdapter<String[]> adapter = (ArrayAdapter<String[]>) navigationList.getAdapter();

      /* Update the data in the adapter. */
      adapter.clear();
      adapter.addAll(result);

      /* Update the subtitle. */
      Utilities.updateSubtitle(m_activity);
   }
}
