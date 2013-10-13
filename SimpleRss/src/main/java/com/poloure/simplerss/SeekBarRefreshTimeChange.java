package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.widget.SeekBar;

class SeekBarRefreshTimeChange implements SeekBar.OnSeekBarChangeListener
{
   private static String[]              s_refreshTimes;
   private final  HolderSettingsSeekBar m_holder;
   private final  String                m_settingsPath;
   private final  Context               m_context;

   SeekBarRefreshTimeChange(HolderSettingsSeekBar holder, String settingPath, Context context)
   {
      m_context = context;
      m_holder = holder;
      m_settingsPath = settingPath;

      Resources resources = context.getResources();
      s_refreshTimes = resources.getStringArray(R.array.refresh_times);
   }

   @Override
   public
   void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
   {
      m_holder.read.setText(s_refreshTimes[progress]);
      Util.remove(m_settingsPath, m_context);
      Write.single(m_settingsPath, Integer.toString(AdapterSettingsFunctions.s_times[progress]),
            m_context);
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
