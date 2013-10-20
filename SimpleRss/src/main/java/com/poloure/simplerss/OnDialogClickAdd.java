package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Locale;

class OnDialogClickAdd implements DialogInterface.OnClickListener
{
   private final View        m_addRssDialog;
   private final BaseAdapter m_navigationAdapter;
   private final Context     m_context;

   OnDialogClickAdd(View addRssDialog, BaseAdapter navigationAdapter, Context context)
   {
      m_addRssDialog = addRssDialog;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String tag = ((TextView) m_addRssDialog.findViewById(R.id.tag_edit)).getText().toString();
      String url = ((TextView) m_addRssDialog.findViewById(R.id.URL_edit)).getText().toString();
      String name = ((TextView) m_addRssDialog.findViewById(R.id.name_edit)).getText().toString();

      tag = 0 == tag.length()
            ? m_context.getString(R.string.all_tag)
            : tag.toLowerCase(Locale.getDefault());

      Update.executeFeedCheck((AlertDialog) dialog, tag, name, Constants.ADD, "", url,
            m_navigationAdapter, m_context);

      /* Update the navigation drawer. */
      Util.updateTags(m_navigationAdapter, m_context);
   }
}
