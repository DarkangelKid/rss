package com.poloure.simplerss;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

class OnClickNavDrawerItem implements AdapterView.OnItemClickListener
{
   private final ActionBarActivity m_activity;

   OnClickNavDrawerItem(ActionBarActivity activity)
   {
      m_activity = activity;
   }

   @Override
   public
   void onItemClick(AdapterView parent, View view, int position, long id)
   {
      /* Close the drawer on any click of a navigation item. */
      DrawerLayout drawerLayout = (DrawerLayout) m_activity.findViewById(R.id.drawer_layout);
      drawerLayout.closeDrawers();

      /* Determine the new m_title based on the position of the item clicked. */
      Resources resources = m_activity.getResources();
      String[] navTitles = resources.getStringArray(R.array.nav_titles);
      String selectedTitle = 3 < position ? navTitles[0] : navTitles[position];

      /* If the item selected was a m_imageViewTag, change the s_viewPager to that
      image. */
      if(3 < position)
      {
         ViewPager viewPager = (ViewPager) m_activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
         viewPager.setCurrentItem(position - 4);
      }

      /* If the selected title is the title of the current page, exit.
       * This stops the animation from showing on page change.*/
      String previousTitle = ((FeedsActivity) m_activity).getPreviousNavigationTitle();
      if(previousTitle.equals(selectedTitle))
      {
         return;
      }

      /* Show the title first to stop truncated subtitles.
       * Bypass the method here. */
      ((FeedsActivity) m_activity).setNavigationTitle(selectedTitle, false);

      /* Hide the current fragment and display the selected one. */
      showFragment(selectedTitle);
   }

   void showFragment(String title)
   {
      FragmentManager fragmentManager = m_activity.getSupportFragmentManager();
      Fragment fragment = fragmentManager.findFragmentByTag(title);
      FragmentTransaction transaction = fragmentManager.beginTransaction();

      Resources resources = m_activity.getResources();
      String[] navTitles = resources.getStringArray(R.array.nav_titles);
      for(String navTitle : navTitles)
      {
         Fragment frag = fragmentManager.findFragmentByTag(navTitle);
         if(null != frag && !frag.equals(fragment) && !frag.isHidden())
         {
            transaction.hide(frag);
         }
      }
      transaction.show(fragment);
      transaction.commit();

      ActionBar actionBar = m_activity.getSupportActionBar();

      if(!navTitles[0].equals(title))
      {
         actionBar.setSubtitle(null);
      }
      else
      {
         /* Update the ActionBar subtitle. */
         ListView navigationList = (ListView) m_activity.findViewById(R.id.navigation_drawer);
         ListAdapter navigationAdapter = navigationList.getAdapter();

         ViewPager viewPager = (ViewPager) m_activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
         int currentPage = viewPager.getCurrentItem();

         String unread = (String) navigationAdapter.getItem(currentPage);
         actionBar.setSubtitle("Unread: " + unread);
      }
   }
}
