package yay.poloure.simplerss;

import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import java.util.List;

public class adapter_manage_filter extends BaseAdapter
{
	private static String[] title_array = new String[0];
	private static String[] info_array = new String[0];

	private static LayoutInflater inflater;

	public adapter_manage_filter(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void set_items(String[] new_titles, String[] new_infos)
	{
		title_array = new_titles;
		info_array = new_infos;
	}

	public void set_position(int pos, String new_title, String new_info)
	{
		title_array[pos]	= new_title;
		info_array[pos]		= new_info;
	}

	@Override
	public int getCount()
	{
		return title_array.length;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	public void remove_item(int position)
	{
		title_array	= utilities.remove_element(title_array, position);
		info_array	= utilities.remove_element(info_array, position);
	}

	@Override
	public String getItem(int position)
	{
		return title_array[position];
	}

	public String get_info(int position)
	{
		return info_array[position];
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
			ViewHolder holder;
			if(convertView == null)
			{

				convertView = inflater.inflate(R.layout.feed_list_item, parent, false);
				holder = new ViewHolder();
				holder.title_view = (TextView) convertView.findViewById(R.id.title_item);
				holder.info_view = (TextView) convertView.findViewById(R.id.info_item);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();

			holder.title_view.setText(title_array[position]);
			holder.info_view.setText(info_array[position]);

			return convertView;
	}

	static class ViewHolder
	{
		TextView title_view;
		TextView info_view;
	}

}
