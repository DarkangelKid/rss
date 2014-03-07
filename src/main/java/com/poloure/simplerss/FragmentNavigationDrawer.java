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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public
class FragmentNavigationDrawer extends Fragment
{
   private static
   class ItemLongClickListener implements AdapterView.OnItemLongClickListener
   {
      @Override
      public
      boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
      {
         /* reorder tags. */
         return true;
      }
   }

   private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

   /* m_Callbacks is the activity. */
   private NavigationDrawerCallbacks m_Callbacks;
   ActionBarDrawerToggle m_drawerToggle;

   private DrawerLayout m_drawerLayout;
   private View m_fragmentContainerView;

   private boolean m_userLearnedDrawer;

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
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
         @Override
         public
         void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {
            if(null != m_drawerLayout)
            {
               m_drawerLayout.closeDrawer(m_fragmentContainerView);
            }
            if(null != m_Callbacks)
            {
               m_Callbacks.onNavigationDrawerItemSelected(position);
            }
         }
      });
      listView.setOnItemLongClickListener(new ItemLongClickListener());

      return listView;
   }

   void setUp(int fragmentId, DrawerLayout drawerLayout)
   {
      final FeedsActivity activity = (FeedsActivity) getActivity();

      m_fragmentContainerView = activity.findViewById(fragmentId);
      m_drawerLayout = drawerLayout;

      m_drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

      /* Set up the action bar. */
      ActionBar actionBar = activity.getActionBar();
      Resources resources = getResources();

      Drawable appIcon = resources.getDrawable(R.drawable.ic_action_location_broadcast);
      Drawable indicator = resources.getDrawable(R.drawable.ic_drawer);

      appIcon.setAutoMirrored(true);
      indicator.setAutoMirrored(true);

      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setHomeAsUpIndicator(indicator);
      actionBar.setIcon(appIcon);

      m_drawerToggle = new ActionBarDrawerToggle(getActivity(), m_drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
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
         m_drawerLayout.openDrawer(m_fragmentContainerView);
      }

      m_drawerLayout.post(new Runnable()
      {
         @Override
         public
         void run()
         {
            m_drawerToggle.syncState();
         }
      });

      m_drawerLayout.setDrawerListener(m_drawerToggle);
   }

   @Override
   public
   void onAttach(Activity activity)
   {
      super.onAttach(activity);
      m_Callbacks = (NavigationDrawerCallbacks) activity;
   }

   @Override
   public
   void onDetach()
   {
      super.onDetach();
      m_Callbacks = null;
   }

   @Override
   public
   void onConfigurationChanged(Configuration newConfig)
   {
      super.onConfigurationChanged(newConfig);
      m_drawerToggle.onConfigurationChanged(newConfig);
   }

   public
   interface NavigationDrawerCallbacks
   {
      /* Called when an item in the navigation drawer is selected. */
      void onNavigationDrawerItemSelected(int position);
   }
}
