package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.widget.SeekBar;
import android.widget.TextView;

class OnSeekBarChange implements SeekBar.OnSeekBarChangeListener
{
   private final TextView m_seekText;
   private final String   m_settingsPath;
   private final Context  m_context;
   private final int[]    m_values;
   private final String[] m_valuesString;


   OnSeekBarChange(TextView seekText, String settingPath, Context context, int arrayInt,
         int arrayString)
   {
      m_context = context;
      m_seekText = seekText;
      m_settingsPath = settingPath;

      Resources resources = context.getResources();
      m_values = resources.getIntArray(arrayInt);
      m_valuesString = resources.getStringArray(arrayString);
   }

   @Override
   public
   void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
   {
      m_seekText.setText(m_valuesString[progress]);
      Util.remove(m_settingsPath, m_context);

      String valueString = Integer.toString(m_values[progress]);
      Write.single(m_settingsPath, valueString, m_context);
   }

   @Override
   public
   void onStartTrackingTouch(SeekBar seekBar)
   {
   }

   @Override
   public
   void onStopTrackingTouch(SeekBar seekBar)
   {
   }
}
