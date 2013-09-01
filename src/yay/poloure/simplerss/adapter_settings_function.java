package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class adapter_settings_function extends BaseAdapter
{
   TextView title_view;
   String[] title_array;
   String[] summary_array;

   static final String[] refresh_times  = {"15m","30m","45m","1h","2h","3h","4h","8h","12h","24h"};
   static final int[] times             = {15, 30, 45, 60, 120, 180, 240, 480, 720, 1440};
   static final String[] file_names =
   {
      "null",
      "auto_refresh_boolean",
      "refresh_time",
      "notifications_boolean",
      "offline_mode"
   };

   static LayoutInflater inflater;

   public adapter_settings_function(Context con)
   {
      inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      title_array   = util.get_array(con, R.array.settings_function_titles);
      summary_array = util.get_array(con, R.array.settings_function_summaries);
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
      return 5;
   }

   @Override
   public boolean isEnabled(int position)
   {
      return false;
   }

   @Override
   public int getViewTypeCount(){
      return 3;
   }

   @Override
   public int getItemViewType(int position)
   {
      if(position == 0)
         return 0;

      else if(position == 1 || position > 2)
         return 1;

      else
         return 2;
   }

   @Override
   public View getView(final int position, View cv, ViewGroup parent)
   {
      final int view_type = getItemViewType(position);
      final String setting_path = main.storage + main.SETTINGS
                                  + file_names[position] + main.TXT;
      String[] check;

      /* This type is the a heading. */
      if(view_type == 0)
      {
         if(cv == null)
         {
            cv = inflater.inflate(R.layout.settings_heading, parent, false);
            title_view = (TextView) cv.findViewById(R.id.settings_heading);
         }

         title_view.setText(title_array[position]);
      }

      /* This type is a checkbox setting. */
      else if(view_type == 1)
      {
         final settings_checkbox_holder holder;
         if(cv == null)
         {
            cv = inflater.inflate(R.layout.settings_checkbox, parent, false);
            holder              = new settings_checkbox_holder();
            holder.title_view   = (TextView) cv.findViewById(R.id.check_title);
            holder.summary_view = (TextView) cv.findViewById(R.id.check_summary);
            holder.checkbox     = (CheckBox) cv.findViewById(R.id.checkbox);
            cv.setTag(holder);
         }
         else
            holder = (settings_checkbox_holder) cv.getTag();

         holder.title_view.setText(title_array[position]);
         holder.summary_view.setText(summary_array[position]);

         /* On click, save the value of the click to a settings file. */
         holder.checkbox.setOnClickListener(new OnClickListener()
         {
            @Override
            public void onClick(View v)
            {
               util.rm(setting_path);
               String value = Boolean.toString(((CheckBox) v).isChecked());
               write.single(setting_path, value);
            }
         });

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(util.strbol(read.setting(setting_path)));
      }

      /* Otherwise, the type will default to a seekbar. */
      else
      {
         final settings_seekbar_holder holder;
         if(cv == null)
         {
            cv = inflater.inflate(R.layout.settings_seekbar, parent, false);
            holder              = new settings_seekbar_holder();
            holder.title_view   = (TextView) cv.findViewById(R.id.seek_title);
            holder.summary_view = (TextView) cv.findViewById(R.id.seek_summary);
            holder.seekbar      = (SeekBar) cv.findViewById(R.id.seekbar);
            holder.read_view    = (TextView) cv.findViewById(R.id.seek_read);
            cv.setTag(holder);
         }
         else
            holder = (settings_seekbar_holder) cv.getTag();

         holder.title_view.setText(title_array[position]);
         holder.summary_view.setText(summary_array[position]);
         holder.seekbar.setMax(9);
         holder.seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
               holder.read_view.setText(refresh_times[progress]);
               util.rm(setting_path);
               write.single(setting_path, Integer.toString(times[progress]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
         });

         /* Load the saved value and set the progress.*/
         int time = util.stoi(read.setting(setting_path));
         holder.seekbar.setProgress(util.index(times, time));
      }
      return cv;
   }

   static class settings_checkbox_holder
   {
      TextView title_view;
      TextView summary_view;
      CheckBox checkbox;
   }

   static class settings_seekbar_holder
   {
      TextView title_view;
      TextView summary_view;
      TextView read_view;
      SeekBar seekbar;
   }

}
