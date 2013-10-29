package com.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

class OnPageChangeTags extends SimpleOnPageChangeListener
{
   private final Fragment    m_fragment;
   private final BaseAdapter m_navigationAdapter;
   private final int         m_sixteenDp;

   OnPageChangeTags(Fragment fragment, BaseAdapter navigationAdapter, int sixteenDp)
   {
      m_fragment = fragment;
      m_navigationAdapter = navigationAdapter;
      m_sixteenDp = sixteenDp;
   }

   @Override
   public
   void onPageSelected(int position)
   {
      ActionBarActivity activity = (ActionBarActivity) m_fragment.getActivity();
      FragmentManager fragmentManager = m_fragment.getFragmentManager();

      /* Update the ActionBar subtitle. */
      String[] item = (String[]) m_navigationAdapter.getItem(position);
      String unread = item[0];
      String tag = item[1];

      String subtitle = String.format(AsyncRefreshNavigationAdapter.TAG_SUBTITLE_FORMAT, unread, tag);

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(subtitle);

      /* Refresh the page if it has no items on display. */
      String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + position;
      ListFragment tagFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
      Adapter listAdapter = tagFragment.getListAdapter();

      /* If the page has no items in the ListView yet, refresh the page. */
      if(0 == listAdapter.getCount())
      {
         ListView listView = tagFragment.getListView();
         String applicationFolder = FeedsActivity.getApplicationFolder(activity);
         AsyncRefreshPage.newInstance(position, listView, applicationFolder, m_sixteenDp,
               0 == position);
      }
   }
}
