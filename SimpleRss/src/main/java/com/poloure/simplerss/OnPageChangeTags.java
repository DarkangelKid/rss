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
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

class OnPageChangeTags extends SimpleOnPageChangeListener
{
   /* This is because we steal the unread counts from this BaseAdapter. */
   private final Activity m_activity;
   private final String m_applicationFolder;
   private int m_position;

   OnPageChangeTags(Activity activity, String applicationFolder)
   {
      m_activity = activity;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   void onPageSelected(int position)
   {
      m_position = position;

      /* Set the subtitle to the unread count. */
      Utilities.updateSubtitleCount(m_activity, position);
   }

   @Override
   public
   void onPageScrollStateChanged(int state)
   {
      if(ViewPager.SCROLL_STATE_IDLE == state)
      {
         /* Refresh the page if it has no items on display. */
         String fragmentTag = FragmentFeeds.FRAGMENT_ID_PREFIX + m_position;
         FragmentManager manager = m_activity.getFragmentManager();
         ListFragment tagFragment = (ListFragment) manager.findFragmentByTag(fragmentTag);
         Adapter listAdapter = tagFragment.getListAdapter();

         /* If the page has no items in the ListView yet, refresh the page. */
         if(0 == listAdapter.getCount())
         {
            ListView listView = tagFragment.getListView();
            AsyncTagPage.newInstance(m_position, listView, m_applicationFolder, 0 == m_position);
         }
      }
   }
}
