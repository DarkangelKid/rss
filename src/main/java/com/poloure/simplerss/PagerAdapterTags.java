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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

class PagerAdapterTags extends FragmentPagerAdapter
{
   static final Pattern SPLIT_COMMA = Pattern.compile(", ");
   static final List<String> TAG_LIST = new ArrayList<String>(0);

   PagerAdapterTags(FragmentManager fm, Activity activity)
   {
      super(fm);

      Utilities.replaceAll(TAG_LIST, getTagsFromDisk(activity));
      notifyDataSetChanged();
   }

   static
   void update(Activity activity)
   {
      Utilities.replaceAll(TAG_LIST, getTagsFromDisk(activity));

      ((ViewPager) activity.findViewById(R.id.viewpager)).getAdapter().notifyDataSetChanged();
   }

   static
   Collection<String> getTagsFromDisk(Context context)
   {
      /* Get the all tag from resources. */
      String allTag = context.getString(R.string.all_tag);

      /* Make the allTag the first tag. */
      Set<String> tagSet = Collections.synchronizedSet(new LinkedHashSet<String>(0));
      tagSet.add(allTag);

      String[] tags = Read.csvFile(context, Read.INDEX, 't')[0];
      for(String tag : tags)
      {
         tagSet.addAll(Arrays.asList(SPLIT_COMMA.split(tag)));
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
      return TAG_LIST.get(position);
   }
}
