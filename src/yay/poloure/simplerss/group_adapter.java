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
import java.util.List;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Point;

public class group_adapter extends BaseAdapter
{
	private List<String> group_list = new ArrayList();

	LayoutInflater inflater;

	private final Context context;

	public group_adapter(Context context)
	{
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void add_list(String new_group)
	{
		group_list.add(new_group);
	}

	public void clear_list(){
		group_list = new ArrayList();
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
			ViewHolder holder;
			if(convertView == null)
			{
				convertView = inflater.inflate(R.layout.manage_list_item, parent, false);
				holder = new ViewHolder();
				holder.group_view = (TextView) convertView.findViewById(R.id.group_item);
				holder.image_view = (ImageView) convertView.findViewById(R.id.drag_image);
				convertView.setTag(holder);
			}
			else
				holder = (ViewHolder) convertView.getTag();

			holder.group_view.setText(group_list.get(position));
			holder.image_view.setOnTouchListener(new MyTouchListener());
			
			return convertView;
	}

	static class ViewHolder
	{
		TextView group_view;
		ImageView image_view;
	}

	public final class MyTouchListener implements OnTouchListener
	{
		
		public boolean onTouch(View view, MotionEvent motionEvent)
		{
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			{
				View view_parent = (View) view.getParent();
				ClipData data = ClipData.newPlainText("", "");
				custom_drag_builder shadowBuilder = new custom_drag_builder(view_parent);
				view_parent.startDrag(data, shadowBuilder, view_parent, 0);
				view_parent.setVisibility(View.INVISIBLE);
				return true;
			}
			else {
				return false;
			}
		  }
	}

	///Pish starts here
	/*class MyDragListener implements OnDragListener
	{
		@Override
		public boolean onDrag(View v, DragEvent event)
		{
			String old_title;
			String new_title;
			View old_view;
			int action = event.getAction();
			switch (event.getAction())
			{
				case DragEvent.ACTION_DRAG_STARTED:
					// Do nothing
					break;
				case DragEvent.ACTION_DRAG_ENTERED:
					new_title = ((TextView)v.findViewById(R.id.group_item)).getText().toString();
					v.setVisibility(View.INVISIBLE);
					((TextView)old_view.findViewById(R.id.group_item)).setText(new_title);
					old_view.setVisibility(View.VISIBLE);
					break;
				case DragEvent.ACTION_DRAG_EXITED:        
					old_view = v;
					//v.setVisibility(View.VISIBLE);
					break;
				case DragEvent.ACTION_DROP:
					// Dropped, reassign View to ViewGroup
					View view = (View) event.getLocalState();
					ViewGroup owner = (ViewGroup) view.getParent();
					owner.removeView(view);
					LinearLayout container = (LinearLayout) v;
					container.addView(view);
					view.setVisibility(View.VISIBLE);
					break;
				case DragEvent.ACTION_DRAG_ENDED:
					default:
					break;
			}
			return true;
		}
	}*/

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
} 
