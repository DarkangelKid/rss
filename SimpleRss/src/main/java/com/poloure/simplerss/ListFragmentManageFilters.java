package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

/* Must be public for rotation. */
public
class ListFragmentManageFilters extends ListFragment
{
   static
   ListFragment newInstance()
   {
      return new ListFragmentManageFilters();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      Context context = getActivity();
      LayoutInflater layoutInflater = getLayoutInflater(savedInstanceState);
      String applicationFolder = FeedsActivity.getApplicationFolder(context);

      BaseAdapter baseAdapter = new AdapterManageFilters(applicationFolder,
            FeedsActivity.FILTER_LIST, layoutInflater);

      setListAdapter(baseAdapter);
      baseAdapter.notifyDataSetChanged();

      ListView listview = getListView();

      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      String deleteString = getString(R.string.delete_filter);

      AdapterView.OnItemLongClickListener onFilterLongClick = new OnLongClickFilterItem(builder,
            deleteString, applicationFolder);

      listview.setOnItemLongClickListener(onFilterLongClick);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview, container, false);
   }
}
