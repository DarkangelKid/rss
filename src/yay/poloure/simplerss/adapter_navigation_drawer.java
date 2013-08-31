package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class adapter_navigation_drawer extends BaseAdapter
{
	private static String[] menu_array  = new String[0];
	private static int[]    count_array = new int[0];
	private int twelve;
	private static int[]    title_array = new int[]{R.drawable.feeds, R.drawable.manage, R.drawable.feeds};

	private static LayoutInflater inflater;

	private TextView main_item;

	private static class divider
	{
		TextView title;
		ImageView divider_view;
	}

	private static class group_item
	{
		TextView title;
		TextView unread_view;
	}

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
	public int getViewTypeCount()
	{
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

		switch(view_type)
		{
			/* This view is for the main items Feeds, Manage, & Settings. */
			case(0):
				if(convertView == null)
				{
					convertView = inflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
					main_item = (TextView) convertView.findViewById(R.id.menu_item);
				}

				main_item.setText(navigation_drawer.NAVIGATION_TITLES[position]);

				/* Set the item's image as a CompoundDrawable of the textview. */
				main_item.setCompoundDrawablesWithIntrinsicBounds(title_array[position], 0, 0, 0);
				main_item.setCompoundDrawablePadding(twelve);
				break;

			/* This view is for the divider and "Groups" subtitle. The imageview divider is below the subtitle. */
			case(1):
				divider holder;
				if(convertView == null)
				{
					convertView = inflater.inflate(R.layout.navigation_drawer_subtitle_divider, parent, false);
					holder = new divider();
					holder.title = (TextView) convertView.findViewById(R.id.title_item);
					holder.divider_view = (ImageView) convertView.findViewById(R.id.divider_item);
					convertView.setTag(holder);
				}
				else
					holder = (divider) convertView.getTag();
				break;

			/* This view is for the group items of the navigation drawer. The one with unread counters. */
			default:
				group_item holder2;
				if(convertView == null)
				{
					convertView = inflater.inflate(R.layout.navigation_drawer_group_item, parent, false);
					holder2 = new group_item();
					holder2.title = (TextView) convertView.findViewById(R.id.group_title);
					holder2.unread_view = (TextView) convertView.findViewById(R.id.unread_item);
					convertView.setTag(holder2);
				}
				else
					holder2 = (group_item) convertView.getTag();

				holder2.title.setText(menu_array[position - 4]);
				String number = Integer.toString(count_array[position - 4]);
				holder2.unread_view.setText((number.equals("0")) ? "" : number);
		}
		return convertView;
	}
}
