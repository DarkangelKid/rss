package com.poloure.simplerss;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

class FragmentManageTags extends ListFragment
{
   private static
   void asyncCompatManageTagsRefresh(ListView listView, BaseAdapter listAdapter, Context context)
   {
      AsyncTask<Void, String[], Void> task = new AsyncManageTagsRefresh(listView, listAdapter,
            context);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView listView = getListView();
      Context context = getActivity();
      BaseAdapter baseAdapter = new AdapterManagerTags(context);
      setListAdapter(baseAdapter);

      asyncCompatManageTagsRefresh(listView, baseAdapter, context);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_cards, container, false);
   }

}
