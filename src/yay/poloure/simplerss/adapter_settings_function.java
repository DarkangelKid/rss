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
	private static final List<String> menu_list = new ArrayList<String>();
	private static final List<Integer> count_list = new ArrayList<Integer>();

	private static LayoutInflater inflater;

	public adapter_settings_function(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void add_list(List<String> new_title)
	{
		menu_list.clear();
		menu_list.addAll(new_title);
	}

	public void add_count(List<Integer> new_count)
	{
		count_list.clear();
		count_list.addAll(new_count);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public String getItem(int position)
	{
		return menu_list.get(position);
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
		if(position == 0)
			return 0;

		else if(position == 1)
			return 1;

		else
			return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final int view_type = getItemViewType(position);
		/* This view is for the main items Feeds, Manage, & Settings. */
		if(view_type == 0)
		{
			group_item_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_heading, parent, false);
				holder = new group_item_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.settings_heading);
				convertView.setTag(holder);
			}
			else
				holder = (group_item_holder) convertView.getTag();

			holder.title_view.setText("penis");
		}

		else if(view_type == 1)
		{
			main_item_holder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_checkbox, parent, false);
				holder = new main_item_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.check_title);
				convertView.setTag(holder);
			}
			else
				holder = (main_item_holder) convertView.getTag();

			holder.title_view.setText("penis");
		}
		else
		{
			TextViewHolder holder;
			if(convertView == null)
			{
				convertView = (View) inflater.inflate(R.layout.settings_seekbar, parent, false);
				holder = new TextViewHolder();
				holder.title_view = (TextView) convertView.findViewById(R.id.seek_title);
				convertView.setTag(holder);
			}
			else
				holder = (TextViewHolder) convertView.getTag();

			holder.title_view.setText("boobs");
		}
		return convertView;
	}

	static class group_item_holder
	{
		TextView title_view;
	}

	static class main_item_holder
	{
		TextView title_view;
		ImageView divider_view;
	}

	static class TextViewHolder
	{
		TextView title_view;
		TextView unread_view;
	}

}
