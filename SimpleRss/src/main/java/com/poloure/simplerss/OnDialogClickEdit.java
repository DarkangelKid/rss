package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

class OnDialogClickEdit implements DialogInterface.OnClickListener
{
   private static final String UNSORTED_TAG = "Unsorted";
   private final View        m_editRssDialog;
   private final String      m_title;
   private final BaseAdapter m_navigationAdapter;
   private final Context     m_context;

   OnDialogClickEdit(View editRssDialog, String title, BaseAdapter navigationAdapter,
         Context context)
   {
      m_editRssDialog = editRssDialog;
      m_title = title;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String editTag = ((TextView) m_editRssDialog.findViewById(R.id.tag_edit)).getText()
            .toString();
      String newUrl = ((TextView) m_editRssDialog.findViewById(R.id.URL_edit)).getText().toString();
      String newName = ((TextView) m_editRssDialog.findViewById(R.id.name_edit)).getText()
            .toString();

      if(0 == editTag.length())
      {
         editTag = UNSORTED_TAG;
      }

      Update.executeFeedCheck((AlertDialog) dialog, editTag, newName, Constants.EDIT, m_title,
            newUrl, m_navigationAdapter, m_context);
   }
}
