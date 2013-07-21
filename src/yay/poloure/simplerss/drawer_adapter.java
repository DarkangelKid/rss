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

public class drawer_adapter extends BaseAdapter
{
	private static final List<String> menu_list = new ArrayList<String>();
	private static final List<Integer> count_list = new ArrayList<Integer>();
	private static int twelve;

	private static LayoutInflater inflater;

	public drawer_adapter(Context context_main)
	{
		inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		twelve = (int) ((12 * (context_main.getResources().getDisplayMetrics().density) + 0.5f));
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
	public long getItemId(int position){
		return position;
	}

	@Override
	public String getItem(int position){
		return menu_list.get(position);
	}

	@Override
	public int getCount(){
		return menu_list.size();
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
		int view_type = getItemViewType(position);
		View view;
		if(view_type == 0)
		{
			StaticViewHolder holder;
			if(convertView == null)
			{
				View view_group = (View) inflater.inflate(R.layout.static_drawer_item, parent, false);
				holder = new StaticViewHolder();
				holder.title_view = (TextView) view_group.findViewById(R.id.menu_item);
				view_group.setTag(holder);
				view = view_group;
			}
			else
			{
				view = convertView;
				holder = (StaticViewHolder) convertView.getTag();
			}

			holder.title_view.setText(menu_list.get(position));

			if(position == 0)
				holder.title_view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.feeds, 0, 0, 0);
			else if(position == 1)
				holder.title_view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.manage, 0, 0, 0);
			else
				holder.title_view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings, 0, 0, 0);

			holder.title_view.setCompoundDrawablePadding(twelve);
		}
		else if(view_type == 1)
		{
			///return a header view
			HeaderViewHolder holder;
			if(convertView == null)
			{
				ViewGroup view_group = (ViewGroup)inflater.inflate(R.layout.header_drawer_item, parent, false);
				holder = new HeaderViewHolder();
				holder.title_view = (TextView) view_group.findViewById(R.id.title_item);
				holder.divider_view = (ImageView) view_group.findViewById(R.id.divider_item);
				view_group.setTag(holder);
				view = view_group;
			}
			else
			{
				view = convertView;
				holder = (HeaderViewHolder) convertView.getTag();
			}
			holder.title_view.setText(menu_list.get(position));
			holder.divider_view.setImageResource(R.drawable.drawer_divider);
		}
		else
		{
			///return a feed group view
			TextViewHolder holder;
			if(convertView == null)
			{
				ViewGroup view_group = (ViewGroup)inflater.inflate(R.layout.group_drawer_item, parent, false);
				holder = new TextViewHolder();
				holder.title_view = (TextView) view_group.findViewById(R.id.group_title);
				holder.unread_view = (TextView) view_group.findViewById(R.id.unread_item);
				view_group.setTag(holder);
				view = view_group;
			}
			else
			{
				view = convertView;
				holder = (TextViewHolder) convertView.getTag();
			}
			holder.title_view.setText(menu_list.get(position));
			String number = Integer.toString(count_list.get(position - 4));
			holder.unread_view.setText((number.equals("0")) ? "" : number);
		}
		return view;
	}

	static class StaticViewHolder
	{
		TextView title_view;
	}

	static class HeaderViewHolder
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
