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
import java.util.TreeMap;

class AsyncNewTagAdapters extends AsyncTask<Void, Void, TreeMap<Long, FeedItem>[]>
{
   private static final int MIN_DES = 8;
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
      String[] tags = PagerAdapterTags.TAG_LIST.toArray(new String[PagerAdapterTags.TAG_LIST.size()]);

      TreeMap<Long, FeedItem>[] maps = new TreeMap[tags.length];
      for(int i = 0; tags.length > i; i++)
      {
         maps[i] = new TreeMap<>(Collections.reverseOrder());
      }

      String[][] index = Read.csvFile(m_activity, Read.INDEX, 'f', 't');
      Collection<Integer> indices = new ArrayList<>(8);

      /* For each feed. */
      for(int i = 0; i < index[0].length; i++)
      {
         String[][] content = Read.csvFile(m_activity, index[0][i] + ServiceUpdate.CONTENT_FILE, 't', 'l', 'b', 'i', 'p', 'x', 'y', 'z');

         /* Get a list of tags that this feed belongs to. */
         List<String> feedsTags = Arrays.asList(PagerAdapterTags.SPLIT_COMMA.split(index[1][i]));

         /* Make an array of indices that these items will be added to in maps[index]. */
         indices.clear();
         for(int j = 0; tags.length > j; j++)
         {
            if(0 == j || feedsTags.contains(tags[j]))
            {
               indices.add(j);
            }
         }

         for(int j = 0; j < content[0].length; j++)
         {
            FeedItem data = new FeedItem();

            /* Edit the data. */
            data.m_imageName = getImageName(m_activity, content[3][j]);

            for(int k = 0; 3 > k; k++)
            {
               data.m_desLines[k] = MIN_DES > content[5][j].length() ? "" : content[k + 5][j];
            }
            data.m_time = fastParseLong(content[4][j]);
            data.m_title = content[0][j];
            data.m_urlFull = content[1][j];
            data.m_url = content[2][j];

            /* Put the item in each map that the feed is tagged with. */
            for(int k : indices)
            {
               maps[k].put(data.m_time, data);
            }
         }
      }

      return maps;
   }

   private static
   String getImageName(Activity activity, String imageUrl)
   {
      if(imageUrl.isEmpty())
      {
         return "";
      }

      String name = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

      return !Read.fileExists(activity, name) ? "" : name;

   }

   private static
   long fastParseLong(String s)
   {
      if(null == s)
      {
         return 0L;
      }
      char[] chars = s.toCharArray();
      long num = 0L;

      for(char c : chars)
      {
         int value = c - 48;
         num = num * 10L + value;
      }
      return num;
   }

   @Override
   protected
   void onPostExecute(TreeMap<Long, FeedItem>[] maps)
   {
      ViewPager pager = (ViewPager) m_activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      int pageCount = pager.getAdapter().getCount();

      for(int i = 0; pageCount > i; i++)
      {
         /* Get the tag page. */
         ListView listView = (ListView) m_activity.findViewById(20000 + i);

         if(null != listView)
         {
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
                  if(0 != top && null != listView.getChildAt(1))
                  {
                     View childAt = listView.getChildAt(1);
                     if(null != childAt)
                     {
                        top = childAt.getTop();
                     }
                  }
               }

               /* Set the adapters to be these new lists and do not read items while updating. */
               adapterTag.m_isReadingItems = false;

               adapterTag.m_feedItems.clear();
               adapterTag.m_times.clear();

               adapterTag.m_feedItems.addAll(maps[i].values());
               adapterTag.m_times.addAll(maps[i].keySet());

               adapterTag.notifyDataSetChanged();

               /* We now need to find the position of the item with the time timeBefore. */
               int newPositionOfTop = adapterTag.m_times.indexOf(timeBefore);
               if(-1 == newPositionOfTop)
               {
                  FeedsActivity.gotoLatestUnread(listView);
               }
               else
               {
                  listView.setSelectionFromTop(newPositionOfTop + 1, top - listView.getPaddingTop());
               }
               adapterTag.m_isReadingItems = true;
            }
         }
      }
   }
}
