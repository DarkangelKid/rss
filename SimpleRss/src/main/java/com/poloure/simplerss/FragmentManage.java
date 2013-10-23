package com.poloure.simplerss;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

class FragmentManage extends Fragment
{
   static final int VIEW_PAGER_ID = 0x2000;

   static
   Fragment newInstance()
   {
      return new FragmentManage();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      ActionBarActivity activity = (ActionBarActivity) getActivity();

      if(null == container)
      {
         return new View(activity);
      }

      setHasOptionsMenu(true);

      Resources resources = getResources();
      String[] manageTitles = resources.getStringArray(R.array.manage_titles);

      FragmentManager fragmentManager = getFragmentManager();
      ActionBar actionBar = activity.getSupportActionBar();

      PagerAdapter pagerAdapter = new PagerAdapterManage(fragmentManager, manageTitles);
      ViewPager.OnPageChangeListener pageChangeManage = new OnPageChangeSubtitle(actionBar,
            manageTitles);

      ViewPager pager = new ViewPager(activity);
      pager.setAdapter(pagerAdapter);
      pager.setOnPageChangeListener(pageChangeManage);
      pager.setId(VIEW_PAGER_ID);

      return pager;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      MenuItem refreshMenu = menu.findItem(R.id.refresh);
      MenuItem unreadMenu = menu.findItem(R.id.unread);
      MenuItem addFeedMenu = menu.findItem(R.id.add_feed);

      if(null != refreshMenu && null != unreadMenu && null != addFeedMenu)
      {
         refreshMenu.setVisible(false);
         unreadMenu.setVisible(false);
         addFeedMenu.setVisible(true);
      }
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      FragmentActivity activity = getActivity();
      return super.onOptionsItemSelected(item) || activity.onOptionsItemSelected(item);
   }
}
