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
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

class DialogEditFeed extends Dialog
{
   private final Activity m_activity;
   private final int m_pos;

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
      tagEdit.setAdapter(new ArrayAdapter<>(m_activity, oneLine, tags));
      tagEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

      String oldLine = "";
      String oldName = "";

      /* If this is an edit dialog, set the EditTexts and save the old information. */
      if(-1 != m_pos)
      {
         String[][] content = Read.csvFile(getContext(), Read.INDEX, 'f', 'u', 't');
         oldName = content[0][m_pos];
         String oldUrl = content[1][m_pos];
         String oldTags = content[2][m_pos];

         ((TextView) findViewById(R.id.dialog_name)).setText(oldName);
         ((TextView) findViewById(R.id.dialog_url)).setText(oldUrl);
         ((TextView) findViewById(R.id.dialog_tags)).setText(oldTags);

         oldLine = String.format(AsyncCheckFeed.INDEX_FORMAT, oldName, oldUrl, oldTags);
      }

      /* Create the button OnClickListeners. */
      View.OnClickListener positiveButtonClick = new OnClickPositive(this, oldLine, oldName);
      View.OnClickListener negativeButtonClick = new View.OnClickListener()
      {
         @Override
         public
         void onClick(View v)
         {
            dismiss();
         }
      };

      findViewById(R.id.dialog_button_negative).setOnClickListener(negativeButtonClick);
      findViewById(R.id.dialog_button_positive).setOnClickListener(positiveButtonClick);
   }

   private
   class OnClickPositive implements View.OnClickListener
   {
      private final Dialog m_dialog;
      private final String m_oldFeed;
      private final String m_oldName;

      OnClickPositive(Dialog dialog, String oldFeed, String oldName)
      {
         m_dialog = dialog;
         m_oldFeed = oldFeed;
         m_oldName = oldName;
      }

      @Override
      public
      void onClick(View v)
      {
         AsyncCheckFeed.newInstance(m_activity, m_dialog, m_oldFeed, m_oldName);
      }
   }
}
