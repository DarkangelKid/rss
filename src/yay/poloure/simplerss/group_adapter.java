package yay.poloure.simplerss;

import android.content.ClipData;
import android.content.Context;

import android.graphics.Point;

import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;

import java.util.List;
import java.util.ArrayList;

public class group_adapter extends BaseAdapter
{
	private String old_title = "";
	private String new_title = "";

	private String[] group_array = new String[0];
	private String[] info_array = new String[0];

	private static LayoutInflater inflater;

	public group_adapter(Context context)
	{
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void set_items(String[] new_groups, String[] new_infos)
	{
		group_array = new_groups;
		info_array = new_infos;
	}

	public String[] return_titles()
	{
		return group_array;
	}

	@Override
	public int getCount()
	{
		return group_array.length;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public String getItem(int position)
	{
		return group_array[position];
	}

	public void remove_item(int position)
	{
		utilities.remove_element(group_array, position);
		utilities.remove_element(info_array, position);
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

			holder.group_view.setText(group_array[position]);
			holder.info_view.setText(info_array[position]);
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

	private class MyTouchListener implements OnTouchListener
	{
		public boolean onTouch(View view, MotionEvent motionEvent)
		{
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
			{
				View view_parent = (View) view.getParent();
				old_title = ((TextView)view_parent.findViewById(R.id.group_item)).getText().toString();
				custom_drag_builder shadowBuilder = new custom_drag_builder(view_parent);
				view_parent.startDrag(null, shadowBuilder, view_parent, 0);
				return true;
			}
			return false;
		}
	}

	private class MyDragListener implements OnDragListener
	{
		int[] position = new int[2];
		int old_view;
		final int height = (int) (main_view.activity_context.getResources().getDisplayMetrics().heightPixels);
		@Override
		public boolean onDrag(View v, DragEvent event)
		{
			final int action = event.getAction();
			if(action == DragEvent.ACTION_DRAG_ENTERED)
			{
				final ListView listview = ((ListView) v.getParent());
				new_title = ((TextView) v.findViewById(R.id.group_item)).getText().toString();
				rearrange_groups(old_title, new_title);
				notifyDataSetChanged();
				for(int i = 0; i < listview.getChildCount();  i++)
				{
					View temp = listview.getChildAt(i);
					if(temp != null)
					{
						if(temp == v)
						{
							Animation fadeOut = new AlphaAnimation(1, 0);
							fadeOut.setDuration(210);
							fadeOut.setInterpolator(new DecelerateInterpolator());
							temp.setAnimation(fadeOut);
							temp.setVisibility(View.INVISIBLE);
							old_view = temp.hashCode();
						}
						else if(temp.hashCode() == old_view)
						{
							Animation fadeIn = new AlphaAnimation(0, 1);
							fadeIn.setDuration(210);
							fadeIn.setInterpolator(new DecelerateInterpolator());
							temp.setAnimation(fadeIn);
							temp.setVisibility(View.VISIBLE);
						}
						else
							temp.setVisibility(View.VISIBLE);
					}
				}
				v.getLocationOnScreen(position);
				int first = listview.getFirstVisiblePosition();

				if(position[1] > (4/5.0)*height)
					listview.smoothScrollBy(v.getHeight(), 400);
				else if(position[1] < (1/5.0)*height)
					listview.smoothScrollBy((int)((-1.0)* v.getHeight()), 400);
			}
			else if(action == DragEvent.ACTION_DROP)
			{
				Animation fadeIn2 = new AlphaAnimation(0, 1);
				fadeIn2.setDuration(210);
				fadeIn2.setInterpolator(new DecelerateInterpolator());
				v.setAnimation(fadeIn2);
				v.setVisibility(View.VISIBLE);
				main_view.update_group_order(group_array);
			}
			return true;
		}
	}

	private void rearrange_groups(String previous, String next)
	{
		int i = 0;
		while(!previous.equals(group_array[i])){
			i++;
		}
		int j = 0;
		while(!next.equals(group_array[j])){
			j++;
		}
		String old_info = info_array[i];
		String old 		= group_array[i];

		info_array[i] 	= info_array[j];
		group_array[i] 	= group_array[j];

		info_array[j]	= old_info;
		group_array[j]	= old;
	}

	private class custom_drag_builder extends View.DragShadowBuilder
	{
		private final View view_store;

		private custom_drag_builder(View v)
		{
			super(v);
			view_store = v;
		}

		@Override
		public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint)
		{
			shadowSize.x = view_store.getWidth();
			shadowSize.y = view_store.getHeight();

			shadowTouchPoint.x = (int) (shadowSize.x * 19.0 / 20);
			shadowTouchPoint.y = (shadowSize.y / 2);
		}
	}
}
