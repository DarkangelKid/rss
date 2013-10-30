package com.poloure.simplerss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

class AdapterSettingsUi extends BaseAdapter
{
   private static final int   TYPE_HEADING  = 0;
   private static final int   TYPE_CHECKBOX = 1;
   private static final int   TYPE_SEEK_BAR = 2;
   private static final int[] TYPES         = {TYPE_HEADING, TYPE_CHECKBOX, TYPE_SEEK_BAR};
   private final String[]       m_interfaceTitles;
   private final String[]       m_interfaceSummaries;
   private final LayoutInflater m_layoutInflater;
   private final String         m_applicationFolder;
   private       TextView       m_settingsHeading;

   AdapterSettingsUi(String applicationFolder, String[] adapterTitles, String[] adapterSummaries,
         LayoutInflater layoutInflater)
   {
      m_applicationFolder = applicationFolder;
      m_interfaceTitles = adapterTitles.clone();
      m_interfaceSummaries = adapterSummaries.clone();
      m_layoutInflater = layoutInflater;
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
      String settingFileName = FeedsActivity.SETTINGS_DIR + title + ".txt";

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
         holder.m_checkbox
               .setOnClickListener(new SettingBooleanChecked(settingFileName, m_applicationFolder));

         /* Load the saved boolean value and set the box as checked if true. */
         String[] check = Read.file(settingFileName, m_applicationFolder);
         String settingString = 0 == check.length ? "" : check[0];
         boolean settingBoolean = Boolean.parseBoolean(settingString);
         holder.m_checkbox.setChecked(settingBoolean);
      }
      else if(TYPE_SEEK_BAR == viewType)
      {
         HolderSettingsSeekBar holder;
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_seekbar, parent, false);
            holder = new HolderSettingsSeekBar();
            holder.m_titleView = (TextView) view.findViewById(R.id.seek_title);
            holder.m_summaryView = (TextView) view.findViewById(R.id.seek_summary);
            holder.m_seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            holder.m_readView = (TextView) view.findViewById(R.id.seek_read);
            view.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsSeekBar) view.getTag();
         }

         holder.m_titleView.setText(m_interfaceTitles[position]);
         holder.m_summaryView.setText(m_interfaceSummaries[position]);

         if(0 == position)
         {
            holder.m_seekBar.setMax(100);
         }

         holder.m_seekBar
               .setOnSeekBarChangeListener(
                     new OnSeekBarChange(holder.m_readView, settingFileName, m_applicationFolder));

         /* Load the saved value and set the progress.*/
         String[] check = Read.file(settingFileName, m_applicationFolder);
         int settingInteger = 0 == check.length || 0 == check[0].length()
               ? 100
               : Integer.parseInt(check[0]);
         holder.m_seekBar.setProgress(settingInteger);
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
      return 0 == position ? TYPE_HEADING : TYPE_SEEK_BAR;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }

}
