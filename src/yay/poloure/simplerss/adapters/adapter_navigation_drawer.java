package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class adapter_navigation_drawer extends BaseAdapter
{
   static String[] menu_array  = new String[0];
   static int[] count_array = new int[0];
   int twelve;
   static int[] title_array = new int[]
   {
      R.drawable.feeds,
      R.drawable.manage,
      R.drawable.feeds,
   };

   TextView main_item;

   static class divider
   {
      TextView title;
      ImageView divider_view;
   }

   static class group_item
   {
      TextView title;
      TextView unread_view;
   }

   public adapter_navigation_drawer()
   {
      if(twelve == 0)
      {
         twelve = (int) ((12 * (util.get_context().getResources().getDisplayMetrics().density) + 0.5f));
      }
   }

   void set_titles(String[] new_titles)
   {
      menu_array = new_titles;
   }

   void set_counts(int[] new_counts)
   {
      count_array = new_counts;
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public String getItem(int position)
   {
      return menu_array[position];
   }

   @Override
   public int getCount()
   {
      return menu_array.length + 4;
   }

   @Override
   public boolean isEnabled(int position)
   {
      return position != 3;
   }

   @Override
   public int getViewTypeCount()
   {
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
   public View getView(int position, View cv, ViewGroup parent)
   {
      final int view_type = getItemViewType(position);
      LayoutInflater inflater = util.get_inflater();

      switch(view_type)
      {
         /* This view is for the main items Feeds, Manage, & Settings. */
         case(0):
            if(cv == null)
            {
               cv = inflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
               main_item = (TextView) cv.findViewById(R.id.menu_item);
            }

            main_item.setText(navigation_drawer.NAV_TITLES[position]);

            /* Set the item's image as a CompoundDrawable of the textview. */
            main_item.setCompoundDrawablesWithIntrinsicBounds(title_array[position], 0, 0, 0);
            main_item.setCompoundDrawablePadding(twelve);
            break;

         /* This view is for the divider and "Groups" subtitle.
          * The imageview divider is below the subtitle. */
         case(1):
            divider holder;
            if(cv == null)
            {
               cv = inflater.inflate(R.layout.navigation_drawer_subtitle_divider, parent, false);
               holder = new divider();
               holder.title = (TextView) cv.findViewById(R.id.title_item);
               holder.divider_view = (ImageView) cv.findViewById(R.id.divider_item);
               cv.setTag(holder);
            }
            else
               holder = (divider) cv.getTag();
            break;

         /* This view is for the group items of the navigation drawer.
          * The one with unread counters. */
         default:
            group_item holder2;
            if(cv == null)
            {
               cv = inflater.inflate(R.layout.navigation_drawer_group_item, parent, false);
               holder2 = new group_item();
               holder2.title = (TextView) cv.findViewById(R.id.group_title);
               holder2.unread_view = (TextView) cv.findViewById(R.id.unread_item);
               cv.setTag(holder2);
            }
            else
               holder2 = (group_item) cv.getTag();

            holder2.title.setText(menu_array[position - 4]);
            String number = Integer.toString(count_array[position - 4]);
            holder2.unread_view.setText((number.equals("0")) ? "" : number);
      }
      return cv;
   }
}
