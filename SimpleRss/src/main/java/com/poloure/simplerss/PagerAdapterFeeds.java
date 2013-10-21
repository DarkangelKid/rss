package com.poloure.simplerss;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   private static       Set<String> s_tagSet    = Collections.synchronizedSet(
         new LinkedHashSet<String>(0));
   private static final Pattern     SPLIT_COMMA = Pattern.compile(",");
   private final ViewPager.OnPageChangeListener m_pageChange;

   PagerAdapterFeeds(FragmentManager fm, ViewPager.OnPageChangeListener pageChange, Context context)
   {
      super(fm);
      m_pageChange = pageChange;
      getTagsFromDisk(context);
   }

   static
   Set<String> getTagsFromDisk(Context context)
   {
      Set<String> tagSet = Collections.synchronizedSet(new LinkedHashSet<String>(0));
      String[] tagArray = Read.indexFile(context)[2];

      String allTag = context.getString(R.string.all_tag);
      tagSet.add(allTag);

      for(String tag : tagArray)
      {
         String[] tags = SPLIT_COMMA.split(tag);
         for(String singleTag : tags)
         {
            String trimmedTag = singleTag.trim();
            tagSet.add(trimmedTag);
         }
      }
      s_tagSet = tagSet;
      return tagSet;
   }

   /* TODO */
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
   /* END */

   @Override
   public
   Fragment getItem(int position)
   {
      return FragmentTag.newInstance(m_pageChange, position);
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
