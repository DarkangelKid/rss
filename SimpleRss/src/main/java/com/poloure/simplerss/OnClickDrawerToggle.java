package com.poloure.simplerss;
import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

class OnClickDrawerToggle extends ActionBarDrawerToggle
{
   private static String        s_navigation;
   private final  FeedsActivity m_activity;

   OnClickDrawerToggle(Activity activity, DrawerLayout drawerLayout)
   {
      super(activity, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
            R.string.drawer_close);
      m_activity = (FeedsActivity) activity;
      s_navigation = m_activity.getString(R.string.navigation_title);
   }

   @Override
   public
   void onDrawerOpened(View drawerView)
   {
      m_activity.setNavigationTitle(s_navigation);
   }

   @Override
   public
   void onDrawerClosed(View drawerView)
   {
      /* Change it back to s_currentTitle. */
      String title = m_activity.getNavigationTitle();
      if(s_navigation.equals(title))
      {
         String previousTitle = m_activity.getPreviousNavigationTitle();
         m_activity.setNavigationTitle(previousTitle);
      }
   }
}
