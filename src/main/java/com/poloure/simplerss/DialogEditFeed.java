/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

class DialogEditFeed extends Dialog
{
   final Activity m_activity;
   final int m_pos;
   String m_oldUid = "";
   AsyncTask<Void, Void, String[]> m_task;

   private
   DialogEditFeed(Activity activity, int position)
   {
      super(activity, android.R.style.Theme_Holo_Light_Dialog);
      m_activity = activity;
      m_pos = position;
   }

   static
   Dialog newInstance(Activity activity, int position)
   {
      Dialog dialog = new DialogEditFeed(activity, position);

      /* Get the text resources and set the title of the dialog. */
      int title = -1 == position ? R.string.dialog_title_add : R.string.dialog_title_edit;
      dialog.setTitle(activity.getString(title));

      return dialog;
   }

   @Override
   protected
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.add_edit_dialog);

      /* Get the current tags. */
      int tagListSize = PagerAdapterTags.TAG_LIST.size();
      String[] tags = PagerAdapterTags.TAG_LIST.toArray(new String[tagListSize]);
      int oneLine = android.R.layout.simple_dropdown_item_1line;

      /* Configure the MultiAutoCompleteTextView. */
      MultiAutoCompleteTextView tagEdit = (MultiAutoCompleteTextView) findViewById(R.id.dialog_tags);
      tagEdit.setAdapter(new ArrayAdapter<String>(m_activity, oneLine, tags));
      tagEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

      String oldLine = "";

      /* If this is an edit dialog, set the EditTexts and save the old information. */
      if(-1 != m_pos)
      {
         String[][] content = Read.csvFile(getContext(), Read.INDEX, 'i', 'u', 't');
         m_oldUid = content[0][m_pos];
         String oldUrl = content[1][m_pos];
         String oldTags = content[2][m_pos];

         ((TextView) findViewById(R.id.dialog_url)).setText(oldUrl);
         ((TextView) findViewById(R.id.dialog_tags)).setText(oldTags);

         oldLine = String.format(AsyncCheckFeed.INDEX_FORMAT, m_oldUid, oldUrl, oldTags);
      }

      final String oldIndex = oldLine;
      final Dialog dialog = this;

      Button buttonNegative = (Button) findViewById(R.id.dialog_button_negative);
      final Button buttonPositive = (Button) findViewById(R.id.dialog_button_positive);

      /* Set the click listeners. */
      buttonNegative.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public
         void onClick(View v)
         {
            /* If the positive button says checking... */
            if(buttonPositive.getText().equals(m_activity.getString(R.string.dialog_checking)))
            {
               /* Cancel the Async task. */
               if(null != m_task)
               {
                  m_task.cancel(true);
                  buttonPositive.setText(R.string.dialog_accept);
                  buttonPositive.setEnabled(true);
               }
            }
            else
            {
               dismiss();
            }
         }
      });
      buttonPositive.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public
         void onClick(View v)
         {
            m_task = AsyncCheckFeed.newInstance(m_activity, dialog, oldIndex, -1 == m_pos ? "" : m_oldUid);
         }
      });
   }
}
