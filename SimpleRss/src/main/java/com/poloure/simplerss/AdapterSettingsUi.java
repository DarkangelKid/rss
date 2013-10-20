package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

class AdapterSettingsUi extends BaseAdapter
{
   private static final int   TYPE_HEADING  = 0;
   private static final int   TYPE_CHECKBOX = 1;
   private static final int[] TYPES         = {TYPE_HEADING, TYPE_CHECKBOX};
   private final String[]       m_interfaceTitles;
   private final String[]       m_interfaceSummaries;
   private final LayoutInflater m_layoutInflater;
   private final Context        m_context;
   private       TextView       m_settingsHeading;

   AdapterSettingsUi(Context context)
   {
      m_context = context;
      Resources resources = context.getResources();
      m_interfaceTitles = resources.getStringArray(R.array.settings_interface_titles);
      m_interfaceSummaries = resources.getStringArray(R.array.settings_interface_summaries);
      m_layoutInflater = (LayoutInflater) m_context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
   }

   @Override
   public
   int getCount()
   {
      return m_interfaceTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return m_interfaceTitles[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      int viewType = getItemViewType(position);
      String title = m_interfaceTitles[position];
      String summary = m_interfaceSummaries[position];
      String settingPath = Constants.SETTINGS_DIR + title + Constants.TXT;

      if(TYPE_HEADING == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_heading, parent, false);
            m_settingsHeading = (TextView) view.findViewById(R.id.settings_heading);
         }

         m_settingsHeading.setText(title);
      }
      else if(TYPE_CHECKBOX == viewType)
      {
         HolderSettingsCheckBox holder;
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_checkbox, parent, false);
            holder = new HolderSettingsCheckBox();
            holder.m_titleView = (TextView) view.findViewById(R.id.check_title);
            holder.m_summaryView = (TextView) view.findViewById(R.id.check_summary);
            holder.m_checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsCheckBox) view.getTag();
         }

         holder.m_titleView.setText(title);
         holder.m_summaryView.setText(summary);
         holder.m_checkbox.setOnClickListener(new SettingBooleanChecked(settingPath, m_context));

         /* Load the saved boolean value and set the box as checked if true. */
         String settingString = Read.setting(settingPath, m_context);
         boolean settingBoolean = Boolean.parseBoolean(settingString);
         holder.m_checkbox.setChecked(settingBoolean);
      }
      return view;
   }

   @Override
   public
   boolean isEnabled(int position)
   {
      return false;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      return 0 == position ? TYPE_HEADING : TYPE_CHECKBOX;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }

}
