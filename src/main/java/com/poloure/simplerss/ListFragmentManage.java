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
import android.widget.ListView;

import java.io.File;

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
      final String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      final ArrayAdapter<Editable> baseAdapter = new ArrayAdapter<>(activity,
            R.layout.manage_text_view);

      setListAdapter(baseAdapter);

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
               SparseBooleanArray checked = listView.getCheckedItemPositions();
               for(int i = 0; checked.size() > i; i++)
               {
                  if(checked.valueAt(i))
                  {
                     String text = baseAdapter.getItem(checked.keyAt(i)).toString();
                     String feedName = text.substring(1, text.indexOf('\n'));

                     switch(item.getItemId())
                     {
                        case R.id.delete_feed:
                           Write.editIndexLine(feedName, applicationFolder, Write.MODE_REMOVE, "");
                        case R.id.delete_content:
                           Utilities.deleteDirectory(new File(applicationFolder + feedName));
                     }
                  }
               }

               ViewPager feedPager = (ViewPager) activity.findViewById(R.id.view_pager_tags);
               PagerAdapterFeeds pagerAdapterFeeds = (PagerAdapterFeeds) feedPager.getAdapter();

               pagerAdapterFeeds.updateTags(applicationFolder, activity);
               AsyncNavigationAdapter.newInstance(activity, applicationFolder, -1);
               AsyncManage.newInstance((ArrayAdapter<Editable>) listView.getAdapter(),
                     getResources(), applicationFolder);

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

      AsyncManage.newInstance(baseAdapter, getResources(), applicationFolder);
   }

   @Override
   public
   void onListItemClick(ListView l, View v, int position, long id)
   {
      Dialog dialog = DialogEditFeed.newInstance((Activity) l.getContext(), position);
      dialog.show();
   }
}
