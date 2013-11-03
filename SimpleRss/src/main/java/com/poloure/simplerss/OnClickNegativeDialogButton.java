package com.poloure.simplerss;

import android.app.Dialog;
import android.view.View;

class OnClickNegativeDialogButton implements View.OnClickListener
{
   private final Dialog m_dialog;

   OnClickNegativeDialogButton(Dialog editDialog)
   {
      m_dialog = editDialog;
   }

   @Override
   public
   void onClick(View v)
   {
      m_dialog.dismiss();
   }
}
