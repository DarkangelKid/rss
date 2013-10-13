package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

class FragmentManageTags extends ListFragment
{
   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView listView = getListView();
      Context context = getActivity();
      ListAdapter listAdapter = new AdapterManagerTags(context);
      setListAdapter(listAdapter);

      Update.AsyncCompatManageTagsRefresh(listView, listAdapter, context);
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      FragmentActivity activity = getActivity();
      String addFeed = activity.getString(R.string.add_feed);
      CharSequence menuTitle = item.getTitle();

      return activity.onOptionsItemSelected(item) ||
            addFeed.equals(menuTitle) ||
            super.onOptionsItemSelected(item);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_cards, container, false);
   }

}
