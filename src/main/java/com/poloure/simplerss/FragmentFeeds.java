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
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public
class FragmentFeeds extends Fragment
{
   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {

      /* Inflate and configure the ViewPager. */
      ViewPager pager = (ViewPager) inflater.inflate(R.layout.view_pager_tags, container, false);
      pager.setOffscreenPageLimit(128);
      pager.setAdapter(new PagerAdapterTags(getFragmentManager()));
      pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
      {
         @Override
         public
         void onPageSelected(int position)
         {
            /* Set the subtitle to the unread count. */
            Utilities.updateSubtitle(getActivity());
         }
      });

      return pager;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      PagerAdapterTags.update(getActivity());
   }
}
