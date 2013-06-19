package yay.poloure.simplerss;

import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.content.ClipData;
import android.view.View.DragShadowBuilder;

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
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view_parent);
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
		Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
		Drawable normalShape = getResources().getDrawable(R.drawable.shape);
		
		@Override
		public boolean onDrag(View v, DragEvent event)
		{
			int action = event.getAction();
			switch (event.getAction())
			{
				case DragEvent.ACTION_DRAG_STARTED:
					// Do nothing
					break;
				case DragEvent.ACTION_DRAG_ENTERED:
					v.setBackgroundDrawable(enterShape);
					break;
				case DragEvent.ACTION_DRAG_EXITED:        
					v.setBackgroundDrawable(normalShape);
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
					v.setBackgroundDrawable(normalShape);
					default:
					break;
			}
			return true;
		}
	} */
} 
