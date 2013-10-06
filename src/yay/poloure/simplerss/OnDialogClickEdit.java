package yay.poloure.simplerss;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;

class OnDialogClickEdit implements DialogInterface.OnClickListener
{
   private View                        m_editRssDialog;
   private AdapterView<SpinnerAdapter> m_tag;
   private AlertDialog                 m_editFeedDialog;
   private String                      m_title;

   public
   OnDialogClickEdit(View editRssDialog, AdapterView<SpinnerAdapter> spinnerTag,
         AlertDialog editFeedDialog, String title)
   {
      m_editRssDialog = editRssDialog;
      m_tag = spinnerTag;
      m_editFeedDialog = editFeedDialog;
      m_title = title;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String editTag = Util.getText(m_editRssDialog, R.id.tag_edit);
      String newUrl = Util.getText(m_editRssDialog, R.id.URL_edit);
      String newName = Util.getText(m_editRssDialog, R.id.name_edit);
      String spinnerTag = m_tag.getSelectedItem().toString();
      if(editTag.isEmpty())
      {
         editTag = spinnerTag.toLowerCase(Constants.LOCALE);
      }
      if(editTag.isEmpty())
      {
         editTag = Constants.UNSORTED_TAG;
      }

      Update.executeFeedCheck(m_editFeedDialog, editTag, newName, Constants.EDIT, m_title, newUrl,
            which);
   }
}
