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
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public
class FragmentManage extends Fragment
{
   static final int LIST_VIEW_MANAGE = 5634126;

   static
   Fragment newInstance()
   {
      return new FragmentManage();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      final Activity activity = (Activity) container.getContext();

      final String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      BaseAdapter baseAdapter = new AdapterManageFragments(activity);

      ListView listView = new ListView(activity);
      listView.setAdapter(baseAdapter);
      listView.setId(LIST_VIEW_MANAGE);

      /* Set the onItemClickListener that makes the EditDialog show. */
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
         @Override
         public
         void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {
            Dialog dialog = DialogEditFeed.newInstance(activity, position, applicationFolder);
            dialog.show();
         }
      });

      /* Get the items that the onClick listener needs to refresh when deleting/clearing a feed. */
      ViewPager feedPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      PagerAdapterFeeds pagerAdapterFeeds = (PagerAdapterFeeds) feedPager.getAdapter();

      listView.setOnItemLongClickListener(
            new OnLongClickManageFeedItem(activity, listView, pagerAdapterFeeds,
                  applicationFolder));

      /* Create a slight grey divider. */
      listView.setDivider(new ColorDrawable(Color.argb(255, 237, 237, 237)));
      listView.setDividerHeight(2);

      AsyncManage.newInstance(baseAdapter, applicationFolder);

      return listView;
   }
}
