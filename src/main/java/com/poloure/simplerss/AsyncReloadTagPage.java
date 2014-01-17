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

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

class AsyncReloadTagPage extends AsyncTask<Integer, Collection, Void>
{
   private static final int MIN_DES = 8;
   private final ListView m_listView;

   private
   AsyncReloadTagPage(ListView listView)
   {
      m_listView = listView;
   }

   static
   void newInstance(int pageNumber, ListView listView)
   {
      AsyncTask<Integer, Collection, Void> task = new AsyncReloadTagPage(listView);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR, pageNumber);
   }

   @Override
   protected
   Void doInBackground(Integer... page)
   {
      Context context = m_listView.getContext();

      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();
      Map<Long, FeedItem> map = new TreeMap<>(Collections.reverseOrder());

      String tag = PagerAdapterFeeds.TAG_LIST.get(page[0]);
      String[][] index = Read.csvFile(context, Read.INDEX, 'f', 't');

      for(int i = 0; i < index[0].length; i++)
      {
         if(0 == page[0] ||
            Arrays.asList(PagerAdapterFeeds.SPLIT_COMMA.split(index[1][i])).contains(tag))
         {
            String[][] content = Read.csvFile(context, index[0][i] + ServiceUpdate.CONTENT_FILE,
                  't', 'l', 'b', 'i', 'p', 'x', 'y', 'z');

            for(int j = 0; j < content[0].length; j++)
            {
               FeedItem data = new FeedItem();

               /* Edit the data. */
               if(!content[3][j].isEmpty())
               {
                  int lastSlash = content[3][j].lastIndexOf('/') + 1;
                  data.m_imageName = content[3][j].substring(lastSlash);

                  /* If we have not downloaded the image yet, fake no image. */
                  if(!Read.fileExists(context, data.m_imageName))
                  {
                     data.m_imageName = "";
                  }
               }

               for(int k = 0; 3 > k; k++)
               {
                  data.m_desLines[k] = MIN_DES > content[5][j].length() ? "" : content[k + 5][j];
               }
               data.m_time = fastParseLong(content[4][j]);
               data.m_title = content[0][j];
               data.m_urlFull = content[1][j];
               data.m_url = content[2][j];

               /* Do not add duplicates. */
               if(!adapterTag.m_times.contains(data.m_time))
               {
                  map.put(data.m_time, data);
               }
            }
         }
      }
      if(!map.isEmpty())
      {
         publishProgress(map.values(), map.keySet());
      }
      return null;
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
   void onProgressUpdate(Collection... values)
   {
      int top = 0;
      long timeBefore = 0L;
      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();

      boolean notFirstLoad = 0 != m_listView.getCount();

      /* Find the top visible item and pixel offset of this item in the list. */
      if(notFirstLoad)
      {
         /* Get the time of the top item. */
         int topVisibleItem = m_listView.getFirstVisiblePosition();
         timeBefore = adapterTag.m_feedItems.get(topVisibleItem).m_time;

         View v = m_listView.getChildAt(0);
         top = null == v ? 0 : v.getTop();
         if(0 != top)
         {
            View childAt = m_listView.getChildAt(1);
            if(null != childAt)
            {
               top = childAt.getTop();
            }
         }
      }

      /* Prepend the new items (that were not in the adapter already) to the adapter lists. */
      /* Do not count items as Read while we are updating the list. */
      adapterTag.m_isReadingItems = false;
      adapterTag.m_feedItems.addAll(0, values[0]);
      adapterTag.m_times.addAll(0, values[1]);
      adapterTag.notifyDataSetChanged();

      /* We now need to find the position of the item with the time timeBefore. */
      int newPositionOfTop = adapterTag.m_times.indexOf(timeBefore);
      if(-1 == newPositionOfTop)
      {
         FeedsActivity.gotoLatestUnread(m_listView);
      }
      else
      {
         m_listView.setSelectionFromTop(newPositionOfTop + 1, top - m_listView.getPaddingTop());
      }
      adapterTag.m_isReadingItems = true;
   }
}
