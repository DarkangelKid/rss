package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class adapter_navigation_drawer extends BaseAdapter
{
	private static String[] menu_array  = new String[0];
	private static int[]    count_array = new int[0];
	private int twelve;

	private static LayoutInflater inflater;

	public adapter_navigation_drawer(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		twelve = (int) ((12 * (context_main.getResources().getDisplayMetrics().density) + 0.5f));
	}

	public void set_titles(String[] new_titles)
	{
		menu_array = new_titles;
	}

	public void set_counts(int[] new_counts)
	{
		count_array = new_counts;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public String getItem(int position)
	{
		return menu_array[position];
	}

	@Override
	public int getCount()
	{
		return menu_array.length + 4;
	}

	@Override
	public boolean isEnabled(int position)
	{
		return position != 3;
	}

	@Override
	public int getViewTypeCount(){
		return 3;
	}

	@Override
	public int getItemViewType(int position)
	{
		if(position < 3)
			return 0;

		else if(position == 3)
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
				convertView = inflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
				holder = new group_item_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.menu_item);
				convertView.setTag(holder);
			}
			else
				holder = (group_item_holder) convertView.getTag();

			/* Set the text for the item. TODO: This should be a static array with the localised names.*/
			holder.title_view.setText(main.NAVIGATION_TITLES[position]);

			/* Set the item's image as a CompoundDrawable of the textview. */
			switch(position)
			{
				case(0):
				{
					holder.title_view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.feeds, 0, 0, 0);
					break;
				}
				case(1):
				{
					holder.title_view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.manage, 0, 0, 0);
					break;
				}
				case(2):
					holder.title_view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings, 0, 0, 0);
			}
			holder.title_view.setCompoundDrawablePadding(twelve);
		}
		/* This view is for the divider and "Groups" subtitle. The imageview divider is below the subtitle. */
		else if(view_type == 1)
		{
			main_item_holder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.navigation_drawer_subtitle_divider, parent, false);
				holder = new main_item_holder();
				holder.title_view = (TextView) convertView.findViewById(R.id.title_item);
				holder.divider_view = (ImageView) convertView.findViewById(R.id.divider_item);
				convertView.setTag(holder);
			}
			else
				holder = (main_item_holder) convertView.getTag();
		}
		/* This view is for the group items of the navigation drawer. The one with unread counters. */
		else
		{
			TextViewHolder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.navigation_drawer_group_item, parent, false);
				holder = new TextViewHolder();
				holder.title_view = (TextView) convertView.findViewById(R.id.group_title);
				holder.unread_view = (TextView) convertView.findViewById(R.id.unread_item);
				convertView.setTag(holder);
			}
			else
				holder = (TextViewHolder) convertView.getTag();

			holder.title_view.setText(menu_array[position - 4]);
			String number = Integer.toString(count_array[position - 4]);
			holder.unread_view.setText((number.equals("0")) ? "" : number);
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
