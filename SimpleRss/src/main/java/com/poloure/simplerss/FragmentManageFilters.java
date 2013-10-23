package com.poloure.simplerss;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

class FragmentManageFilters extends ListFragment
{
   static
   ListFragment newInstance()
   {
      return new FragmentManageFilters();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      setHasOptionsMenu(true);

      Context context = getActivity();
      BaseAdapter baseAdapter = new AdapterManageFilters(context);
      setListAdapter(baseAdapter);
      baseAdapter.notifyDataSetChanged();

      ListView listview = getListView();

      AdapterView.OnItemLongClickListener onFilterLongClick = new OnFilterLongClick(this);
      listview.setOnItemLongClickListener(onFilterLongClick);
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      CharSequence itemTitle = item.getTitle();
      String addFeed = getString(R.string.add_feed);

      if(addFeed.equals(itemTitle))
      {
         showAddFilterDialog();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   private
   void showAddFilterDialog()
   {
      Context context = getActivity();
      LayoutInflater inflater = LayoutInflater.from(context);
      View addFilterLayout = inflater.inflate(R.layout.add_filter_dialog, null);

      String cancelText = getString(R.string.cancel_dialog);
      String addText = getString(R.string.add_dialog);
      String addFilterText = getString(R.string.add_filter);

      ListView navigationList = (ListView) ((Activity) context).findViewById(R.id.left_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      DialogInterface.OnClickListener onClickCancel = new OnDialogClickCancel();
      DialogInterface.OnClickListener onClickAdd = new OnFilterDialogClickAdd(addFilterLayout,
            navigationAdapter, context);

      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(addFilterText);
      build.setView(addFilterLayout);
      build.setNegativeButton(cancelText, onClickCancel);
      build.setPositiveButton(addText, onClickAdd);
      build.show();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_cards, container, false);
   }

}
