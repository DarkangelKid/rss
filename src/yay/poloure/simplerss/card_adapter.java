package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;

public class card_adapter extends BaseAdapter
{
	private static List<String> all_content_titles = new ArrayList();
	private static List<String> all_content_des = new ArrayList();
	private static List<String> all_content_links = new ArrayList();

	private List<String> content_titles = new ArrayList();
	private List<String> content_des = new ArrayList();
	private List<String> content_links = new ArrayList();
	LayoutInflater inflater;

	private final Context context;

	public card_adapter(Context context)
	{
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void add_list(List<String> new_titles, List<String> new_des, List<String> new_links)
	{
		content_titles = new_titles;
		content_des = new_des;
		content_links = new_links;
	}
	
	public void add_static_list(List<String> new_titless, List<String> new_dess, List<String> new_linkss)
	{
		all_content_titles.addAll(new_titless);
		all_content_des.addAll(new_dess);
		all_content_links.addAll(new_linkss);
		content_titles = all_content_titles;
		content_des = all_content_des;
		content_links = all_content_links;
	}

	public void clear_static_list(){
		all_content_titles = new ArrayList();
		all_content_des = new ArrayList();
		all_content_links = new ArrayList();
	}

	@Override
	public int getCount(){
		return content_titles.size();
	}

	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public String getItem(int position){
		return "hey";
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		/*if((position == 0))
		{
			 View fake_card = (inflater.inflate(R.layout.dummy_card_layout, parent, false));
			 ((TextView)fake_card.findViewById(R.id.divider_text)).setText("cocks");
			 return fake_card;
		}
		else
		{*/
			ViewHolder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.card_layout, parent, false);
				holder = new ViewHolder();
				holder.title_view = (TextView) convertView.findViewById(R.id.title);
				holder.time_view = (TextView) convertView.findViewById(R.id.time);
				holder.description_view = (TextView) convertView.findViewById(R.id.description);
				//holder.image_view = (ImageView) convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();

			holder.title_view.setText(content_titles.get(position));
			holder.time_view.setText(content_links.get(position));
			holder.description_view.setText(content_des.get(position));
			//holder.image_view.setImageResource(R.drawable.ok);
			
			return convertView;
		//}
	}

	static class ViewHolder
	{
		TextView title_view;
		TextView time_view;
		TextView description_view;
		ImageView image_view;
	}
} 
