package com.poloure.simplerss;

import android.content.DialogInterface;
import android.widget.BaseAdapter;

class OnFilterClickDelete implements DialogInterface.OnClickListener
{
   private final BaseAdapter m_adapter;
   private final String      m_filterFileName;
   private final String      m_applicationFolder;
   private final String      m_filterName;

   OnFilterClickDelete(BaseAdapter adapter, String applicationFolder, String filterFileName,
         String filterName)
   {
      m_adapter = adapter;
      m_filterFileName = filterFileName;
      m_filterName = filterName;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      Write.removeLine(m_filterFileName, m_filterName, false, m_applicationFolder);
      m_adapter.notifyDataSetChanged();
   }
}
