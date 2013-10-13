package com.poloure.simplerss;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

class OnPageChangeSettings extends SimpleOnPageChangeListener
{
   private final Fragment m_fragment;

   OnPageChangeSettings(Fragment fragment)
   {
      m_fragment = fragment;
   }

   @Override
   public
   void onPageSelected(int pos)
   {
      ActionBarActivity activity = (ActionBarActivity) m_fragment.getActivity();

      Resources resources = activity.getResources();
      String[] manageTitles = resources.getStringArray(R.array.settings_titles);

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(manageTitles[pos]);
   }
}
