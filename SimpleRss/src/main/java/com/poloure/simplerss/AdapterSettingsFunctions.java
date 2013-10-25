package com.poloure.simplerss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

class AdapterSettingsFunctions extends BaseAdapter
{
   private static final int   TYPE_HEADING  = 0;
   private static final int   TYPE_CHECKBOX = 1;
   private static final int   TYPE_SEEK_BAR = 2;
   private static final int[] TYPES         = {TYPE_HEADING, TYPE_CHECKBOX, TYPE_SEEK_BAR};

   private final String[]       m_fileNames;
   private final String[]       m_functionTitles;
   private final String[]       m_functionSummaries;
   private final LayoutInflater m_layoutInflater;
   private       TextView       m_titleView;
   private final String         m_applicationFolder;

   AdapterSettingsFunctions(String applicationFolder, String[] adapterTitles,
         String[] adapterSummaries, String[] adapterFileNames, LayoutInflater layoutInflater)
   {
      m_applicationFolder = applicationFolder;
      m_functionTitles = adapterTitles.clone();
      m_functionSummaries = adapterSummaries.clone();
      m_fileNames = adapterFileNames.clone();
      m_layoutInflater = layoutInflater;
   }

   @Override
   public
   int getCount()
   {
      return m_functionTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return m_functionTitles[position];
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
      String settingFileName = FeedsActivity.SETTINGS_DIR + m_fileNames[position] + ".txt";

      if(TYPE_HEADING == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_heading, parent, false);
            m_titleView = (TextView) view.findViewById(R.id.settings_heading);
         }

         m_titleView.setText(m_functionTitles[position]);
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

         holder.m_titleView.setText(m_functionTitles[position]);
         holder.m_summaryView.setText(m_functionSummaries[position]);

         /* On click, save the value of the click to a settings file. */
         holder.m_checkbox
               .setOnClickListener(new SettingBooleanChecked(settingFileName, m_applicationFolder));

         /* Load the saved boolean value and set the box as checked if true. */
         String[] check = Read.file(settingFileName, m_applicationFolder);
         String settingString = 0 == check.length ? "" : check[0];
         boolean settingBoolean = Boolean.parseBoolean(settingString);
         holder.m_checkbox.setChecked(settingBoolean);
      }
      else
      {
         HolderSettingsSeekBar holder;
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_seekbar, parent, false);
            holder = new HolderSettingsSeekBar();
            holder.m_titleView = (TextView) view.findViewById(R.id.seek_title);
            holder.m_summaryView = (TextView) view.findViewById(R.id.seek_summary);
            holder.m_seekBar = (SeekBar) view.findViewById(R.id.seekbar);
            holder.m_readView = (TextView) view.findViewById(R.id.seek_read);
            view.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsSeekBar) view.getTag();
         }

         holder.m_titleView.setText(m_functionTitles[position]);
         holder.m_summaryView.setText(m_functionSummaries[position]);

         if(3 == position)
         {
            holder.m_seekBar.setKeyProgressIncrement(5);
            holder.m_seekBar.setMax(1440);
         }
         else
         {
            holder.m_seekBar.setKeyProgressIncrement(10);
            holder.m_seekBar.setMax(1000);
         }

         holder.m_seekBar
               .setOnSeekBarChangeListener(
                     new OnSeekBarChange(holder.m_readView, settingFileName, m_applicationFolder));

         /* Load the saved value and set the progress.*/
         String[] check = Read.file(settingFileName, m_applicationFolder);
         int settingInteger = null == check || 0 == check[0].length()
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
      if(0 == position || 4 == position)
      {
         return TYPE_HEADING;
      }
      else
      {
         return 2 == position || 5 == position ? TYPE_SEEK_BAR : TYPE_CHECKBOX;
      }
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }
}
