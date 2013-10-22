package com.poloure.simplerss;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;

class SettingBooleanChecked implements View.OnClickListener
{
   private final String  m_settingPath;
   private final Context m_context;

   SettingBooleanChecked(String settingPath, Context context)
   {
      m_context = context;
      m_settingPath = settingPath;
   }

   @Override
   public
   void onClick(View v)
   {
      Write.remove(m_settingPath, m_context);
      boolean checked = ((Checkable) v).isChecked();
      String value = Boolean.toString(checked);
      Write.single(m_settingPath, value, m_context);
   }
}
