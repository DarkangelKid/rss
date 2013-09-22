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

   public static
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
   static
   void refreshFeeds()
   {
      Util.setRefreshingIcon(true);

      /* Set the service handler in FeedsActivity so we can check and call it
       * from ServiceUpdate. */

      FeedsActivity.service_handler = new Handler()
      {
         /* The stuff we would like to run when the service completes. */
         @Override
         public
         void handleMessage(Message msg)
         {
            Util.setRefreshingIcon(false);
            int page = msg.getData().getInt("page_number");
            Util.refreshPages(page);
         }
      };
      Context context = Util.getContext();
      int current_page = FeedsActivity.viewpager.getCurrentItem();
      Intent intent = Util.getServiceIntent(current_page);
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
   View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
   {
      View v = inf.inflate(R.layout.viewpager, cont, false);

      FeedsActivity.viewpager = (ViewPager) v.findViewById(R.id.pager);
      FeedsActivity.viewpager.setAdapter(new PagerAdapterFeeds(FeedsActivity.fman));
      FeedsActivity.viewpager.setOffscreenPageLimit(128);
      FeedsActivity.viewpager.setOnPageChangeListener(new PageChange());

      FeedsActivity.PAGER_TAB_STRIPS[0] = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);
      FeedsActivity.PAGER_TAB_STRIPS[0].setDrawFullUnderline(true);
      Util.setStripColor(FeedsActivity.PAGER_TAB_STRIPS[0]);

      return v;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inf)
   {
      FeedsActivity.optionsMenu = menu;
      FeedsActivity.optionsMenu.clear();

      inf.inflate(R.menu.main_overflow, FeedsActivity.optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.optionsMenu, inf);

      Activity activity = (Activity) Util.getContext();
      Util.setRefreshingIcon(isServiceRunning(activity));
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.DRAWER_TOGGLE.onOptionsItemSelected(item))
      {
         return true;
      }
      if(item.getTitle().equals(Util.getString(R.string.add_feed)))
      {
         FeedDialog.showAddDialog(FeedsActivity.ctags);
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

   static
   class PageChange implements ViewPager.OnPageChangeListener
   {
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
}
