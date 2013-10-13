package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

class AdapterSettingsFunctions extends BaseAdapter
{
   private static String[]       s_fileNames;
   static         int[]          s_times;
   private static String[]       s_functionTitles;
   private static String[]       s_functionSummaries;
   private        TextView       m_titleView;
   private final  LayoutInflater m_layoutInflater;
   private final  Context        m_context;

   AdapterSettingsFunctions(Context context)
   {
      m_context = context;
      m_layoutInflater = (LayoutInflater) m_context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
      Resources resources = m_context.getResources();
      s_functionTitles = resources.getStringArray(R.array.settings_function_titles);
      s_functionSummaries = resources.getStringArray(R.array.settings_function_summaries);
      s_fileNames = resources.getStringArray(R.array.settings_names);
      s_times = resources.getIntArray(R.array.refresh_integers);
   }

   @Override
   public
   int getCount()
   {
      return s_functionTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return s_functionTitles[position];
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
      String settingPath = Constants.SETTINGS_DIR + s_fileNames[position] + Constants.TXT;

      /* This type is the a heading. */
      if(0 == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_heading, parent, false);
            m_titleView = (TextView) view.findViewById(R.id.settings_heading);
         }

         m_titleView.setText(s_functionTitles[position]);
      }

      /* This type is a checkbox setting. */
      else if(1 == viewType)
      {
         HolderSettingsCheckBox holder;
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_checkbox, parent, false);
            holder = new HolderSettingsCheckBox();
            holder.title = (TextView) view.findViewById(R.id.check_title);
            holder.summary = (TextView) view.findViewById(R.id.check_summary);
            holder.checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            view.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsCheckBox) view.getTag();
         }

         holder.title.setText(s_functionTitles[position]);
         holder.summary.setText(s_functionSummaries[position]);

         /* On click, save the value of the click to a settings file. */
         holder.checkbox.setOnClickListener(new SettingBooleanChecked(settingPath, m_context));

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(Boolean.parseBoolean(Read.setting(settingPath, m_context)));
      }

      /* Otherwise, the type will default to a SeekBar. */
      else
      {
         HolderSettingsSeekBar holder;
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_seekbar, parent, false);
            holder = new HolderSettingsSeekBar();
            holder.title = (TextView) view.findViewById(R.id.seek_title);
            holder.summary = (TextView) view.findViewById(R.id.seek_summary);
            holder.seekbar = (SeekBar) view.findViewById(R.id.seekbar);
            holder.read = (TextView) view.findViewById(R.id.seek_read);
            view.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsSeekBar) view.getTag();
         }

         holder.title.setText(s_functionTitles[position]);
         holder.summary.setText(s_functionSummaries[position]);
         holder.seekbar.setMax(9);
         holder.seekbar
               .setOnSeekBarChangeListener(
                     new SeekBarRefreshTimeChange(holder, settingPath, m_context));

         /* Load the saved value and set the progress.*/
         String checker = Read.setting(settingPath, m_context);

         int time = 0 == checker.length()
               ? 3
               : null == checker || 0 == checker.length() ? 0 : Integer.parseInt(checker);
         holder.seekbar.setProgress(getIndexOfTime(time));
      }
      return view;
   }

   private
   int getIndexOfTime(int value)
   {
      if(null == s_times)
      {
         return -1;
      }
      int arrayLength = s_times.length;
      for(int i = 0; i < arrayLength; i++)
      {
         if(s_times[i] == value)
         {
            return i;
         }
      }
      return -1;
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
      if(0 == position)
      {
         return 0;
      }
      else
      {
         return 1 == position || 2 < position ? 1 : 2;
      }
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 3;
   }
}
