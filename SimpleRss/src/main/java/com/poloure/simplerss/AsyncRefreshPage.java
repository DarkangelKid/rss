package com.poloure.simplerss;

import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
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
   private static final int MAX_DESCRIPTION_LENGTH = 360;
   private static final int MIN_DESCRIPTION_LENGTH = 8;
   private static final int MIN_IMAGE_HEIGHT = 32;
   private static final ForegroundColorSpan COLOR_LINK = new ForegroundColorSpan(
         Color.argb(128, 0, 0, 0));
   private static final AbsoluteSizeSpan LINK_SIZE = new AbsoluteSizeSpan(10, true);
   private static final ForegroundColorSpan COLOR_TITLE = new ForegroundColorSpan(
         Color.argb(255, 0, 0, 0));
   private static final AbsoluteSizeSpan TITLE_SIZE = new AbsoluteSizeSpan(14, true);
   private static final ForegroundColorSpan COLOR_DESCRIPTION = new ForegroundColorSpan(
         Color.argb(205, 0, 0, 0));
   private static final AbsoluteSizeSpan DESCRIPTION_SIZE = new AbsoluteSizeSpan(12, true);
   private static final int FULL_SPAN = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
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

      task.executeOnExecutor(THREAD_POOL_EXECUTOR, pageNumber);
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
      final List<Long> timeListInAdapter = adapterTag.getTimeList();

      int feedsLength = feedNames.length;
      for(int j = 0; j < feedsLength; j++)
      {
         if(m_isAllTag || feedTags[j].contains(tag))
         {
            String[][] content = Read.csvFile(feedNames[j] + contentFile, m_applicationFolder, 't',
                  'd', 'l', 'i', 'h', 'p');

            if(0 == content.length)
            {
               return null;
            }

            String[] titles = content[0];
            String[] descriptions = content[1];
            String[] links = content[2];
            String[] imageUrls = content[3];
            String[] heights = content[4];
            String[] times = content[5];

            String feedThumbnailDir = feedNames[j] + thumbnailDir;

            int timesLength = times.length;
            for(int i = 0; i < timesLength; i++)
            {
               FeedItem data = new FeedItem();

               /* Edit the data. */
               if(null != imageUrls[i])
               {

                  data.m_EffImageHeight = null == heights[i] || 0 == heights[i].length()
                        ? 0
                        : fastParseInt(heights[i]);

                  /* If the image is large enough so that we should care about it. */
                  if(MIN_IMAGE_HEIGHT < data.m_EffImageHeight)
                  {
                     int lastSlash = imageUrls[i].lastIndexOf(File.separatorChar) + 1;
                     data.m_imageName = feedThumbnailDir + imageUrls[i].substring(lastSlash);
                  }
                  else
                  {
                     data.m_imageName = "";
                     data.m_EffImageHeight = 0;
                  }
               }

               if(null == descriptions[i] || MIN_DESCRIPTION_LENGTH > descriptions[i].length())
               {
                  descriptions[i] = "";
               }
               else if(MAX_DESCRIPTION_LENGTH <= descriptions[i].length())
               {
                  descriptions[i] = descriptions[i].substring(0, MAX_DESCRIPTION_LENGTH);
               }

               String itemTitle = null == titles[i] ? "" : titles[i];
               String itemUrl = links[i];
               data.m_itemDescription = descriptions[i];

               data.m_itemTime = fastParseLong(times[i]);

               /* Make the editable. */
               Editable editable = new SpannableStringBuilder();

                /* First, the title. */
               int titleLength = itemTitle.length();
               editable.append(itemTitle);
               editable.setSpan(TITLE_SIZE, 0, titleLength, FULL_SPAN);
               editable.setSpan(COLOR_TITLE, 0, titleLength, FULL_SPAN);
               editable.append("\n");

                /* Next the link. */
               int linkStart = editable.length();

               if(containsArabic(itemTitle))
               {
                  editable.append((char) 0x200F);
               }
               editable.append(itemUrl);
               editable.setSpan(LINK_SIZE, linkStart, editable.length(), FULL_SPAN);
               editable.setSpan(COLOR_LINK, linkStart, editable.length(), FULL_SPAN);

               boolean isNoImage = 0 == data.m_EffImageHeight;
               if(isNoImage)
               {
                  editable.append("\n");

                  /* Finally the description. */
                  int descriptionStart = editable.length();
                  editable.append(data.m_itemDescription);
                  editable.setSpan(DESCRIPTION_SIZE, descriptionStart, editable.length(),
                        FULL_SPAN);
                  editable.setSpan(COLOR_DESCRIPTION, descriptionStart, editable.length(),
                        FULL_SPAN);
               }

               data.m_titleAndLink = editable;

               /* Do not add duplicates, do not add read items if opacity == 0 */
               boolean notInAdapter = !timeListInAdapter.contains(data.m_itemTime);

               if(notInAdapter)
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

      boolean notFirstLoad = 0 != m_listView.getCount();

      /* Find the exact mPosition in the list. */
      if(notFirstLoad)
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
      if(notFirstLoad)
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

   private static
   int fastParseInt(String s)
   {
      char[] chars = s.toCharArray();
      int num = 0;

      for(char c : chars)
      {
         int value = (int) c - 48;
         num = num * 10 + value;
      }
      return num;
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
   boolean containsArabic(String text)
   {
      char[] chars = text.toCharArray();
      for(int i : chars)
      {
         if(0x600 <= i && 0x6ff >= i)
         {
            return true;
         }
         if(0x750 <= i && 0x77f >= i)
         {
            return true;
         }
         if(0xfb50 <= i && 0xfc3f >= i)
         {
            return true;
         }
         if(0xfe70 <= i && 0xfefc >= i)
         {
            return true;
         }
      }
      return false;
   }
}
