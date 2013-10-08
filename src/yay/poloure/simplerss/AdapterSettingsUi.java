package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class AdapterSettingsUi extends BaseAdapter
{
   static final         float    COLOR_SELECT_OPACITY = 0.3f;
   private static final String[] INTERFACE_TITLES     = Util.getArray(
         R.array.settings_interface_titles);
   private static final String[] INTERFACE_SUMMARIES  = Util.getArray(
         R.array.settings_interface_summaries);
   ImageView[] m_colorViews;
   private TextView settings_heading;

   @Override
   public
   int getCount()
   {
      return INTERFACE_TITLES.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return INTERFACE_TITLES[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View cv, ViewGroup parent)
   {
      View cv1 = cv;
      int viewType = getItemViewType(position);
      String title = INTERFACE_TITLES[position];
      String summary = INTERFACE_SUMMARIES[position];
      String settingPath = Constants.SETTINGS_DIR + title + Constants.TXT;

      String inflate = Context.LAYOUT_INFLATER_SERVICE;
      LayoutInflater inf = (LayoutInflater) Util.getContext().getSystemService(inflate);

      /* This type is a heading. */
      if(0 == viewType)
      {
         if(null == cv1)
         {
            cv1 = inf.inflate(R.layout.settings_heading, parent, false);
            settings_heading = (TextView) cv1.findViewById(R.id.settings_heading);
         }

         settings_heading.setText(title);
      }

      /* This type is the colour selector. */
      else if(1 == viewType)
      {
         HolderSettingsColor holder;
         if(null == cv1)
         {
            cv1 = inf.inflate(R.layout.settings_holocolour_select, parent, false);
            holder = new HolderSettingsColor();
            holder.title = (TextView) cv1.findViewById(R.id.colour_title);
            holder.summary = (TextView) cv1.findViewById(R.id.colour_summary);
            holder.blue = (ImageView) cv1.findViewById(R.id.blue_image);
            holder.purple = (ImageView) cv1.findViewById(R.id.purple_image);
            holder.green = (ImageView) cv1.findViewById(R.id.green_image);
            holder.yellow = (ImageView) cv1.findViewById(R.id.yellow_image);
            holder.red = (ImageView) cv1.findViewById(R.id.red_image);
            cv1.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsColor) cv1.getTag();
         }

         holder.title.setText(title);
         holder.summary.setText(summary);

         /* Read the colour from settings, if null, set as blue. */
         String[] colorArray = Read.file(Constants.SETTINGS_DIR + Constants.STRIP_COLOR);

         String colour = 0 == colorArray.length ? "blue" : colorArray[0];

         /* Save the private static variable m_colorViews. */
         m_colorViews = new ImageView[]{
               holder.blue, holder.purple, holder.green, holder.yellow, holder.red,
         };

         /* Set the alpha to 0.5 if not the currently selected colour. */
         int viewsLength = m_colorViews.length;
         for(int i = 0; i < viewsLength; i++)
         {
            m_colorViews[i].setOnClickListener(new OnClickSettingsColor(this, i));

            float alpha = colour.equals(FeedsActivity.HOLO_COLORS[i]) ? 1.0f : COLOR_SELECT_OPACITY;
            m_colorViews[i].setAlpha(alpha);
         }
      }

      /* This type is a checkbox setting. */
      else if(2 == viewType)
      {
         HolderSettingsCheckBox holder;
         if(null == cv1)
         {
            cv1 = inf.inflate(R.layout.settings_checkbox, parent, false);
            holder = new HolderSettingsCheckBox();
            holder.title = (TextView) cv1.findViewById(R.id.check_title);
            holder.summary = (TextView) cv1.findViewById(R.id.check_summary);
            holder.checkbox = (CheckBox) cv1.findViewById(R.id.checkbox);
            cv1.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsCheckBox) cv1.getTag();
         }

         holder.title.setText(title);
         holder.summary.setText(summary);
         holder.checkbox.setOnClickListener(new SettingBooleanChecked(settingPath));

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(Util.strbol(Read.setting(settingPath)));
      }
      return cv1;
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
