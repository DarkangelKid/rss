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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ListView;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static com.poloure.simplerss.Constants.*;

public
class FragmentFeeds extends Fragment
{
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
         Intent intent = new Intent(m_activity, ServiceUpdate.class);
         intent.putExtra(EXTRA_PAGE_NAME, s_viewPager.getCurrentItem());
         m_activity.startService(intent);
      }
   }

   static
   class OnPageChangeListener extends ViewPager.SimpleOnPageChangeListener
   {
      @Override
      public
      void onPageSelected(int position)
      {
         Utilities.setTitlesAndDrawerAndPage(null, -10);
      }
   }

   static final String EXTRA_PAGE_NAME = "GROUP_NUMBER";
   private static final float PULL_DISTANCE = 0.5F;

   ListView getCurrentTagListView()
   {
      int currentPage = s_viewPager.getCurrentItem();
      return getTagListView(currentPage);
   }

   static
   ListView getTagListView(int page)
   {
      String tag = "android:switcher:" + R.id.viewpager + ':' + page;
      FragmentTag fragment = (FragmentTag) s_fragmentManager.findFragmentByTag(tag);
      return fragment.getListView();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      PullToRefreshLayout layout = (PullToRefreshLayout) inflater.inflate(R.layout.viewpager, container, false);

      s_viewPager = (ViewPager) layout.findViewById(R.id.viewpager);

      ActionBarPullToRefresh.from(getActivity())
            .allChildrenArePullable()
            .options(Options.create().scrollDistance(PULL_DISTANCE).build())
            .useViewDelegate(ViewPager.class, new ViewPagerDelegate())
            .listener(new RefreshListener((FeedsActivity) getActivity()))
            .setup(layout);

      /* Inflate and configure the ViewPager. */
      s_viewPager.setOffscreenPageLimit(128);
      s_viewPager.setOnPageChangeListener(new OnPageChangeListener());
      return layout;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      s_viewPager.setAdapter(new PagerAdapterTags(s_fragmentManager, s_activity, s_activity.m_index));
      Utilities.setTitlesAndDrawerAndPage(null, -10);

      WebView webView = s_fragmentWeb.getWebView();
      WebSettings settings = webView.getSettings();
      settings.setBuiltInZoomControls(true);
      settings.setDisplayZoomControls(false);
      settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
   }
}
