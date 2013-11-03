package com.poloure.simplerss;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.MenuItem;
import android.widget.ListView;

class ServiceHandler extends Handler
{
   private final FragmentManager m_fragmentManager;
   private final MenuItem        m_refreshItem;
   private final String          m_applicationFolder;

   ServiceHandler(FragmentManager fragmentManager, MenuItem refreshItem, String applicationFolder)
   {
      m_fragmentManager = fragmentManager;
      m_refreshItem = refreshItem;
      m_applicationFolder = applicationFolder;
   }

   /* The stuff we would like to run when the service completes. */
   @Override
   public
   void handleMessage(Message msg)
   {
      FeedsActivity.setRefreshingIcon(false, m_refreshItem);

      Bundle bundle = msg.getData();
      if(null == bundle)
      {
         return;
      }

      int updatedPage = bundle.getInt("page_number");

         /* Find which pages we want to refresh. */
      int tagsCount = PagerAdapterFeeds.getSize();
      int[] pagesToRefresh;

      if(0 == updatedPage)
      {
         pagesToRefresh = new int[tagsCount];
         for(int i = 0; i < tagsCount; i++)
         {
            pagesToRefresh[i] = i;
         }
      }
      else
      {
         pagesToRefresh = new int[]{0, updatedPage};
      }
      String tagPrefix = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':';

      /* Refresh those Pages. */
      for(int page : pagesToRefresh)
      {
         ListFragment listFragment = (ListFragment) m_fragmentManager.findFragmentByTag(
               tagPrefix + page);
         ListView listView = listFragment.getListView();

         /* TODO isAllTag not 0. */
         AsyncRefreshPage.newInstance(page, listView, m_applicationFolder, 0 == page);
      }
   }
}
