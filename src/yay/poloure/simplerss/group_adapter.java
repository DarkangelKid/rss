package yay.poloure.simplerss;

import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.content.ClipData;
import android.view.View.DragShadowBuilder;

import android.view.DragEvent;
import android.view.View.OnDragListener;
import android.widget.LinearLayout;
import android.view.ViewManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import java.util.List;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Point;

import java.io.FileWriter;
import java.io.BufferedWriter;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class group_adapter extends BaseAdapter
{
	String old_title = "";
	String new_title = "";

	String long_press_title;

	private List<String> group_list = new ArrayList<String>();
	private List<String> info_list = new ArrayList<String>();

	LayoutInflater inflater;

	private final Context context;
	private ListView list_view;

	public group_adapter(Context context)
	{
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		list_view = ((ListView)(inflater.inflate(R.layout.manage_fragment, null)).findViewById(R.id.group_listview));
	}

	public void add_list(String new_group, String new_info)
	{
		group_list.add(new_group);
		info_list.add(new_info);
	}

	public void clear_list(){
		group_list = new ArrayList<String>();
		info_list = new ArrayList<String>();
	}
	public List<String> return_titles(){
		return group_list;
	}

	@Override
	public int getCount(){
		return group_list.size();
	}

	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public String getItem(int position){
		return group_list.get(position);
	}

	public void remove_item(int position)
	{
		group_list.remove(position);
		info_list.remove(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
			ViewHolder holder;
			if(convertView == null)
			{
				
				convertView = inflater.inflate(R.layout.manage_list_item, parent, false);
				holder = new ViewHolder();
				holder.group_view = (TextView) convertView.findViewById(R.id.group_item);
				holder.info_view = (TextView) convertView.findViewById(R.id.group_feeds);
				holder.image_view = (ImageView) convertView.findViewById(R.id.drag_image);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();

			

			holder.group_view.setText(group_list.get(position));
			holder.info_view.setText(info_list.get(position));
			if(position != 0)
			{
				holder.image_view.setOnTouchListener(new MyTouchListener());
				convertView.setOnDragListener(new MyDragListener());
			}
			else
				holder.image_view.setVisibility(View.INVISIBLE);
			
			return convertView;
	}

	static class ViewHolder
	{
		TextView group_view;
		TextView info_view;
		ImageView image_view;
	}

	public final class MyTouchListener implements OnTouchListener
	{
		public boolean onTouch(View view, MotionEvent motionEvent)
		{
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			{
				View view_parent = (View) view.getParent();
				old_title = ((TextView)view_parent.findViewById(R.id.group_item)).getText().toString();
				ClipData data = ClipData.newPlainText("", "");
				custom_drag_builder shadowBuilder = new custom_drag_builder(view_parent);
				view_parent.startDrag(data, shadowBuilder, view_parent, 0);
				return true;
			}
			else
				return false;
		}
	}

	private void refresh_data()
	{
		notifyDataSetChanged();
	}

	///Pish starts here
	class MyDragListener implements OnDragListener
	{
		@Override
		public boolean onDrag(View v, DragEvent event)
		{
			int action = event.getAction();
			switch (event.getAction())
			{
				case DragEvent.ACTION_DRAG_STARTED:
					break;
				case DragEvent.ACTION_DRAG_ENTERED:
					new_title = ((TextView) v.findViewById(R.id.group_item)).getText().toString();
					v.setVisibility(View.INVISIBLE);
					rearrange_groups(old_title, new_title);
					refresh_data();
					/// Save the new order by overwriting the group_list.txt file with group_list, then call the update_groups() function.
					break;
				case DragEvent.ACTION_DRAG_EXITED:        
					v.setVisibility(View.VISIBLE);
					break;
				case DragEvent.ACTION_DROP:
					v.setVisibility(View.VISIBLE);
					//List<String> new_l = group_list;
					//new_l.add(0, "All");
					main_view.update_group_order(group_list);
					break;
				case DragEvent.ACTION_DRAG_ENDED:
					default:
					break;
			}
			return true;
		}
	}

	private void rearrange_groups(String previous, String next)
	{
		int i = 0;
		while(!previous.equals(group_list.get(i))){
			i++;
		}
		int j = 0;
		while(!next.equals(group_list.get(j))){
			j++;
		}
		String old_info = info_list.get(i);
		String old = group_list.get(i);

		info_list.set(i, info_list.get(j));
		group_list.set(i, group_list.get(j));

		info_list.set(j, old_info);
		group_list.set(j, old);
	}

	class custom_drag_builder extends View.DragShadowBuilder
	{
		private View view_store;

		private custom_drag_builder(View v) {
			super(v);
			view_store = v;
		}

		@Override
		public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint)
		{
			shadowSize.x = view_store.getWidth();
			shadowSize.y = view_store.getHeight();

			shadowTouchPoint.x = (int)(shadowSize.x * 19 / 20);
			shadowTouchPoint.y = (int)(shadowSize.y / 2);
		}
	}
	
	private void slog(String string)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter("/storage/emulated/0/Android/data/yay.poloure.simplerss/files/dump.txt", true));
			out.write(string + "\n");
			out.close();
		}
		catch (Exception e)
		{
		}
	}
} 
