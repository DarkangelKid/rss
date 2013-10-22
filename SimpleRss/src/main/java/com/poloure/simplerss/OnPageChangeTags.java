package com.poloure.simplerss;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

class OnPageChangeTags extends SimpleOnPageChangeListener
{
   private final Fragment    m_fragment;
   private final BaseAdapter m_navigationAdapter;

   OnPageChangeTags(Fragment fragment, BaseAdapter navigationAdapter)
   {
      m_fragment = fragment;
      m_navigationAdapter = navigationAdapter;
   }

   @Override
   public
   void onPageSelected(int position)
   {
      ActionBarActivity activity = (ActionBarActivity) m_fragment.getActivity();
      FragmentManager fragmentManager = m_fragment.getFragmentManager();

      /* Update the subtitle. */
      ViewPager viewPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      int currentPage = viewPager.getCurrentItem();

      String[] item = (String[]) m_navigationAdapter.getItem(currentPage);
      String unread = item[0];
      String tag = item[1];

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(tag + " | " + unread);

      /* Refresh the page if it has no items on display. */
      String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + position;
      ListFragment tagFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
      ListAdapter adapter = tagFragment.getListAdapter();

      if(0 == adapter.getCount())
      {
         Context context = m_fragment.getActivity();
         Update.asyncCompatRefreshPage(position, fragmentManager, context);
      }
   }
}
