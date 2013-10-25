package com.poloure.simplerss;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AdapterTags extends BaseAdapter
{
   static final Set<Long> S_READ_ITEM_TIMES = Collections.synchronizedSet(new HashSet<Long>(0));
   private final Context m_context;
   boolean m_readingItems = true;
   final List<FeedItem> m_items = new ArrayList<FeedItem>(0);

   AdapterTags(Context context, String readItemFileName, String applicationFolder)
   {
      if(0 == S_READ_ITEM_TIMES.size())
      {
         Set<Long> set = Read.longSet(readItemFileName, applicationFolder);
         S_READ_ITEM_TIMES.addAll(set);
      }
      m_context = context;
   }

   void prependArray(Object... items)
   {
      List<FeedItem> feedItems = Arrays.asList((FeedItem[]) items);
      m_items.addAll(0, feedItems);
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
      View view = null == convertView ? new FeedItemView(m_context) : convertView;

      FeedItem item = m_items.get(position);
      Long time = item.m_itemTime;
      boolean isRead = S_READ_ITEM_TIMES.contains(time);

      ((FeedItemView) view).showItem(item, position, isRead);

      /* The logic that tells whether the item is Read or not. */
      boolean isListViewShown = parent.isShown();
      boolean isNotLastItem = position + 1 < getCount();

      if(isListViewShown && isNotLastItem && m_readingItems)
      {
         FeedItem nextItem = m_items.get(position + 1);
         S_READ_ITEM_TIMES.add(nextItem.m_itemTime);
      }

      return view;
   }
}
