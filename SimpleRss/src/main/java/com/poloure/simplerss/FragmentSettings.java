package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

class FragmentSettings extends Fragment
{
   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      if(null == container)
      {
         return null;
      }

      setHasOptionsMenu(true);
      Context context = getActivity();
      FragmentManager fragmentManager = getFragmentManager();

      ViewPager.OnPageChangeListener pageChangeSettings = new OnPageChangeSettings(this);

      ViewPager pager = new ViewPager(context);
      pager.setAdapter(new PagerAdapterSettings(fragmentManager, context));
      pager.setOnPageChangeListener(pageChangeSettings);
      pager.setId(0x3000);

      return pager;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      menu.findItem(R.id.refresh).setVisible(false);
      menu.findItem(R.id.unread).setVisible(false);
      menu.findItem(R.id.add_feed).setVisible(false);
   }
}
