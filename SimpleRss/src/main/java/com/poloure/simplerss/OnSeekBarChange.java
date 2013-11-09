package com.poloure.simplerss;

import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

class OnSeekBarChange implements SeekBar.OnSeekBarChangeListener
{
   private final TextView m_seekText;
   private final String m_settingsFileName;
   private final String m_applicationFolder;

   OnSeekBarChange(TextView seekText, String settingFileName, String applicationFolder)
   {
      m_seekText = seekText;
      m_settingsFileName = settingFileName;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
   {
      String progressString = Integer.toString(progress);
      m_seekText.setText(progressString);

      File file = new File(m_applicationFolder + m_settingsFileName);
      file.delete();

      String valueString = Integer.toString(progress);
      Write.single(m_settingsFileName, valueString, m_applicationFolder);

      if(m_settingsFileName.contains("Read Item Opacity"))
      {
         /* Set the Opacity values. */
         LayoutFeedItem.setReadItemOpacity((float) progress / 100.0F);
      }
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
