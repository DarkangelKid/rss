package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

class OnFilterLongClick implements AdapterView.OnItemLongClickListener
{
   private final ListFragment m_filters;

   OnFilterLongClick(ListFragment filters)
   {
      m_filters = filters;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
   {
      FragmentActivity activity = m_filters.getActivity();
      ListAdapter listAdapter = m_filters.getListAdapter();

      String deleteString = activity.getString(R.string.delete_dialog);
      DialogInterface.OnClickListener onFilterClickDelete = new OnFilterClickDelete(
            (BaseAdapter) listAdapter, activity);

      AlertDialog.Builder build = new AlertDialog.Builder(activity);
      build.setCancelable(true);
      build.setPositiveButton(deleteString, onFilterClickDelete);
      build.show();
      return true;
   }
}
