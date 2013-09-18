package yay.poloure.simplerss;

import android.graphics.Point;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;

public class adapter_manage_tags extends BaseAdapter
{
   String old_title = "";
   String new_title = "";

   static String[] tag_array = new String[0];
   static String[] info_array = new String[0];

   public adapter_manage_tags()
   {
   }

   void set_items(String[] new_tags, String[] new_infos)
   {
      tag_array = new_tags;
      info_array = new_infos;
   }

   @Override
   public int getCount()
   {
      return tag_array.length;
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public String getItem(int position)
   {
      return tag_array[position];
   }

   @Override
   public int getViewTypeCount(){
      return 2;
   }

   /*@Override
   public int getItemViewType(int position)
   {
      if(main.HONEYCOMB)
         return 0;

      else
         return 1;
   }*/


   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      /*int view_type = getItemViewType(position);
      if(view_type == 0)*/
      {
         ViewHolder holder;
         if(convertView == null)
         {
            convertView = util.get_inflater().inflate(R.layout.manage_group_item, parent, false);
            holder = new ViewHolder();
            holder.tag_view = (TextView) convertView.findViewById(R.id.tag_item);
            holder.info_view = (TextView) convertView.findViewById(R.id.tag_feeds);
            holder.image_view = (ImageView) convertView.findViewById(R.id.drag_image);
            convertView.setTag(holder);
         }
         else
            holder = (ViewHolder) convertView.getTag();

         holder.tag_view.setText(tag_array[position]);
         holder.info_view.setText(info_array[position]);
         if( position != 0 && main.HONEYCOMB )
         {
            holder.image_view.setVisibility(View.VISIBLE);
            holder.image_view.setOnTouchListener(new MyTouchListener());
            convertView.setOnDragListener(new MyDragListener());
         }
         else
            holder.image_view.setVisibility(View.GONE);


         return convertView;
      }
      /*else
      {
         OldViewHolder holder;
         if(convertView == null)
         {
            convertView = inflater.inflate(R.layout.old_manage_list_item, parent, false);
            holder = new OldViewHolder();
            holder.tag_view = (TextView) convertView.findViewById(R.id.tag_item);
            holder.info_view = (TextView) convertView.findViewById(R.id.tag_feeds);
            holder.up_image_view = (ImageView) convertView.findViewById(R.id.up_drag_image);
            holder.down_image_view = (ImageView) convertView.findViewById(R.id.down_drag_image);
            convertView.setTag(holder);
         }
         else
            holder = (OldViewHolder) convertView.getTag();

         holder.tag_view.setText(tag_array[position]);
         holder.info_view.setText(info_array[position]);
         if(position != 0)
         {
            holder.up_image_view.setVisibility(View.VISIBLE);
            holder.up_image_view.setOnClickListener(new up_click_listener());
            holder.down_image_view.setVisibility(View.VISIBLE);

         }
         else
         {
            holder.up_image_view.setVisibility(View.GONE);
            holder.down_image_view.setVisibility(View.GONE);
         }
         return convertView;
      }*/
   }

   static class ViewHolder
   {
      TextView tag_view;
      TextView info_view;
      ImageView image_view;
   }

   /*static class OldViewHolder
   {
      TextView tag_view;
      TextView info_view;
      ImageView up_image_view;
      ImageView down_image_view;
   }*/

   /*private class up_click_listener implements View.OnClickListener
   {
      @Override
      public void onClick(View v)
      {
         View card = (View)v.getParent();
         ListView list = (ListView)card.getParent();

         Animation fadeOut = new AlphaAnimation(1, 0);
         fadeOut.setDuration(210);
         fadeOut.setInterpolator(new DecelerateInterpolator());
         card.setAnimation(fadeOut);
         card.setVisibility(View.INVISIBLE);

         String new_title = ((TextView) card.findViewById(R.id.tag_item)).getText().toString();
         String old_title = null;
         int pos = 0;
         for(int i = 0; i < tag_array.length; i++)
         {
            if(tag_array[i].equals(new_title))
            {
               pos = i - 1;
               old_title = tag_array[pos];
               break;
            }
         }

         View up_card = list.getChildAt(pos);
         up_card.setAnimation(fadeOut);
         up_card.setVisibility(View.INVISIBLE);
         rearrange_tags(old_title, new_title);
         notifyDataSetChanged();

         Animation fadeIn = new AlphaAnimation(0, 1);
         fadeIn.setDuration(210);
         fadeIn.setInterpolator(new DecelerateInterpolator());
         card.setAnimation(fadeIn);
         up_card.setAnimation(fadeIn);
         card.setVisibility(View.VISIBLE);
         up_card.setVisibility(View.VISIBLE);

         write.collection(main.GROUP_LIST, tag_array);
         util.update_tags();
      }
   }*/

   class MyTouchListener implements OnTouchListener
   {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent)
      {
         if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
         {
            View parent = (View) view.getParent();
            old_title = util.getstr(parent, R.id.tag_item);
            custom_drag_builder shadowBuilder = new custom_drag_builder(parent);
            parent.startDrag(null, shadowBuilder, parent, 0);
            return true;
         }
         return false;
      }
   }

   int old_view = 0;
   int[] position = new int[2];
   int height = (int) main.con.getResources().getDisplayMetrics().heightPixels;

   class MyDragListener implements OnDragListener
   {
      @Override
      public boolean onDrag(View v, DragEvent event)
      {
         int action        = event.getAction();
         ListView listview = ((ListView) v.getParent());

         if(action == DragEvent.ACTION_DRAG_ENTERED)
         {
            /* Find and fade out the new view. */
            /* V is the thing to fade out. */
            View temp = (View) event.getLocalState();
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setDuration(120);
            fadeOut.setInterpolator(new DecelerateInterpolator());
            v.setAnimation(fadeOut);
            v.setVisibility(View.INVISIBLE);

            /* Fade in the view that was just left. */
            View last = listview.getChildAt(old_view);
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setDuration(120);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            last.setAnimation(fadeIn);
            last.setVisibility(View.VISIBLE);

            /* Save the position of the view that just faded out. */
            new_title = util.getstr(v, R.id.tag_item);
            for(int i = 0; i < listview.getChildCount(); i++)
            {
               if(new_title.equals(util.getstr(listview.getChildAt(i), R.id.tag_item)));
               {
                  old_view = i;
                  break;
               }
            }

            /* Change the information of the card that just disapeared. */
            /* Old title is the currently touched title and new_title is the one to be replaced */
            rearrange_tags(old_title, new_title);
            notifyDataSetChanged();

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

            write.collection(main.GROUP_LIST, Arrays.asList(tag_array));
            util.update_tags();
         }
         return true;
      }
   }

   void rearrange_tags(String previous, String next)
   {
      int i = 0;
      while(!previous.equals(tag_array[i])){
         i++;
      }
      int j = 0;
      while(!next.equals(tag_array[j])){
         j++;
      }
      String old_info = info_array[i];
      String old     = tag_array[i];

      info_array[i]  = info_array[j];
      tag_array[i]    = tag_array[j];

      info_array[j]  = old_info;
      tag_array[j] = old;
   }

   class custom_drag_builder extends View.DragShadowBuilder
   {
      View view_store;

      public custom_drag_builder(View v)
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
