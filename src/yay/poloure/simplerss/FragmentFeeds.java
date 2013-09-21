package yay.poloure.simplerss;

import android.app.Activity;
import android.os.Bundle;
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

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setRetainInstance(false);
      setHasOptionsMenu(true);
   }

   @Override
   public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
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
   public void onCreateOptionsMenu(Menu menu, MenuInflater inf)
   {
      FeedsActivity.optionsMenu = menu;
      FeedsActivity.optionsMenu.clear();

      inf.inflate(R.menu.main_overflow, FeedsActivity.optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.optionsMenu, inf);

      Activity activity = (Activity) Util.getContext();
      Util.setRefreshingIcon(ServiceUpdate.isServiceRunning(activity));
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
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
         Util.refreshFeeds();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   static class PageChange implements ViewPager.OnPageChangeListener
   {
      @Override
      public void onPageScrollStateChanged(int state)
      {
      }

      @Override
      public void onPageScrolled(int pos, float offset, int offsetPx)
      {
      }

      @Override
      public void onPageSelected(int pos)
      {
         if(0 == Util.getCardAdapter(pos).getCount())
         {
            Update.page(pos);
         }
      }
   }
}
