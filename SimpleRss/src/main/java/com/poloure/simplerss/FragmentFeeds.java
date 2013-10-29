package com.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

class FragmentFeeds extends Fragment
{
   static final         int VIEW_PAGER_ID         = 0x1000;
   private static final int OFF_SCREEN_PAGE_LIMIT = 128;

   static
   Fragment newInstance()
   {
      return new FragmentFeeds();
   }

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
      FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
      Resources resources = getResources();
      String applicationFolder = FeedsActivity.getApplicationFolder(context);
      String allTag = resources.getString(R.string.all_tag);

      ListView navigationList = (ListView) ((Activity) context).findViewById(
            R.id.navigation_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      int sixteenDp = Math.round(displayMetrics.density * 16);

      ViewPager.OnPageChangeListener pageChange = new OnPageChangeTags(this, navigationAdapter,
            sixteenDp);

      PagerAdapter adapter = new PagerAdapterFeeds(fragmentManager);
      PagerAdapterFeeds.getTagsFromDisk(applicationFolder, allTag);
      adapter.notifyDataSetChanged();

      ViewPager viewPager = new ViewPager(context);
      viewPager.setAdapter(adapter);
      viewPager.setOnPageChangeListener(pageChange);

      viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      viewPager.setId(VIEW_PAGER_ID);

      return viewPager;
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
         refreshMenu.setVisible(true);
         unreadMenu.setVisible(true);
         addFeedMenu.setVisible(true);
      }
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      FragmentActivity activity = getActivity();
      return activity.onOptionsItemSelected(item);
   }
}
