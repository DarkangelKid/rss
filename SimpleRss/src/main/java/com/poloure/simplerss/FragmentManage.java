package com.poloure.simplerss;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/* Must be public for rotation. */
public
class FragmentManage extends Fragment
{
   static final int VIEW_PAGER_ID = 20000;
   static final String FRAGMENT_TAGS_ID = "android:switcher:20000:0";
   static final String FRAGMENT_FEEDS_ID = "android:switcher:20000:1";
   static final String FRAGMENT_FILTERS_ID = "android:switcher:20000:2";
   private static final int PAGER_TITLE_STRIP_ID = 54218;

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

      PagerAdapter pagerAdapter = new PagerAdapterManage(fragmentManager, manageTitles);

      ViewPager viewpager = ViewPagerStrip.newInstance(activity, PAGER_TITLE_STRIP_ID);
      viewpager.setAdapter(pagerAdapter);
      viewpager.setId(VIEW_PAGER_ID);

      return viewpager;
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
}
