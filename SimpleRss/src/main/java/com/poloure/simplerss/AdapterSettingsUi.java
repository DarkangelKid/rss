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
   private static final int TYPE_HEADING  = 0;
   private static final int TYPE_CHECKBOX = 1;
   private static String[]       s_interfaceTitles;
   private static String[]       s_interfaceSummaries;
   private final  LayoutInflater m_layoutInflater;
   private final  Context        m_context;
   private        TextView       settings_heading;

   AdapterSettingsUi(Context context)
   {
      m_context = context;
      Resources resources = context.getResources();
      s_interfaceTitles = resources.getStringArray(R.array.settings_interface_titles);
      s_interfaceSummaries = resources.getStringArray(R.array.settings_interface_summaries);
      m_layoutInflater = (LayoutInflater) m_context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE);
   }

   @Override
   public
   int getCount()
   {
      return s_interfaceTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return s_interfaceTitles[position];
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
      String title = s_interfaceTitles[position];
      String summary = s_interfaceSummaries[position];
      String settingPath = Constants.SETTINGS_DIR + title + Constants.TXT;

      if(TYPE_HEADING == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_heading, parent, false);
            settings_heading = (TextView) view.findViewById(R.id.settings_heading);
         }

         settings_heading.setText(title);
      }
      else if(TYPE_CHECKBOX == viewType)
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

         holder.title.setText(title);
         holder.summary.setText(summary);
         holder.checkbox.setOnClickListener(new SettingBooleanChecked(settingPath, m_context));

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(Boolean.parseBoolean(Read.setting(settingPath, m_context)));
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
      return 2;
   }

}
