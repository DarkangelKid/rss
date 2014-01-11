/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   private static final Pattern SPLIT_COMMA = Pattern.compile(",");
   static final List<String> TAG_LIST = new ArrayList<>(0);

   PagerAdapterFeeds(FragmentManager fm)
   {
      super(fm);
   }

   void updateTags(String applicationFolder, Context context)
   {
      Set<String> tagSet = getTagsFromDisk(applicationFolder, context);
      TAG_LIST.clear();
      TAG_LIST.addAll(tagSet);

      notifyDataSetChanged();
   }

   static
   Set<String> getTagsFromDisk(String applicationFolder, Context context)
   {
      Set<String> tagSet = Collections.synchronizedSet(new LinkedHashSet<String>(0));
      String[] tagArray = Read.csvFile(Read.INDEX, applicationFolder, 't')[0];

      /* Get the all tag from resources. */
      String allTag = context.getString(R.string.all_tag);

      /* Make the allTag the first tag. */
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

      return tagSet;
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
      return TAG_LIST.size();
   }

   @Override
   public
   String getPageTitle(int position)
   {
      int size = getCount();
      return TAG_LIST.toArray(new String[size])[position];
   }
}
