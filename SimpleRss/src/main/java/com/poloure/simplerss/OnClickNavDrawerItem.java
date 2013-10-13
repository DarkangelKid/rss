package com.poloure.simplerss;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;

class OnClickNavDrawerItem implements AdapterView.OnItemClickListener
{
   private final DrawerLayout      m_drawerLayout;
   private final ActionBarActivity m_activity;

   OnClickNavDrawerItem(DrawerLayout drawerLayout, ActionBarActivity activity)
   {
      m_drawerLayout = drawerLayout;
      m_activity = activity;
   }

   @Override
   public
   void onItemClick(AdapterView parent, View view, int position, long id)
   {
      /* Close the drawer on any click of a navigation item. */
      m_drawerLayout.closeDrawers();

      /* Determine the new m_title based on the position of the item clicked. */
      Resources resources = m_activity.getResources();
      String[] navTitles = resources.getStringArray(R.array.nav_titles);
      String selectedTitle = 3 < position ? navTitles[0] : navTitles[position];

      /* If the item selected was a m_imageViewTag, change the s_viewPager to that
      image. */
      if(3 < position)
      {
         FragmentFeeds.s_viewPager.setCurrentItem(position - 4);
      }

      /* If the selected title is the title of the current page, exit.
       * This stops the animation from showing on page change.*/
      String previousTitle = ((FeedsActivity) m_activity).getPreviousNavigationTitle();
      if(previousTitle.equals(selectedTitle))
      {
         return;
      }

      /* Hide the current fragment and display the selected one. */
      showFragment(selectedTitle);

      /* Set the m_title text of the actionbar to the selected item. */
      ((FeedsActivity) m_activity).setNavigationTitle(selectedTitle);
   }

   void showFragment(String tag)
   {
      FragmentManager fragmentManager = m_activity.getSupportFragmentManager();
      Fragment fragment = fragmentManager.findFragmentByTag(tag);
      FragmentTransaction transaction = fragmentManager.beginTransaction();

      Resources resources = m_activity.getResources();
      String[] navTitles = resources.getStringArray(R.array.nav_titles);
      for(String title : navTitles)
      {
         Fragment frag = fragmentManager.findFragmentByTag(title);
         if(null != frag && !frag.equals(fragment) && !frag.isHidden())
         {
            transaction.hide(frag);
         }
      }
      transaction.show(fragment);
      transaction.commit();
   }
}
