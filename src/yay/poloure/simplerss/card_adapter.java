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

	private static List<String> content_titles = new ArrayList();
	private static List<String> content_des = new ArrayList();
	private static List<String> content_times = new ArrayList();
	LayoutInflater inflater;

	private final Context context;

	public card_adapter(Context context) {
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public static void add_list(List<String> new_titles, List<String> new_des, List<String> new_times)
	{
		boolean exists = false;
		for(int j=0; j<new_titles.size(); j++)
		{
			for(int i=0; i<content_titles.size(); i++)
			{
				if(content_titles.get(i).equals(new_titles.get(j)))
				{
					exists = true;
					break;
				}
			}
			if(!exists)
			{
				content_titles.add(new_titles.get(j));
				content_des.add(new_des.get(j));
				content_times.add(new_times.get(j));
			}
			exists = false;
		}
	}

	@Override
	public int getCount() {
		return content_titles.size();
	}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public String getItem(int position){
		return "hey";
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
			ViewHolder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.card_layout, parent, false);
				holder = new ViewHolder();
				holder.title_view = (TextView) convertView.findViewById(R.id.title);
				holder.time_view = (TextView) convertView.findViewById(R.id.time);
				holder.description_view = (TextView) convertView.findViewById(R.id.description);
				holder.image_view = (ImageView) convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			}
			else{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.title_view.setText(content_titles.get(position));
			holder.time_view.setText(content_times.get(position));
			holder.description_view.setText(content_des.get(position));
			holder.image_view.setImageResource(R.drawable.ok);
			
			return convertView;
	}

	static class ViewHolder
	{
		TextView title_view;
		TextView time_view;
		TextView description_view;
		ImageView image_view;
	}
} 
