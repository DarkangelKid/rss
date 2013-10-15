package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

class OnDialogClickAdd implements DialogInterface.OnClickListener
{
   private final View                        m_addRssDialog;
   private final AdapterView<SpinnerAdapter> m_tagMenu;
   private final BaseAdapter                 m_navigationAdapter;
   private final Context                     m_context;
   private final FragmentManager             m_fragmentManager;

   OnDialogClickAdd(View addRssDialog, AdapterView<SpinnerAdapter> spinnerTag,
         BaseAdapter navigationAdapter, FragmentManager fragmentManager, Context context)
   {
      m_addRssDialog = addRssDialog;
      m_tagMenu = spinnerTag;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
      m_fragmentManager = fragmentManager;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String tag = ((TextView) m_addRssDialog.findViewById(R.id.tag_edit)).getText().toString();
      String url = ((TextView) m_addRssDialog.findViewById(R.id.URL_edit)).getText().toString();
      String name = ((TextView) m_addRssDialog.findViewById(R.id.name_edit)).getText().toString();

      if(0 == tag.length())
      {
         try
         {
            tag = m_tagMenu.getSelectedItem().toString();
         }
         catch(RuntimeException e)
         {
            e.printStackTrace();
            tag = Constants.UNSORTED_TAG;
         }
      }
      else
      {
         tag = tag.toLowerCase(Constants.LOCALE);
      }

      Update.executeFeedCheck((AlertDialog) dialog, tag, name, Constants.ADD, "", url,
            m_navigationAdapter, m_context);

      /* Update the navigation drawer. */
      Util.updateTags(m_navigationAdapter, m_context);
   }
}
