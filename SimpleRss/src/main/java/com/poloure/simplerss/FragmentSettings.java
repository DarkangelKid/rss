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

class FragmentSettings extends Fragment
{
   private static final int VIEW_PAGER_ID        = 0x3000;
   private static final int PAGER_TITLE_STRIP_ID = 653142;

   static
   Fragment newInstance()
   {
      return new FragmentSettings();
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
      String[] settingsTitles = resources.getStringArray(R.array.settings_titles);

      FragmentManager fragmentManager = getFragmentManager();

      PagerAdapter pagerAdapter = new PagerAdapterSettings(fragmentManager, settingsTitles);

      ViewPager pager = ViewPagerStrip.newInstance(activity, PAGER_TITLE_STRIP_ID);
      pager.setAdapter(pagerAdapter);
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
         addFeedMenu.setVisible(false);
      }
   }
}
