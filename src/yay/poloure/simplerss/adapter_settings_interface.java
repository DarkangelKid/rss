package yay.poloure.simplerss;

import android.widget.BaseAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CheckBox;

public class adapter_settings_interface extends BaseAdapter
{
	private final String[] title_array;
	private final String[] summary_array;

	private static LayoutInflater inflater;

	public adapter_settings_interface(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		title_array = context_main.getResources().getStringArray(R.array.settings_interface_titles);
		summary_array = context_main.getResources().getStringArray(R.array.settings_interface_summaries);
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
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		if(position == 0)
			return 0;

		else
			return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final int view_type = getItemViewType(position);
		final int pos = position;
		if(view_type == 0)
		{
			final settings_heading_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_heading, parent, false);
				holder = new settings_heading_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.settings_heading);
				convertView.setTag(holder);
			}
			else
				holder = (settings_heading_holder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
		}

		else if(view_type == 1)
		{
			final settings_checkbox_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_holocolour_select, parent, false);
				holder = new settings_checkbox_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.colour_title);
				holder.summary_view = (TextView) convertView.findViewById(R.id.colour_summary);
				holder.blue_view = (ImageView) convertView.findViewById(R.id.blue_image);
				holder.purple_view = (ImageView) convertView.findViewById(R.id.purple_image);
				holder.green_view = (ImageView) convertView.findViewById(R.id.green_image);
				holder.yellow_view = (ImageView) convertView.findViewById(R.id.yellow_image);
				holder.red_view = (ImageView) convertView.findViewById(R.id.red_image);
				convertView.setTag(holder);
			}
			else
				holder = (settings_checkbox_holder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
			holder.summary_view.setText(summary_array[position]);
			/*holder.checkbox.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					boolean checked = ((CheckBox) v).isChecked();
					///below code could equally easily be passed through as a final string, whatever.
					String file_name = title_array[pos];
					///PAULTODO
					///this is where values are saved to file
					///for settings files the file name should probably be the title of the settings item
					///as this is already stored in a final array, is unique and will not cause issues between apk updates.
				}
			});*/
		}
		///so far there are only 2 view types, heading and holocolour_select
		///this will change
		/*else
		{
			final settings_seekbar_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_seekbar, parent, false);
				holder = new settings_seekbar_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.seek_title);
				holder.summary_view = (TextView) convertView.findViewById(R.id.seek_summary);
				holder.seekbar = (SeekBar) convertView.findViewById(R.id.seekbar);
				holder.read_view = (TextView) convertView.findViewById(R.id.seek_read);
				convertView.setTag(holder);
			}
			else
				holder = (settings_seekbar_holder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
			holder.summary_view.setText(summary_array[position]);
			holder.seekbar.setMax(9);
			holder.seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
			{
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{
					holder.read_view.setText(refresh_times[progress]);
					String file_name = title_array[pos];
					///PAULTODO
					///may want to consider saving values to file here
				}

				public void onStartTrackingTouch(SeekBar seekBar)
				{
				}

				public void onStopTrackingTouch(SeekBar seekBar)
				{
				}
			});
		}*/
		return convertView;
	}

	static class settings_heading_holder
	{
		TextView title_view;
	}

	static class settings_holocolour_holder
	{
		TextView title_view;
		TextView summary_view;
		ImageView blue_view;
		ImageView purple_view;
		ImageView green_view;
		ImageView yellow_view;
		ImageView red_view;
	}

}
