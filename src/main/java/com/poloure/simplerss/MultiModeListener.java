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

import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

class MultiModeListener implements AbsListView.MultiChoiceModeListener
{
   private final ListView m_listView;
   private final FeedsActivity m_activity;
   private int m_count;

   MultiModeListener(ListView listView, FeedsActivity activity)
   {
      m_listView = listView;
      m_activity = activity;
   }

   @Override
   public
   void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
   {
      m_count += checked ? 1 : -1;
      mode.setTitle(Utilities.NUMBER_FORMAT.format(m_count) + ' ' + m_activity.getString(R.string.managed_selected));
   }

   @Override
   public
   boolean onActionItemClicked(ActionMode mode, MenuItem item)
   {
      int itemId = item.getItemId();

      if(R.id.select_all == itemId)
      {
         for(int i = 0; m_listView.getCount() > i; i++)
         {
            if(!m_listView.isItemChecked(i))
            {
               m_listView.setItemChecked(i, true);
            }
         }
         mode.setTitle(Utilities.NUMBER_FORMAT.format(m_count) + ' ' + m_activity.getString(R.string.managed_selected));
      }
      else
      {
         SparseBooleanArray checked = m_listView.getCheckedItemPositions();

         boolean favourite = Constants.s_fragmentFavourites.isVisible();
         AdapterFavourites adapter = null;
         FeedItem[] items = null;

         if(favourite)
         {
            adapter = (AdapterFavourites) m_listView.getAdapter();
            items = adapter.m_feedItems.toArray(new FeedItem[adapter.m_feedItems.size()]);
         }

         for(int i = 0; checked.size() > i; i++)
         {
            if(checked.valueAt(i))
            {
               int position = checked.keyAt(i);

               switch(itemId)
               {
                  case R.id.delete_feed:
                     if(favourite)
                     {
                        adapter.m_feedItems.remove(items[position]);
                        break;
                     }
                     m_activity.m_index.remove(m_activity.m_index.get(position));
                  case R.id.delete_content:
                     for(String file : ServiceUpdate.FEED_FILES)
                     {
                        m_activity.deleteFile(m_activity.m_index.get(position).m_uid + file);
                     }
               }
            }
         }

         if(favourite)
         {
            adapter.notifyDataSetChanged();
         }
         else
         {
            /* Tags first, then manage, then pages, the unread counts. */
            PagerAdapterTags.run(m_activity);
            AsyncManageAdapter.run(m_activity);
            AsyncNewTagAdapters.update(m_activity);
            AsyncNavigationAdapter.run(m_activity);
         }
         mode.finish();
      }
      return true;
   }

   /* true - the action mode should be created, false - entering this mode should be aborted. */
   @Override
   public
   boolean onCreateActionMode(ActionMode mode, Menu menu)
   {
      MenuInflater inflater = mode.getMenuInflater();
      if(null != inflater)
      {
         inflater.inflate(R.menu.context_manage, menu);

         boolean manage = Constants.s_fragmentManage.isVisible();
         menu.findItem(R.id.delete_content).setVisible(manage);
         return true;
      }
      return false;
   }

   @Override
   public
   void onDestroyActionMode(ActionMode mode)
   {
      m_count = 0;
   }

   /* Called to refresh an action mode's action menu whenever it is invalidated.
    * true if the menu or action mode was updated, false otherwise.
    */
   @Override
   public
   boolean onPrepareActionMode(ActionMode mode, Menu menu)
   {
      return false;
   }
}
