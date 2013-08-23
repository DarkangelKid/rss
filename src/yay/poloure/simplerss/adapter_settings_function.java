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
	private final TextView title_view;
	private final String[] title_array;
	private final String[] summary_array;
	private static final String[] refresh_times	= {"15m","30m","45m","1h","2h","3h","4h","8h","12h","24h"};
	private static final String[] file_names		= {"auto_refresh_boolean", "refresh_time", "notifications_boolean", "offline_mode"};

	private static LayoutInflater inflater;

	public adapter_settings_function(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		title_array = context_main.getResources().getStringArray(R.array.settings_function_titles);
		summary_array = context_main.getResources().getStringArray(R.array.settings_function_summaries);
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
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final int view_type = getItemViewType(position);
		if(view_type == 0)
		{
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.settings_heading, parent, false);
				title_view = (TextView) convertView.findViewById(R.id.settings_heading);
			}

			title_view.setText(title_array[position]);
		}

		else if(view_type == 1)
		{
			final settings_checkbox_holder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.settings_checkbox, parent, false);
				holder = new settings_checkbox_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.check_title);
				holder.summary_view = (TextView) convertView.findViewById(R.id.check_summary);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
				convertView.setTag(holder);
			}
			else
				holder = (settings_checkbox_holder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
			holder.summary_view.setText(summary_array[position]);
			holder.checkbox.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					boolean checked = ((CheckBox) v).isChecked();
					String file_name = title_array[pos];
					/* Here. */
				}
			});
		}
		else
		{
			final settings_seekbar_holder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.settings_seekbar, parent, false);
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
					utilities.delete(main.storage + main.SETTINGS + main.SEPAR + main.REFRESH_TIME);
					utilities.append_string_to_file(main.storage + main.SETTINGS + main.SEPAR + file_names[position], refresh_times[progress]);
				}

				public void onStartTrackingTouch(SeekBar seekBar)
				{
				}

				public void onStopTrackingTouch(SeekBar seekBar)
				{
				}
			});
		}
		return convertView;
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
