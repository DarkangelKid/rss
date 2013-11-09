package com.poloure.simplerss;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterNavigationDrawer extends BaseAdapter
{
   private static final int[] NAV_ICONS = {
         R.drawable.action_feeds, R.drawable.action_manage, R.drawable.action_settings,
   };
   private static final int TYPE_TITLE = 0;
   private static final int TYPE_DIVIDER = 1;
   private static final int TYPE_TAG = 2;
   private static final int[] TYPES = {TYPE_TITLE, TYPE_DIVIDER, TYPE_TAG};
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   private final int m_twelveDp;
   private final String[] m_navigationTitles;
   private final LayoutInflater m_layoutInflater;
   private String[] m_tagArray = new String[0];
   private int[] m_unreadArray = EMPTY_INT_ARRAY;

   AdapterNavigationDrawer(String[] navigationTitles, int twelveDp, LayoutInflater layoutInflater)
   {
      m_navigationTitles = navigationTitles.clone();
      m_layoutInflater = layoutInflater;
      m_twelveDp = twelveDp;
   }

   void setArrays(String[] tags, int[] unreadCounts)
   {
      m_tagArray = tags.clone();
      m_unreadArray = unreadCounts.clone();
   }

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
      return 0 == m_unreadArray.length ? "" : Integer.toString(m_unreadArray[position]);
   }

   @Override
   public
   long getItemId(int position)
   {
      return (long) position;
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
         }

         ((TextView) view).setText(m_navigationTitles[position]);

         /* Set the item's image as a CompoundDrawable of the textView. */
         if(Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT)
         {
            ((TextView) view).setCompoundDrawablesRelativeWithIntrinsicBounds(NAV_ICONS[position],
                  0, 0, 0);
         }
         ((TextView) view).setCompoundDrawablePadding(m_twelveDp);
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
            view = m_layoutInflater.inflate(R.layout.navigation_drawer_tag_item, parent, false);
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
