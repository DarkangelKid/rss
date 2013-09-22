package yay.poloure.simplerss;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;

class OnDialogClickAdd implements DialogInterface.OnClickListener
{
   private final View                        m_addRssDialog;
   private final AdapterView<SpinnerAdapter> m_tag;
   private final AlertDialog                 m_addFeedDialog;

   public
   OnDialogClickAdd(View addRssDialog, AdapterView<SpinnerAdapter> spinnerTag,
         AlertDialog addFeedDialog)
   {
      m_addRssDialog = addRssDialog;
      m_tag = spinnerTag;
      m_addFeedDialog = addFeedDialog;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String tag = Util.getText(m_addRssDialog, R.id.tag_edit);
      String url = Util.getText(m_addRssDialog, R.id.URL_edit);
      String name = Util.getText(m_addRssDialog, R.id.name_edit);

      tag = tag.toLowerCase(Constants.LOCALE);
      String spinnerTag;
      try
      {
         spinnerTag = m_tag.getSelectedItem().toString();
      }
      catch(RuntimeException e)
      {
         e.printStackTrace();
         spinnerTag = "";
      }
      Update.executeFeedCheck(m_addFeedDialog, tag, name, Constants.ADD, "", spinnerTag, 0, url);
   }
}
