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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public
class FragmentFeeds extends Fragment
{
   ViewPager m_pager;

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      PullToRefreshLayout layout = (PullToRefreshLayout) inflater.inflate(R.layout.viewpager, null, false);
      final Activity activity = getActivity();
      m_pager = (ViewPager) layout.findViewById(R.id.viewpager);

      ActionBarPullToRefresh.from(activity)
                            .allChildrenArePullable()
                            .options(Options.create().scrollDistance(0.5F).build())
                            .useViewDelegate(ViewPager.class, new ViewPagerDelegate())
                            .listener(new OnRefreshListener()
                            {
                               @Override
                               public
                               void onRefreshStarted(View view)
                               {
                                  Intent intent = new Intent(activity, ServiceUpdate.class);
                                  intent.putExtra("GROUP_NUMBER", m_pager.getCurrentItem());
                                  activity.startService(intent);
                               }
                            })
                            .setup(layout);

      /* Inflate and configure the ViewPager. */
      m_pager.setOffscreenPageLimit(128);
      m_pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
      {
         @Override
         public
         void onPageSelected(int position)
         {
            /* Set the item to be checked in the navigation drawer. */
            ListView navigationList = (ListView) getActivity().findViewById(R.id.navigation_drawer);
            navigationList.setItemChecked(position + 2, true);

            /* Set the subtitle to the unread count. */
            Utilities.updateTitle(getActivity());
            Utilities.updateSubtitle(getActivity());
         }
      });

      return layout;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      m_pager.setAdapter(new PagerAdapterTags(getFragmentManager(), getActivity()));

      ListView navigationList = (ListView) getActivity().findViewById(R.id.navigation_drawer);
      navigationList.setItemChecked(2, true);
      Utilities.updateTitle(getActivity());
      Utilities.updateSubtitle(getActivity());
   }
}
