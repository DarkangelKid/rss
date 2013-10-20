package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

class FragmentSettings extends Fragment
{
   private static final int VIEW_PAGER_ID = 0x3000;

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      Context context = getActivity();

      if(null == container)
      {
         return new View(context);
      }

      setHasOptionsMenu(true);
      FragmentManager fragmentManager = getFragmentManager();

      ViewPager.OnPageChangeListener pageChangeSettings = new OnPageChangeSettings(this);

      ViewPager pager = new ViewPager(context);
      pager.setAdapter(new PagerAdapterSettings(fragmentManager, context));
      pager.setOnPageChangeListener(pageChangeSettings);
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
