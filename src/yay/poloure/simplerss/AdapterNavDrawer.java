package yay.poloure.simplerss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class AdapterNavDrawer extends BaseAdapter
{
   private static String[] menu_array;
   private static int[]    count_array;
   private        int      twelve;
   private static final int[] NAV_ICONS = {
         R.drawable.feeds, R.drawable.manage, R.drawable.feeds,
   };

   private TextView NavItem;

   static class NavDivider
   {
      TextView  title;
      ImageView divider_view;
   }

   static class TagItem
   {
      TextView title;
      TextView unread_view;
   }

   public AdapterNavDrawer()
   {
      if(0 == twelve)
      {
         twelve = (int) (12.0F * Util.getContext().getResources().getDisplayMetrics().density +
                         0.5f);
      }
   }

   static void setTitles(String... new_titles)
   {
      menu_array = new_titles;
   }

   static void setCounts(int... new_counts)
   {
      count_array = new_counts;
   }

   @Override
   public long getItemId(int position)
   {
      return (long) position;
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
      return 3 != position;
   }

   @Override
   public int getViewTypeCount()
   {
      return 3;
   }

   @Override
   public int getItemViewType(int position)
   {
      if(3 > position)
      {
         return 0;
      }

      else
      {
         return position == 3 ? 1 : 2;
      }
   }

   @Override
   public View getView(int position, View cv, ViewGroup parent)
   {
      int viewType = getItemViewType(position);
      LayoutInflater inflater = Util.getLayoutInflater();

      switch(viewType)
      {
         /* This view is for the FeedsActivity items Feeds, Manage, & Settings. */
         case 0:
            if(null == cv)
            {
               cv = inflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
               NavItem = (TextView) cv.findViewById(R.id.menu_item);
            }

            NavItem.setText(NavDrawer.NAV_TITLES[position]);

            /* Set the item's Image as a CompoundDrawable of the textview. */
            NavItem.setCompoundDrawablesRelativeWithIntrinsicBounds(NAV_ICONS[position], 0, 0, 0);
            NavItem.setCompoundDrawablePadding(twelve);
            break;

         /* This view is for the NavDivider and "Groups" subtitle.
          * The imageview NavDivider is below the subtitle. */
         case 1:
            NavDivider holder;
            if(null == cv)
            {
               cv = inflater.inflate(R.layout.navigation_drawer_subtitle_divider, parent, false);
               holder = new NavDivider();
               holder.title = (TextView) cv.findViewById(R.id.title_item);
               holder.divider_view = (ImageView) cv.findViewById(R.id.divider_item);
               cv.setTag(holder);
            }
            else
            {
               holder = (NavDivider) cv.getTag();
            }
            break;

         /* This view is for the tag items of the navigation drawer.
          * The one with unread counters. */
         default:
            TagItem holder2;
            if(null == cv)
            {
               cv = inflater.inflate(R.layout.navigation_drawer_group_item, parent, false);
               holder2 = new TagItem();
               holder2.title = (TextView) cv.findViewById(R.id.tag_title);
               holder2.unread_view = (TextView) cv.findViewById(R.id.unread_item);
               cv.setTag(holder2);
            }
            else
            {
               holder2 = (TagItem) cv.getTag();
            }

            holder2.title.setText(menu_array[position - 4]);
            String number = Integer.toString(count_array[position - 4]);
            holder2.unread_view.setText("0".equals(number) ? "" : number);
      }
      return cv;
   }
}
