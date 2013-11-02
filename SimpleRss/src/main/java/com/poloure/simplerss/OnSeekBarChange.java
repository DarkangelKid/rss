package com.poloure.simplerss;

import android.graphics.Color;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

class OnSeekBarChange implements SeekBar.OnSeekBarChangeListener
{
   private final TextView m_seekText;
   private final String   m_settingsFileName;
   private final String   m_applicationFolder;

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
         /* TODO Change of static variables. */

         /* Set the Opacity values. */
         Float opacity = progress / 100.0F;
         FeedItemView.s_cardOpacity = opacity;

         FeedItemView.s_titleRead = Color.argb(Math.round(255 * opacity), 0, 0, 0);
         FeedItemView.s_notTitleRead = Color.argb(Math.round(190 * opacity) /* Maybe 66 */, 0, 0,
               0);
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
