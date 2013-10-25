package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

class FragmentTag extends ListFragment
{
   private static final String BACK_STACK_TAG = "BACK";
   private static final String POSITION_KEY   = "POSITION";

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

      BaseAdapter listAdapter = new AdapterTags(context, FeedsActivity.READ_ITEMS,
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
      listView.setOnItemClickListener(new ClickListener(this));

      if(0 == position)
      {
         FragmentManager fragmentManager = getFragmentManager();

         String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + 0;
         String storage = FeedsActivity.getApplicationFolder(context);

         ListFragment listFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
         ListView listViewTags = listFragment.getListView();
         AsyncRefreshPage.newInstance(0, listViewTags, storage, /* TODO 16 DP. */ 24, true);
      }
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_cards, container, false);
   }

   /* Open WebView. */
   class ClickListener implements AdapterView.OnItemClickListener
   {
      private final Fragment m_fragment;

      ClickListener(Fragment fragment)
      {
         m_fragment = fragment;
      }

      @Override
      public
      void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         ActionBarActivity activity = (ActionBarActivity) getActivity();
         FragmentManager fragmentManager = getFragmentManager();
         ActionBar actionBar = activity.getSupportActionBar();

         String actionBarTitle = getString(R.string.offline);
         actionBar.setTitle(actionBarTitle);

         DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);

         drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

         // TODO FeedsActivity.m_drawerToggle.setDrawerIndicatorEnabled(false);

         actionBar.setDisplayHomeAsUpEnabled(true);

         TextView textView = (TextView) view.findViewById(R.id.url);
         CharSequence url = textView.getText();
         Fragment fragmentWebView = FragmentWebView.newInstance();
         ((FragmentWebView) fragmentWebView).setUrl(url);

         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.hide(m_fragment);
         transaction.add(R.id.drawer_layout, fragmentWebView, actionBarTitle);
         transaction.addToBackStack(BACK_STACK_TAG);
         transaction.commit();
      }
   }
}