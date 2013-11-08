package com.poloure.simplerss;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

class OnClickFilterDialogAdd implements DialogInterface.OnClickListener
{
   private final View m_addFilterLayout;
   private final BaseAdapter m_adapter;
   private final String m_applicationFolder;
   private static final String NEW_LINE = System.getProperty("line.separator");

   OnClickFilterDialogAdd(View addFilterLayout, BaseAdapter adapterManageFilters,
         String applicationFolder)
   {
      m_addFilterLayout = addFilterLayout;
      m_adapter = adapterManageFilters;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String filter = ((TextView) m_addFilterLayout).getText().toString().trim();
      String fileName = FeedsActivity.FILTER_LIST;
      String[] filters = Read.file(fileName, m_applicationFolder);
      if(-1 == ServiceUpdate.index(filters, filter))
      {
         Write.single(fileName, filter + NEW_LINE, m_applicationFolder);
      }
      m_adapter.notifyDataSetChanged();
      ((Dialog) dialog).hide();
   }

}
