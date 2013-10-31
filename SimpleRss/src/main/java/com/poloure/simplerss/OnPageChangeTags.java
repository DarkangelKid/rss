package com.poloure.simplerss;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

class OnPageChangeTags extends SimpleOnPageChangeListener
{
   /* This is because we steal the unread counts from this BaseAdapter. */
   private final BaseAdapter     m_navigationAdapter;
   private final FragmentManager m_fragmentManager;
   private final ActionBar       m_actionBar;
   private final String          m_applicationFolder;
   private final int             m_sixteenDp;

   OnPageChangeTags(FragmentManager fragmentManager, ActionBar actionBar,
         BaseAdapter navigationAdapter, String applicationFolder, int sixteenDp)
   {
      m_fragmentManager = fragmentManager;
      m_actionBar = actionBar;
      m_navigationAdapter = navigationAdapter;
      m_applicationFolder = applicationFolder;
      m_sixteenDp = sixteenDp;
   }

   @Override
   public
   void onPageSelected(int position)
   {
      /* Update the ActionBar subtitle. */
      String unread = (String) m_navigationAdapter.getItem(position);
      m_actionBar.setSubtitle("Unread: " + unread);

      /* Refresh the page if it has no items on display. */
      String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + position;
      ListFragment tagFragment = (ListFragment) m_fragmentManager.findFragmentByTag(fragmentTag);
      Adapter listAdapter = tagFragment.getListAdapter();

      /* If the page has no items in the ListView yet, refresh the page. */
      if(0 == listAdapter.getCount())
      {
         ListView listView = tagFragment.getListView();
         AsyncRefreshPage.newInstance(position, listView, m_applicationFolder, m_sixteenDp,
               0 == position);
      }
   }
}
