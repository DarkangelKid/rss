package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

/* This is the context menu that appears when you long click a feed item (card). */
class OnCardLongClick implements AdapterView.OnItemLongClickListener
{
   private final Context m_context;

   OnCardLongClick(Context context)
   {
      m_context = context;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
   {
      CharSequence url = ((TextView) view.findViewById(R.id.url)).getText();

      DialogInterface.OnClickListener onClick = new OnCardContextMenuClick(url, m_context);

      AlertDialog.Builder build = new AlertDialog.Builder(m_context);
      build.setItems(R.array.card_click_menu, onClick);
      build.show();

      return true;
   }
}
