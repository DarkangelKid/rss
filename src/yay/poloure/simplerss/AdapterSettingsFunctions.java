package yay.poloure.simplerss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

class AdapterSettingsFunctions extends BaseAdapter
{
   static final         String[] FILE_NAMES         = Util.getArray(R.array.settings_names);
   static final         int[]    TIMES              = Util.getContext()
         .getResources()
         .getIntArray(R.array.refresh_integers);
   private static final String[] FUNCTION_TITLES    = Util.getArray(
         R.array.settings_function_titles);
   private static final String[] FUNCTION_SUMMARIES = Util.getArray(
         R.array.settings_function_summaries);
   private TextView m_titleView;

   @Override
   public
   int getCount()
   {
      return 5;
   }

   @Override
   public
   String getItem(int position)
   {
      return FUNCTION_TITLES[position];
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
      String settingPath = Constants.SETTINGS_DIR + FILE_NAMES[position] + Constants.TXT;
      LayoutInflater inflater = Util.getLayoutInflater();

      /* This type is the a heading. */
      if(0 == viewType)
      {
         if(null == cv1)
         {
            cv1 = inflater.inflate(R.layout.settings_heading, parent, false);
            m_titleView = (TextView) cv1.findViewById(R.id.settings_heading);
         }

         m_titleView.setText(FUNCTION_TITLES[position]);
      }

      /* This type is a checkbox setting. */
      else if(1 == viewType)
      {
         HolderSettingsCheckBox holder;
         if(null == cv1)
         {
            cv1 = inflater.inflate(R.layout.settings_checkbox, parent, false);
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

         holder.title.setText(FUNCTION_TITLES[position]);
         holder.summary.setText(FUNCTION_SUMMARIES[position]);

         /* On click, save the value of the click to a settings file. */
         holder.checkbox.setOnClickListener(new SettingBooleanChecked(settingPath));

         /* Load the saved boolean value and set the box as checked if true. */
         holder.checkbox.setChecked(Util.strbol(Read.setting(settingPath)));
      }

      /* Otherwise, the type will default to a SeekBar. */
      else
      {
         HolderSettingsSeekBar holder;
         if(null == cv1)
         {
            cv1 = inflater.inflate(R.layout.settings_seekbar, parent, false);
            holder = new HolderSettingsSeekBar();
            holder.title = (TextView) cv1.findViewById(R.id.seek_title);
            holder.summary = (TextView) cv1.findViewById(R.id.seek_summary);
            holder.seekbar = (SeekBar) cv1.findViewById(R.id.seekbar);
            holder.read = (TextView) cv1.findViewById(R.id.seek_read);
            cv1.setTag(holder);
         }
         else
         {
            holder = (HolderSettingsSeekBar) cv1.getTag();
         }

         holder.title.setText(FUNCTION_TITLES[position]);
         holder.summary.setText(FUNCTION_SUMMARIES[position]);
         holder.seekbar.setMax(9);
         holder.seekbar
               .setOnSeekBarChangeListener(new SeekBarRefreshTimeChange(holder, settingPath));

         /* Load the saved value and set the progress.*/
         String checker = Read.setting(settingPath);
         int time = 0 == checker.length() ? 3 : Util.stoi(checker);
         holder.seekbar.setProgress(index(TIMES, time));
      }
      return cv1;
   }

   private static
   int index(int[] array, int value)
   {
      if(null == array)
      {
         return -1;
      }
      int arrayLength = array.length;
      for(int i = 0; i < arrayLength; i++)
      {
         if(array[i] == value)
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
