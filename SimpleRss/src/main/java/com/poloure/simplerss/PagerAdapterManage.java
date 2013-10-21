package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;

class PagerAdapterManage extends FragmentPagerAdapter
{
   private final ListFragment[] m_manageFragments = new ListFragment[3];
   private final String[] m_manageTitles;

   PagerAdapterManage(FragmentManager fragmentManager, Context context)
   {
      super(fragmentManager);
      m_manageFragments[0] = FragmentManageTags.newInstance();
      m_manageFragments[1] = FragmentManageFeeds.newInstance();
      m_manageFragments[2] = FragmentManageFilters.newInstance();

      Resources resources = context.getResources();
      m_manageTitles = resources.getStringArray(R.array.manage_titles);
   }

   @Override
   public
   int getCount()
   {
      return m_manageFragments.length;
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
      return m_manageFragments[position];
   }
}
