package com.poloure.simplerss;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.widget.BaseAdapter;

import java.io.File;
import java.util.Set;

class AsyncRefreshNavigationAdapter extends AsyncTask<String, Void, int[]>
{
   private final AdapterNavigationDrawer m_adapterNavDrawer;
   private final ActionBar               m_actionBar;
   private final int                     m_currentPage;

   private
   AsyncRefreshNavigationAdapter(BaseAdapter adapterNavDrawer, ActionBar actionBar, int currentPage)
   {
      m_adapterNavDrawer = (AdapterNavigationDrawer) adapterNavDrawer;
      m_actionBar = actionBar;
      m_currentPage = currentPage;
   }

   /* For when the user is updating the navigation drawer not from the Feeds page. */
   static
   void newInstance(BaseAdapter adapterNavDrawer, String applicationFolder)
   {
      newInstance(adapterNavDrawer, null, applicationFolder, -1);
   }

   static
   void newInstance(BaseAdapter adapterNavDrawer, ActionBar actionBar, String applicationFolder,
         int currentPage)
   {
      AsyncTask<String, Void, int[]> task = new AsyncRefreshNavigationAdapter(adapterNavDrawer,
            actionBar, currentPage);

      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(THREAD_POOL_EXECUTOR, applicationFolder);
      }
      else
      {
         task.execute(applicationFolder);
      }
   }

   /* Get the unread counts for the tags. */
   @Override
   protected
   int[] doInBackground(String... applicationFolder)
   {
      String appFolder = applicationFolder[0];

      /* If null was passed into the task, count the unread items. */
      String[] currentTags = PagerAdapterFeeds.getTagsArray();

      String[][] content = Read.csvFile(Read.INDEX, appFolder, 'f', 't');
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
            if(indexTags[j].contains(currentTags[i]))
            {
               String longFile = indexNames[j] + append;
               Set<Long> longSet = Read.longSet(longFile, appFolder);

               longSet.removeAll(AdapterTags.READ_ITEM_TIMES);
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
   void onPostExecute(int[] result)
   {
      /* Set the titles & counts arrays in this file and notify the adapter. */
      /* TODO We get the tag array twice in this class. */
      String[] tags = PagerAdapterFeeds.getTagsArray();
      m_adapterNavDrawer.setArrays(tags, result);
      m_adapterNavDrawer.notifyDataSetChanged();

      /* Update the subtitle if actionBar != null. */
      if(null != m_actionBar)
      {
         String unread = m_adapterNavDrawer.getItem(m_currentPage);
         m_actionBar.setSubtitle("Unread: " + unread);
      }
   }
}
