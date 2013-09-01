package yay.poloure.simplerss;

import android.graphics.Color;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.support.v4.view.PagerTabStrip;

public class adapter_settings_interface extends BaseAdapter
{
   String storage;
   String[] title_array;
   String[] summary_array;
   static final String[] colours = new String[]
   {
      "blue",
      "purple",
      "green",
      "orange",
      "red"
   };
   static final int[] colour_ints = new int[]
   {
      Color.rgb(51, 181, 229), // blue
      Color.rgb(170, 102, 204), // purple
      Color.rgb(153, 204, 0), // green
      Color.rgb(255, 187, 51), // orange
      Color.rgb(255, 68, 68) // red
   };

   static ImageView[] colour_views;
   static LayoutInflater inf;

   TextView settings_heading;

   static class settings_holocolour_holder
   {
      TextView  title;
      TextView  summary;
      ImageView blue;
      ImageView purple;
      ImageView green;
      ImageView yellow;
      ImageView red;
   }

   static class settings_checkbox_holder
   {
      TextView title;
      TextView summary;
      CheckBox checkbox;
   }

   static class settings_seekbar_holder
   {
      TextView title;
      TextView summary;
      TextView read;
      SeekBar seekbar;
   }

   public adapter_settings_interface(Context con)
   {
      if(inf == null)
      {
         inf = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         title_array   = util.get_array(con, R.array.settings_interface_titles);
         summary_array = util.get_array(con, R.array.settings_interface_summaries);
         storage       = util.get_storage();
      }
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public String getItem(int position)
   {
      return title_array[position];
   }

   @Override
   public int getCount()
   {
      return title_array.length;
   }

   @Override
   public boolean isEnabled(int position)
   {
      return false;
   }

   @Override
   public int getViewTypeCount()
   {
      return 4;
   }

   @Override
   public int getItemViewType(int position)
   {
      return position;
   }

   @Override
   public View getView(final int position, View cv, ViewGroup parent)
   {
      int view_type       = getItemViewType(position);
      String title        = title_array[position];
      String summary      = summary_array[position];
      final String setting_path = main.storage + main.SETTINGS + title + main.TXT;

      /* This type is a heading. */
      if(view_type == 0)
      {
         if(cv == null)
         {
            cv = inf.inflate(R.layout.settings_heading, parent, false);
            settings_heading = (TextView) cv.findViewById(R.id.settings_heading);
         }

         settings_heading.setText(title);
      }

      /* This type is the colour selector. */
      else if(view_type == 1)
      {
         settings_holocolour_holder holder;
         if(cv == null)
         {
            cv = inf.inflate(R.layout.settings_holocolour_select, parent, false);
            holder         = new settings_holocolour_holder();
            holder.title   = (TextView)  cv.findViewById(R.id.colour_title);
            holder.summary = (TextView)  cv.findViewById(R.id.colour_summary);
            holder.blue    = (ImageView) cv.findViewById(R.id.blue_image);
            holder.purple  = (ImageView) cv.findViewById(R.id.purple_image);
            holder.green   = (ImageView) cv.findViewById(R.id.green_image);
            holder.yellow  = (ImageView) cv.findViewById(R.id.yellow_image);
            holder.red     = (ImageView) cv.findViewById(R.id.red_image);
            cv.setTag(holder);
         }
         else
            holder = (settings_holocolour_holder) cv.getTag();

         holder.title.setText(title);
         holder.summary.setText(summary);

         /* Read the colour from settings, if null, set as blue. */
         String   colour_path  = storage + main.SETTINGS + main.STRIP_COLOR;
         String[] colour_array = read.file(colour_path);

         String colour = (colour_array.length == 0) ? "blue" : colour_array[0];

         /* Save the private static variable colour_views. */
         colour_views = new ImageView[]
         {
            holder.blue,
            holder.purple,
            holder.green,
            holder.yellow,
            holder.red,
         };

         /* Set the alpha to 0.5 if not the currently selected colour. */
         float alpha;
         for(int i = 0; i < colour_views.length; i++)
         {
            colour_views[i].setOnClickListener(new colour_click(i));

            alpha = (colour.equals(colours[i])) ? 1.0f : 0.5f;
            colour_views[i].setAlpha(alpha);
         }
      }

      /* This type is a checkbox setting. */
      else if(view_type == 2)
      {
         settings_checkbox_holder holder;
         if(cv == null)
         {
            cv = (View) inf.inflate(R.layout.settings_checkbox, parent, false);
            holder          = new settings_checkbox_holder();
            holder.title    = (TextView) cv.findViewById(R.id.check_title);
            holder.summary  = (TextView) cv.findViewById(R.id.check_summary);
            holder.checkbox = (CheckBox) cv.findViewById(R.id.checkbox);
            cv.setTag(holder);
         }
         else
            holder = (settings_checkbox_holder) cv.getTag();

         holder.title.setText(title);
         holder.summary.setText(summary);
         holder.checkbox.setOnClickListener(new OnClickListener()
         {
            @Override
            public void onClick(View v)
            {
               /* Save the value of the checkbox to file on click. */
               util.rm(setting_path);
               String value = Boolean.toString(((CheckBox) v).isChecked());
               write.single(setting_path, value);
            }
         });

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(util.strbol(read.setting(setting_path)));
      }
      /* This is the seekbar. */
      else
      {
         final settings_seekbar_holder holder;
         if(cv == null)
         {
            cv = inf.inflate(R.layout.settings_seekbar, parent, false);
            holder         = new settings_seekbar_holder();
            holder.title   = (TextView) cv.findViewById(R.id.seek_title);
            holder.summary = (TextView) cv.findViewById(R.id.seek_summary);
            holder.seekbar = (SeekBar)  cv.findViewById(R.id.seekbar);
            holder.read    = (TextView) cv.findViewById(R.id.seek_read);
            cv.setTag(holder);
         }
         else
            holder = (settings_seekbar_holder) cv.getTag();

         holder.title  .setText(title);
         holder.summary.setText(summary);
         holder.seekbar.setMax(9);
         holder.seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
               holder.read.setText("DICKS");
               /* KIRSTY - refer to the seekbar in adapter_settings_fuction to see what happens in onProgressChanged(). */
            }

            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
         });
      }
      return cv;
   }

   class colour_click implements View.OnClickListener
   {
      int clicked_colour;

      public colour_click(int colour)
      {
         clicked_colour = colour;
      }

      @Override
      public void onClick(View v)
      {
         /* Write the new colour to file. */
         String colour_path = main.storage + main.SETTINGS + main.STRIP_COLOR;
         util.rm(colour_path);
         write.single(colour_path, colours[clicked_colour]);

         /* Change the selected square to alpha 1 and the rest to 0.5. */
         for(ImageView colour : colour_views)
         {
            colour.setAlpha(0.5f);
         }
         v.setAlpha(1.0f);

         /* Set the new colour. */
         for(PagerTabStrip strip : main.strips)
            util.set_strip_colour(strip);
      }
   }
}
