package com.poloure.simplerss;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AdapterTags extends BaseAdapter
{
   static final Set<Long>      S_READ_ITEM_TIMES = Collections.synchronizedSet(
         new HashSet<Long>(0));
   final        List<FeedItem> m_items           = new ArrayList<FeedItem>(0);
   final        List<Long>     m_times           = new ArrayList<Long>(0);
   private final Context m_context;
   boolean m_isReadingItems = true;

   AdapterTags(Context context)
   {
      m_context = context;
   }

   void prependArray(Object... items)
   {
      Collection<FeedItem> longCollection = (Collection<FeedItem>) items[0];
      List<Long> longList = (List<Long>) items[1];
      m_items.addAll(0, longCollection);
      m_times.addAll(0, longList);
   }

   @Override
   public
   int getCount()
   {
      return m_items.size();
   }

   @Override
   public
   Object getItem(int position)
   {
      return m_items.get(position);
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
      View view = null == convertView ? new LayoutFeedItem(m_context) : convertView;

      FeedItem item = m_items.get(position);
      Long time = item.m_itemTime;
      boolean isRead = S_READ_ITEM_TIMES.contains(time);

      if(0.0F == LayoutFeedItem.s_cardOpacity && isRead)
      {
         /* TODO Separators persist. */
         view.setVisibility(View.GONE);
         return view;
      }

      ((LayoutFeedItem) view).showItem(item, position, isRead);

      /* The logic that tells whether the item is Read or not. */
      boolean isListViewShown = parent.isShown();
      boolean isNotLastItem = position + 1 < getCount();

      if(isListViewShown && isNotLastItem && m_isReadingItems)
      {
         FeedItem nextItem = m_items.get(position + 1);
         S_READ_ITEM_TIMES.add(nextItem.m_itemTime);
      }

      return view;
   }
}
