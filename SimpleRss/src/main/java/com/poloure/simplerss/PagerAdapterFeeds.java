package com.poloure.simplerss;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.BaseAdapter;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   private final BaseAdapter m_navigationAdapter;
   private final Context     m_context;
   private final String[] m_currentTags = Read.file(Constants.TAG_LIST);

   PagerAdapterFeeds(BaseAdapter navigationAdapter, FragmentManager fm, Context context)
   {
      super(fm);
      m_navigationAdapter = navigationAdapter;
      m_context = context;
   }

   @Override
   public
   int getCount()
   {
      return m_currentTags.length;
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return m_currentTags[position];
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return new FragmentTag(m_navigationAdapter, m_context);

   }
}
