package yay.poloure.simplerss;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

class FragmentFeeds extends Fragment
{

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

   /* Updates and refreshes the tags with any new content. */
   private static
   void refreshFeeds()
   {
      Util.setRefreshingIcon(true);

      /* Set the service handler in FeedsActivity so we can check and call it
       * from ServiceUpdate. */

      FeedsActivity.s_serviceHandler = new OnFinishService();
      Context context = Util.getContext();
      int currentPage = FeedsActivity.s_ViewPager.getCurrentItem();
      Intent intent = Util.getServiceIntent(currentPage);
      context.startService(intent);
   }

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
      View v = inflater.inflate(R.layout.viewpager, container, false);

      FeedsActivity.s_ViewPager = (ViewPager) v.findViewById(R.id.pager);
      FeedsActivity.s_ViewPager.setAdapter(new PagerAdapterFeeds(FeedsActivity.s_fragmentManager));
      FeedsActivity.s_ViewPager.setOffscreenPageLimit(128);
      FeedsActivity.s_ViewPager.setOnPageChangeListener(new PageChange());

      Constants.PAGER_TAB_STRIPS[0] = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);
      Constants.PAGER_TAB_STRIPS[0].setDrawFullUnderline(true);
      Util.setStripColor(Constants.PAGER_TAB_STRIPS[0]);

      return v;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      FeedsActivity.s_optionsMenu = menu;
      FeedsActivity.s_optionsMenu.clear();

      inflater.inflate(R.menu.main_overflow, FeedsActivity.s_optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.s_optionsMenu, inflater);

      Activity activity = (Activity) Util.getContext();
      Util.setRefreshingIcon(isServiceRunning(activity));
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
         FeedDialog.showAddDialog(FeedsActivity.s_currentTags);
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

   private static
   class PageChange implements ViewPager.OnPageChangeListener
   {
      PageChange()
      {
      }

      @Override
      public
      void onPageScrollStateChanged(int state)
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
   }

   static
   class OnFinishService extends Handler
   {
      /* Use this after content has been updated and you need to ManageRefresh */
      private static
      void refreshPages(int page)
      {
         Update.page(0);

         int tagsLength = FeedsActivity.s_currentTags.length;
         if(0 == page)
         {
            for(int i = 1; i < tagsLength; i++)
            {
               Update.page(i);
            }
         }
         else
         {
            Update.page(page);
         }
      }

      /* The stuff we would like to run when the service completes. */
      @Override
      public
      void handleMessage(Message msg)
      {
         Util.setRefreshingIcon(false);
         int page = msg.getData().getInt("page_number");
         refreshPages(page);
      }
   }
}
