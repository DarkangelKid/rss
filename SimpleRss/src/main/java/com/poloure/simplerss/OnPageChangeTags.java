package com.poloure.simplerss;

import android.app.Activity;
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
   private final Activity m_activity;
   private final String m_applicationFolder;
   private int m_position;

   OnPageChangeTags(Activity activity, BaseAdapter navigationAdapter, String applicationFolder)
   {
      m_activity = activity;
      m_navigationAdapter = navigationAdapter;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   void onPageSelected(int position)
   {
      m_position = position;

      /* Get the unread count from the navigation drawer Adapter.*/
      String unread = (String) m_navigationAdapter.getItem(position);

      /* Set the subtitle to the unread count. */
      String unreadText = m_activity.getString(R.string.subtitle_unread);
      m_activity.getActionBar().setSubtitle(unreadText + ' ' + unread);
   }

   @Override
   public
   void onPageScrollStateChanged(int state)
   {
      if(ViewPager.SCROLL_STATE_IDLE == state)
      {
         /* Refresh the page if it has no items on display. */
         String fragmentTag = FragmentFeeds.FRAGMENT_ID_PREFIX + m_position;
         FragmentManager manager = m_activity.getFragmentManager();
         ListFragment tagFragment = (ListFragment) manager.findFragmentByTag(fragmentTag);
         Adapter listAdapter = tagFragment.getListAdapter();

         /* If the page has no items in the ListView yet, refresh the page. */
         if(0 == listAdapter.getCount())
         {
            ListView listView = tagFragment.getListView();
            AsyncTagPage.newInstance(m_position, listView, m_applicationFolder, 0 == m_position);
         }
      }
   }
}
