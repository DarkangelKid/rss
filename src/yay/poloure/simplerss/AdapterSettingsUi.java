package yay.poloure.simplerss;

import android.graphics.Color;
import android.support.v4.view.PagerTabStrip;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

class AdapterSettingsUi extends BaseAdapter
{
   static final         String[] HOLO_COLORS         = Util.getArray(R.array.settings_colours);
   static final         int[]    COLOR_INTS          = {
         Color.rgb(51, 181, 229), // blue
         Color.rgb(170, 102, 204), // purple
         Color.rgb(153, 204, 0), // green
         Color.rgb(255, 187, 51), // orange
         Color.rgb(255, 68, 68) // red
   };
   private static final String[] INTERFACE_TITLES    = Util.getArray(
         R.array.settings_interface_titles);
   private static final String[] INTERFACE_SUMMARIES = Util.getArray(
         R.array.settings_interface_summaries);
   private static ImageView[] colour_views;
   private        TextView    settings_heading;

   @Override
   public int getCount()
   {
      return INTERFACE_TITLES.length;
   }

   @Override
   public String getItem(int position)
   {
      return INTERFACE_TITLES[position];
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public View getView(int position, View cv, ViewGroup parent)
   {
      int viewType = getItemViewType(position);
      String title = INTERFACE_TITLES[position];
      String summary = INTERFACE_SUMMARIES[position];
      final String settingPath = FeedsActivity.SETTINGS + title + FeedsActivity.TXT;

      LayoutInflater inf = Util.getLayoutInflater();

      /* This type is a heading. */
      if(0 == viewType)
      {
         if(null == cv)
         {
            cv = inf.inflate(R.layout.settings_heading, parent, false);
            settings_heading = (TextView) cv.findViewById(R.id.settings_heading);
         }

         settings_heading.setText(title);
      }

      /* This type is the colour selector. */
      else if(1 == viewType)
      {
         SettingsColorHolder holder;
         if(null == cv)
         {
            cv = inf.inflate(R.layout.settings_holocolour_select, parent, false);
            holder = new SettingsColorHolder();
            holder.title = (TextView) cv.findViewById(R.id.colour_title);
            holder.summary = (TextView) cv.findViewById(R.id.colour_summary);
            holder.blue = (ImageView) cv.findViewById(R.id.blue_image);
            holder.purple = (ImageView) cv.findViewById(R.id.purple_image);
            holder.green = (ImageView) cv.findViewById(R.id.green_image);
            holder.yellow = (ImageView) cv.findViewById(R.id.yellow_image);
            holder.red = (ImageView) cv.findViewById(R.id.red_image);
            cv.setTag(holder);
         }
         else
         {
            holder = (SettingsColorHolder) cv.getTag();
         }

         holder.title.setText(title);
         holder.summary.setText(summary);

         /* Read the colour from settings, if null, set as blue. */
         String[] colorArray = Read.file(FeedsActivity.SETTINGS + FeedsActivity.STRIP_COLOR);

         String colour = 0 == colorArray.length ? "blue" : colorArray[0];

         /* Save the private static variable colour_views. */
         colour_views = new ImageView[]{
               holder.blue, holder.purple, holder.green, holder.yellow, holder.red,
         };

         /* Set the alpha to 0.5 if not the currently selected colour. */
         for(int i = 0; i < colour_views.length; i++)
         {
            colour_views[i].setOnClickListener(new ColorClick(i));

            float alpha = colour.equals(HOLO_COLORS[i]) ? 1.0f : 0.5f;
            colour_views[i].setAlpha(alpha);
         }
      }

      /* This type is a checkbox setting. */
      else if(2 == viewType)
      {
         AdapterSettingsFunctions.SettingsCheckHolder holder;
         if(null == cv)
         {
            cv = inf.inflate(R.layout.settings_checkbox, parent, false);
            holder = new AdapterSettingsFunctions.SettingsCheckHolder();
            holder.title = (TextView) cv.findViewById(R.id.check_title);
            holder.summary = (TextView) cv.findViewById(R.id.check_summary);
            holder.checkbox = (CheckBox) cv.findViewById(R.id.checkbox);
            cv.setTag(holder);
         }
         else
         {
            holder = (AdapterSettingsFunctions.SettingsCheckHolder) cv.getTag();
         }

         holder.title.setText(title);
         holder.summary.setText(summary);
         holder.checkbox.setOnClickListener(new OnClickListener()
         {
            @Override
            public void onClick(View v)
            {
               /* Save the value of the checkbox to file on click. */
               Util.remove(settingPath);
               String value = Boolean.toString(((Checkable) v).isChecked());
               Write.single(settingPath, value);
            }
         });

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(Util.strbol(Read.setting(settingPath)));
      }
      /* This is the seekbar. */
      else
      {
         AdapterSettingsFunctions.SettingsSeekHolder holder;
         if(null == cv)
         {
            cv = inf.inflate(R.layout.settings_seekbar, parent, false);
            holder = new AdapterSettingsFunctions.SettingsSeekHolder();
            holder.title = (TextView) cv.findViewById(R.id.seek_title);
            holder.summary = (TextView) cv.findViewById(R.id.seek_summary);
            holder.seekbar = (SeekBar) cv.findViewById(R.id.seekbar);
            holder.read = (TextView) cv.findViewById(R.id.seek_read);
            cv.setTag(holder);
         }
         else
         {
            holder = (AdapterSettingsFunctions.SettingsSeekHolder) cv.getTag();
         }

         holder.title.setText(title);
         holder.summary.setText(summary);
         holder.seekbar.setMax(9);
         holder.seekbar.setOnSeekBarChangeListener(new SeekBarChangeListenerUi(holder));
      }
      return cv;
   }

   @Override
   public boolean isEnabled(int position)
   {
      return false;
   }

   @Override
   public int getItemViewType(int position)
   {
      return position;
   }

   @Override
   public int getViewTypeCount()
   {
      return 4;
   }

   static class SettingsColorHolder
   {
      TextView  title;
      TextView  summary;
      ImageView blue;
      ImageView purple;
      ImageView green;
      ImageView yellow;
      ImageView red;
   }

   static class ColorClick implements OnClickListener
   {
      final int clicked_colour;

      ColorClick(int colour)
      {
         clicked_colour = colour;
      }

      @Override
      public void onClick(View v)
      {
         /* Write the new colour to file. */
         String colorSettingsPath = FeedsActivity.SETTINGS + FeedsActivity.STRIP_COLOR;
         Util.remove(colorSettingsPath);
         Write.single(colorSettingsPath, HOLO_COLORS[clicked_colour]);

         /* Change the selected square to alpha 1 and the rest to 0.5. */
         for(ImageView colour : colour_views)
         {
            colour.setAlpha(0.5f);
         }
         v.setAlpha(1.0f);

         /* Set the new colour. */
         for(PagerTabStrip strip : FeedsActivity.PAGER_TAB_STRIPS)
         {
            Util.setStripColor(strip);
         }
      }
   }

   static class SeekBarChangeListenerUi implements OnSeekBarChangeListener
   {
      AdapterSettingsFunctions.SettingsSeekHolder holder;

      public SeekBarChangeListenerUi(AdapterSettingsFunctions.SettingsSeekHolder holder)
      {
         this.holder = holder;
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
      {
         holder.read.setText("DICKS");
         /* KIRSTY - refer to the seekbar in adapter_settings_fuction to see what happens
         in onProgressChanged(). */
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar)
      {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar)
      {
      }
   }
}
