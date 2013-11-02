package com.poloure.simplerss;

import android.app.Dialog;
import android.view.View;

class OnClickNegativeButton implements View.OnClickListener
{
   private final Dialog m_dialog;

   OnClickNegativeButton(Dialog editDialog)
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
