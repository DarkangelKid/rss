package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.Arrays;

class FeedDialog
{
   static
   void showAddDialog()
   {
      Context con = Util.getContext();
      LayoutInflater inf = LayoutInflater.from(con);
      View addFeedLayout = inf.inflate(R.layout.add_rss_dialog, null);

      /* Remove all from the spinner. */
      String[] currentTags = Read.file(Constants.TAG_LIST);
      String[] spinnerTags = Arrays.copyOfRange(currentTags, 1, currentTags.length);

      AdapterView<SpinnerAdapter> spinnerTag
            = (AdapterView<SpinnerAdapter>) addFeedLayout.findViewById(R.id.tag_spinner);

      SpinnerAdapter adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
            spinnerTags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      spinnerTag.setAdapter(adapter);

      AlertDialog.Builder build = new AlertDialog.Builder(con);
      build.setTitle(R.string.add_dialog_title).setView(addFeedLayout).setCancelable(true);

      AlertDialog addFeedDialog = build.create();

      addFeedDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            con.getString(R.string.cancel_dialog), new OnDialogClickCancel());

      addFeedDialog.setButton(DialogInterface.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
            new OnDialogClickAdd(addFeedLayout, spinnerTag));

      addFeedDialog.show();
   }
}
