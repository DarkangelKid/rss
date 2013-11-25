package com.poloure.simplerss;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   private static final Pattern SPLIT_COMMA = Pattern.compile(",");
   static Set<String> s_tagSet = Collections.synchronizedSet(new LinkedHashSet<String>(0));

   PagerAdapterFeeds(FragmentManager fm)
   {
      super(fm);
   }

   static
   Set<String> getTagsFromDisk(String applicationFolder)
   {
      return getAndSaveTagsFromDisk(applicationFolder, "");
   }

   static
   Set<String> getAndSaveTagsFromDisk(String applicationFolder, String allTag)
   {
      Set<String> tagSet = Collections.synchronizedSet(new LinkedHashSet<String>(0));
      String[] tagArray = Read.csvFile(Read.INDEX, applicationFolder, 't')[0];

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
      if(0 != allTag.length())
      {
         s_tagSet = tagSet;
      }
      return tagSet;
   }

   static
   String[] getTagsArray()
   {
      int size = s_tagSet.size();
      return s_tagSet.toArray(new String[size]);
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return ListFragmentTag.newInstance(position);
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
