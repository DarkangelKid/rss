package com.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapterSettings extends FragmentPagerAdapter
{
   private static final Fragment[] SETTINGS_DIR_FRAGMENTS = {
         new FragmentSettingsFunctions(), new FragmentSettingsUi(),
   };
   private static final String[]   SETTINGS_DIR_TITLES    = Util.getArray(R.array.settings_titles);

   PagerAdapterSettings(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public
   int getCount()
   {
      return SETTINGS_DIR_TITLES.length;
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return SETTINGS_DIR_TITLES[position];
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return SETTINGS_DIR_FRAGMENTS[position];
   }
}
