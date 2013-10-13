package com.poloure.simplerss;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class FragmentFeeds extends Fragment
{
   private static final int OFF_SCREEN_PAGE_LIMIT = 128;
   static        ViewPager   s_viewPager;
   private final BaseAdapter m_navigationAdapter;

   FragmentFeeds(BaseAdapter navigationAdapter)
   {
      m_navigationAdapter = navigationAdapter;
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
      Constants.PAGER_TAB_STRIPS[0] = Util.newPagerTabStrip(context);

      ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      s_viewPager = new ViewPager(context);

      s_viewPager.addView(Constants.PAGER_TAB_STRIPS[0], layoutParams);

      FragmentManager fragmentManager = getFragmentManager();
      s_viewPager.setAdapter(new PagerAdapterFeeds(m_navigationAdapter, fragmentManager, context));
      s_viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      s_viewPager.setOnPageChangeListener(new OnPageChangeTags(this, m_navigationAdapter));
      s_viewPager.setId(0x1000);

      return s_viewPager;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      menu.findItem(R.id.refresh).setVisible(true);
      menu.findItem(R.id.unread).setVisible(true);
      menu.findItem(R.id.add_feed).setVisible(true);
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      FragmentActivity activity = getActivity();
      return activity.onOptionsItemSelected(item);
   }

   /* Updates and refreshes the tags with any new content. */
   static
   void refreshFeeds(ActionBarActivity activity, BaseAdapter navigationAdapter)
   {
      Menu menu = ((FeedsActivity) activity).getOptionsMenu();
      Util.setRefreshingIcon(true, menu);

      /* Set the service handler in FeedsActivity so we can check and call it
       * from ServiceUpdate. */
      FeedsActivity.s_serviceHandler = new OnFinishService(activity, navigationAdapter);
      int currentPage = s_viewPager.getCurrentItem();
      Intent intent = Util.getServiceIntent(currentPage, null, activity);
      activity.startService(intent);
   }

   static
   boolean isServiceRunning(Activity activity)
   {
      ActivityManager manager = (ActivityManager) activity.getSystemService(
            Context.ACTIVITY_SERVICE);
      for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(
            Integer.MAX_VALUE))
      {
         if(ServiceUpdate.class.getName().equals(service.service.getClassName()))
         {
            return true;
         }
      }
      return false;
   }

   static
   class OnFinishService extends Handler
   {
      private final ActionBarActivity m_activity;
      private final BaseAdapter       m_navigationAdapter;

      OnFinishService(ActionBarActivity activity, BaseAdapter navigationAdapter)
      {
         m_activity = activity;
         m_navigationAdapter = navigationAdapter;
      }

      /* The stuff we would like to run when the service completes. */
      @Override
      public
      void handleMessage(Message msg)
      {
         Menu menu = ((FeedsActivity) m_activity).getOptionsMenu();
         Util.setRefreshingIcon(false, menu);

         int page = msg.getData().getInt("page_number");
         refreshPages(page);
      }

      /* Use this after content has been updated and you need to ManageFeedsRefresh */
      private
      void refreshPages(int page)
      {
         FragmentManager fragmentManager = m_activity.getSupportFragmentManager();

         Update.page(m_navigationAdapter, 0, fragmentManager, m_activity);

         int tagsCount = Read.count(Constants.TAG_LIST, m_activity);
         if(0 == page)
         {
            for(int i = 1; i < tagsCount; i++)
            {
               Update.page(m_navigationAdapter, i, fragmentManager, m_activity);
            }
         }
         else
         {
            Update.page(m_navigationAdapter, page, fragmentManager, m_activity);
         }
      }
   }

}
