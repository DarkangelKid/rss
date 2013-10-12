package com.poloure.simplerss;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;

class OnDialogClickEdit implements DialogInterface.OnClickListener
{
   private final View                        m_editRssDialog;
   private final AdapterView<SpinnerAdapter> m_tag;
   private final String                      m_title;
   private final BaseAdapter                 m_navigationAdapter;

   OnDialogClickEdit(View editRssDialog, AdapterView<SpinnerAdapter> spinnerTag, String title,
         BaseAdapter navigationAdapter)
   {
      m_editRssDialog = editRssDialog;
      m_tag = spinnerTag;
      m_title = title;
      m_navigationAdapter = navigationAdapter;

   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String editTag = Util.getText(m_editRssDialog, R.id.tag_edit);
      String newUrl = Util.getText(m_editRssDialog, R.id.URL_edit);
      String newName = Util.getText(m_editRssDialog, R.id.name_edit);
      String spinnerTag = m_tag.getSelectedItem().toString();
      if(0 == editTag.length())
      {
         editTag = spinnerTag.toLowerCase(Constants.LOCALE);
      }
      if(0 == editTag.length())
      {
         editTag = Constants.UNSORTED_TAG;
      }

      Update.executeFeedCheck((AlertDialog) dialog, editTag, newName, Constants.EDIT, m_title,
            newUrl, m_navigationAdapter);
   }
}
