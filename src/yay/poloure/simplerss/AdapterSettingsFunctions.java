package yay.poloure.simplerss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

class AdapterSettingsFunctions extends BaseAdapter
{
   private TextView title_view;
   private static final String[] FUNCTION_TITLES    = Util
         .getArray(R.array.settings_function_titles);
   private static final String[] FUNCTION_SUMMARIES = Util
         .getArray(R.array.settings_function_summaries);
   static final String[] FILE_NAMES         = Util.getArray(R.array.settings_names);
   static final String[] REFRESH_TIMES      = {
         "15m", "30m", "45m", "1h", "2h", "3h", "4h", "8h", "12h", "24h"
   };
   static final int[]    TIMES              = {
         15, 30, 45, 60, 120, 180, 240, 480, 720, 1440
   };

   @Override
   public long getItemId(int position)
   {
      return (long) position;
   }

   @Override
   public String getItem(int position)
   {
      return FUNCTION_TITLES[position];
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
   public int getViewTypeCount()
   {
      return 3;
   }

   @Override
   public int getItemViewType(int position)
   {
      if(0 == position)
         return 0;

      else
         return position == 1 || position > 2 ? 1 : 2;
   }

   @Override
   public View getView(int position, View cv, ViewGroup parent)
   {
      int viewType = getItemViewType(position);
      final String settingPath = FeedsActivity.SETTINGS + FILE_NAMES[position] + FeedsActivity.TXT;
      LayoutInflater inflater = Util.getLayoutInflater();

      /* This type is the a heading. */
      if(0 == viewType)
      {
         if(null == cv)
         {
            cv = inflater.inflate(R.layout.settings_heading, parent, false);
            title_view = (TextView) cv.findViewById(R.id.settings_heading);
         }

         title_view.setText(FUNCTION_TITLES[position]);
      }

      /* This type is a checkbox setting. */
      else if(1 == viewType)
      {
         SettingsCheckHolder holder;
         if(null == cv)
         {
            cv = inflater.inflate(R.layout.settings_checkbox, parent, false);
            holder = new SettingsCheckHolder();
            holder.title = (TextView) cv.findViewById(R.id.check_title);
            holder.summary = (TextView) cv.findViewById(R.id.check_summary);
            holder.checkbox = (CheckBox) cv.findViewById(R.id.checkbox);
            cv.setTag(holder);
         }
         else
            holder = (SettingsCheckHolder) cv.getTag();

         holder.title.setText(FUNCTION_TITLES[position]);
         holder.summary.setText(FUNCTION_SUMMARIES[position]);

         /* On click, save the value of the click to a settings file. */
         holder.checkbox.setOnClickListener(new OnClickListener()
         {
            @Override
            public void onClick(View v)
            {
               Util.remove(settingPath);
               String value = Boolean.toString(((Checkable) v).isChecked());
               Write.single(settingPath, value);
            }
         });

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(Util.strbol(Read.setting(settingPath)));
      }

      /* Otherwise, the type will default to a seekbar. */
      else
      {
         final SettingsSeekHolder holder;
         if(null == cv)
         {
            cv = inflater.inflate(R.layout.settings_seekbar, parent, false);
            holder = new SettingsSeekHolder();
            holder.title = (TextView) cv.findViewById(R.id.seek_title);
            holder.summary = (TextView) cv.findViewById(R.id.seek_summary);
            holder.seekbar = (SeekBar) cv.findViewById(R.id.seekbar);
            holder.read = (TextView) cv.findViewById(R.id.seek_read);
            cv.setTag(holder);
         }
         else
            holder = (SettingsSeekHolder) cv.getTag();

         holder.title.setText(FUNCTION_TITLES[position]);
         holder.summary.setText(FUNCTION_SUMMARIES[position]);
         holder.seekbar.setMax(9);
         holder.seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
         {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
               holder.read.setText(REFRESH_TIMES[progress]);
               Util.remove(settingPath);
               Write.single(settingPath, Integer.toString(TIMES[progress]));
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
         String checker = Read.setting(settingPath);
         int time = checker.isEmpty() ? 3 : Util.stoi(checker);
         holder.seekbar.setProgress(Util.index(TIMES, time));
      }
      return cv;
   }

   static class SettingsCheckHolder
   {
      TextView title;
      TextView summary;
      CheckBox checkbox;
   }

   static class SettingsSeekHolder
   {
      TextView title;
      TextView summary;
      TextView read;
      SeekBar  seekbar;
   }

}
