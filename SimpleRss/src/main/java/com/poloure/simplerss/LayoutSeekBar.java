package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

class LayoutSeekBar extends RelativeLayout
{
   private final TextView m_titleView;
   private final TextView m_summaryView;
   private final SeekBar  m_seekBar;
   private final TextView m_seekText;

   LayoutSeekBar(Context context)
   {
      super(context);
      inflate(context, R.layout.settings_seekbar, this);

      /* Save the inflated views from the xml file as class fields. */
      m_titleView = (TextView) findViewById(R.id.seek_title);
      m_summaryView = (TextView) findViewById(R.id.seek_summary);
      m_seekBar = (SeekBar) findViewById(R.id.seek_bar);
      m_seekText = (TextView) findViewById(R.id.seek_read);

      /* Set the LayoutParams to match parent, match_parent. */
      int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
      AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(matchParent,
            matchParent);
      setLayoutParams(layoutParams);

      /* Set the background color of the ListView items. */
      setBackgroundColor(Color.WHITE);
   }

   void showItem(CharSequence title, CharSequence summary, int maxValue, String applicationFolder)
   {
      m_titleView.setText(title);
      m_summaryView.setText(summary);
      m_seekBar.setMax(maxValue);

      String settingFileName = FeedsActivity.SETTINGS_DIR + title + ".txt";

      m_seekBar.setOnSeekBarChangeListener(
            new OnSeekBarChange(m_seekText, settingFileName, applicationFolder));

      /* Load the saved value and set the progress.*/
      String[] check = Read.file(settingFileName, applicationFolder);
      int settingInteger = 0 == check.length || 0 == check[0].length()
            ? 100
            : Integer.parseInt(check[0]);
      m_seekBar.setProgress(settingInteger);
   }
}
