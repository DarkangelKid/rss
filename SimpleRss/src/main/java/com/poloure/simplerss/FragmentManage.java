package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class FragmentManage extends Fragment
{
   private final BaseAdapter m_navigationAdapter;

   FragmentManage(BaseAdapter baseAdapter)
   {
      m_navigationAdapter = baseAdapter;
   }

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
      ViewPager pager = new ViewPager(context);
      Constants.PAGER_TAB_STRIPS[1] = Util.newPagerTabStrip(context);

      ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      FragmentManager fragmentManager = getFragmentManager();
      PagerAdapter pagerAdapter = new PagerAdapterManage(m_navigationAdapter, fragmentManager,
            context);

      pager.setAdapter(pagerAdapter);
      pager.addView(Constants.PAGER_TAB_STRIPS[1], layoutParams);
      pager.setId(0x2000);

      return pager;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      menu.findItem(R.id.refresh).setVisible(false);
      menu.findItem(R.id.unread).setVisible(false);
      menu.findItem(R.id.add_feed).setVisible(true);
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      FragmentActivity activity = getActivity();
      return activity.onOptionsItemSelected(item);
   }
}
