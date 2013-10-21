package com.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class AdapterTags extends BaseAdapter
{
   static final Set<Long> S_READ_ITEM_TIMES = Collections.synchronizedSet(new HashSet<Long>(0));
   private final LayoutInflater m_inflater;
   boolean    m_touchedScreen = true;
   FeedItem[] m_items         = new FeedItem[0];

   AdapterTags(Context context)
   {
      if(0 == S_READ_ITEM_TIMES.size())
      {
         Set<Long> set = Read.longSet(Constants.READ_ITEMS, context);
         S_READ_ITEM_TIMES.addAll(set);
      }
      m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   void prependArray(Object... items)
   {
      m_items = concat((FeedItem[]) items, m_items);
   }

   private static
   <T> T[] concat(T[] first, T[] second)
   {
      int length = first.length + second.length;

      Class firstClass = first.getClass();
      Class type = firstClass.getComponentType();
      T[] result = (T[]) Array.newInstance(type, length);
      System.arraycopy(first, 0, result, 0, first.length);
      System.arraycopy(second, 0, result, first.length, second.length);

      return result;
   }

   @Override
   public
   int getCount()
   {
      return null == m_items ? 0 : m_items.length;
   }

   @Override
   public
   Object getItem(int position)
   {
      return m_items[position];
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
      ItemView view = null == convertView
            ? (ItemView) m_inflater.inflate(R.layout.card_full, null)
            : (ItemView) convertView;

      if(null != view)
      {
         Object item = getItem(position);
         view.showItem((FeedItem) item, position);
      }

      /* The logic that tells whether the item is Read or not. */
      if(View.VISIBLE == parent.getVisibility() && position + 1 < m_items.length &&
            m_touchedScreen)
      {
         S_READ_ITEM_TIMES.add(m_items[position + 1].m_itemTime);
      }

      return view;
   }

   boolean isScreenTouched()
   {
      return m_touchedScreen;
   }
}
