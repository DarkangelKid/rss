package com.poloure.simplerss;

import android.app.Activity;
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

      BaseAdapter listAdapter = new AdapterTags(context);
      setListAdapter(listAdapter);

      Bundle bundle = getArguments();
      int position = bundle.getInt(POSITION_KEY);

      ListView navigationList = (ListView) ((Activity) context).findViewById(R.id.left_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      AbsListView.OnScrollListener scrollListener = new OnScrollFeedListener(listAdapter, context);

      listView.setOnScrollListener(scrollListener);
      listView.setOnItemClickListener(new ClickListener(this));

      if(0 == position)
      {
         FragmentManager fragmentManager = getFragmentManager();
         Update.asyncCompatRefreshPage(0, fragmentManager, context);
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