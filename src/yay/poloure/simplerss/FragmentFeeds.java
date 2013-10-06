package yay.poloure.simplerss;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

class FragmentFeeds extends Fragment
{
   static final         ViewPager VIEW_PAGER            = new ViewPager(Util.getContext());
   private static final int       OFF_SCREEN_PAGE_LIMIT = 128;

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setRetainInstance(false);
      setHasOptionsMenu(true);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      Constants.PAGER_TAB_STRIPS[0] = new PagerTabStrip(Util.getContext());

      ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      VIEW_PAGER.addView(Constants.PAGER_TAB_STRIPS[0], layoutParams);

      FragmentManager fragmentManager = FeedsActivity.getActivity().getSupportFragmentManager();
      VIEW_PAGER.setAdapter(new PagerAdapterFeeds(fragmentManager));
      VIEW_PAGER.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      VIEW_PAGER.setOnPageChangeListener(new PageChange());
      VIEW_PAGER.setId(0x1000);

      Constants.PAGER_TAB_STRIPS[0].setDrawFullUnderline(true);
      Constants.PAGER_TAB_STRIPS[0].setGravity(Gravity.START);
      Constants.PAGER_TAB_STRIPS[0].setPadding(0, AdapterCard.EIGHT / 2, 0, AdapterCard.EIGHT / 2);
      Constants.PAGER_TAB_STRIPS[0].setTextColor(Color.WHITE);
      Constants.PAGER_TAB_STRIPS[0].setBackgroundColor(Color.parseColor("#404040"));
      Util.setStripColor(Constants.PAGER_TAB_STRIPS[0]);

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

      Util.setRefreshingIcon(isServiceRunning(FeedsActivity.getActivity()));
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
         FeedDialog.showAddDialog();
         return true;
      }
      if(item.getTitle().equals(Util.getString(R.string.unread)))
      {
         Util.gotoLatestUnread(null, true, 0);
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
   private static
   void refreshFeeds()
   {
      Util.setRefreshingIcon(true);

      /* Set the service handler in FeedsActivity so we can check and call it
       * from ServiceUpdate. */
      FeedsActivity.s_serviceHandler = new OnFinishService();
      int currentPage = VIEW_PAGER.getCurrentItem();
      Intent intent = Util.getServiceIntent(currentPage);
      Util.getContext().startService(intent);
   }

   private static
   class PageChange implements ViewPager.OnPageChangeListener
   {
      PageChange()
      {
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
         if(0 == Util.getCardAdapter(pos).getCount())
         {
            Update.page(pos);
         }
      }

      @Override
      public
      void onPageScrollStateChanged(int state)
      {
      }
   }

   static private
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
      private static
      void refreshPages(int page)
      {
         Update.page(0);

         int tagsCount = Read.count(Constants.TAG_LIST);
         if(0 == page)
         {
            for(int i = 1; i < tagsCount; i++)
            {
               Update.page(i);
            }
         }
         else
         {
            Update.page(page);
         }
      }
   }
}
