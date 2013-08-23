package yay.poloure.simplerss;

import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.support.v4.view.PagerTabStrip;

public class adapter_settings_interface extends BaseAdapter
{
	private final String[] title_array;
	private final String[] summary_array;
	private final static String[] colours = new String[]{"blue", "purple", "green", "orange", "red"};
	private final String storage;

	private static LayoutInflater inflater;

	public adapter_settings_interface(Context context_main, String stor)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		title_array = context_main.getResources().getStringArray(R.array.settings_interface_titles);
		summary_array = context_main.getResources().getStringArray(R.array.settings_interface_summaries);
		storage = stor;
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
		return 2;
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
		if(view_type == 0)
		{
			final settings_heading_holder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.settings_heading, parent, false);
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
			final settings_holocolour_holder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.settings_holocolour_select, parent, false);
				holder = new settings_holocolour_holder();
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
				holder = (settings_holocolour_holder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
			holder.summary_view.setText(summary_array[position]);
			holder.blue_view.setOnClickListener(new colour_click(0));
			holder.purple_view.setOnClickListener(new colour_click(1));
			holder.green_view.setOnClickListener(new colour_click(2));
			holder.yellow_view.setOnClickListener(new colour_click(3));
			holder.red_view.setOnClickListener(new colour_click(4));

			holder.blue_view.setAlpha(0.5f);
			holder.purple_view.setAlpha(0.5f);
			holder.green_view.setAlpha(0.5f);
			holder.yellow_view.setAlpha(0.5f);
			holder.red_view.setAlpha(0.5f);

			String[] colour_array = utilities.read_file_to_array(storage + main.SETTINGS + main.SEPAR + main.PAGERTABSTRIPCOLOUR);

			if(colour_array[0].equals("blue"))
				holder.blue_view.setAlpha(1.0f);
			else if(colour_array[0].equals("purple"))
				holder.purple_view.setAlpha(1.0f);
			else if(colour_array[0].equals("green"))
				holder.green_view.setAlpha(1.0f);
			else if(colour_array[0].equals("yellow"))
				holder.yellow_view.setAlpha(1.0f);
			else
				holder.red_view.setAlpha(1.0f);
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

	private class colour_click implements View.OnClickListener
	{
		final int clicked_colour;

		public colour_click(int colour)
		{
			clicked_colour = colour;
		}

		@Override
		public void onClick(View v)
		{
			utilities.delete(main.storage + main.SETTINGS + main.SEPAR + main.PAGERTABSTRIPCOLOUR);
			utilities.append_string_to_file(main.storage + main.SETTINGS + main.SEPAR + main.PAGERTABSTRIPCOLOUR, colours[clicked_colour]);
			View parent = (View) v.getParent();
			(parent.findViewById(R.id.blue_image)).setAlpha(0.5f);
			(parent.findViewById(R.id.purple_image)).setAlpha(0.5f);
			(parent.findViewById(R.id.green_image)).setAlpha(0.5f);
			(parent.findViewById(R.id.yellow_image)).setAlpha(0.5f);
			(parent.findViewById(R.id.red_image)).setAlpha(0.5f);
			(v).setAlpha(1.0f);
			/* Set the new colour. */
			for(PagerTabStrip strip : main.strips)
				utilities.set_pagertabstrip_colour(main.storage, strip);
		}
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
