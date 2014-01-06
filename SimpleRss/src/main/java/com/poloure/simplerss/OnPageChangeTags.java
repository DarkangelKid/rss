package com.poloure.simplerss;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

class OnPageChangeTags extends SimpleOnPageChangeListener
{
   /* This is because we steal the unread counts from this BaseAdapter. */
   private final BaseAdapter m_navigationAdapter;
   private final FragmentManager m_fragmentManager;
   private final ActionBar m_actionBar;
   private final String m_applicationFolder;
   private int m_position;

   OnPageChangeTags(FragmentManager fragmentManager, ActionBar actionBar,
         BaseAdapter navigationAdapter, String applicationFolder)
   {
      m_fragmentManager = fragmentManager;
      m_actionBar = actionBar;
      m_navigationAdapter = navigationAdapter;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   void onPageSelected(int position)
   {
      m_position = position;

      /* Update the ActionBar subtitle. */
      String unread = (String) m_navigationAdapter.getItem(position);
      m_actionBar.setSubtitle("Unread: " + unread);
   }

   @Override
   public
   void onPageScrollStateChanged(int state)
   {
      if(ViewPager.SCROLL_STATE_IDLE != state)
      {
         return;
      }

      /* Refresh the page if it has no items on display. */
      String fragmentTag = FragmentFeeds.FRAGMENT_ID_PREFIX + m_position;
      ListFragment tagFragment = (ListFragment) m_fragmentManager.findFragmentByTag(fragmentTag);
      Adapter listAdapter = tagFragment.getListAdapter();

      /* If the page has no items in the ListView yet, refresh the page. */
      if(0 == listAdapter.getCount())
      {
         ListView listView = tagFragment.getListView();
         AsyncTagPage.newInstance(m_position, listView, m_applicationFolder, 0 == m_position);
      }

   }
}
