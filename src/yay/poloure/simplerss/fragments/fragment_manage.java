package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

class fragment_manage extends Fragment
{
   public fragment_manage()
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
   public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle b)
   {
      View v = inf.inflate(R.layout.viewpager_manage, container, false);

      ViewPager pager = (ViewPager) v.findViewById(R.id.manage_viewpager);
      pager.setAdapter(new main.pageradapter_manage(main.fman));

      main.strips[1] = (PagerTabStrip) v.findViewById(R.id.manage_title_strip);
      main.strips[1].setDrawFullUnderline(true);
      util.set_strip_colour(main.strips[1]);

      return v;
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inf)
   {
      main.optionsMenu = menu;
      main.optionsMenu.clear();

      inf.inflate(R.menu.manage_overflow, main.optionsMenu);
      super.onCreateOptionsMenu(main.optionsMenu, inf);
   }
}
