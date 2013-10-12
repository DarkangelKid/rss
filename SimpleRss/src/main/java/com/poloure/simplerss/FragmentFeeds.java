package com.poloure.simplerss;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
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
   static        ViewPager   VIEW_PAGER;
   private final BaseAdapter m_navigationAdapter;
   private final Context     m_context;

   FragmentFeeds(BaseAdapter navigationAdapter, Context context)
   {
      m_navigationAdapter = navigationAdapter;
      m_context = context;
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      if(container == null)
      {
         return null;
      }

      //

      setHasOptionsMenu(true);

      Constants.PAGER_TAB_STRIPS[0] = Util.newPagerTabStrip(m_context);

      ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      VIEW_PAGER = new ViewPager(m_context);

      VIEW_PAGER.addView(Constants.PAGER_TAB_STRIPS[0], layoutParams);

      FragmentManager fragmentManager = getFragmentManager();
      VIEW_PAGER.setAdapter(new PagerAdapterFeeds(m_navigationAdapter, fragmentManager, m_context));
      VIEW_PAGER.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      VIEW_PAGER.setOnPageChangeListener(new PageChange(this));
      VIEW_PAGER.setId(0x1000);

      return VIEW_PAGER;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      FeedsActivity.s_optionsMenu = menu;
      FeedsActivity.s_optionsMenu.clear();

      inflater.inflate(R.menu.main_overflow, FeedsActivity.s_optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.s_optionsMenu, inflater);

      Activity activity = getActivity();
      boolean serviceRunning = isServiceRunning(activity);
      Util.setRefreshingIcon(serviceRunning);
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.s_drawerToggle.onOptionsItemSelected(item))
      {
         return true;
      }
      if(item.getTitle().equals(Util.getString(R.string.add_feed)))
      {
         FeedDialog.showAddDialog(m_navigationAdapter);
         return true;
      }
      if(item.getTitle().equals(Util.getString(R.string.unread)))
      {
         Util.gotoLatestUnread(null, true, 0, this);
         return true;
      }
      if(item.getTitle().equals(Util.getString(R.string.refresh)))
      {
         refreshFeeds();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   /* Updates and refreshes the tags with any new content. */
   private
   void refreshFeeds()
   {
      Util.setRefreshingIcon(true);

      /* Set the service handler in FeedsActivity so we can check and call it
       * from ServiceUpdate. */
      FeedsActivity.s_serviceHandler = new OnFinishService();
      int currentPage = VIEW_PAGER.getCurrentItem();
      Intent intent = Util.getServiceIntent(currentPage);
      m_context.startService(intent);
   }

   private static
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

   class OnFinishService extends Handler
   {
      /* The stuff we would like to run when the service completes. */
      @Override
      public
      void handleMessage(Message msg)
      {
         Util.setRefreshingIcon(false);
         int page = msg.getData().getInt("page_number");
         refreshPages(page);
      }

      /* Use this after content has been updated and you need to ManageFeedsRefresh */
      private
      void refreshPages(int page)
      {
         Update.page(m_navigationAdapter, 0);

         int tagsCount = Read.count(Constants.TAG_LIST);
         if(0 == page)
         {
            for(int i = 1; i < tagsCount; i++)
            {
               Update.page(m_navigationAdapter, i);
            }
         }
         else
         {
            Update.page(m_navigationAdapter, page);
         }
      }
   }

   private
   class PageChange implements ViewPager.OnPageChangeListener
   {
      private final Fragment m_fragment;

      PageChange(Fragment fragment)
      {
         m_fragment = fragment;
      }

      @Override
      public
      void onPageScrolled(int pos, float offset, int offsetPx)
      {
      }

      @Override
      public
      void onPageSelected(int pos)
      {
         if(0 == Util.getCardAdapter(pos, m_fragment).getCount())
         {
            Update.page(m_navigationAdapter, pos);
         }
      }

      @Override
      public
      void onPageScrollStateChanged(int state)
      {
      }
   }
}
