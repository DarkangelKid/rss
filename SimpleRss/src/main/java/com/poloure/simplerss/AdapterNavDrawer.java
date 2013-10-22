package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterNavDrawer extends BaseAdapter
{
   private static final int[] NAV_ICONS    = {
         R.drawable.feeds, R.drawable.manage, R.drawable.feeds,
   };
   private static final int   TYPE_TITLE   = 0;
   private static final int   TYPE_DIVIDER = 1;
   private static final int   TYPE_TAG     = 2;
   private static final int[] TYPES        = {TYPE_TITLE, TYPE_DIVIDER, TYPE_TAG};
   private static final float TWELVE_VALUE = 12.0F;
   private final int            m_twelve;
   private final LayoutInflater m_layoutInflater;
   private final Context        m_context;
   String[] m_tagArray    = Util.EMPTY_STRING_ARRAY;
   int[]    m_unreadArray = Util.EMPTY_INT_ARRAY;
   private TextView m_navigationMainItem;

   AdapterNavDrawer(Context context)
   {
      m_context = context;
      m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      m_twelve = Math.round(TWELVE_VALUE * displayMetrics.density);
   }

   @Override
   public
   int getCount()
   {
      return m_tagArray.length + 4;
   }

   @Override
   public
   String[] getItem(int position)
   {
      String unread = 0 == m_unreadArray.length ? "" : Integer.toString(m_unreadArray[position]);
      String tag = m_tagArray[position];
      return new String[]{unread, tag};
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

      if(TYPE_TITLE == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
            m_navigationMainItem = (TextView) view.findViewById(R.id.menu_item);
         }

         Resources resources = m_context.getResources();
         String[] navTitles = resources.getStringArray(R.array.nav_titles);

         m_navigationMainItem.setText(navTitles[position]);

         /* Set the item's image as a CompoundDrawable of the textView. */
         if(Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT)
         {
            m_navigationMainItem.setCompoundDrawablesRelativeWithIntrinsicBounds(
                  NAV_ICONS[position], 0, 0, 0);
         }
         m_navigationMainItem.setCompoundDrawablePadding(m_twelve);
      }
      else if(TYPE_DIVIDER == viewType && null == view)
      {
         view = m_layoutInflater.inflate(R.layout.navigation_drawer_subtitle_divider, parent,
               false);
      }
      else if(TYPE_TAG == viewType)
      {
         NavigationTagItem holder2;
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.navigation_drawer_group_item, parent, false);
            holder2 = new NavigationTagItem();
            holder2.m_tagTitle = (TextView) view.findViewById(R.id.tag_title);
            holder2.m_unreadCountView = (TextView) view.findViewById(R.id.unread_item);
            view.setTag(holder2);
         }
         else
         {
            holder2 = (NavigationTagItem) view.getTag();
         }

         holder2.m_tagTitle.setText(m_tagArray[position - 4]);
         String number = Integer.toString(m_unreadArray[position - 4]);
         String unreadText = "0".equals(number) ? "" : number;
         holder2.m_unreadCountView.setText(unreadText);
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
         return TYPE_TITLE;
      }

      else
      {
         return 3 == position ? TYPE_DIVIDER : TYPE_TAG;
      }
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }

   static
   class NavigationTagItem
   {
      TextView m_tagTitle;
      TextView m_unreadCountView;
   }
}
