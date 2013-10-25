package com.poloure.simplerss;

import android.view.View;
import android.widget.Checkable;

import java.io.File;

class SettingBooleanChecked implements View.OnClickListener
{
   private final String m_settingFileName;
   private final String m_applicationFolder;

   SettingBooleanChecked(String settingPath, String applicationFolder)
   {
      m_applicationFolder = applicationFolder;
      m_settingFileName = settingPath;
   }

   @Override
   public
   void onClick(View v)
   {
      File file = new File(m_settingFileName);
      file.delete();

      boolean checked = ((Checkable) v).isChecked();
      String value = Boolean.toString(checked);
      Write.single(m_settingFileName, value, m_applicationFolder);
   }
}
