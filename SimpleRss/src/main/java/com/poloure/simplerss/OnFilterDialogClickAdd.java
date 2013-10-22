package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

class OnFilterDialogClickAdd implements DialogInterface.OnClickListener
{
   private final View        m_addFilterLayout;
   private final BaseAdapter m_adapter;
   private final Context     m_context;

   OnFilterDialogClickAdd(View addFilterLayout, BaseAdapter adapterManageFilters, Context context)
   {
      m_context = context;
      m_addFilterLayout = addFilterLayout;
      m_adapter = adapterManageFilters;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String filter = getText((TextView) m_addFilterLayout);
      String path = AdapterManageFilters.FILTER_LIST;
      String[] filters = Read.file(path, m_context);
      if(-1 == ServiceUpdate.index(filters, filter))
      {
         Write.single(path, filter + System.getProperty("line.separator"), m_context);
      }
      m_adapter.notifyDataSetChanged();
      ((Dialog) dialog).hide();
   }

   private static
   String getText(TextView view)
   {
      return view.getText().toString().trim();
   }
}
