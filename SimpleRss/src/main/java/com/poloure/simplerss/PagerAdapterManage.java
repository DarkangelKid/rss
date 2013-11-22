package com.poloure.simplerss;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

class PagerAdapterManage extends FragmentPagerAdapter
{
   private final String[] m_manageTitles;

   PagerAdapterManage(FragmentManager fragmentManager, String[] manageTitles)
   {
      super(fragmentManager);
      m_manageTitles = manageTitles.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_manageTitles.length;
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return m_manageTitles[position];
   }

   @Override
   public
   Fragment getItem(int position)
   {
      switch(position)
      {
         case 0:
            return ListFragmentManageTags.newInstance();
         case 1:
            return ListFragmentManageFeeds.newInstance();
         default:
            return ListFragmentManageFilters.newInstance();
      }
   }
}
