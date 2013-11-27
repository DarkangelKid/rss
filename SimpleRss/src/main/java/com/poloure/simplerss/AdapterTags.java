package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Typeface;
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
   static final Set<Long> READ_ITEM_TIMES = Collections.synchronizedSet(new HashSet<Long>(0));
   private static final Typeface SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
   private static final float ALPHA_READ_ITEM = 0.50F;
   private static final float PADDING_BASIC_ITEM = 8.0F;
   private final List<FeedItem> m_feedItems = new ArrayList<FeedItem>(0);
   private final List<Long> m_times = new ArrayList<Long>(0);
   private final Context m_context;
   private final String m_applicationFolder;
   boolean m_isReadingItems = true;

   AdapterTags(Context context, String applicationFolder)
   {
      m_context = context;
      m_applicationFolder = applicationFolder;
   }

   void prependArray(Object... items)
   {
      Collection<FeedItem> longCollection = (Collection<FeedItem>) items[0];
      Collection<Long> longList = (Collection<Long>) items[1];
      m_feedItems.addAll(0, longCollection);
      m_times.addAll(0, longList);
   }

   /* Do not edit the list once you get it. */
   List<Long> getTimeList()
   {
      return m_times;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      boolean isImage = 0 != m_feedItems.get(position).m_EffImageHeight;

      return isImage ? 1 : 0;
   }

   @Override
   public
   int getCount()
   {
      return m_feedItems.size();
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 2;
   }

   @Override
   public
   Object getItem(int position)
   {
      return m_feedItems.get(position);
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
      boolean isNewView = null == convertView;
      int viewType = getItemViewType(position);
      FeedItem item = m_feedItems.get(position);

      Long time = item.m_itemTime;
      boolean isRead = READ_ITEM_TIMES.contains(time);

      View view;

      if(0 == viewType)
      {
         /* Make the single TextView view. */
         view = isNewView ? new LayoutFeedItemPlain(m_context) : convertView;
         ((LayoutFeedItemPlain) view).showItem(item.m_itemTitle, item.m_url,
               item.m_itemDescription);
      }
      else
      {
         view = isNewView ? new LayoutFeedItem(m_context) : convertView;
         ((LayoutFeedItem) view).showItem(item, m_applicationFolder, position);
      }

      /* TODO set colors not alpha. */
      //view.setAlpha(isRead ? ALPHA_READ_ITEM : 1.0F);

      /* The logic that tells whether the item is Read or not. */
      boolean isListViewShown = parent.isShown();
      boolean isNotLastItem = position + 1 < getCount();

      if(isListViewShown && isNotLastItem && m_isReadingItems)
      {
         FeedItem nextItem = m_feedItems.get(position + 1);
         READ_ITEM_TIMES.add(nextItem.m_itemTime);
      }

      return view;
   }
}
