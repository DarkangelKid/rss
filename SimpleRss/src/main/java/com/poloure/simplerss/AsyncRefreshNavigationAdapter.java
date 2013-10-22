package com.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.Set;

class AsyncRefreshNavigationAdapter extends AsyncTask<int[], Void, int[]>
{
   private final ActionBarActivity m_activity;

   AsyncRefreshNavigationAdapter(Activity activity)
   {
      m_activity = (ActionBarActivity) activity;
   }

   @Override
   protected
   int[] doInBackground(int[]... counts)
   {
      /* If null was passed into the task, count the unread items. */
      return 0 == counts[0].length ? getUnreadCounts(m_activity) : counts[0];
   }

   @Override
   protected
   void onPostExecute(int[] result)
   {
      ListView navigationList = (ListView) m_activity.findViewById(R.id.left_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      /* Set the titles & counts arrays in this file and notify the adapter. */
      ((AdapterNavDrawer) navigationAdapter).m_tagArray = PagerAdapterFeeds.getTagsArray();
      ((AdapterNavDrawer) navigationAdapter).m_unreadArray = result;
      navigationAdapter.notifyDataSetChanged();

      /* Update the subtitle. */
      ViewPager viewPager = (ViewPager) m_activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      int currentPage = viewPager.getCurrentItem();

      String[] item = (String[]) navigationAdapter.getItem(currentPage);
      String unread = item[0];
      String tag = item[1];

      ActionBar actionBar = m_activity.getSupportActionBar();
      actionBar.setSubtitle(tag + " | " + unread);
   }

   private static
   int[] getUnreadCounts(Context context)
   {
      String[] currentTags = PagerAdapterFeeds.getTagsArray();

      String[][] content = Read.csvFile(Read.INDEX, context, 'f', 't');
      String[] indexNames = content[0];
      String[] indexTags = content[1];

      String append = File.separatorChar + ServiceUpdate.ITEM_LIST;
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
}
