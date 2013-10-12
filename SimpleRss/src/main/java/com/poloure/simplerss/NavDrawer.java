package com.poloure.simplerss;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

class NavDrawer
{
   static final         String[] NAV_TITLES = Util.getArray(R.array.nav_titles);
   private static final String   NAVIGATION = Util.getString(R.string.navigation_title);
   static         DrawerLayout          s_drawerLayout;
   static         ActionBarDrawerToggle s_drawerToggle;
   private static String                s_currentTitle;
   private final  ListView              m_navList;

   NavDrawer(ListView navList, DrawerLayout drawerLayout)
   {
      /* Set the listeners (and save the navigation list to the public static variable). */
      s_drawerLayout = drawerLayout;
      s_drawerLayout.setDrawerListener(s_drawerToggle);

      m_navList = navList;
      m_navList.setOnItemClickListener(new NavDrawerItemClick());

      s_drawerToggle = new DrawerToggleClick();
      s_drawerLayout.setDrawerListener(s_drawerToggle);

      m_navList.setAdapter(new AdapterNavDrawer());
   }

   BaseAdapter getAdapter()
   {
      return (BaseAdapter) m_navList.getAdapter();
   }

   static
   class DrawerToggleClick extends ActionBarDrawerToggle
   {
      private final ActionBar m_actionBar = FeedsActivity.getActivity().getSupportActionBar();

      DrawerToggleClick()
      {
         super(FeedsActivity.getActivity(), s_drawerLayout, R.drawable.ic_drawer,
               R.string.drawer_open, R.string.drawer_close);
      }

      @Override
      public
      void onDrawerOpened(View drawerView)
      {
         s_currentTitle = (String) m_actionBar.getTitle();
         m_actionBar.setTitle(NAVIGATION);
      }

      @Override
      public
      void onDrawerClosed(View drawerView)
      {
         /* Change it back to s_currentTitle. */
         if(NAVIGATION.equals(m_actionBar.getTitle()))
         {
            m_actionBar.setTitle(s_currentTitle);
         }
      }
   }

   class NavDrawerItemClick implements AdapterView.OnItemClickListener
   {
      @Override
      public
      void onItemClick(AdapterView parent, View view, int position, long id)
      {
         /* Close the drawer on any click of a navigation item. */
         s_drawerLayout.closeDrawer(m_navList);

         /* Determine the new m_title based on the position of the item clicked. */
         String selectedTitle = 3 < position ? NAV_TITLES[0] : NAV_TITLES[position];

         /* If the item selected was a m_imageViewTag, change the VIEW_PAGER to that
         image. */
         if(3 < position)
         {
            FragmentFeeds.VIEW_PAGER.setCurrentItem(position - 4);
         }

         /* If the selected title is the title of the current page, exit.
          * This stops the animation from showing on page change.*/
         if(s_currentTitle.equals(selectedTitle))
         {
            return;
         }

         /* Hide the current fragment and display the selected one. */
         showFragment(FeedsActivity.getActivity()
               .getSupportFragmentManager()
               .findFragmentByTag(selectedTitle));

         /* Set the m_title text of the actionbar to the selected item. */
         ActionBar actionBar = FeedsActivity.getActivity().getSupportActionBar();
         actionBar.setTitle(selectedTitle);
      }

      void showFragment(Fragment fragment)
      {
         FragmentTransaction tran = FeedsActivity.getActivity()
               .getSupportFragmentManager()
               .beginTransaction();
         for(String NAV_TITLE : NAV_TITLES)
         {
            Fragment frag = FeedsActivity.getActivity()
                  .getSupportFragmentManager()
                  .findFragmentByTag(NAV_TITLE);
            if(null != frag && !frag.equals(fragment) && !frag.isHidden())
            {
               tran.hide(frag);
            }
         }
         tran.show(fragment).commit();
      }
   }
}
