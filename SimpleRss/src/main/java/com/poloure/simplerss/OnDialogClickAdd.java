package com.poloure.simplerss;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;

class OnDialogClickAdd implements DialogInterface.OnClickListener
{
   private final View                        m_addRssDialog;
   private final AdapterView<SpinnerAdapter> m_tagMenu;
   private final BaseAdapter                 m_navigationAdapter;
   private final Context                     m_context;

   OnDialogClickAdd(View addRssDialog, AdapterView<SpinnerAdapter> spinnerTag,
         BaseAdapter navigationAdapter, Context context)
   {
      m_addRssDialog = addRssDialog;
      m_tagMenu = spinnerTag;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String tag = Util.getText(m_addRssDialog, R.id.tag_edit);
      String url = Util.getText(m_addRssDialog, R.id.URL_edit);
      String name = Util.getText(m_addRssDialog, R.id.name_edit);

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
   }
}
