package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

class fragment_settings extends Fragment
{
   public fragment_settings()
   {
   }

   @Override
   public View onCreateView(LayoutInflater in, ViewGroup container, Bundle b)
   {
      View v = in.inflate(R.layout.viewpager, container, false);
      ViewPager vp = (ViewPager) v.findViewById(R.id.pager);
      vp.setAdapter(new pageradapter_settings(main.fman));

      main.strips[2] = (PagerTabStrip) v.findViewById(R.id.pager_tab_strip);
      main.strips[2].setDrawFullUnderline(true);
      util.set_strip_colour(main.strips[2]);

      return v;
   }
}
