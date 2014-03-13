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
import android.app.ListFragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

public
class ListFragmentFavourites extends ListFragment
{
   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      FeedsActivity activity = (FeedsActivity) getActivity();
      ListView listView = getListView();

      setListAdapter(new AdapterFavourites(activity));

      registerForContextMenu(listView);
      listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
      listView.setOnItemClickListener(new OnItemClickWebView(activity));
      listView.setMultiChoiceModeListener(new MultiModeListener(listView, activity));
   }

   private static
   class MultiModeListener implements AbsListView.MultiChoiceModeListener
   {
      private final ListView m_listView;
      private final Activity m_activity;
      private int m_count;

      MultiModeListener(ListView listView, Activity activity)
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
            AdapterFavourites adapter = (AdapterFavourites) m_listView.getAdapter();

            FeedItem[] items = adapter.m_feedItems.toArray(new FeedItem[adapter.m_feedItems.size()]);

            SparseBooleanArray checked = m_listView.getCheckedItemPositions();
            for(int i = 0; checked.size() > i; i++)
            {
               if(checked.valueAt(i))
               {
                  int position = checked.keyAt(i);

                  if(R.id.delete_feed == itemId)
                  {
                     adapter.m_feedItems.remove(items[position]);
                  }
               }
            }
            mode.finish();
            adapter.notifyDataSetChanged();
         } return true;
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
            menu.findItem(R.id.delete_content).setVisible(false);
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
}
