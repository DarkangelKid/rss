package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
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
   private final BaseAdapter                    m_navigationAdapter;
   private final int                            m_position;
   private final ViewPager.OnPageChangeListener m_pageChange;

   FragmentTag(BaseAdapter navigationAdapter, ViewPager.OnPageChangeListener pageChange,
         int position)
   {
      m_navigationAdapter = navigationAdapter;
      m_pageChange = pageChange;
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
            listAdapter, m_pageChange, m_position, context);

      listView.setOnScrollListener(scrollListener);
      listView.setOnItemClickListener(new ClickListener(this));

      if(0 == m_position)
      {
         FragmentManager fragmentManager = getFragmentManager();
         Update.page(m_navigationAdapter, 0, fragmentManager, context);

         /*ActionBarActivity activity = (ActionBarActivity) getActivity();

         String allTag = PagerAdapterFeeds.getTagsArray()[0];

         String unread = (String) m_navigationAdapter.getItem(0);

         ActionBar actionBar = activity.getSupportActionBar();
         actionBar.setSubtitle(allTag + " | " + unread);*/
         Update.navigation(m_navigationAdapter, m_pageChange, 0, context);
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