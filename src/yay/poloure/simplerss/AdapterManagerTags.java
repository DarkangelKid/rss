package yay.poloure.simplerss;

import android.content.Context;
import android.graphics.Point;
import android.view.DragEvent;
import android.view.LayoutInflater;
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

class AdapterManagerTags extends BaseAdapter
{
   private static final int      SCREEN_HEIGHT = (int) Util.getContext()
         .getResources()
         .getDisplayMetrics().heightPixels;
   static               String[] s_tagArray    = Util.EMPTY_STRING_ARRAY;
   static               String[] s_infoArray   = Util.EMPTY_STRING_ARRAY;
   private final        int[]    m_position    = new int[2];
   private              String   old_title     = "";
   private int old_view;

   @Override
   public
   int getCount()
   {
      return s_tagArray.length;
   }

   @Override
   public
   String getItem(int pos)
   {
      return s_tagArray[pos];
   }

   @Override
   public
   long getItemId(int pos)
   {
      return pos;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      ViewHolder holder;
      if(null == view)
      {
         String inflate = Context.LAYOUT_INFLATER_SERVICE;
         view = ((LayoutInflater) Util.getContext().getSystemService(inflate)).inflate(
               R.layout.manage_group_item, parent, false);
         holder = new ViewHolder();
         holder.tag_view = (TextView) view.findViewById(R.id.tag_item);
         holder.info_view = (TextView) view.findViewById(R.id.tag_feeds);
         holder.image_view = (ImageView) view.findViewById(R.id.drag_image);
         view.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) view.getTag();
      }

      holder.tag_view.setText(s_tagArray[position]);
      holder.info_view.setText(s_infoArray[position]);
      if(0 != position && Constants.HONEYCOMB)
      {
         holder.image_view.setVisibility(View.VISIBLE);
         holder.image_view.setOnTouchListener(new MyTouchListener());
         view.setOnDragListener(new MyDragListener());
      }
      else
      {
         holder.image_view.setVisibility(View.GONE);
      }


      return view;
   }

   static
   class ViewHolder
   {
      TextView  tag_view;
      TextView  info_view;
      ImageView image_view;
   }

   private static
   class CardShadowBuilder extends View.DragShadowBuilder
   {
      final View view_store;

      CardShadowBuilder(View v)
      {
         super(v);
         view_store = v;
      }

      @Override
      public
      void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint)
      {
         super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
         shadowSize.x = view_store.getWidth();
         shadowSize.y = view_store.getHeight();

         shadowTouchPoint.x = (int) (shadowSize.x * 19.0 / 20.0);
         shadowTouchPoint.y = shadowSize.y / 2;
      }
   }

   class MyTouchListener implements OnTouchListener
   {
      @Override
      public
      boolean onTouch(View view, MotionEvent motionEvent)
      {
         if(MotionEvent.ACTION_DOWN == motionEvent.getAction())
         {
            View parent = (View) view.getParent();
            old_title = Util.getText(parent, R.id.tag_item);
            CardShadowBuilder shadowBuilder = new CardShadowBuilder(parent);
            parent.startDrag(null, shadowBuilder, parent, 0);
            return true;
         }
         return false;
      }
   }

   class MyDragListener implements OnDragListener
   {
      @Override
      public
      boolean onDrag(View v, DragEvent event)
      {
         int action = event.getAction();
         ListView listview = (ListView) v.getParent();

         if(DragEvent.ACTION_DRAG_ENTERED == action)
         {
            /* Find and fade out the new view. */
            /* V is the thing to fade out. */
            Animation fadeOut = new AlphaAnimation(1.0F, 0.0F);
            fadeOut.setDuration(120L);
            fadeOut.setInterpolator(new DecelerateInterpolator());
            v.setAnimation(fadeOut);
            v.setVisibility(View.INVISIBLE);

            /* Fade in the view that was just left. */
            View last = listview.getChildAt(old_view);
            Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
            fadeIn.setDuration(120L);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            last.setAnimation(fadeIn);
            last.setVisibility(View.VISIBLE);

            /* Save the position of the view that just faded out. */
            String newTitle = Util.getText(v, R.id.tag_item);

            int childCount = listview.getChildCount();
            for(int i = 0; i < childCount; i++)
            {
               if(newTitle.equals(Util.getText(listview.getChildAt(i), R.id.tag_item)))
               {
                  old_view = i;
                  break;
               }
            }

            /* Change the information of the card that just disappeared. */
            /* Old m_title is the currently touched m_title and new_title is the one to be
            replaced */
            rearrangeTags(old_title, newTitle);
            notifyDataSetChanged();

            v.getLocationOnScreen(m_position);

            if(m_position[1] > 4.0 / 5.0 * SCREEN_HEIGHT)
            {
               listview.smoothScrollBy(v.getHeight(), 400);
            }
            else if(m_position[1] < 1.0 / 5.0 * SCREEN_HEIGHT)
            {
               listview.smoothScrollBy((int) (-1.0 * v.getHeight()), 400);
            }
         }
         else if(DragEvent.ACTION_DROP == action)
         {
            Animation fadeIn2 = new AlphaAnimation(0.0F, 1.0F);
            fadeIn2.setDuration(210L);
            fadeIn2.setInterpolator(new DecelerateInterpolator());
            v.setAnimation(fadeIn2);
            v.setVisibility(View.VISIBLE);

            Write.collection(Constants.TAG_LIST, Arrays.asList(s_tagArray));
            // TODO Util.updateTags();
         }
         return true;
      }

      void rearrangeTags(String previous, String next)
      {
         int i = Util.index(s_tagArray, previous);
         int j = Util.index(s_tagArray, next);

         String oldInfo = s_infoArray[i];
         String old = s_tagArray[i];

         s_infoArray[i] = s_infoArray[j];
         s_tagArray[i] = s_tagArray[j];

         s_infoArray[j] = oldInfo;
         s_tagArray[j] = old;
      }
   }
}
