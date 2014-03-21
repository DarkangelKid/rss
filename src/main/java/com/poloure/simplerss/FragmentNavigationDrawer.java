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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import static com.poloure.simplerss.Constants.*;

public
class FragmentNavigationDrawer extends Fragment
{
   private static
   class OnNavigationItemLongClick implements AdapterView.OnItemLongClickListener
   {
      OnNavigationItemLongClick()
      {
      }

      @Override
      public
      boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
      {
         /* reorder tags. */
         return true;
      }
   }

   private static
   class OnNavigationItemClick implements AdapterView.OnItemClickListener
   {
      @Override
      public
      void onItemClick(AdapterView<?> parent, View view, int absolutePos, long id)
      {
         /* Close the navigation drawer in all cases. */
         s_drawerLayout.closeDrawers();

         Fragment fragment;

         if(0 == absolutePos)
         {
            fragment = s_fragmentFavourites;
         }
         else if(1 == absolutePos)
         {
            fragment = s_fragmentManage;
         }
         else if(2 == absolutePos)
         {
            fragment = s_fragmentSettings;
         }
         else
         {
            fragment = s_fragmentFeeds;
         }

         /* If a tag was clicked, set the ViewPager position to that tag. */
         Utilities.setTitlesAndDrawerAndPage(fragment, absolutePos);
      }
   }

   private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

   private boolean m_userLearnedDrawer;
   ListView m_listView;

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
      m_listView = (ListView) inflater.inflate(R.layout.navigation_drawer, container, false);
      m_listView.setOnItemClickListener(new OnNavigationItemClick());
      m_listView.setOnItemLongClickListener(new OnNavigationItemLongClick());
      m_listView.setHeaderDividersEnabled(false);

      String[] navTitles = getResources().getStringArray(R.array.navigation_titles);

      for(int i = 0; i < navTitles.length; i++)
      {
         ViewNavItem item = new ViewNavItem(getActivity());
         item.m_text = navTitles[i];
         item.m_count = "";
         item.m_image = i;
         m_listView.addHeaderView(item);
      }

      TextView divider = (TextView) inflater.inflate(R.layout.navigation_divider, container, false);
      divider.setText(R.string.tags);

      m_listView.addHeaderView(divider, null, false);
      m_listView.setAdapter(new AdapterNavigationDrawer(getActivity()));

      return m_listView;
   }

   void setUp(DrawerLayout drawerLayout)
   {
      final FeedsActivity activity = (FeedsActivity) getActivity();

      drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

      /* Set up the action bar. */
      ActionBar actionBar = activity.getActionBar();

      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);

      s_drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
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

      drawerLayout.post(new SyncPost());
      drawerLayout.setDrawerListener(s_drawerToggle);
   }

   @Override
   public
   void onConfigurationChanged(Configuration newConfig)
   {
      super.onConfigurationChanged(newConfig);
      s_drawerToggle.onConfigurationChanged(newConfig);
   }

   private static
   class SyncPost implements Runnable
   {
      @Override
      public
      void run()
      {
         s_drawerToggle.syncState();
      }
   }
}
