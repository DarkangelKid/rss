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
class NavigationDrawerFragment extends Fragment
{
   private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
   private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

   /* m_Callbacks is the activity. */
   private NavigationDrawerCallbacks m_Callbacks;
   ActionBarDrawerToggle m_drawerToggle;

   private DrawerLayout m_drawerLayout;
   private ListView m_drawerListView;
   private View m_fragmentContainerView;

   private int m_currentSelectedPosition;
   private boolean m_fromSavedInstanceState;
   private boolean m_userLearnedDrawer;

   public
   NavigationDrawerFragment()
   {
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      /* Read the application preferences to see if the user knows the drawer exists. */
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
      m_userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

      /* If we have closed the application and saved the users page. */
      if(null != savedInstanceState)
      {
         m_currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
         m_fromSavedInstanceState = true;
      }
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      m_drawerListView = (ListView) inflater.inflate(R.layout.navigation_drawer, container, false);

      /* The onClickListener for navigation items. */
      m_drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
         @Override
         public
         void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {
            selectItem(position);
         }
      });

      /* This is the ArrayAdapter for the navigation drawer. */
      Activity activity = getActivity();
      m_drawerListView.setAdapter(new AdapterNavigationDrawer(activity));
      m_drawerListView.setItemChecked(m_currentSelectedPosition, true);
      return m_drawerListView;
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
      String[] navigationTitles = resources.getStringArray(R.array.navigation_titles);

      actionBar.setTitle(navigationTitles[0]);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);

      Drawable appIcon = resources.getDrawable(R.drawable.ic_action_location_broadcast);
      if(null != appIcon)
      {
         appIcon.setAutoMirrored(true);
         actionBar.setIcon(appIcon);
      }

      Drawable indicator = resources.getDrawable(R.drawable.ic_drawer);
      if(null != indicator)
      {
         indicator.setAutoMirrored(true);
         actionBar.setHomeAsUpIndicator(indicator);
      }

      m_drawerToggle = new ActionBarDrawerToggle(getActivity(), m_drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
      {
         @Override
         public
         void onDrawerSlide(View drawerView, float slideOffset)
         {
            super.onDrawerSlide(drawerView, slideOffset);

            if(!isAdded())
            {
               return;
            }

            activity.m_showMenuItems = 0.0F == slideOffset;
            activity.invalidateOptionsMenu();
         }

         @Override
         public
         void onDrawerOpened(View drawerView)
         {
            super.onDrawerOpened(drawerView);

            if(!isAdded())
            {
               return;
            }

            if(!m_userLearnedDrawer)
            {
               m_userLearnedDrawer = true;
               SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
               sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
            }
         }
      };

      /* Open the drawer if the user has never opened it manually before. */
      if(!m_userLearnedDrawer && !m_fromSavedInstanceState)
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

   private
   void selectItem(int position)
   {
      m_currentSelectedPosition = position;
      if(null != m_drawerListView)
      {
         m_drawerListView.setItemChecked(position, true);
      }
      if(null != m_drawerLayout)
      {
         m_drawerLayout.closeDrawer(m_fragmentContainerView);
      }
      if(null != m_Callbacks)
      {
         m_Callbacks.onNavigationDrawerItemSelected(position);
      }
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
   void onSaveInstanceState(Bundle outState)
   {
      super.onSaveInstanceState(outState);
      outState.putInt(STATE_SELECTED_POSITION, m_currentSelectedPosition);
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
