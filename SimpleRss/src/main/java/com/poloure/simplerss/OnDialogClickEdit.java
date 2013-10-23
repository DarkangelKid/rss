package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

class OnDialogClickEdit implements DialogInterface.OnClickListener
{
   private final String      m_title;
   private final Context     m_context;

   OnDialogClickEdit(String title, Context context)
   {
      m_title = title;
      m_context = context;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      Update.executeFeedCheck((AlertDialog) dialog, AsyncCheckFeed.MODE_EDIT_FEED, m_title,
            m_context);
   }
}
