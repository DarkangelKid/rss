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
      TextView titleAndLinkView = (TextView) view.findViewById(100);
      CharSequence titleAndLink = titleAndLinkView.getText();
      String titleLink = titleAndLink.toString();
      String url;

      int firstNewLine = titleLink.indexOf('\n');
      int lastNewLine = titleLink.lastIndexOf('\n');

      url = lastNewLine == firstNewLine
            ? titleLink.substring(firstNewLine)
            : titleLink.substring(firstNewLine, titleLink.indexOf('\n', firstNewLine + 1));

      System.out.println(url);

      DialogInterface.OnClickListener onClick = new OnCardContextMenuClick(url.trim(), m_context);

      AlertDialog.Builder build = new AlertDialog.Builder(m_context);
      build.setItems(R.array.card_click_menu, onClick);
      build.show();

      return true;
   }
}
