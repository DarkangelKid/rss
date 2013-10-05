package yay.poloure.simplerss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterNavDrawer extends BaseAdapter
{
   static String[] s_menuArray;
   static int[]    s_unreadArray;

   private static final int   TWELVE    = Math.round(
         12.0F * Util.getContext().getResources().getDisplayMetrics().density + 0.5f);
   private static final int[] NAV_ICONS = {
         R.drawable.feeds, R.drawable.manage, R.drawable.feeds,
   };

   private TextView m_navigationMainItem;

   static
   class NavigationTagItem
   {
      TextView title;
      TextView unread_view;
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   String getItem(int position)
   {
      return s_menuArray[position];
   }

   @Override
   public
   int getCount()
   {
      return null == s_menuArray ? 4 : s_menuArray.length + 4;
   }

   @Override
   public
   boolean isEnabled(int position)
   {
      return 3 != position;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 3;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      if(3 > position)
      {
         return 0;
      }

      else
      {
         return 3 == position ? 1 : 2;
      }
   }

   @Override
   public
   View getView(int position, View cv, ViewGroup parent)
   {
      View cv1 = cv;
      int viewType = getItemViewType(position);
      LayoutInflater inflater = Util.getLayoutInflater();

      switch(viewType)
      {
         /* This view is for the FeedsActivity items Feeds, Manage, & Settings. */
         case 0:
            if(null == cv1)
            {
               cv1 = inflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
               m_navigationMainItem = (TextView) cv1.findViewById(R.id.menu_item);
            }

            m_navigationMainItem.setText(NavDrawer.NAV_TITLES[position]);

            /* Set the item's image as a CompoundDrawable of the textview. */
            m_navigationMainItem.setCompoundDrawablesRelativeWithIntrinsicBounds(
                  NAV_ICONS[position], 0, 0, 0);
            m_navigationMainItem.setCompoundDrawablePadding(TWELVE);
            break;

         /* This view is for the NavDivider and "Groups" subtitle.
          * The imageview NavDivider is below the subtitle. */
         case 1:
            if(null == cv1)
            {
               cv1 = inflater.inflate(R.layout.navigation_drawer_subtitle_divider, parent, false);
            }
            break;

         /* This view is for the m_imageViewTag items of the navigation drawer.
          * The one with unread counters. */
         default:
            NavigationTagItem holder2;
            if(null == cv1)
            {
               cv1 = inflater.inflate(R.layout.navigation_drawer_group_item, parent, false);
               holder2 = new NavigationTagItem();
               holder2.title = (TextView) cv1.findViewById(R.id.tag_title);
               holder2.unread_view = (TextView) cv1.findViewById(R.id.unread_item);
               cv1.setTag(holder2);
            }
            else
            {
               holder2 = (NavigationTagItem) cv1.getTag();
            }

            holder2.title.setText(s_menuArray[position - 4]);
            String number = Integer.toString(s_unreadArray[position - 4]);
            holder2.unread_view.setText("0".equals(number) ? "" : number);
      }
      return cv1;
   }
}
