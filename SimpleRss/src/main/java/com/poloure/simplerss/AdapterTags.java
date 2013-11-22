package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
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
   private static final AbsoluteSizeSpan TITLE_SIZE = new AbsoluteSizeSpan(14, true);
   private static final AbsoluteSizeSpan LINK_SIZE = new AbsoluteSizeSpan(10, true);
   private static final AbsoluteSizeSpan DESCRIPTION_SIZE = new AbsoluteSizeSpan(12, true);
   private static final Typeface SANS_SERIF_LITE = Typeface.create("sans-serif-light",
         Typeface.NORMAL);
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
      boolean isImage = 0 != m_items.get(position).m_imageWidth;

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

      Editable editable = new SpannableStringBuilder();

      /* TODO, set the text colors. */
      /* First, the title. */
      editable.append(item.m_itemTitle);
      editable.setSpan(TITLE_SIZE, 0, editable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      editable.append("\n");

      /* Next the link. */
      int offset = editable.length();
      editable.append(item.m_itemUrl);
      editable.setSpan(LINK_SIZE, offset, editable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

      View view;

      if(0 == viewType)
      {
         /* Make the single TextView view. */
         view = isNewView ? new TextView(m_context) : convertView;
         editable.append("\n");

         /* Finally the description. */
         offset = editable.length();
         editable.append(item.m_itemDescription);
         editable.setSpan(DESCRIPTION_SIZE, offset, editable.length(),
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

         ((TextView) view).setText(editable);
         ((TextView) view).setTypeface(SANS_SERIF_LITE);
         Resources resources = m_context.getResources();
         DisplayMetrics metrics = resources.getDisplayMetrics();

         int eight = Math.round(
               TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics));

         view.setPadding(eight, eight, eight, eight);
      }
      else
      {
         view = isNewView ? new LayoutFeedItem(m_context) : convertView;
         ((LayoutFeedItem) view).showItem(item, m_applicationFolder, position, isRead, editable);
      }

      float opacity = LayoutFeedItem.getReadItemOpacity();
      if(0.0F > opacity)
      {
         Resources resources = m_context.getResources();
         String applicationFolder = FeedsActivity.getApplicationFolder(m_context);
         String[] settingTitles = resources.getStringArray(R.array.settings_interface_titles);
         String opacityPath = FeedsActivity.SETTINGS_DIR + settingTitles[1] + ".txt";
         String[] opacityFile = Read.file(opacityPath, applicationFolder);

         boolean valueExists = 0 != opacityFile.length && 0 != opacityFile[0].length();

         float opacityFloat = valueExists ? Float.parseFloat(opacityFile[0]) / 100.0F : 0.66F;
         LayoutFeedItem.setReadItemOpacity(opacityFloat);
      }

      /* If read and read items are hidden, remove the item. */
      /*TextView textView = (TextView) view.findViewById(R.id.title);
      int color = textView.getCurrentTextColor();

      if(Color.TRANSPARENT == color)
      {
         view.setVisibility(View.GONE);
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
