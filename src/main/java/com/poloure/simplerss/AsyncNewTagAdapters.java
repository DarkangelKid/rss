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
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class AsyncNewTagAdapters extends AsyncTask<Void, Void, TreeMap<Long, FeedItem>[]>
{
   private final Activity m_activity;

   private
   AsyncNewTagAdapters(Activity activity)
   {
      m_activity = activity;
   }

   static
   void update(Activity activity)
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

      /* For each feed. */
      for(IndexItem indexItem : FeedsActivity.s_index)
      {
         /* Get a list of tags that this feed belongs to. */
         List<String> feedsTags = Arrays.asList(indexItem.m_tags);

         /* Make an array of indices that these items will be added to in maps[index]. */
         indices.clear();
         for(int j = 0; tags.length > j; j++)
         {
            if(0 == j || feedsTags.contains(tags[j]))
            {
               indices.add(j);
            }
         }

         /* Load the data from file. */
         Map<Long, FeedItem> tempMap = (Map<Long, FeedItem>) Read.object(m_activity, indexItem.m_uid + ServiceUpdate.CONTENT_FILE);
         if(null != tempMap)
         {
            /* Put the item in each map that the feed is tagged with. */
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
   void onPostExecute(TreeMap<Long, FeedItem>[] maps)
   {
      ViewPager pager = (ViewPager) m_activity.findViewById(R.id.viewpager);
      int pageCount = pager.getAdapter().getCount();

      for(int i = 0; pageCount > i; i++)
      {
         /* Get the tag page and skip ListViews that are null. */
         ListView listView = Utilities.getTagListView(m_activity, i);
         while(null == listView)
         {
            if(i == pageCount)
            {
               return;
            }

            listView = Utilities.getTagListView(m_activity, i);
         }

         AdapterTags adapterTag = (AdapterTags) listView.getAdapter();

         boolean firstLoad = null == listView || 0 == listView.getCount();

            /* If there are items in the currently viewed page, save the position. */
         if(null != adapterTag)
         {
            long timeBefore = 0L;
            int top = 0;
            if(!firstLoad && i == pager.getCurrentItem())
            {
                  /* Get the time of the top item. */
               int topVisibleItem = listView.getFirstVisiblePosition();
               timeBefore = adapterTag.m_feedItems.get(topVisibleItem).m_time;

               View v = listView.getChildAt(0);
               top = null == v ? 0 : v.getTop();
            }

            /* Set the adapters to be these new lists and do not read items while updating. */
            adapterTag.m_feedItems = new ArrayList<FeedItem>(maps[i].values());
            adapterTag.m_times = new ArrayList<Long>(maps[i].keySet());
            adapterTag.notifyDataSetChanged();

            /* We now need to find the position of the item with the time timeBefore. */
            int newPositionOfTop = adapterTag.m_times.indexOf(timeBefore);
            if(-1 == newPositionOfTop)
            {
               FeedsActivity.gotoLatestUnread(listView);
            }
            else
            {
               listView.setSelectionFromTop(newPositionOfTop, top - listView.getPaddingTop());
            }
         }
         ((View) listView.getParent()).setVisibility(View.VISIBLE);
      }
   }
}
