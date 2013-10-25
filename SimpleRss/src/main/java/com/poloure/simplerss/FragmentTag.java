package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

class FragmentTag extends ListFragment
{
   private static final String POSITION_KEY = "POSITION";

   static
   ListFragment newInstance(int position)
   {
      ListFragment listFragment = new FragmentTag();
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

      Context context = getActivity();
      ListView listView = getListView();

      String applicationFolder = FeedsActivity.getApplicationFolder(context);

      ListAdapter listAdapter = new AdapterTags(context, FeedsActivity.READ_ITEMS,
            applicationFolder);

      setListAdapter(listAdapter);

      Bundle bundle = getArguments();
      int position = bundle.getInt(POSITION_KEY);

      ActionBarActivity activity = (ActionBarActivity) getActivity();
      ListView navigationList = (ListView) activity.findViewById(R.id.left_drawer);
      AdapterNavDrawer adapterNavDrawer = (AdapterNavDrawer) navigationList.getAdapter();
      ActionBar actionBar = activity.getSupportActionBar();

      AbsListView.OnScrollListener scrollListener = new OnScrollFeedListener(adapterNavDrawer,
            actionBar, applicationFolder, position, /* TODO */ 24);

      listView.setOnScrollListener(scrollListener);

      if(0 == position)
      {
         FragmentManager fragmentManager = getFragmentManager();

         String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + 0;

         ListFragment listFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
         ListView listViewTags = listFragment.getListView();
         AsyncRefreshPage.newInstance(0, listViewTags, applicationFolder, /* TODO 16 DP. */ 24,
               true);
      }
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_cards, container, false);
   }
}
