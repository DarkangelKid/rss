package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AdapterTags extends BaseAdapter
{
   static final Set<Long> READ_ITEM_TIMES = Collections.synchronizedSet(new HashSet<Long>(0));
   private static final Typeface SERIF = Typeface.create("serif", Typeface.NORMAL);
   private final List<FeedItem> m_items = new ArrayList<FeedItem>(0);
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
      m_items.addAll(0, longCollection);
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
      boolean isImage = 0 != m_items.get(position).m_EffImageHeight;

      return isImage ? 1 : 0;
   }

   @Override
   public
   int getCount()
   {
      return m_items.size();
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
      return m_items.get(position);
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
      FeedItem item = m_items.get(position);

      Long time = item.m_itemTime;
      boolean isRead = READ_ITEM_TIMES.contains(time);

      View view;

      if(0 == viewType)
      {
         /* Make the single TextView view. */
         view = isNewView ? new TextView(m_context) : convertView;
         if(isNewView)
         {
            ((TextView) view).setTypeface(SERIF);
            Resources resources = m_context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();

            int eight = Math.round(
                  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics));

            view.setPadding(eight, eight, eight, eight);
         }

         ((TextView) view).setText(item.m_titleAndLink);
      }
      else
      {
         view = isNewView ? new LayoutFeedItem(m_context) : convertView;
         ((LayoutFeedItem) view).showItem(item, m_applicationFolder, position, item.m_titleAndLink);
      }

      view.setAlpha(isRead ? 0.50F : 1.0F);

      /* The logic that tells whether the item is Read or not. */
      boolean isListViewShown = parent.isShown();
      boolean isNotLastItem = position + 1 < getCount();

      if(isListViewShown && isNotLastItem && m_isReadingItems)
      {
         FeedItem nextItem = m_items.get(position + 1);
         READ_ITEM_TIMES.add(nextItem.m_itemTime);
      }

      return view;
   }
}
