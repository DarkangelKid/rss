package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

class OnLongClickManageFeedItem implements AdapterView.OnItemLongClickListener
{
   private final BaseAdapter m_adapter;
   private final BaseAdapter m_navigationAdapter;
   private final Fragment    m_fragmentManageFeeds;

   OnLongClickManageFeedItem(Fragment fragmentManageFeeds, BaseAdapter adapter,
         BaseAdapter navigationAdapter)
   {
      m_navigationAdapter = navigationAdapter;
      m_fragmentManageFeeds = fragmentManageFeeds;
      m_adapter = adapter;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
   {
      Context context = m_fragmentManageFeeds.getActivity();

      String deleteText = context.getString(R.string.delete_dialog);
      String clearText = context.getString(R.string.clear_dialog);

      DialogInterface.OnClickListener feedDeleteClick = new OnClickManageFeedDelete(m_adapter, pos,
            context);
      DialogInterface.OnClickListener feedClearCacheClick = new OnClickManageFeedClearCache(
            m_adapter, pos, context);

      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setCancelable(true);
      build.setNegativeButton(deleteText, feedDeleteClick);
      build.setPositiveButton(clearText, feedClearCacheClick);
      build.show();
      return true;
   }
}
