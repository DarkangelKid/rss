package com.poloure.simplerss;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.widget.ListView;

class ServiceHandler extends Handler
{
   static MenuItem s_refreshItem;
   private final FragmentManager m_fragmentManager;
   private final String m_applicationFolder;

   ServiceHandler(FragmentManager fragmentManager, MenuItem refreshItem, String applicationFolder)
   {
      m_fragmentManager = fragmentManager;

      /* TODO, this is not the same MenuItem when you restart the app. */
      s_refreshItem = refreshItem;
      m_applicationFolder = applicationFolder;
   }

   /* The stuff we would like to run when the service completes. */
   @Override
   public
   void handleMessage(Message msg)
   {
      /* Tell the refresh icon to stop spinning. */
      MenuItemCompat.setActionView(s_refreshItem, null);

      Bundle bundle = msg.getData();
      if(null == bundle)
      {
         return;
      }

      int updatedPage = bundle.getInt("page_number");

      /* Find which pages we want to refresh. */
      /* Because this method is only called when the activity exists, TAG_LIST should exist. */
      int tagsCount = PagerAdapterFeeds.TAG_LIST.size();
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

      /* Refresh those Pages. */
      for(int page : pagesToRefresh)
      {
         ListFragment listFragment = (ListFragment) m_fragmentManager
               .findFragmentByTag(FragmentFeeds.FRAGMENT_ID_PREFIX + page);
         if(null != listFragment)
         {
            /* TODO isAllTag not 0. */
            ListView listView = listFragment.getListView();
            AsyncTagPage.newInstance(page, listView, m_applicationFolder, 0 == page);
         }
      }
   }
}
