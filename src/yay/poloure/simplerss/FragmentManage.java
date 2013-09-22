package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

class FragmentManage extends Fragment
{

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
   View onCreateView(LayoutInflater inf, ViewGroup container, Bundle b)
   {
      View v = inf.inflate(R.layout.viewpager, container, false);

      ViewPager pager = (ViewPager) v.findViewById(R.id.pager);
      pager.setAdapter(new PagerAdapterManage(FeedsActivity.fman));

      FeedsActivity.PAGER_TAB_STRIPS[1] = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);
      FeedsActivity.PAGER_TAB_STRIPS[1].setDrawFullUnderline(true);
      Util.setStripColor(FeedsActivity.PAGER_TAB_STRIPS[1]);

      return v;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inf)
   {
      FeedsActivity.optionsMenu = menu;
      FeedsActivity.optionsMenu.clear();

      inf.inflate(R.menu.manage_overflow, FeedsActivity.optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.optionsMenu, inf);
   }
}
