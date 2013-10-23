package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

/* This is the context menu that appears when you long click a feed item (card). */
class OnCardLongClick implements View.OnLongClickListener
{
   private final Context m_context;

   OnCardLongClick(Context context)
   {
      m_context = context;
   }

   @Override
   public
   boolean onLongClick(View v)
   {
      TextView textView = (TextView) v.findViewById(R.id.url);
      CharSequence url = textView.getText();

      DialogInterface.OnClickListener onClick = new OnCardContextMenuClick(url, m_context);

      AlertDialog.Builder build = new AlertDialog.Builder(m_context);
      build.setItems(R.array.card_click_menu, onClick);
      build.show();

      return true;
   }
}
