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

import java.util.Arrays;

class AdapterSettingsFunctions extends BaseAdapter
{
   private static final int   TYPE_HEADING  = 0;
   private static final int   TYPE_CHECKBOX = 1;
   private static final int   TYPE_SEEKBAR  = 2;
   private static final int[] TYPES         = {TYPE_HEADING, TYPE_CHECKBOX, TYPE_SEEKBAR};

   static         int[]          s_times;
   private static String[]       s_fileNames;
   private static String[]       s_functionTitles;
   private static String[]       s_functionSummaries;
   private final  LayoutInflater m_layoutInflater;
   private final  Context        m_context;
   private        TextView       m_titleView;

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

      if(TYPE_HEADING == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_heading, parent, false);
            m_titleView = (TextView) view.findViewById(R.id.settings_heading);
         }

         m_titleView.setText(s_functionTitles[position]);
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

         holder.m_titleView.setText(s_functionTitles[position]);
         holder.m_summaryView.setText(s_functionSummaries[position]);

         /* On click, save the value of the click to a settings file. */
         holder.m_checkbox.setOnClickListener(new SettingBooleanChecked(settingPath, m_context));

         /* Load the saved boolean value and set the box as checked if true. */
         holder.m_checkbox.setChecked(Boolean.parseBoolean(Read.setting(settingPath, m_context)));
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

         holder.m_titleView.setText(s_functionTitles[position]);
         holder.m_summaryView.setText(s_functionSummaries[position]);
         holder.m_seekBar.setMax(9);

         /* Load the saved value and set the progress.*/
         String checker = Read.setting(settingPath, m_context);
         Resources resources = m_context.getResources();
         int resInteger;

         if(3 == position)
         {
            resInteger = R.array.refresh_integers;
            holder.m_seekBar
                  .setOnSeekBarChangeListener(
                        new OnSeekBarChange(holder.m_readView, settingPath, m_context, resInteger,
                              R.array.refresh_values));
         }
         else
         {
            resInteger = R.array.history_integers;
            holder.m_seekBar
                  .setOnSeekBarChangeListener(
                        new OnSeekBarChange(holder.m_readView, settingPath, m_context,
                              R.array.history_integers, R.array.history_values));
         }
         int[] settingIntegers = resources.getIntArray(resInteger);

         int settingInteger = null == checker || 0 == checker.length()
               ? 9
               : Integer.parseInt(checker);
         int settingIndex = Arrays.binarySearch(settingIntegers, settingInteger);
         holder.m_seekBar.setProgress(settingIndex);
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
         return 2 == position || 5 == position ? TYPE_SEEKBAR : TYPE_CHECKBOX;
      }
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 3;
   }
}
