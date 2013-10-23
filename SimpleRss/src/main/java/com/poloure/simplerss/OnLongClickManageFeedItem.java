package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

class OnLongClickManageFeedItem implements AdapterView.OnItemLongClickListener
{
   private final BaseAdapter m_adapter;
   private final Context    m_context;

   OnLongClickManageFeedItem(Context context, BaseAdapter adapter)
   {
      m_context = context;
      m_adapter = adapter;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
   {
      String deleteText = m_context.getString(R.string.delete_dialog);
      String clearText = m_context.getString(R.string.clear_dialog);

      DialogInterface.OnClickListener feedDeleteClick = new OnClickManageFeedDelete(m_adapter, pos,
            m_context);
      DialogInterface.OnClickListener feedClearCacheClick = new OnClickManageFeedClearCache(
            m_adapter, pos, m_context);

      AlertDialog.Builder build = new AlertDialog.Builder(m_context);
      build.setNegativeButton(deleteText, feedDeleteClick);
      build.setPositiveButton(clearText, feedClearCacheClick);
      build.show();
      return true;
   }
}
