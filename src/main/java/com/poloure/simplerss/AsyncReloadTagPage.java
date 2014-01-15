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
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

class AsyncReloadTagPage extends AsyncTask<Integer, Collection, Void>
{
   private static final int MIN_DES = 8;
   private final String m_applicationFolder;
   private final ListView m_listView;
   private final boolean m_isAllTag;

   private
   AsyncReloadTagPage(ListView listView, String applicationFolder, boolean isAllTag)
   {
      m_listView = listView;
      m_applicationFolder = applicationFolder;
      m_isAllTag = isAllTag;
   }

   static
   void newInstance(int pageNumber, ListView listView, String storage, boolean allTag)
   {
      AsyncTask<Integer, Collection, Void> task = new AsyncReloadTagPage(listView, storage, allTag);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR, pageNumber);
   }

   @Override
   protected
   Void doInBackground(Integer... page)
   {
      int pageNumber = page[0];
      String tag = PagerAdapterFeeds.TAG_LIST.get(pageNumber);

      String thumbnailDir = File.separatorChar + ServiceUpdate.THUMBNAIL_DIR;
      String contentFile = File.separatorChar + ServiceUpdate.CONTENT_FILE;

      String[][] index = Read.csvFile(Read.INDEX, m_applicationFolder, 'f', 't');
      if(0 == index[0].length)
      {
         return null;
      }

      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();
      Map<Long, FeedItem> map = new TreeMap<>(Collections.reverseOrder());

      for(int i = 0; i < index[0].length; i++)
      {
         if(m_isAllTag || index[1][i].contains(tag))
         {
            String[][] content = Read.csvFile(index[0][i] + contentFile, m_applicationFolder, 't',
                  'l', 'b', 'i', 'p', 'x', 'y', 'z');

            if(0 == content[0].length)
            {
               return null;
            }

            String feedThumbnailDir = index[0][i] + thumbnailDir;

            for(int j = 0; j < content[0].length; j++)
            {
               FeedItem data = new FeedItem();

               /* Edit the data. */
               if(!content[3][j].isEmpty())
               {
                  int lastSlash = content[3][j].lastIndexOf(File.separatorChar) + 1;
                  data.m_imageName = feedThumbnailDir + content[3][j].substring(lastSlash);

                  /* If we have not downloaded the image yet, fake no image. */
                  File image = new File(m_applicationFolder + data.m_imageName);
                  if(!image.exists())
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
