package com.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterNavDrawer extends BaseAdapter
{
   private static final int   TWELVE    = Math.round(
         12.0F * Util.getContext().getResources().getDisplayMetrics().density + 0.5f);
   private static final int[] NAV_ICONS = {
         R.drawable.feeds, R.drawable.manage, R.drawable.feeds,
   };
   String[] m_tagArray    = Util.EMPTY_STRING_ARRAY;
   int[]    m_unreadArray = Util.EMPTY_INT_ARRAY;
   private TextView m_navigationMainItem;

   @Override
   public
   int getCount()
   {
      return m_tagArray.length + 4;
   }

   @Override
   public
   String getItem(int position)
   {
      return m_tagArray[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      int viewType = getItemViewType(position);
      String inflate = Context.LAYOUT_INFLATER_SERVICE;
      LayoutInflater inflater = (LayoutInflater) Util.getContext().getSystemService(inflate);

         /* This view is for the FeedsActivity items Feeds, Manage, & Settings. */
      if(0 == viewType)
      {
         if(null == view)
         {
            view = inflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
            m_navigationMainItem = (TextView) view.findViewById(R.id.menu_item);
         }

         m_navigationMainItem.setText(NavDrawer.NAV_TITLES[position]);

         /* Set the item's image as a CompoundDrawable of the textview. */
         m_navigationMainItem.setCompoundDrawablesRelativeWithIntrinsicBounds(NAV_ICONS[position],
               0, 0, 0);
         m_navigationMainItem.setCompoundDrawablePadding(TWELVE);
      }

      /* This view is for the NavDivider and "Groups" subtitle.
       * The imageView NavDivider is below the subtitle. */
      if(1 == viewType && null == view)
      {
         view = inflater.inflate(R.layout.navigation_drawer_subtitle_divider, parent, false);
      }
      /* This view is for the m_imageViewTag items of the navigation drawer.
       * The one with unread counters. */
      else if(2 == viewType)
      {
         NavigationTagItem holder2;
         if(null == view)
         {
            view = inflater.inflate(R.layout.navigation_drawer_group_item, parent, false);
            holder2 = new NavigationTagItem();
            holder2.title = (TextView) view.findViewById(R.id.tag_title);
            holder2.m_unreadCountView = (TextView) view.findViewById(R.id.unread_item);
            view.setTag(holder2);
         }
         else
         {
            holder2 = (NavigationTagItem) view.getTag();
         }

         holder2.title.setText(m_tagArray[position - 4]);
         String number = Integer.toString(m_unreadArray[position - 4]);
         holder2.m_unreadCountView.setText("0".equals(number) ? "" : number);
      }
      return view;
   }

   @Override
   public
   boolean isEnabled(int position)
   {
      return 3 != position;
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
   int getViewTypeCount()
   {
      return 3;
   }

   static
   class NavigationTagItem
   {
      TextView title;
      TextView m_unreadCountView;
   }
}
