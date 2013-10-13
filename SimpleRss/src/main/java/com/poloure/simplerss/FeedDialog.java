package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;

import java.util.Arrays;

class FeedDialog
{
   static
   void showAddDialog(BaseAdapter navigationAdapter, Context context)
   {
      LayoutInflater inf = LayoutInflater.from(context);
      View addFeedLayout = inf.inflate(R.layout.add_rss_dialog, null);

      /* Remove all from the spinner. */
      String[] currentTags = Read.file(Constants.TAG_LIST, context);
      String[] spinnerTags = Arrays.copyOfRange(currentTags, 1, currentTags.length);

      AdapterView<SpinnerAdapter> spinnerTag
            = (AdapterView<SpinnerAdapter>) addFeedLayout.findViewById(R.id.tag_spinner);

      String cancelText = context.getString(R.string.cancel_dialog);
      String addText = context.getString(R.string.add_dialog);

      DialogInterface.OnClickListener onCancel = new OnDialogClickCancel();
      DialogInterface.OnClickListener onAdd = new OnDialogClickAdd(addFeedLayout, spinnerTag,
            navigationAdapter, context);

      SpinnerAdapter adapter = new ArrayAdapter<String>(context, R.layout.group_spinner_text,
            spinnerTags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      spinnerTag.setAdapter(adapter);

      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(R.string.add_dialog_title);
      build.setView(addFeedLayout);
      build.setCancelable(true);
      build.setNegativeButton(cancelText, onCancel);
      build.setPositiveButton(addText, onAdd);
      build.show();
   }
}
