package yay.poloure.simplerss;

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
		return 3;
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
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
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
		}
		/* So far there are only 2 view types, heading and holocolour_select. This will change */
		else
		{
			final settings_checkbox_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_checkbox, parent, false);
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
					String file_name = title_array[position];
					///PAULTODO
				}
			});
			holder.checkbox.setChecked(false);
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

	static class settings_checkbox_holder
	{
		TextView title_view;
		TextView summary_view;
		CheckBox checkbox;
	}
}
