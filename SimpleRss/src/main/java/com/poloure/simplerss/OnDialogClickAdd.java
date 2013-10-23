package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

class OnDialogClickAdd implements DialogInterface.OnClickListener
{
   private final Context m_context;

   OnDialogClickAdd(Context context)
   {
      m_context = context;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      Update.executeFeedCheck((AlertDialog) dialog, AsyncCheckFeed.MODE_ADD_FEED, "", m_context);
   }
}
