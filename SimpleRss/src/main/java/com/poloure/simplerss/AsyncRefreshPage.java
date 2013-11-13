package com.poloure.simplerss;

import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class AsyncRefreshPage extends AsyncTask<Integer, Object, Void>
{
   private static final short MAX_DESCRIPTION_LENGTH = (short) 360;
   private static final byte MIN_DESCRIPTION_LENGTH = (byte) 8;
   private static final byte MIN_IMAGE_WIDTH = (byte) 32;
   private final String m_applicationFolder;
   private final ListView m_listView;
   private final boolean m_isAllTag;

   private
   AsyncRefreshPage(ListView listView, String applicationFolder, boolean isAllTag)
   {
      m_listView = listView;
      m_applicationFolder = applicationFolder;
      m_isAllTag = isAllTag;
   }

   static
   void newInstance(int pageNumber, ListView listView, String storage, boolean isAllTag)
   {
      AsyncTask<Integer, Object, Void> task = new AsyncRefreshPage(listView, storage, isAllTag);

      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(THREAD_POOL_EXECUTOR, pageNumber);
      }
      else
      {
         task.execute(pageNumber);
      }
   }

   @Override
   protected
   Void doInBackground(Integer... page)
   {
      int pageNumber = page[0];
      String tag = PagerAdapterFeeds.getTagsArray()[pageNumber];
      String thumbnailDir = File.separatorChar + ServiceUpdate.THUMBNAIL_DIR;
      String contentFile = File.separatorChar + ServiceUpdate.CONTENT;

      String[][] feedsIndex = Read.csvFile(Read.INDEX, m_applicationFolder, 'f', 't');
      if(0 == feedsIndex.length)
      {
         return null;
      }
      String[] feedNames = feedsIndex[0];
      String[] feedTags = feedsIndex[1];

      Comparator<Long> reverse = Collections.reverseOrder();
      Map<Long, FeedItem> map = new TreeMap<Long, FeedItem>(reverse);

      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();

      int feedsLength = feedNames.length;
      for(int j = 0; j < feedsLength; j++)
      {
         if(m_isAllTag || feedTags[j].contains(tag))
         {
            String[][] content = Read.csvFile(feedNames[j] + contentFile, m_applicationFolder, 't',
                  'd', 'l', 'i', 'w', 'h', 'p');

            if(0 == content.length)
            {
               return null;
            }

            String[] titles = content[0];
            String[] descriptions = content[1];
            String[] links = content[2];
            String[] imageUrls = content[3];
            String[] widths = content[4];
            String[] heights = content[5];
            String[] times = content[6];

            String feedThumbnailDir = feedNames[j] + thumbnailDir;

            int timesLength = times.length;
            for(int i = 0; i < timesLength; i++)
            {
               FeedItem data = new FeedItem();

               /* Edit the data. */
               if(null != imageUrls[i])
               {
                  data.m_imageWidth = null == widths[i] || 0 == widths[i].length()
                        ? (short) 0
                        : fastParseShort(widths[i]);

                  /* If the image is large enough so that we should care about it. */
                  if((int) MIN_IMAGE_WIDTH < (int) data.m_imageWidth)
                  {
                     int lastSlash = imageUrls[i].lastIndexOf(File.separatorChar) + 1;
                     data.m_imagePath = feedThumbnailDir + imageUrls[i].substring(lastSlash);
                     data.m_imageHeight = null == heights[i] || 0 == heights[i].length()
                           ? (short) 0
                           : fastParseShort(heights[i]);
                  }
                  else
                  {
                     data.m_imagePath = "";
                     data.m_imageWidth = (short) 0;
                     data.m_imageHeight = (short) 0;
                  }
               }

               if(null == descriptions[i] ||
                     (int) MIN_DESCRIPTION_LENGTH > descriptions[i].length())
               {
                  descriptions[i] = "";
               }
               else if((int) MAX_DESCRIPTION_LENGTH <= descriptions[i].length())
               {
                  descriptions[i] = descriptions[i].substring(0, (int) MAX_DESCRIPTION_LENGTH);
               }

               data.m_itemTitle = null == titles[i] ? "" : titles[i];
               data.m_itemUrl = links[i];
               data.m_itemDescription = descriptions[i];

               data.m_itemTime = fastParseLong(times[i]);

               /* Do not add duplicates, do not add read items if opacity == 0 */
               final List<Long> timeListInAdapter = adapterTag.getTimeList();
               boolean notInAdapter = !timeListInAdapter.contains(data.m_itemTime);
               boolean isUnread = !AdapterTags.READ_ITEM_TIMES.contains(data.m_itemTime);
               boolean isOpaque = 0.0F != LayoutFeedItem.getReadItemOpacity();

               if(notInAdapter && isUnread || isOpaque)
               {
                  map.put(data.m_itemTime, data);
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

      if(0 < itemCollection.size())
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
         int value = (int) c - 48;
         num = num * 10L + (long) value;
      }
      return num;
   }

   private static
   short fastParseShort(String s)
   {
      char[] chars = s.toCharArray();
      int num = 0;

      for(char c : chars)
      {
         int value = (int) c - 48;
         num = num * 10 + value;
      }
      /* We are not reading images larger than 32,767px so (short) is fine. */
      return (short) num;
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
      int index = 0;
      long timeBefore = 0L;
      AdapterTags adapterTag = (AdapterTags) m_listView.getAdapter();
      final List<Long> timeListInAdapter = adapterTag.getTimeList();

      /* If these are the first items to be added to the list. */
      if(0 == m_listView.getCount())
      {
         m_listView.setVisibility(View.INVISIBLE);
      }
      /* Find the exact mPosition in the list. */
      else
      {
         /* Get the time of the top item. */
         index = m_listView.getFirstVisiblePosition();
         timeBefore = timeListInAdapter.get(index);

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

      adapterTag.prependArray(values[0], values[1]);
      adapterTag.notifyDataSetChanged();

      /* If this was the first time loading the tag data, jump to the latest unread item. */
      if(m_listView.isShown())
      {
         /* We now need to find the position of the item with the time timeBefore. */
         /* NOTE Do not change anything in itemList. */
         int timeListSize = timeListInAdapter.size();
         int i = 0;
         while(i < timeListSize && 0 == index)
         {
            boolean sameItem = timeBefore == timeListInAdapter.get(i);
            if(sameItem)
            {
               index = i + 1;
            }
            i++;
         }

         int listViewPaddingTop = m_listView.getPaddingTop();
         m_listView.setSelectionFromTop(index, top - listViewPaddingTop);
      }
      else
      {
         FeedsActivity.gotoLatestUnread(m_listView);
      }
   }
}
