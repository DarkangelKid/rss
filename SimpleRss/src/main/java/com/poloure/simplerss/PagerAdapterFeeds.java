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
import java.util.regex.Pattern;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   private static        Set<String> m_tagSet    = Collections.synchronizedSet(
         new LinkedHashSet<String>(0));
   private static final Pattern     SPLIT_COMMA = Pattern.compile(",");
   private final BaseAdapter                    m_navigationAdapter;
   private final ViewPager.OnPageChangeListener m_pageChange;

   PagerAdapterFeeds(BaseAdapter navigationAdapter, FragmentManager fm,
         ViewPager.OnPageChangeListener pageChange, Context context)
   {
      super(fm);
      m_navigationAdapter = navigationAdapter;
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
      m_tagSet = tagSet;
      return tagSet;
   }

   /* TODO */
   static
   Set<String> getTags()
   {
      return m_tagSet;
   }

   static
   String[] getTagsArray()
   {
      int size = m_tagSet.size();
      return m_tagSet.toArray(new String[size]);
   }

   static
   int getSize()
   {
      return m_tagSet.size();
   }
   /* END */

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
      return m_tagSet.size();
   }

   @Override
   public
   String getPageTitle(int position)
   {
      int size = getCount();
      return m_tagSet.toArray(new String[size])[position];
   }

}
