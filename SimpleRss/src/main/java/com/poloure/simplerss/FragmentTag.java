package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

class FragmentTag extends ListFragment
{
   private final BaseAdapter m_navigationAdapter;
   private final BaseAdapter m_tagAdapter;
   private final int         m_position;

   FragmentTag(BaseAdapter navigationAdapter, Context context, int position)
   {
      m_navigationAdapter = navigationAdapter;
      m_tagAdapter = new AdapterTag(context);
      m_position = position;
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setListAdapter(m_tagAdapter);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      ListView listView = getListView();
      Context context = getActivity();

      listView.setOnScrollListener(
            new OnScrollFeedListener(m_navigationAdapter, m_tagAdapter, context));

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
   @Override
   public
   void onListItemClick(ListView l, View v, int position, long id)
   {
      super.onListItemClick(l, v, position, id);
      new WebViewMode();
   }
}
