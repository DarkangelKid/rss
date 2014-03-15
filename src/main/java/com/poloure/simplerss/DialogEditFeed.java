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
   final FeedsActivity m_activity;
   final int m_pos;
   AsyncTask<Void, Void, IndexItem> m_task;

   private
   DialogEditFeed(FeedsActivity activity, int position)
   {
      super(activity, android.R.style.Theme_Holo_Light_Dialog);
      m_activity = activity;
      m_pos = position;
   }

   static
   Dialog newInstance(FeedsActivity activity, int position)
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
      int tagListSize = PagerAdapterTags.s_tagList.size();
      String[] tags = PagerAdapterTags.s_tagList.toArray(new String[tagListSize]);
      int oneLine = android.R.layout.simple_dropdown_item_1line;

      /* Configure the MultiAutoCompleteTextView. */
      MultiAutoCompleteTextView tagEdit = (MultiAutoCompleteTextView) findViewById(R.id.dialog_tags);
      tagEdit.setAdapter(new ArrayAdapter<String>(m_activity, oneLine, tags));
      tagEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

      final IndexItem oldItem;

      /* If this is an edit dialog, set the EditTexts and save the old information. */
      if(-1 != m_pos)
      {
         oldItem = m_activity.m_index.get(m_pos);

         ((TextView) findViewById(R.id.dialog_url)).setText(oldItem.m_url);
         ((TextView) findViewById(R.id.dialog_tags)).setText(Utilities.formatTags(oldItem.m_tags));
      }
      else
      {
         oldItem = null;
      }

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
            m_task = AsyncCheckFeed.newInstance(m_activity, dialog, oldItem);
         }
      });
   }
}
