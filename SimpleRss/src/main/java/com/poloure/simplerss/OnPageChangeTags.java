package com.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.BaseAdapter;
import android.widget.ListView;

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

      /* Update the ActionBar subtitle. */
      String[] item = (String[]) m_navigationAdapter.getItem(position);
      String unread = item[0];
      String tag = item[1];

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(tag + " | " + unread);

      /* Refresh the page if it has no items on display. */
      String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + position;
      ListFragment tagFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
      AdapterTags listAdapter = (AdapterTags) tagFragment.getListAdapter();

      /* If the page has no items in the ListView yet, refresh the page. */
      if(0 == listAdapter.getCount())
      {
         ListView listView = tagFragment.getListView();
         String applicationFolder = FeedsActivity.getApplicationFolder(activity);
         AsyncRefreshPage.newInstance(position, listView, applicationFolder, /* TODO */ 24,
               0 == position);
      }
   }
}
