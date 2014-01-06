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

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/* Must be public for rotation. */
public
class ListFragmentTag extends ListFragment
{
   private static final String POSITION_KEY = "POSITION";

   static
   ListFragment newInstance(int position)
   {
      ListFragment listFragment = new ListFragmentTag();
      Bundle bundle = new Bundle();
      bundle.putInt(POSITION_KEY, position);
      listFragment.setArguments(bundle);
      return listFragment;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView listView = getListView();
      Activity activity = getActivity();
      ActionBar actionBar = activity.getActionBar();
      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      ListView navigationList = (ListView) activity.findViewById(R.id.navigation_drawer);
      AdapterNavigationDrawer adapterNavigationDrawer = (AdapterNavigationDrawer) navigationList
            .getAdapter();

      ListAdapter listAdapter = new AdapterTags(activity, applicationFolder);
      setListAdapter(listAdapter);

      Bundle bundle = getArguments();
      int position = bundle.getInt(POSITION_KEY);

      /* Get what the listViewTopPadding is. */
      int listViewTopPadding = listView.getPaddingTop();

      AbsListView.OnScrollListener scrollListener = new OnScrollFeedListener(
            adapterNavigationDrawer, actionBar, applicationFolder, position, listViewTopPadding);

      listView.setOnScrollListener(scrollListener);
      listView.setDividerHeight(0);
      listView.setOnItemLongClickListener(new OnFeedItemLongClick(activity));

      if(0 == position)
      {
         AsyncTagPage.newInstance(0, listView, applicationFolder, true);
      }
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      ListView listView = new ListView(getActivity());

      /* Set to android.R.id.list so that the ListFragment knows to use this list. */
      listView.setId(android.R.id.list);
      listView.setBackgroundColor(Color.WHITE);
      listView.setFadingEdgeLength(0);

      return listView;
   }
}
