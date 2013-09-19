package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class FragmentSettings extends Fragment
{

   @Override
   public View onCreateView(LayoutInflater in, ViewGroup container, Bundle b)
   {
      View v = in.inflate(R.layout.viewpager, container, false);
      ViewPager vp = (ViewPager) v.findViewById(R.id.pager);
      vp.setAdapter(new PagerAdapterSettings(FeedsActivity.fman));

      FeedsActivity.PAGER_TAB_STRIPS[2] = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);
      FeedsActivity.PAGER_TAB_STRIPS[2].setDrawFullUnderline(true);
      Util.setStripColor(FeedsActivity.PAGER_TAB_STRIPS[2]);

      return v;
   }
}
