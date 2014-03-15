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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public
class FragmentFeeds extends Fragment
{
   private static final float PULL_DISTANCE = 0.5F;
   ViewPager m_viewPager;

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      PullToRefreshLayout layout = (PullToRefreshLayout) inflater.inflate(R.layout.viewpager, null, false);
      final FeedsActivity activity = (FeedsActivity) getActivity();

      m_viewPager = (ViewPager) layout.findViewById(R.id.viewpager);

      ActionBarPullToRefresh.from(activity)
            .allChildrenArePullable()
            .options(Options.create().scrollDistance(PULL_DISTANCE).build())
            .useViewDelegate(ViewPager.class, new ViewPagerDelegate())
            .listener(new RefreshListener(activity))
            .setup(layout);

      /* Inflate and configure the ViewPager. */
      m_viewPager.setOffscreenPageLimit(128);
      m_viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
      {
         @Override
         public
         void onPageSelected(int position)
         {
            Utilities.setTitlesAndDrawerAndPage(activity, FeedsActivity.FEED_TAG, -10);
         }
      });
      return layout;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      FeedsActivity activity = (FeedsActivity) getActivity();

      m_viewPager.setAdapter(new PagerAdapterTags(getFragmentManager(), activity, activity.m_index));
      Utilities.setTitlesAndDrawerAndPage(activity, FeedsActivity.FEED_TAG, -10);
   }

   private static
   class RefreshListener implements OnRefreshListener
   {
      private final FeedsActivity m_activity;

      RefreshListener(FeedsActivity activity)
      {
         m_activity = activity;
      }

      @Override
      public
      void onRefreshStarted(View view)
      {
         ViewPager pager = (ViewPager) m_activity.findViewById(R.id.viewpager);
         Intent intent = new Intent(m_activity, ServiceUpdate.class);
         intent.putExtra("GROUP_NUMBER", pager.getCurrentItem());
         m_activity.startService(intent);
      }
   }
}
