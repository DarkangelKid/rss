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
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;

class OnNavigationListItemClick implements AdapterView.OnItemClickListener
{
   private final FeedsActivity m_activity;

   OnNavigationListItemClick(FeedsActivity activity)
   {
      m_activity = activity;
   }

   @Override
   public
   void onItemClick(AdapterView<?> parent, View view, int position, long id)
   {
      /* Close the navigation drawer in all cases. */
      DrawerLayout drawerLayout = (DrawerLayout) m_activity.findViewById(R.id.drawer_layout);
      drawerLayout.closeDrawers();

      String[] navTitles = m_activity.getResources().getStringArray(R.array.navigation_titles);
      ActionBar actionBar = m_activity.getActionBar();

      /* Set the title and switch fragment. */
      String selectedTitle = navTitles[2 < position ? 0 : position];
      Utilities.switchFragments(m_activity, m_activity.m_currentFragment, selectedTitle);
      if(null != actionBar)
      {
         actionBar.setTitle(selectedTitle);
      }

      ViewPager viewPager = (ViewPager) m_activity.findViewById(R.id.view_pager_tags);
      /* If a tag was clicked, set the ViewPager position to that tag. */
      if(2 < position)
      {
         viewPager.setCurrentItem(position - 3);
      }
      /* If a main title was clicked, set the subtitle accordingly. */
      else
      {
         Utilities.updateSubtitle(m_activity, 0 == position ? viewPager.getCurrentItem() : -1);
      }
   }
}
