package com.poloure.simplerss;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.widget.BaseAdapter;

import java.io.File;
import java.util.Set;

class AsyncRefreshNavigationAdapter extends AsyncTask<int[], Void, int[]>
{
   private final BaseAdapter                    m_navigationAdapter;
   private final Context                        m_context;
   private final ViewPager.OnPageChangeListener m_pageChange;
   private final int                            m_page;

   AsyncRefreshNavigationAdapter(BaseAdapter navigationAdapter, Context context,
         ViewPager.OnPageChangeListener pageChange, int pos)
   {
      m_navigationAdapter = navigationAdapter;
      m_pageChange = pageChange;
      m_page = pos;
      m_context = context;
   }

   private static
   int[] getUnreadCounts(Context context)
   {
      String[] currentTags = PagerAdapterFeeds.getTagsArray();

      String[][] content = Read.csvFile(Constants.INDEX, context, 'f', 't');
      String[] indexNames = content[0];
      String[] indexTags = content[1];

      String append = File.separator + Constants.ITEM_LIST;
      int total = 0;
      int tagCount = currentTags.length;
      int indexTagsCount = indexTags.length;

      int[] unreadCounts = new int[tagCount];

      /* For each tag. */
      for(int i = 1; i < tagCount; i++)
      {
         /* For each index entry. */
         for(int j = 0; j < indexTagsCount; j++)
         {
            if(indexTags[j].equals(currentTags[i]))
            {
               String longFile = indexNames[j] + append;
               Set<Long> longSet = Read.longSet(longFile, context);

               longSet.removeAll(AdapterTags.S_READ_ITEM_TIMES);
               unreadCounts[i] += longSet.size();
            }
         }
         total += unreadCounts[i];
      }

      unreadCounts[0] = total;
      return unreadCounts;
   }

   @Override
   protected
   int[] doInBackground(int[]... counts)
   {
      /* If null was passed into the task, count the unread items. */
      return 0 == counts[0].length ? getUnreadCounts(m_context) : counts[0];
   }

   @Override
   protected
   void onPostExecute(int[] result)
   {
      /* Set the titles & counts arrays in this file and notify the adapter. */
      ((AdapterNavDrawer) m_navigationAdapter).m_tagArray = PagerAdapterFeeds.getTagsArray();
      ((AdapterNavDrawer) m_navigationAdapter).m_unreadArray = result;
      m_navigationAdapter.notifyDataSetChanged();
      if(null != m_pageChange)
      {
         m_pageChange.onPageSelected(m_page);
      }
   }
}
