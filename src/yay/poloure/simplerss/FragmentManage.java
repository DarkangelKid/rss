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
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View v = inflater.inflate(R.layout.viewpager, container, false);

      ViewPager pager = (ViewPager) v.findViewById(R.id.pager);
      pager.setAdapter(new PagerAdapterManage(FeedsActivity.s_fragmentManager));

      Constants.PAGER_TAB_STRIPS[1] = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);
      Constants.PAGER_TAB_STRIPS[1].setDrawFullUnderline(true);
      Util.setStripColor(Constants.PAGER_TAB_STRIPS[1]);

      return v;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      FeedsActivity.s_optionsMenu = menu;
      FeedsActivity.s_optionsMenu.clear();

      inflater.inflate(R.menu.manage_overflow, FeedsActivity.s_optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.s_optionsMenu, inflater);
   }
}
