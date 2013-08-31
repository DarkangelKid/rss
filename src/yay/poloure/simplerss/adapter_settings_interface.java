package yay.poloure.simplerss;

import android.graphics.Color;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.support.v4.view.PagerTabStrip;

public class adapter_settings_interface extends BaseAdapter
{
	private final String storage;
	private final String[] title_array;
	private final String[] summary_array;
	public  final static String[] colours	= new String[]{"blue", "purple", "green", "orange", "red"};
	public  final static int[] colour_ints	= new int[]
	{
		Color.rgb(51, 181, 229), // blue
		Color.rgb(170, 102, 204), // purple
		Color.rgb(153, 204, 0), // green
		Color.rgb(255, 187, 51), // orange
		Color.rgb(255, 68, 68) // red
	};

	private static LayoutInflater inflater;

		TextView settings_heading;

	private static class settings_holocolour_holder
	{
		TextView title_view;
		TextView summary_view;
		ImageView blue_view;
		ImageView purple_view;
		ImageView green_view;
		ImageView yellow_view;
		ImageView red_view;
	}

	private static class settings_checkbox_holder
	{
		TextView title_view;
		TextView summary_view;
		CheckBox checkbox;
	}

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
		return 3;
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
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final int view_type = getItemViewType(position);
		final String setting_path = main.storage + main.SETTINGS + title_array[position] + main.TXT;
		switch(view_type)
		{
			/* This type is a heading. */
			case(0):
				if(convertView == null)
				{
					convertView = inflater.inflate(R.layout.settings_heading, parent, false);
					settings_heading = (TextView) convertView.findViewById(R.id.settings_heading);
				}

				settings_heading.setText(title_array[position]);
				break;

			/* This type is the colour selector. */
			case(1):
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

				String[] colour_array = utilities.read_file_to_array(storage + main.SETTINGS + main.PAGERTABSTRIPCOLOUR);

				if(colour_array.length == 0)
				{
					colour_array = new String[]{"blue"};
					utilities.append_string_to_file(storage + main.SETTINGS + main.PAGERTABSTRIPCOLOUR, "blue");
				}

				ImageView[] colour_views = new ImageView[]{holder.blue_view, holder.purple_view, holder.green_view, holder.yellow_view, holder.red_view};

				for(int i = 0; i < colour_views.length; i++)
				{
					colour_views[i].setOnClickListener(new colour_click(i));

					/* Set the alpha to fade out if it is not the currently selected colour. */
					if(colour_array[0].equals(colours[i]))
						colour_views[i].setAlpha(1.0f);
					else
						colour_views[i].setAlpha(0.5f);
				}
				break;

		/* This type is a checkbox setting. */
			default:
				final settings_checkbox_holder hold;
				if(convertView == null)
				{
					convertView = (View) inflater.inflate(R.layout.settings_checkbox, parent, false);
					hold = new settings_checkbox_holder();
					hold.title_view = (TextView) convertView.findViewById(R.id.check_title);
					hold.summary_view = (TextView) convertView.findViewById(R.id.check_summary);
					hold.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
					convertView.setTag(hold);
				}
				else
					hold = (settings_checkbox_holder) convertView.getTag();

				hold.title_view.setText(title_array[position]);
				hold.summary_view.setText(summary_array[position]);
				hold.checkbox.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						utilities.delete(setting_path);
						utilities.append_string_to_file(setting_path, Boolean.toString(((CheckBox) v).isChecked()));
					}
				});
				/* Load the saved boolean value and set the box as checked if true. */
				utilities.load_checkbox(hold.checkbox, setting_path);
		}
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
			utilities.delete(main.storage + main.SETTINGS + main.PAGERTABSTRIPCOLOUR);
			utilities.append_string_to_file(main.storage + main.SETTINGS + main.PAGERTABSTRIPCOLOUR, colours[clicked_colour]);
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
}
