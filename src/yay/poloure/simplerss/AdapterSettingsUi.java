package yay.poloure.simplerss;

import android.graphics.Color;
import android.support.v4.view.PagerTabStrip;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

class AdapterSettingsUi extends BaseAdapter
{
   static final         String[] HOLO_COLORS          = Util.getArray(R.array.settings_colours);
   static final         int[]    COLOR_INTS           = {
         Color.rgb(51, 181, 229), // blue
         Color.rgb(170, 102, 204), // purple
         Color.rgb(153, 204, 0), // green
         Color.rgb(255, 187, 51), // orange
         Color.rgb(255, 68, 68) // red
   };
   private static final float    COLOR_SELECT_OPACITY = 0.5f;
   private static final String[] INTERFACE_TITLES     = Util.getArray(
         R.array.settings_interface_titles);
   private static final String[] INTERFACE_SUMMARIES  = Util.getArray(
         R.array.settings_interface_summaries);
   private static ImageView[] s_colorViews;
   private        TextView    settings_heading;

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

      LayoutInflater inf = Util.getLayoutInflater();

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
         SettingsColorHolder holder;
         if(null == cv1)
         {
            cv1 = inf.inflate(R.layout.settings_holocolour_select, parent, false);
            holder = new SettingsColorHolder();
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
            holder = (SettingsColorHolder) cv1.getTag();
         }

         holder.title.setText(title);
         holder.summary.setText(summary);

         /* Read the colour from settings, if null, set as blue. */
         String[] colorArray = Read.file(Constants.SETTINGS_DIR + Constants.STRIP_COLOR);

         String colour = 0 == colorArray.length ? "blue" : colorArray[0];

         /* Save the private static variable s_colorViews. */
         s_colorViews = new ImageView[]{
               holder.blue, holder.purple, holder.green, holder.yellow, holder.red,
         };

         /* Set the alpha to 0.5 if not the currently selected colour. */
         int viewsLength = s_colorViews.length;
         for(int i = 0; i < viewsLength; i++)
         {
            s_colorViews[i].setOnClickListener(new ColorClick(i));

            float alpha = colour.equals(HOLO_COLORS[i]) ? 1.0f : COLOR_SELECT_OPACITY;
            s_colorViews[i].setAlpha(alpha);
         }
      }

      /* This type is a checkbox setting. */
      else if(2 == viewType)
      {
         AdapterSettingsFunctions.SettingsCheckHolder holder;
         if(null == cv1)
         {
            cv1 = inf.inflate(R.layout.settings_checkbox, parent, false);
            holder = new AdapterSettingsFunctions.SettingsCheckHolder();
            holder.title = (TextView) cv1.findViewById(R.id.check_title);
            holder.summary = (TextView) cv1.findViewById(R.id.check_summary);
            holder.checkbox = (CheckBox) cv1.findViewById(R.id.checkbox);
            cv1.setTag(holder);
         }
         else
         {
            holder = (AdapterSettingsFunctions.SettingsCheckHolder) cv1.getTag();
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

   static
   class SettingsColorHolder
   {
      TextView  title;
      TextView  summary;
      ImageView blue;
      ImageView purple;
      ImageView green;
      ImageView yellow;
      ImageView red;
   }

   static
   class ColorClick implements OnClickListener
   {
      final int clicked_colour;

      ColorClick(int colour)
      {
         clicked_colour = colour;
      }

      @Override
      public
      void onClick(View v)
      {
         /* Write the new colour to file. */
         String colorSettingsPath = Constants.SETTINGS_DIR + Constants.STRIP_COLOR;
         Util.remove(colorSettingsPath);
         Write.single(colorSettingsPath, HOLO_COLORS[clicked_colour]);

         /* Change the selected square to alpha 1 and the rest to 0.5. */
         for(ImageView colour : s_colorViews)
         {
            colour.setAlpha(0.5f);
         }
         v.setAlpha(1.0f);

         /* Set the new colour. */
         for(PagerTabStrip strip : Constants.PAGER_TAB_STRIPS)
         {
            Util.setStripColor(strip);
         }
      }
   }
}
