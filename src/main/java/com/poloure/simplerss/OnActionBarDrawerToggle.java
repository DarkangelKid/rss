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

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

class OnActionBarDrawerToggle extends ActionBarDrawerToggle
{
   private final FeedsActivity m_activity;

   OnActionBarDrawerToggle(FeedsActivity activity, DrawerLayout drawerLayout)
   {
      super(activity, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
            R.string.drawer_close);
      m_activity = activity;
   }

   @Override
   public
   void onDrawerSlide(View drawerView, float slideOffset)
   {
      super.onDrawerSlide(drawerView, slideOffset);
      m_activity.m_showMenuItems = 0.0F == slideOffset;
      m_activity.invalidateOptionsMenu();
   }
}
