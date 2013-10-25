package com.poloure.simplerss;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

class OnLongClickManageFeedItem implements AdapterView.OnItemLongClickListener
{
   private final AlertDialog.Builder m_builder;
   private final String              m_applicationFolder;

   OnLongClickManageFeedItem(AlertDialog.Builder builder, String applicationFolder)
   {
      m_builder = builder;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
   {
      Adapter adapter = parent.getAdapter();
      String feedName = (String) adapter.getItem(pos);
      m_builder.setItems(R.array.long_click_manage_feeds,
            new OnClickManageFeedDialogItem(feedName, m_applicationFolder));
      m_builder.show();
      return true;
   }
}
