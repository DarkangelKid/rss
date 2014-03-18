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

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebViewFragment;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

class Constants
{
   static FeedsActivity s_activity;
   static Resources s_resources;
   static DisplayMetrics s_displayMetrics;
   static FragmentManager s_fragmentManager;
   static ActionBar s_actionBar;
   static WindowManager s_windowManager;
   static int s_eightDp;

   /* Fragments .*/
   static FragmentFeeds s_fragmentFeeds;
   static ListFragmentFavourites s_fragmentFavourites;
   static ListFragmentManage s_fragmentManage;
   static FragmentSettings s_fragmentSettings;
   static WebViewFragment s_fragmentWeb;
   static FragmentNavigationDrawer s_fragmentDrawer;

   static ViewPager s_viewPager;
   static PullToRefreshLayout s_pullToRefreshLayout;
   static DrawerLayout s_drawerLayout;
   static ActionBarDrawerToggle s_drawerToggle;

   static
   void saveInitialConstants(FeedsActivity activity)
   {
      s_activity = activity;
      s_resources = activity.getResources();
      s_displayMetrics = s_resources.getDisplayMetrics();
      s_fragmentManager = activity.getFragmentManager();
      s_actionBar = activity.getActionBar();
      s_windowManager = activity.getWindowManager();
      s_eightDp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, s_displayMetrics));

      s_fragmentFeeds = (FragmentFeeds) findFragment(R.id.fragment_feeds);
      s_fragmentFavourites = (ListFragmentFavourites) findFragment(R.id.fragment_favourites);
      s_fragmentManage = (ListFragmentManage) findFragment(R.id.fragment_manage);
      s_fragmentSettings = (FragmentSettings) findFragment(R.id.fragment_settings);
      s_fragmentWeb = (WebViewFragment) findFragment(R.id.fragment_web);
      s_fragmentDrawer = (FragmentNavigationDrawer) findFragment(R.id.fragment_navigation_drawer);

      s_drawerLayout = (DrawerLayout) findView(R.id.drawer_layout);
   }

   static
   void saveViews()
   {
      s_viewPager = (ViewPager) findView(R.id.viewpager);
      s_pullToRefreshLayout = (PullToRefreshLayout) s_viewPager.getParent();;
   }

   private static
   Fragment findFragment(int id)
   {
      return s_fragmentManager.findFragmentById(id);
   }

   static
   View findView(int id)
   {
      return s_activity.findViewById(id);
   }

   static
   void hideFragments(Fragment... fragments)
   {
      FragmentTransaction transaction = s_fragmentManager.beginTransaction();
      for(Fragment fragment : fragments)
      {
         transaction.hide(fragment);
      }
      transaction.commit();
   }

   static
   void showFragments(Fragment... fragments)
   {
      FragmentTransaction transaction = s_fragmentManager.beginTransaction();
      for(Fragment fragment : fragments)
      {
         transaction.show(fragment);
      }
      transaction.commit();
   }
}
