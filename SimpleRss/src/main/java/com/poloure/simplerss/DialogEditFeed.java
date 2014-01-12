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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

class DialogEditFeed extends Dialog
{
   static final int[] BUTTON_IDS = {2501, 2502};
   static final int[] IDS = {2601, 2602, 2603};
   private static final int[] HINTS = {
         R.string.feed_name_hint, R.string.feed_url_hint, R.string.feed_tag_hint
   };
   private static final int[] TEXTS = {
         R.string.feed_name_dialog, R.string.feed_url_dialog, R.string.feed_tag_dialog
   };
   private static final int[] BUTTON_TEXTS = {R.string.cancel_dialog, R.string.accept_dialog};
   private static final int COLOR_UNSELECTED = Color.argb(0, 0, 0, 0);
   private static final int COLOR_SELECTED = Color.parseColor("#ff33b5e5");
   private final String m_applicationFolder;
   private final Activity m_activity;
   private final int m_position;

   private
   DialogEditFeed(Activity activity, int position, String applicationFolder)
   {
      super(activity, android.R.style.Theme_Holo_Light_Dialog);
      m_activity = activity;
      m_position = position;
      m_applicationFolder = applicationFolder;
   }

   static
   Dialog newInstance(Activity activity, int position, String applicationFolder)
   {
      Dialog dialog = new DialogEditFeed(activity, position, applicationFolder);

      /* Get the text resources and set the title of the dialog. */
      int titleResource = -1 == position ? R.string.add_dialog_title : R.string.edit_dialog_title;
      String titleText = activity.getString(titleResource);
      dialog.setTitle(titleText);

      return dialog;
   }

   @Override
   protected
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      /* Get the current tags. */
      int tagListSize = PagerAdapterFeeds.TAG_LIST.size();
      String[] tags = PagerAdapterFeeds.TAG_LIST.toArray(new String[tagListSize]);

      /* Configure the ViewGroup. */
      LinearLayout layout = new LinearLayout(m_activity);
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.setPadding(Utilities.EIGHT_DP, Utilities.EIGHT_DP, Utilities.EIGHT_DP, 0);

      /* Make the tag EditText. */
      int oneLine = android.R.layout.simple_dropdown_item_1line;
      ArrayAdapter<String> adapter = new ArrayAdapter<>(m_activity, oneLine, tags);
      MultiAutoCompleteTextView tagEdit = new MultiAutoCompleteTextView(m_activity);
      tagEdit.setAdapter(adapter);
      tagEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

      EditText[] editTexts = {new EditText(m_activity), new EditText(m_activity), tagEdit};
      String[][] content = Read.csvFile(Read.INDEX, m_applicationFolder, 'f', 'u', 't');

      for(int i = 0; 3 > i; i++)
      {
         TextView textView = new TextView(m_activity);
         textView.setText(TEXTS[i]);

         editTexts[i].setId(IDS[i]);
         editTexts[i].setSingleLine(true);
         editTexts[i].setHint(HINTS[i]);
         editTexts[i].setText(-1 == m_position ? "" : content[i][m_position]);

         layout.addView(textView);
         layout.addView(editTexts[i]);
      }

      editTexts[1].setImeOptions(InputType.TYPE_TEXT_VARIATION_URI);
      editTexts[1].setImeOptions(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

      /* Get what the feed used to be called. */
      String oldTitle = -1 == m_position ? "" : content[0][m_position];

      /* Create the button OnClickListeners. */
      View.OnClickListener positiveButtonClick = new OnClickPositive(this, oldTitle);
      View.OnClickListener negativeButtonClick = new View.OnClickListener()
      {
         @Override
         public
         void onClick(View v)
         {
            dismiss();
         }
      };

      View.OnClickListener[] onClickListeners = {negativeButtonClick, positiveButtonClick};

      /* Create the buttons. */
      LinearLayout buttonBar = new LinearLayout(m_activity);
      buttonBar.setOrientation(LinearLayout.HORIZONTAL);
      buttonBar.setWeightSum(1.0f);
      Button[] buttons = new Button[2];

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.5f);

      /* Set up objects used for the StateListDrawable. */
      int[] unselected = {android.R.attr.drawable};
      int[] selected = {android.R.attr.state_pressed};
      ColorDrawable colorUnselected = new ColorDrawable(COLOR_UNSELECTED);
      ColorDrawable colorSelected = new ColorDrawable(COLOR_SELECTED);

      for(int i = 0; 2 > i; i++)
      {
         buttons[i] = new Button(m_activity);
         buttons[i].setText(BUTTON_TEXTS[i]);
         buttons[i].setId(BUTTON_IDS[i]);
         buttons[i].setLayoutParams(params);

         /* Create the state drawable for each button. */
         StateListDrawable states = new StateListDrawable();
         states.addState(unselected, colorUnselected);
         states.addState(selected, colorSelected);

         buttons[i].setBackground(states);
         buttons[i].setOnClickListener(onClickListeners[i]);
         buttonBar.addView(buttons[i]);
      }

      /* Create the top layout. */
      LinearLayout topLayout = new LinearLayout(m_activity);
      topLayout.setOrientation(LinearLayout.VERTICAL);
      topLayout.addView(layout);
      topLayout.addView(buttonBar);

      setContentView(topLayout);
   }

   private
   class OnClickPositive implements View.OnClickListener
   {
      private final Dialog m_dialog;
      private final String m_oldFeed;

      OnClickPositive(Dialog dialog, String oldFeed)
      {
         m_dialog = dialog;
         m_oldFeed = oldFeed;
      }

      @Override
      public
      void onClick(View v)
      {
         AsyncCheckFeed.newInstance(m_activity, m_dialog, m_oldFeed, m_applicationFolder);
      }
   }
}
