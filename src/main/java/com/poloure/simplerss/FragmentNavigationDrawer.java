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
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public
class FragmentNavigationDrawer extends Fragment
{
   static
   class OnNavigationItemLongClick implements AdapterView.OnItemLongClickListener
   {
      @Override
      public
      boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
      {
         /* reorder tags. */
         return true;
      }
   }

   static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

   ActionBarDrawerToggle m_drawerToggle;

   boolean m_userLearnedDrawer;

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      /* Read the application preferences to see if the user knows the drawer exists. */
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
      m_userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      ListView listView = (ListView) inflater.inflate(R.layout.navigation_drawer, container, false);
      listView.setAdapter(new AdapterNavigationDrawer(getActivity()));
      listView.setOnItemClickListener(new OnNavigationItemClick(getActivity()));
      listView.setOnItemLongClickListener(new OnNavigationItemLongClick());
      listView.setHeaderDividersEnabled(false);

      String[] navTitles = getResources().getStringArray(R.array.navigation_titles);

      for(int i = 0; i < navTitles.length; i++)
      {
         ViewNavItem item = new ViewNavItem(getActivity());
         item.m_text = navTitles[i];
         item.m_count = "";
         item.m_image = i;
         listView.addHeaderView(item);
      }

      TextView divider = (TextView) inflater.inflate(R.layout.navigation_divider, container, false);
      divider.setText(R.string.tags);

      listView.addHeaderView(divider, null, false);
      return listView;
   }

   void setUp(DrawerLayout drawerLayout)
   {
      final FeedsActivity activity = (FeedsActivity) getActivity();

      drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

      /* Set up the action bar. */
      ActionBar actionBar = activity.getActionBar();
      Resources resources = getResources();

      Drawable appIcon = resources.getDrawable(R.drawable.ic_action_location_broadcast);

      DrawableCompat.setAutoMirrored(appIcon, true);

      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setIcon(appIcon);

      /* If the version is above API 18, flip the icon in RTL layouts. */
      if(Build.VERSION_CODES.JELLY_BEAN_MR2 > Build.VERSION.SDK_INT)
      {
         Drawable indicator = resources.getDrawable(R.drawable.ic_drawer);
         DrawableCompat.setAutoMirrored(indicator, true);
         actionBar.setHomeAsUpIndicator(indicator);
      }

      m_drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
      {
         @Override
         public
         void onDrawerSlide(View drawerView, float slideOffset)
         {
            super.onDrawerSlide(drawerView, slideOffset);

            activity.m_showMenuItems = 0.0F == slideOffset;
            activity.invalidateOptionsMenu();
         }

         @Override
         public
         void onDrawerOpened(View drawerView)
         {
            super.onDrawerOpened(drawerView);

            if(!m_userLearnedDrawer)
            {
               m_userLearnedDrawer = true;
               SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
               sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
            }
         }
      };

      /* Open the drawer if the user has never opened it manually before. */
      if(!m_userLearnedDrawer)
      {
         /* TODO */
         //drawerLayout.openDrawer(R.id.navigation_drawer);
      }

      drawerLayout.post(new Runnable()
      {
         @Override
         public
         void run()
         {
            m_drawerToggle.syncState();
         }
      });
      drawerLayout.setDrawerListener(m_drawerToggle);
   }

   @Override
   public
   void onConfigurationChanged(Configuration newConfig)
   {
      super.onConfigurationChanged(newConfig);
      m_drawerToggle.onConfigurationChanged(newConfig);
   }

   private static
   class OnNavigationItemClick implements AdapterView.OnItemClickListener
   {
      private final FeedsActivity m_activity;
      private final FragmentManager m_manager;
      private final ActionBar m_bar;
      private final String[] m_titles;
      private final Drawable[] m_icons = new Drawable[2];

      OnNavigationItemClick(Activity activity)
      {
         m_activity = (FeedsActivity) activity;
         m_manager = activity.getFragmentManager();
         m_bar = activity.getActionBar();

         Resources resources = activity.getResources();

         m_titles = resources.getStringArray(R.array.navigation_titles);

         m_icons[0] = resources.getDrawable(R.drawable.ic_action_storage);
         m_icons[1] = resources.getDrawable(R.drawable.ic_action_settings);

         DrawableCompat.setAutoMirrored(m_icons[0], true);
         DrawableCompat.setAutoMirrored(m_icons[1], true);
      }

      @Override
      public
      void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         /* Close the navigation drawer in all cases. */
         ((DrawerLayout) parent.getParent()).closeDrawers();

         /* If a divider. */
         if(2 == position)
         {
            return;
         }

         int offset = ((HeaderViewListAdapter) parent.getAdapter()).getHeadersCount();

         int fragmentTag = offset <= position ? 0 : position + 1;

         /* If we are switching fragments, check if we need to. */
         String newTag = FeedsActivity.FRAGMENT_TAGS[fragmentTag];
         if(!m_activity.m_currentFragment.equals(newTag))
         {
            /* Switch the content frame fragment. */
            m_manager.beginTransaction()
                     .hide(FeedsActivity.getFragment(m_manager, m_activity.m_currentFragment))
                     .show(FeedsActivity.getFragment(m_manager, newTag))
                     .commit();
            m_activity.m_currentFragment = newTag;
         }

         /* Set the item to be checked. */
         ((AbsListView) parent).setItemChecked(position, true);

         /* If a tag was clicked, set the ViewPager position to that tag. */
         if(1 < position)
         {
            ViewPager pager = (ViewPager) m_activity.findViewById(R.id.viewpager);

            /* Does not call the onPageChangeListener if the page is the same as before. */
            if(position - offset == pager.getCurrentItem())
            {
               /* Set the subtitle to the unread count. */
               Utilities.updateTitle(m_activity);
               Utilities.updateSubtitle(m_activity);
            }
            pager.setCurrentItem(position - offset);
         }
         else
         {
            /* Change the icon and title of the ActionBar. */
            m_bar.setIcon(m_icons[position]);
            m_bar.setTitle(m_titles[position]);
            m_bar.setSubtitle(null);
         }
      }
   }
}
