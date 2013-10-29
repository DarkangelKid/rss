package com.poloure.simplerss;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
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

      ListView listView = getListView();
      Resources resources = getResources();
      ActionBarActivity activity = (ActionBarActivity) getActivity();
      ActionBar actionBar = activity.getSupportActionBar();
      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      ListView navigationList = (ListView) activity.findViewById(R.id.navigation_drawer);
      AdapterNavDrawer adapterNavDrawer = (AdapterNavDrawer) navigationList.getAdapter();

      ListAdapter listAdapter = new AdapterTags(activity);
      setListAdapter(listAdapter);

      Bundle bundle = getArguments();
      int position = bundle.getInt(POSITION_KEY);

      /* Get what 16DP is. */
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      int sixteenDp = Math.round(displayMetrics.density * 16);

      AbsListView.OnScrollListener scrollListener = new OnScrollFeedListener(adapterNavDrawer,
            actionBar, applicationFolder, position, sixteenDp);

      listView.setOnScrollListener(scrollListener);

      if(0 == position)
      {
         FragmentManager fragmentManager = getFragmentManager();

         String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + 0;

         ListFragment listFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
         ListView listViewTags = listFragment.getListView();
         AsyncRefreshPage.newInstance(0, listViewTags, applicationFolder, sixteenDp, true);
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
