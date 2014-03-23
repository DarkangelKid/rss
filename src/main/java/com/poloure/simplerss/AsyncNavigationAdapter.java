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
import android.widget.WrapperListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

class AsyncNavigationAdapter extends AsyncTask<String, Void, String[][]>
{
    private final FeedsActivity m_activity;

    private
    AsyncNavigationAdapter(Activity activity)
    {
        m_activity = (FeedsActivity) activity;
    }

    static
    void run(Activity activity)
    {
        AsyncTask<String, Void, String[][]> task = new AsyncNavigationAdapter(activity);
        task.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    /* Get the unread counts for the tags. */
    @Override
    protected
    String[][] doInBackground(String... applicationFolder)
    {
      /* Get the total number of tags and feeds that exist. */
        int tagTotal = PagerAdapterTags.s_tagList.size();

      /* Make a NavItem for each tag we will display in the navigation drawer. */
        String[][] navItems = new String[tagTotal][2];

      /* Number of feeds. */
        int feedCount = m_activity.m_index.size();

      /* This is a list of Sets each containing all of the feed's items. */
        List<Collection<Long>> feedItems = new ArrayList<Collection<Long>>(feedCount);

        for(IndexItem indexItem : m_activity.m_index)
        {
            ObjectIO reader = new ObjectIO(m_activity, indexItem.m_uid + ServiceUpdate.ITEM_LIST);
            Collection<Long> set = reader.readCollection(HashSet.class);
            feedItems.add(set);
        }

      /* Create a temporary collection we will .clear() each iteration of the next for loop. */
        Collection<Long> itemsInTag = Collections.synchronizedCollection(new HashSet<Long>(0));

      /* For each tag excluding the all tag. */
        for(int i = 0; tagTotal > i; i++)
        {
            String tag = PagerAdapterTags.s_tagList.get(i);

         /* For each feed, if the feed ∈ this tag, add the feed items to the tag collection. */
            for(int j = 0; j < feedCount; j++)
            {
            /* If the feed's index entry (tag1, tag2, etc) contains this tag or is the all tag. */
                if(0 == i || Arrays.asList(m_activity.m_index.get(j).m_tags).contains(tag))
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
        ListView navigationList = (ListView) m_activity.findViewById(R.id.fragment_navigation_drawer);
        WrapperListAdapter wrapperAdapter = (WrapperListAdapter) navigationList.getAdapter();
        ArrayAdapter<String[]> adapter = (ArrayAdapter<String[]>) wrapperAdapter.getWrappedAdapter();

      /* Update the data in the adapter. */
        adapter.clear();
        adapter.addAll(result);

      /* Update the subtitle. */
        if(Constants.s_fragmentFeeds.isVisible())
        {
            Utilities.setTitlesAndDrawerAndPage(null, -10);
        }
    }
}
