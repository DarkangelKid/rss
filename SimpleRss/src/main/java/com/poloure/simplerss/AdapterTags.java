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
   static final Set<Long> READ_ITEM_TIMES = Collections.synchronizedSet(new HashSet<Long>(0));
   private static final int TYPE_PLAIN = 0;
   private static final int TYPE_IMAGE = 1;
   private static final int TYPE_IMAGE_SANS_DESCRIPTION = 2;
   private static final int TYPE_PLAIN_SANS_DESCRIPTION = 3;
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
      FeedItem feedItem = m_feedItems.get(position);

      boolean isImage = !feedItem.m_imageName.isEmpty();
      boolean isDescription = !feedItem.m_descriptionOne.isEmpty();

      int type;

      if(isImage && isDescription)
      {
         type = TYPE_IMAGE;
      }
      else if(isImage)
      {
         type = TYPE_IMAGE_SANS_DESCRIPTION;
      }
      else
      {
         type = isDescription ? TYPE_PLAIN : TYPE_PLAIN_SANS_DESCRIPTION;
      }

      return type;
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
      return 4;
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
      return position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      boolean isNewView = null == convertView;
      int viewType = getItemViewType(position);
      FeedItem item = m_feedItems.get(position);

      //Long time = item.m_time;
      //boolean isRead = READ_ITEM_TIMES.contains(time);
      boolean hasImage = TYPE_IMAGE == viewType || TYPE_IMAGE_SANS_DESCRIPTION == viewType;
      boolean hasDes = TYPE_PLAIN == viewType || TYPE_IMAGE == viewType;

      ViewCustom view = (ViewCustom) convertView;

      if(isNewView)
      {
         if(hasImage)
         {
            view = hasDes ? new ViewImageFeed(m_context, 560)
                  : new ViewImageSansDesFeed(m_context, 460);
         }
         else
         {
            view = hasDes ? new ViewBasicFeed(m_context, 180)
                  : new ViewBasicSansDesFeed(m_context, 90);
         }
      }

      view.setTexts(item.m_title, item.m_url, item.m_urlFull, item.m_descriptionOne,
            item.m_descriptionTwo, item.m_descriptionThree);

      /* If the view was an image, load the image. */
      if(hasImage)
      {
         view.setBitmap(null);
         view.setTag(position);
         AsyncLoadImage
               .newInstance(view, m_applicationFolder, item.m_imageName, position, m_context);
      }

      /* TODO set colors not alpha. */

      /* The logic that tells whether the item is Read or not. */
      boolean isListViewShown = parent.isShown();
      boolean isNotLastItem = position + 1 < getCount();

      if(isListViewShown && isNotLastItem && m_isReadingItems)
      {
         FeedItem nextItem = m_feedItems.get(position + 1);
         READ_ITEM_TIMES.add(nextItem.m_time);
      }

      return view;
   }
}
