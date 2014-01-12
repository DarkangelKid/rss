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
import android.widget.Adapter;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class AsyncReloadTagPage extends AsyncTask<Integer, Object, Void>
{
   private static final int MIN_DESCRIPTION_LENGTH = 8;
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
   void newInstance(int pageNumber, ListView listView, String storage, boolean isAllTag)
   {
      AsyncTask<Integer, Object, Void> task = new AsyncReloadTagPage(listView, storage, isAllTag);

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

      String[][] feedsIndex = Read.csvFile(Read.INDEX, m_applicationFolder, 'f', 't');
      if(0 == feedsIndex.length)
      {
         return null;
      }
      String[] feedNames = feedsIndex[0];
      String[] feedTags = feedsIndex[1];

      Map<Long, FeedItem> map = new TreeMap<>(Collections.reverseOrder());

      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();

      int feedsLength = feedNames.length;
      for(int j = 0; j < feedsLength; j++)
      {
         if(m_isAllTag || feedTags[j].contains(tag))
         {
            String[][] content = Read.csvFile(feedNames[j] + contentFile, m_applicationFolder, 't',
                  'l', 'b', 'i', 'p', 'x', 'y', 'z');

            if(0 == content.length)
            {
               return null;
            }

            String[] titles = content[0];
            String[] links = content[1];
            String[] trimmedLinks = content[2];
            String[] imageUrls = content[3];
            String[] times = content[4];
            String[] descriptionsX = content[5];
            String[] descriptionsY = content[6];
            String[] descriptionsZ = content[7];

            String feedThumbnailDir = feedNames[j] + thumbnailDir;

            int timesLength = times.length;
            for(int i = 0; i < timesLength; i++)
            {
               FeedItem data = new FeedItem();

               /* Edit the data. */
               data.m_imageName = "";
               if(null != imageUrls[i])
               {
                  int lastSlash = imageUrls[i].lastIndexOf(File.separatorChar) + 1;
                  data.m_imageName = feedThumbnailDir + imageUrls[i].substring(lastSlash);

                  /* If we have not downloaded the image yet, fake no image. */
                  File image = new File(m_applicationFolder + data.m_imageName);
                  if(!image.exists())
                  {
                     data.m_imageName = "";
                  }
               }

               boolean desTooShort = null == descriptionsX[i] ||
                                     MIN_DESCRIPTION_LENGTH > descriptionsX[i].length();

               String[] desLines = {descriptionsX[i], descriptionsY[i], descriptionsZ[i]};
               for(int k = 0; 3 > k; k++)
               {
                  data.m_desLines[k] = desTooShort ? "" : desLines[k];
               }

               data.m_time = fastParseLong(times[i]);

               data.m_title = null == titles[i] ? "" : titles[i];
               data.m_url = null == trimmedLinks[i] ? "" : trimmedLinks[i];
               data.m_urlFull = links[i];

               /* Do not add duplicates. */
               if(!adapterTag.m_times.contains(data.m_time))
               {
                  map.put(data.m_time, data);
               }
            }
         }
      }

      /* Do not count items as Read while we are updating the list. */
      adapterTag.m_isReadingItems = false;

      int mapSize = map.size();
      Collection<FeedItem> itemCollection = map.values();
      Set<Long> longSet = map.keySet();
      Long[] longArray = longSet.toArray(new Long[mapSize]);
      List<Long> longList = Arrays.asList(longArray);

      if(!itemCollection.isEmpty())
      {
         publishProgress(itemCollection, longList);
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
   void onPostExecute(Void result)
   {
      /* Resume Read item checking. */
      Adapter adapterTag = m_listView.getAdapter();
      ((AdapterTags) adapterTag).m_isReadingItems = true;
   }

   @Override
   protected
   void onProgressUpdate(Object... values)
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
      adapterTag.m_feedItems.addAll(0, (Collection<FeedItem>) values[0]);
      adapterTag.m_times.addAll(0, (Collection<Long>) values[1]);
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
   }
}
