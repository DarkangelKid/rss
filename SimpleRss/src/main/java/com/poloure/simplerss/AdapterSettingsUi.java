package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class AdapterSettingsUi extends BaseAdapter
{
   static final float COLOR_SELECT_OPACITY = 0.3f;
   private static String[] s_interfaceTitles;
   private static String[] s_interfaceSummaries;
   ImageView[] m_colorViews;
   private       TextView       settings_heading;
   private final LayoutInflater m_layoutInflater;
   private final Context        m_context;

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

      /* This type is a heading. */
      if(0 == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_heading, parent, false);
            settings_heading = (TextView) view.findViewById(R.id.settings_heading);
         }

         settings_heading.setText(title);
      }

      /* This type is the colour selector. */
      else if(1 == viewType)
      {
         HolderSettingsColor holder;
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.settings_holocolour_select, parent, false);
            holder = new HolderSettingsColor();
            holder.title = (TextView) view.findViewById(R.id.colour_title);
            holder.summary = (TextView) view.findViewById(R.id.colour_summary);
            holder.blue = (ImageView) view.findViewById(R.id.blue_image);
            holder.purple = (ImageView) view.findViewById(R.id.purple_image);
            holder.green = (ImageView) view.findViewById(R.id.green_image);
            holder.yellow = (ImageView) view.findViewById(R.id.yellow_image);
            holder.red = (ImageView) view.findViewById(R.id.red_image);
            view.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsColor) view.getTag();
         }

         holder.title.setText(title);
         holder.summary.setText(summary);

         /* Read the colour from settings, if null, set as blue. */
         String[] colorArray = Read.file(Constants.SETTINGS_DIR + Constants.STRIP_COLOR, m_context);

         String colour = 0 == colorArray.length ? "blue" : colorArray[0];

         /* Save the private static variable m_colorViews. */
         m_colorViews = new ImageView[]{
               holder.blue, holder.purple, holder.green, holder.yellow, holder.red,
         };

         /* Set the alpha to 0.5 if not the currently selected colour. */
         Resources resources = m_context.getResources();
         String[] colors = resources.getStringArray(R.array.settings_colours);
         int viewsLength = m_colorViews.length;
         for(int i = 0; i < viewsLength; i++)
         {
            m_colorViews[i].setOnClickListener(new OnClickSettingsColor(this, i, m_context));

            float alpha = colour.equals(colors[i]) ? 1.0f : COLOR_SELECT_OPACITY;
            m_colorViews[i].setAlpha(alpha);
         }
      }

      /* This type is a checkbox setting. */
      else if(2 == viewType)
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
      return 2 < position ? 2 : position;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 3;
   }

}
