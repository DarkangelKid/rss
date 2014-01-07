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
import android.app.AlertDialog;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

class OnLongClickManageFeedItem implements AdapterView.OnItemLongClickListener
{
   private final Activity m_activity;
   private final ListView m_listView;
   private final String m_applicationFolder;
   private final PagerAdapterFeeds m_pagerAdapterFeeds;

   OnLongClickManageFeedItem(Activity activity, ListView listView,
         PagerAdapterFeeds pagerAdapterFeeds, String applicationFolder)
   {
      m_activity = activity;
      m_listView = listView;
      m_pagerAdapterFeeds = pagerAdapterFeeds;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
   {
      Adapter adapter = parent.getAdapter();

      String feedName = adapter.getItem(pos).toString();
      feedName = feedName.substring(0, feedName.indexOf('\n'));

      AlertDialog.Builder build = new AlertDialog.Builder(m_activity);

      build.setItems(R.array.long_click_manage_feeds,
            new OnClickManageFeedDialogItem(m_activity, m_listView, m_pagerAdapterFeeds, feedName,
                  m_applicationFolder));
      build.show();
      return true;
   }
}
