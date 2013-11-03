package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

class LayoutCheckBox extends RelativeLayout
{
   private final TextView m_titleView;
   private final TextView m_summaryView;
   private final CheckBox m_checkbox;

   LayoutCheckBox(Context context)
   {
      super(context);
      inflate(context, R.layout.settings_checkbox, this);

      /* Save the inflated views from the xml file as class fields. */
      m_titleView = (TextView) findViewById(R.id.check_title);
      m_summaryView = (TextView) findViewById(R.id.check_summary);
      m_checkbox = (CheckBox) findViewById(R.id.checkbox);

      /* Set the LayoutParams to match parent, match_parent. */
      int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
      AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(matchParent,
            matchParent);
      setLayoutParams(layoutParams);

      /* Set the background color of the ListView items. */
      setBackgroundColor(Color.WHITE);
   }

   void showItem(CharSequence title, CharSequence summary, String applicationFolder)
   {
      String settingFileName = FeedsActivity.SETTINGS_DIR + title + ".txt";

      m_titleView.setText(title);
      m_summaryView.setText(summary);
      m_checkbox.setOnClickListener(new SettingBooleanChecked(settingFileName, applicationFolder));

      /* Load the saved boolean value and set the box as checked if true. */
      String[] check = Read.file(settingFileName, applicationFolder);
      String settingString = 0 == check.length ? "false" : check[0];
      boolean settingBoolean = Boolean.parseBoolean(settingString);
      m_checkbox.setChecked(settingBoolean);
   }
}
