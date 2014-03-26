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

import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.poloure.simplerss.adapters.AdapterFeedItems;
import com.poloure.simplerss.adapters.LinkedMapAdapter;
import com.poloure.simplerss.ui.ListViewFeeds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public
class AsyncNewTagAdapters extends AsyncTask<Void, Void, TreeMap<Long, FeedItem>[]>
{
    private final FeedsActivity m_activity;

    private
    AsyncNewTagAdapters(FeedsActivity activity)
    {
        m_activity = activity;
    }

    public static
    void update(FeedsActivity activity)
    {
        new AsyncNewTagAdapters(activity).executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    @Override
    protected
    TreeMap<Long, FeedItem>[] doInBackground(Void... nothing)
    {
        String[] tags = PagerAdapterTags.s_tagList.toArray(new String[PagerAdapterTags.s_tagList.size()]);

        TreeMap<Long, FeedItem>[] maps = new TreeMap[tags.length];
        for(int i = 0; tags.length > i; i++)
        {
            maps[i] = new TreeMap<Long, FeedItem>(Collections.reverseOrder());
        }

        Collection<Integer> indices = new ArrayList<Integer>(8);

        // For each feed.
        for(IndexItem indexItem : m_activity.m_index)
        {
            // Get a list of tags that this feed belongs to.
            List<String> feedsTags = Arrays.asList(indexItem.m_tags);

            // Make an array of indices that these items will be added to in maps[index].
            indices.clear();
            for(int j = 0; tags.length > j; j++)
            {
                if(0 == j || feedsTags.contains(tags[j]))
                {
                    indices.add(j);
                }
            }

            // Load the data from file.
            ObjectIO reader = new ObjectIO(m_activity, indexItem.m_uid + ServiceUpdate.CONTENT_FILE);
            Map<Long, FeedItem> tempMap = (Map<Long, FeedItem>) reader.read();

            // Put the item in each map that the feed is tagged with.
            if(null != tempMap)
            {
                for(int k : indices)
                {
                    maps[k].putAll(tempMap);
                }
            }
        }
        return maps;
    }

    @Override
    protected
    void onPostExecute(TreeMap<Long, FeedItem>[] result)
    {
        ViewPager pager = (ViewPager) m_activity.findViewById(R.id.viewpager);
        PagerAdapter pagerAdapter = pager.getAdapter();
        int pageCount = pagerAdapter.getCount();

        for(int i = 0; pageCount > i; i++)
        {
            // Get the tag page and skip ListViews that are null.
            ListFragmentTag fragment = FragmentFeeds.getViewPagerFragment(i);
            LinkedMapAdapter<Long, FeedItem> adapterTag = fragment.getListAdapter();
            ListViewFeeds listView = fragment.getListView();

            if(null == adapterTag)
            {
                fragment.setListAdapter(new AdapterFeedItems(m_activity, result[i]));
                listView.setSelectionOldestUnread(m_activity.getReadItemTimes());
            }
            else
            {
                long topKeyBefore = 0L;
                int top = 0;

                // If there are items in the currently viewed page, save the position.
                boolean firstLoad = null == listView || 0 == listView.getCount();
                if(!firstLoad)
                {
                    // Get the time of the top item.
                    int topVisibleItem = listView.getFirstVisiblePosition();
                    topKeyBefore = adapterTag.getKey(topVisibleItem);

                    View v = listView.getChildAt(0);
                    top = null == v ? 0 : v.getTop();
                }

                // Update the feedItems in the adapter.
                adapterTag.replaceAll(result[i]);

                // Now find the position of the item with the time timeBefore.
                if(firstLoad)
                {
                    listView.setSelectionOldestUnread(m_activity.getReadItemTimes());
                }
                else
                {
                    int newPos = adapterTag.indexOf(topKeyBefore);
                    listView.setSelectionFromTop(newPos, top - listView.getPaddingTop());
                }
            }
        }

        // If the pager is invisible, use a fade in animation.
        if(View.VISIBLE != pager.getVisibility())
        {
            pager.setVisibility(View.VISIBLE);
            AlphaAnimation animation = new AlphaAnimation(0.0F, 1.0F);
            animation.setDuration(300);
            pager.startAnimation(animation);
        }
    }
}
