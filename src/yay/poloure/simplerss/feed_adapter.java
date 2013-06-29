package yay.poloure.simplerss;

import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import java.util.List;
import android.widget.ListView;

public class feed_adapter extends BaseAdapter
{
	private List<String> title_list = new ArrayList<String>();
	private List<String> info_list = new ArrayList<String>();
	
	LayoutInflater inflater;
	
	private final Context context;
	
	public feed_adapter(Context context_main)
	{
		context = context_main;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void add_list(String new_title, String new_info)
	{
		title_list.add(new_title);
		info_list.add(new_info);
	}
	
	public void clear_list(){
		title_list = new ArrayList();
	}
	
	public List<String> return_titles(){
		return title_list;
	}
	
	@Override
	public int getCount(){
		return title_list.size();
	}
	
	@Override
	public long getItemId(int position){
		return position;
	}
	
	@Override
	public String getItem(int position){
		return title_list.get(position);
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

			holder.title_view.setText(title_list.get(position));
			holder.info_view.setText(info_list.get(position));
			
			//convertView.setOnLongClickListener(new long_press_listener());
			
			return convertView;
	}

	static class ViewHolder
	{
		TextView title_view;
		TextView info_view;
	}

}
