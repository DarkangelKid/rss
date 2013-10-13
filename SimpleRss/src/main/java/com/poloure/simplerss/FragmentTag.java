package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
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
   private final BaseAdapter m_navigationAdapter;
   private final int         m_position;
   private static final String BACK_STACK_TAG = "BACK";

   FragmentTag(BaseAdapter navigationAdapter, int position)
   {
      m_navigationAdapter = navigationAdapter;
      m_position = position;
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

      AbsListView.OnScrollListener scrollListener = new OnScrollFeedListener(m_navigationAdapter,
            listAdapter, context);

      listView.setOnScrollListener(scrollListener);
      listView.setOnItemClickListener(new ClickListener(this));

      if(0 == m_position)
      {
         FragmentManager fragmentManager = getFragmentManager();
         Update.page(m_navigationAdapter, 0, fragmentManager, context);
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

         /* TODO FeedsActivity.s_drawerLayout.setDrawerLockMode(DrawerLayout
         .LOCK_MODE_LOCKED_CLOSED);
         FeedsActivity.m_drawerToggle.setDrawerIndicatorEnabled(false);*/

         actionBar.setDisplayHomeAsUpEnabled(true);

         String url = ((TextView) view.findViewById(R.id.url)).getText().toString();
         Fragment fragmentWebView = new FragmentWebView();
         ((FragmentWebView) fragmentWebView).setUrl(url);

         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.hide(m_fragment);
         transaction.add(R.id.drawer_layout, fragmentWebView, actionBarTitle);
         transaction.addToBackStack(BACK_STACK_TAG);
         transaction.commit();
      }
   }
}