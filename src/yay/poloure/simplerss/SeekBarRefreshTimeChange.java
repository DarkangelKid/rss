package yay.poloure.simplerss;
import android.widget.SeekBar;

class SeekBarRefreshTimeChange implements SeekBar.OnSeekBarChangeListener
{
   private final AdapterSettingsFunctions.SettingsSeekHolder m_holder;
   private final String                                      m_settingsPath;
   private static final String[] REFRESH_TIMES = Util.getArray(R.array.refresh_times);

   public
   SeekBarRefreshTimeChange(AdapterSettingsFunctions.SettingsSeekHolder holder, String settingPath)
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
