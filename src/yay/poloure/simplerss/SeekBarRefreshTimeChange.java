package yay.poloure.simplerss;

import android.widget.SeekBar;

class SeekBarRefreshTimeChange implements SeekBar.OnSeekBarChangeListener
{
   private static final String[] REFRESH_TIMES = Util.getArray(R.array.refresh_times);
   private final HolderSettingsSeekBar m_holder;
   private final String                m_settingsPath;

   SeekBarRefreshTimeChange(HolderSettingsSeekBar holder, String settingPath)
   {
      m_holder = holder;
      m_settingsPath = settingPath;
   }

   @Override
   public
   void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
   {
      m_holder.read.setText(REFRESH_TIMES[progress]);
      Util.remove(m_settingsPath);
      Write.single(m_settingsPath, Integer.toString(AdapterSettingsFunctions.TIMES[progress]));
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
