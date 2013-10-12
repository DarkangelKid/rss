package com.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class FragmentSettings extends Fragment
{
   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      if(container == null)
      {
         return null;
      }

      ViewPager pager = new ViewPager(Util.getContext());
      Constants.PAGER_TAB_STRIPS[2] = Util.newPagerTabStrip(Util.getContext());

      ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      FragmentManager fragmentManager = getChildFragmentManager();
      pager.setAdapter(new PagerAdapterSettings(fragmentManager));
      pager.addView(Constants.PAGER_TAB_STRIPS[2], layoutParams);
      pager.setId(0x3000);

      return pager;
   }
}
