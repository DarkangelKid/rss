package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

class FragmentManageFilters extends ListFragment
{
   private final BaseAdapter m_navigationAdapter;

   FragmentManageFilters(BaseAdapter navigationAdapter)
   {
      m_navigationAdapter = navigationAdapter;
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      Context context = getActivity();
      ListAdapter listAdapter = new AdapterManageFilters(context);
      setListAdapter(listAdapter);

      ((AdapterManageFilters) listAdapter).setTitles(Read.file(Constants.FILTER_LIST, context));

      ListView listview = getListView();
      listview.setOnItemLongClickListener(new OnFilterLongClick(this));
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      FragmentActivity activity = getActivity();
      if(activity.onOptionsItemSelected(item))
      {
         return true;
      }

      CharSequence itemTitle = item.getTitle();
      String addFeed = activity.getString(R.string.add_feed);

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

      String cancelText = context.getString(R.string.cancel_dialog);
      String addText = context.getString(R.string.add_dialog);
      String addFilterText = context.getString(R.string.add_filter);

      DialogInterface.OnClickListener onClickCancel = new OnDialogClickCancel();
      DialogInterface.OnClickListener onClickAdd = new OnFilterDialogClickAdd(addFilterLayout,
            m_navigationAdapter, context);

      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(addFilterText);
      build.setView(addFilterLayout);
      build.setCancelable(true);
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
