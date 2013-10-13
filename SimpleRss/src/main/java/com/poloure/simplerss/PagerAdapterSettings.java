package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapterSettings extends FragmentPagerAdapter
{
   private static final Fragment[] SETTINGS_DIR_FRAGMENTS = {
         new FragmentSettingsFunctions(), new FragmentSettingsUi(),
   };
   private static String[] s_settingsTitles;

   PagerAdapterSettings(FragmentManager fm, Context context)
   {
      super(fm);
      Resources resources = context.getResources();
      s_settingsTitles = resources.getStringArray(R.array.settings_titles);
   }

   @Override
   public
   int getCount()
   {
      return s_settingsTitles.length;
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return s_settingsTitles[position];
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return SETTINGS_DIR_FRAGMENTS[position];
   }
}
