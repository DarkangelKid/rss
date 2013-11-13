package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
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
      return (long) position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      boolean isNewView = null == convertView;

      LayoutFeedItem view = isNewView
            ? new LayoutFeedItem(m_context)
            : (LayoutFeedItem) convertView;

      float opacity = LayoutFeedItem.getReadItemOpacity();
      if(0.0F > opacity)
      {
         Resources resources = m_context.getResources();
         String applicationFolder = FeedsActivity.getApplicationFolder(m_context);
         String[] settingTitles = resources.getStringArray(R.array.settings_interface_titles);
         String opacityPath = FeedsActivity.SETTINGS_DIR + settingTitles[1] + ".txt";
         String[] opacityFile = Read.file(opacityPath, applicationFolder);

         boolean valueExists = 0 != opacityFile.length && 0 != opacityFile[0].length();

         LayoutFeedItem.setReadItemOpacity(
               valueExists ? Float.parseFloat(opacityFile[0]) / 100.0F : 0.66F);
      }

      FeedItem item = m_items.get(position);
      Long time = item.m_itemTime;
      boolean isRead = READ_ITEM_TIMES.contains(time);

      view.showItem(item, m_applicationFolder, position, isRead);

      /* If read and read items are hidden, remove the item. */
      /*TextView textView = (TextView) view.findViewById(R.id.title);
      int color = textView.getCurrentTextColor();

      if(Color.TRANSPARENT == color)
      {
         /* TODO, the item still shows empty space. */
        /* view.setVisibility(View.GONE);
         return view;
      }*/

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
