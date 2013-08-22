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
	private static List<String> title_list = new ArrayList<String>();
	//private static String[] info_array = new String[0];

	private static LayoutInflater inflater;

	public adapter_manage_filter(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void set_items(List<String> new_titles)
	{
		title_list = new_titles;
	}

	@Override
	public int getCount()
	{
		return title_list.size();
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	public void remove_item(int position)
	{
		title_list.remove(position);
	}

	@Override
	public String getItem(int position)
	{
		return title_list.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
			ViewHolder holder;
			if(convertView == null)
			{

				convertView = inflater.inflate(R.layout.manage_feed_item, parent, false);
				holder = new ViewHolder();
				holder.title_view = (TextView) convertView.findViewById(R.id.title_item);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();

			holder.title_view.setText(title_list.get(position));

			return convertView;
	}

	static class ViewHolder
	{
		TextView title_view;
	}

}
