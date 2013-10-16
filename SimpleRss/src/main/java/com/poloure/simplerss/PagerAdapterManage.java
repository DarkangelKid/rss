package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.widget.BaseAdapter;

class PagerAdapterManage extends FragmentPagerAdapter
{
   private final ListFragment[] m_manageFragments = new ListFragment[3];
   private final String[] m_manageTitles;

   PagerAdapterManage(BaseAdapter navigationAdapter, FragmentManager fragmentManager,
         Context context)
   {
      super(fragmentManager);
      m_manageFragments[0] = new FragmentManageTags();
      m_manageFragments[1] = new FragmentManageFeeds(navigationAdapter);
      m_manageFragments[2] = new FragmentManageFilters(navigationAdapter);

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
