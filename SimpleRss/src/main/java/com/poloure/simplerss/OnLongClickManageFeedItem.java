package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

class OnLongClickManageFeedItem implements AdapterView.OnItemLongClickListener
{
   private final AlertDialog.Builder m_build;

   OnLongClickManageFeedItem(FragmentManageFeeds fragmentManageFeeds, BaseAdapter adapter,
         BaseAdapter navigationAdapter)
   {
      Context context = fragmentManageFeeds.getActivity();

      String deleteText = context.getString(R.string.delete_dialog);
      String clearText = context.getString(R.string.clear_dialog);

      DialogInterface.OnClickListener feedDeleteClick = new OnFilterClickDelete(adapter, context);
      DialogInterface.OnClickListener feedClearCacheClick = new OnClickManageFeedClearCache(adapter,
            navigationAdapter, context);

      m_build = new AlertDialog.Builder(context);
      m_build.setCancelable(true);
      m_build.setNegativeButton(deleteText, feedDeleteClick);
      m_build.setPositiveButton(clearText, feedClearCacheClick);
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
   {
      m_build.show();
      return true;
   }
}
