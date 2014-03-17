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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

class FragmentUtils
{
   static
   void switchToFragment(FeedsActivity activity, int nextId, boolean addToBackStack)
   {
      if(activity.m_currentFragmentId != nextId)
      {
         FragmentManager manager = activity.getFragmentManager();
         FragmentTransaction transaction = manager.beginTransaction();

         Fragment currentFragment = manager.findFragmentById(activity.m_currentFragmentId);
         Fragment nextFragment = manager.findFragmentById(nextId);

         transaction.hide(currentFragment).show(nextFragment);
         if(addToBackStack)
         {
            transaction.addToBackStack(null);
         }
         transaction.commit();

         activity.m_previousFragmentId = activity.m_currentFragmentId;
         activity.m_currentFragmentId = nextId;
      }
   }
}
