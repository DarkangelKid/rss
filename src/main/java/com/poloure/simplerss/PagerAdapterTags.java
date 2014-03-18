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
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.poloure.simplerss.Constants.*;

class PagerAdapterTags extends FragmentPagerAdapter
{
   static List<String> s_tagList = new ArrayList<String>(0);

   PagerAdapterTags(FragmentManager fm, Context context, Iterable<IndexItem> indexItems)
   {
      super(fm);

      s_tagList = getTagsFromIndex(context, indexItems);
      notifyDataSetChanged();
   }

   static
   void run(FeedsActivity activity)
   {
      s_tagList = getTagsFromIndex(activity, activity.m_index);
      PagerAdapter adapter = s_viewPager.getAdapter();
      adapter.notifyDataSetChanged();
   }

   static
   List<String> getTagsFromIndex(Context context, Iterable<IndexItem> indexItems)
   {
      /* Get the all tag from resources. */
      String allTag = context.getString(R.string.all_tag);

      /* Make the allTag the first tag. */
      Set<String> tagSet = Collections.synchronizedSet(new LinkedHashSet<String>(0));
      tagSet.add(allTag);

      for(IndexItem indexItem : indexItems)
      {
         tagSet.addAll(Arrays.asList(indexItem.m_tags));
      }

      return new ArrayList<String>(tagSet);
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return FragmentTag.newInstance(position);
   }

   @Override
   public
   int getCount()
   {
      return s_tagList.size();
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return s_tagList.get(position);
   }
}
