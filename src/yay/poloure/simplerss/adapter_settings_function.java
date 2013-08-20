package yay.poloure.simplerss;

import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import java.util.List;
import android.widget.ImageView;

public class adapter_settings_function extends BaseAdapter
{
	private final String[] title_array;
	private final String[] summary_array;

	private static LayoutInflater inflater;

	public adapter_settings_function(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		title_array = context_main.getResources().getStringArray(R.array.settings_function_titles);
		summary_array = context_main.getResources().getStringArray(R.array.settings_function_summaries);
	}

	/*public void add_list(List<String> new_title)
	{
		title_array.clear();
		title_array.addAll(new_title);
	}

	public void add_count(List<Integer> new_count)
	{
		summary_array.clear();
		summary_array.addAll(new_count);
	}*/
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
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final int view_type = getItemViewType(position);
		if(view_type == 0)
		{
			settings_heading_holder holder;
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
			settings_checkbox_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_checkbox, parent, false);
				holder = new settings_checkbox_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.check_title);
				holder.summary_view = (TextView) convertView.findViewById(R.id.check_summary);
				convertView.setTag(holder);
			}
			else
				holder = (settings_checkbox_holder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
			holder.summary_view.setText(summary_array[position]);
		}
		else
		{
			settings_seekbar_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_seekbar, parent, false);
				holder = new settings_seekbar_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.seek_title);
				holder.summary_view = (TextView) convertView.findViewById(R.id.seek_summary);
				convertView.setTag(holder);
			}
			else
				holder = (settings_seekbar_holder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
			holder.summary_view.setText(summary_array[position]);
		}
		return convertView;
	}

	static class settings_heading_holder
	{
		TextView title_view;
	}

	static class settings_checkbox_holder
	{
		TextView title_view;
		TextView summary_view;
	}

	static class settings_seekbar_holder
	{
		TextView title_view;
		TextView summary_view;
	}

}
