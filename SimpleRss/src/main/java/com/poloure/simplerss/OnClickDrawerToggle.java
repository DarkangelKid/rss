package com.poloure.simplerss;

import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

class OnClickDrawerToggle extends ActionBarDrawerToggle
{
   private final String m_navigationText;
   private final FeedsActivity m_activity;

   OnClickDrawerToggle(Activity activity, DrawerLayout drawerLayout)
   {
      super(activity, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
            R.string.drawer_close);
      m_activity = (FeedsActivity) activity;
      m_navigationText = activity.getString(R.string.navigation_title);
   }

   @Override
   public
   void onDrawerOpened(View drawerView)
   {
      m_activity.setNavigationTitle(m_navigationText, true);
   }

   @Override
   public
   void onDrawerClosed(View drawerView)
   {
      /* If the title is still R.string.navigation_title, change it to the previous title. */
      String title = m_activity.getNavigationTitle();
      if(m_navigationText.equals(title))
      {
         String previousTitle = m_activity.m_previousActionBarTitle;
         m_activity.setNavigationTitle(previousTitle, false);
      }
   }
}
