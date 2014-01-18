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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

class ServiceHandler extends Handler
{
   static MenuItem s_refreshItem;
   private final Activity m_activity;
   private final FragmentManager m_fragmentManager;

   ServiceHandler(Activity activity, FragmentManager fragmentManager, MenuItem refreshItem)
   {
      m_fragmentManager = fragmentManager;
      m_activity = activity;

      /* TODO, this is not the same MenuItem when you restart the app. */
      s_refreshItem = refreshItem;
   }

   /* The stuff we would like to run when the service completes. */
   @Override
   public
   void handleMessage(Message msg)
   {
      /* Tell the refresh icon to stop spinning. */
      MenuItemCompat.setActionView(s_refreshItem, null);

      Bundle bundle = msg.getData();
      if(null == bundle)
      {
         return;
      }

      int page = bundle.getInt("page_number");

      /* Refresh the tag page. */
      for(int i : new int[]{0, page})
      {
         ListFragment listFragment = (ListFragment) m_fragmentManager.findFragmentByTag(
               Utilities.FRAGMENT_ID_PREFIX + i);

         if(null != listFragment)
         {
            AsyncReloadTagPage.newInstance(i, listFragment.getListView());
         }
      }

      /* Update the manage page if we can see it. */
      ListFragment manageFragment = (ListFragment) m_fragmentManager.findFragmentByTag(
            FeedsActivity.FRAGMENT_TAGS[1]);
      if(null != manageFragment && manageFragment.isVisible())
      {
         AsyncManage.newInstance(manageFragment.getActivity(),
               (ArrayAdapter<Editable>) manageFragment.getListAdapter());
      }

      /* Update the navigationDrawer. */
      AsyncNavigationAdapter.newInstance(m_activity);
   }
}
