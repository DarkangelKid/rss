package com.poloure.simplerss;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

class PagerAdapterSettings extends FragmentPagerAdapter
{
   private final String[] m_settingsTitles;

   PagerAdapterSettings(FragmentManager fm, String[] settingsTitles)
   {
      super(fm);
      m_settingsTitles = settingsTitles.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_settingsTitles.length;
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return m_settingsTitles[position];
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return 0 == position
            ? ListFragmentSettingsFunctions.newInstance()
            : ListFragmentSettingsUi.newInstance();
   }
}
