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

public class fragment_feeds extends Fragment
{
   public fragment_feeds()
   {
   }

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

      main.viewpager = (ViewPager) v.findViewById(R.id.pager);
      main.viewpager.setAdapter(new pageradapter_feeds(main.fman));
      main.viewpager.setOffscreenPageLimit(128);
      main.viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
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
            if(util.get_card_adapter(pos).getCount() == 0)
               update.page(pos);
         }
      });

      main.strips[0] = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);
      main.strips[0].setDrawFullUnderline(true);
      util.set_strip_colour(main.strips[0]);

      return v;
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inf)
   {
      main.optionsMenu = menu;
      main.optionsMenu.clear();

      inf.inflate(R.menu.main_overflow, main.optionsMenu);
      super.onCreateOptionsMenu(main.optionsMenu, inf);

      Activity main_instance = (Activity) main.con;
      util.set_refresh(service_update.check_service_running(main_instance));
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(navigation_drawer.drawer_toggle.onOptionsItemSelected(item))
         return true;
      else if(item.getTitle().equals(util.get_string(R.string.add_feed)))
      {
         add_edit_dialog.show_add_dialog(main.ctags);
         return true;
      }
      else if(item.getTitle().equals(util.get_string(R.string.unread)))
      {
         util.jump_to_latest_unread(null, true, 0);
         return true;
      }
      else if(item.getTitle().equals(util.get_string(R.string.refresh)))
      {
         util.refresh_feeds();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
}
