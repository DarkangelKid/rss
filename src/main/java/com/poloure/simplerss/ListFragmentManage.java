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
import android.app.Dialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public
class ListFragmentManage extends ListFragment
{
   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      final Activity activity = getActivity();
      final ListView listView = getListView();

      setListAdapter(new ArrayAdapter<>(activity, R.layout.manage_text_view));

      registerForContextMenu(listView);
      listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
      listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
      {
         @Override
         public
         void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
         {
         }

         @Override
         public
         boolean onActionItemClicked(ActionMode mode, MenuItem item)
         {
            if(R.id.select_all == item.getItemId())
            {
               for(int i = 0; listView.getCount() > i; i++)
               {
                  listView.setItemChecked(i, true);
               }
            }
            else
            {
               /* Read this once so that the positions are the same. */
               String[][] content = Read.csvFile(activity, Read.INDEX, 'f', 'u', 't');

               SparseBooleanArray checked = listView.getCheckedItemPositions();
               for(int i = 0; checked.size() > i; i++)
               {
                  if(checked.valueAt(i))
                  {
                     int position = checked.keyAt(i);

                     String feedName = content[0][position];
                     String oldLine = String.format(AsyncCheckFeed.INDEX_FORMAT, feedName,
                           content[1][position], content[2][position]);

                     switch(item.getItemId())
                     {
                        case R.id.delete_feed:
                           Write.editIndexLine(activity, oldLine, Write.MODE_REMOVE, "");
                        case R.id.delete_content:
                           for(String file : ServiceUpdate.FEED_FILES)
                           {
                              activity.deleteFile(feedName + file);
                           }
                     }
                  }
               }

               ViewPager feedPager = (ViewPager) activity.findViewById(R.id.view_pager_tags);
               PagerAdapterFeeds pagerAdapterFeeds = (PagerAdapterFeeds) feedPager.getAdapter();

               pagerAdapterFeeds.updateTags(activity);
               AsyncNavigationAdapter.newInstance(activity, -1);
               AsyncManage.newInstance(activity, (ArrayAdapter<Editable>) listView.getAdapter());

               mode.finish();
            }
            return true;
         }

         @Override
         public
         boolean onCreateActionMode(ActionMode mode, Menu menu)
         {
            mode.getMenuInflater().inflate(R.menu.context_manage, menu);
            return true;
         }

         @Override
         public
         void onDestroyActionMode(ActionMode mode)
         {
         }

         @Override
         public
         boolean onPrepareActionMode(ActionMode mode, Menu menu)
         {
            return false;
         }
      });
   }

   /* Called when the fragment is shown and when the Add/Edit dialog closes. */
   @Override
   public
   void onHiddenChanged(boolean hidden)
   {
      super.onHiddenChanged(hidden);
      if(!hidden)
      {
         AsyncManage.newInstance(getActivity(), (ArrayAdapter<Editable>) getListAdapter());
      }
   }

   @Override
   public
   void onListItemClick(ListView l, View v, int position, long id)
   {
      Dialog dialog = DialogEditFeed.newInstance((Activity) l.getContext(), position);
      dialog.show();
   }
}
