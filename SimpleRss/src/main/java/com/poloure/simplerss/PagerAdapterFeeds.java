package com.poloure.simplerss;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.BaseAdapter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   static Set<String> s_tagSet = new LinkedHashSet<String>(0);
   private final BaseAdapter                    m_navigationAdapter;
   private final ViewPager.OnPageChangeListener m_pageChange;

   PagerAdapterFeeds(BaseAdapter navigationAdapter, FragmentManager fm,
         ViewPager.OnPageChangeListener pageChange, Context context)
   {
      super(fm);
      m_navigationAdapter = navigationAdapter;
      m_pageChange = pageChange;
      updateTags(context);
   }

   static
   Set<String> updateTags(Context context)
   {
      s_tagSet = Collections.synchronizedSet(new LinkedHashSet<String>(0));
      String[] tagArray = Read.csv(context)[2];

      s_tagSet.add(context.getString(R.string.all_tag));

      for(String tag : tagArray)
      {
         String[] tags = tag.split(",");
         for(String singleTag : tags)
         {
            s_tagSet.add(singleTag.trim());
         }
      }
      return s_tagSet;
   }

   static
   Set<String> getTags()
   {
      return s_tagSet;
   }

   static
   String[] getTagsArray()
   {
      int size = s_tagSet.size();
      return s_tagSet.toArray(new String[size]);
   }

   static
   int getSize()
   {
      return s_tagSet.size();
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return new FragmentTag(m_navigationAdapter, m_pageChange, position);
   }

   @Override
   public
   int getCount()
   {
      return s_tagSet.size();
   }

   @Override
   public
   String getPageTitle(int position)
   {
      int size = getCount();
      return s_tagSet.toArray(new String[size])[position];
   }

}
