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
import android.widget.ListView;

public
class FragmentFeeds extends Fragment
{
   static final int VIEW_PAGER_ID = 786534126;
   private ViewPager m_pager;

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      /* Inflate and configure the ViewPager. */
      m_pager = new ViewPager(getActivity());
      m_pager.setId(VIEW_PAGER_ID);
      m_pager.setOffscreenPageLimit(128);
      m_pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
      {
         @Override
         public
         void onPageSelected(int position)
         {
            /* Set the item to be checked in the navigation drawer. */
            ListView navigationList = (ListView) getActivity().findViewById(R.id.navigation_drawer);
            navigationList.setItemChecked(position + 3, true);

            /* Set the subtitle to the unread count. */
            Utilities.updateSubtitle(getActivity());
         }
      });

      return m_pager;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      m_pager.setAdapter(new PagerAdapterTags(getFragmentManager(), getActivity()));

      ListView navigationList = (ListView) getActivity().findViewById(R.id.navigation_drawer);
      navigationList.setItemChecked(3, true);
   }
}
