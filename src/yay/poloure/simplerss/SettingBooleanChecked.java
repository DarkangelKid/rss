package yay.poloure.simplerss;
import android.view.View;
import android.widget.Checkable;

class SettingBooleanChecked implements View.OnClickListener
{
   private final String m_settingPath;

   SettingBooleanChecked(String settingPath)
   {
      m_settingPath = settingPath;
   }

   @Override
   public
   void onClick(View v)
   {
      Util.remove(m_settingPath);
      String value = Boolean.toString(((Checkable) v).isChecked());
      Write.single(m_settingPath, value);
   }
}
