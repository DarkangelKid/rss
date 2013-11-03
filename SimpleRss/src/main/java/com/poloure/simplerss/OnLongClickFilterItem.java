package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

class OnLongClickFilterItem implements AdapterView.OnItemLongClickListener
{
   private final AlertDialog.Builder m_builder;
   private final String              m_deleteString;
   private final String              m_applicationFolder;

   OnLongClickFilterItem(AlertDialog.Builder builder, String deleteString, String applicationFolder)
   {
      m_builder = builder;
      m_deleteString = deleteString;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
   {
      BaseAdapter adapter = (BaseAdapter) parent.getAdapter();
      String filterName = (String) adapter.getItem(position);

      DialogInterface.OnClickListener onFilterClickDelete = new OnClickFilterDialogDelete(adapter,
            m_applicationFolder, FeedsActivity.FILTER_LIST, filterName);

      m_builder.setPositiveButton(m_deleteString, onFilterClickDelete);

      m_builder.show();
      return true;
   }
}
