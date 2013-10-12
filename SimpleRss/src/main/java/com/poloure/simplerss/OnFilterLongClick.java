package com.poloure.simplerss;
import android.app.AlertDialog;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

class OnFilterLongClick implements AdapterView.OnItemLongClickListener
{
   private final Adapter      m_filterAdapter;
   private final ListFragment m_filters;

   OnFilterLongClick(ListFragment filters, Adapter adapter)
   {
      m_filters = filters;
      m_filterAdapter = adapter;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
   {
      AlertDialog.Builder build = new AlertDialog.Builder(m_filters.getActivity());
      build.setCancelable(true)
            .setPositiveButton(Util.getString(R.string.delete_dialog),
                  new OnFilterClickDelete(m_filterAdapter))
            .show();
      return true;
   }
}
