package com.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapterManage extends FragmentPagerAdapter
{
   private final String[] m_manageTitles;

   PagerAdapterManage(FragmentManager fragmentManager, String[] manageTitles)
   {
      super(fragmentManager);

      m_manageTitles = manageTitles;
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
      if(0 == position)
      {
         return FragmentManageTags.newInstance();
      }
      if(1 == position)
      {
         return FragmentManageFeeds.newInstance();
      }

      return FragmentManageFilters.newInstance();
   }
}
