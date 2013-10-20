package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class AdapterTags extends BaseAdapter
{
   static final         Set<Long> S_READ_ITEM_TIMES = Collections.synchronizedSet(
         new HashSet<Long>(0));
   private static final int       VIEW_TYPE_COUNT   = 4;
   private final LayoutInflater m_inflater;
   private final Context        m_context;
   private final int            m_titleBlack;
   private final int            m_linkBlack;
   private final int            m_descriptionBlack;
   private final int            m_titleGrey;
   private final int            m_linkGrey;
   private final int            m_descriptionGrey;
   boolean    m_touchedScreen = true;
   FeedItem[] m_items         = new FeedItem[0];

   AdapterTags(Context context)
   {
      m_context = context;
      if(0 == S_READ_ITEM_TIMES.size())
      {
         Set<Long> set = Read.longSet(Constants.READ_ITEMS, m_context);
         S_READ_ITEM_TIMES.addAll(set);
      }
      m_inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      Resources res = context.getResources();
      m_titleGrey = res.getColor(R.color.title_grey);
      m_linkGrey = res.getColor(R.color.link_grey);
      m_descriptionGrey = res.getColor(R.color.des_grey);

      m_titleBlack = res.getColor(R.color.title_black);
      m_linkBlack = res.getColor(R.color.link_black);
      m_descriptionBlack = res.getColor(R.color.des_black);
   }

   void prependArray(Object... items)
   {
      m_items = concat((FeedItem[]) items, m_items);
   }

   private static
   <T> T[] concat(T[] first, T[]... rest)
   {
      int length = first.length;
      for(T[] array : rest)
      {
         length += array.length;
      }

      Class firstClass = first.getClass();
      Class type = firstClass.getComponentType();
      T[] result = (T[]) Array.newInstance(type, length);
      System.arraycopy(first, 0, result, 0, first.length);

      int offset = first.length;
      for(T[] array : rest)
      {
         System.arraycopy(array, 0, result, offset, array.length);
         offset += array.length;
      }

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
      View view = convertView;
      int viewType = getItemViewType(position);

      String link = m_items[position].m_itemUrl;
      String title = m_items[position].m_itemTitle;
      Long time = m_items[position].m_itemTime;

      /* card_full.xml img && m_des. */
      if(0 == viewType)
      {
         FullHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_full, parent, false);
            holder = new FullHolder();
            holder.m_titleView = (TextView) view.findViewById(R.id.title);
            holder.m_urlView = (TextView) view.findViewById(R.id.url);
            holder.m_descriptionView = (TextView) view.findViewById(R.id.description);
            holder.m_imageView = (ImageView) view.findViewById(R.id.image);
            view.setOnLongClickListener(new OnCardLongClick(m_context));
            view.setTag(holder);
         }
         else
         {
            holder = (FullHolder) view.getTag();
         }

         displayImage(holder.m_imageView, position, m_items[position].m_itemImage, time);

         holder.m_titleView.setText(title);
         holder.m_descriptionView.setText(m_items[position].m_itemDescription);
         holder.m_urlView.setText(link);
         setCardAlpha(holder.m_titleView, holder.m_urlView, holder.m_descriptionView, time);
      }
      /* card_no_des_img.xml no description, image, m_title. */
      else if(1 == viewType)
      {
         ImgHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_no_des_img, parent, false);
            holder = new ImgHolder();
            holder.m_titleView = (TextView) view.findViewById(R.id.title);
            holder.m_urlView = (TextView) view.findViewById(R.id.url);
            holder.m_imageView = (ImageView) view.findViewById(R.id.image);
            view.setOnLongClickListener(new OnCardLongClick(m_context));
            view.setTag(holder);
         }
         else
         {
            holder = (ImgHolder) view.getTag();
         }

         displayImage(holder.m_imageView, position, m_items[position].m_itemImage, time);

         holder.m_titleView.setText(title);
         holder.m_urlView.setText(link);
         setCardAlpha(holder.m_titleView, holder.m_urlView, null, time);
      }
      /* card_des_no_img.xml no image, description, title. */
      else if(2 == viewType)
      {
         DesHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_des_no_img, parent, false);
            holder = new DesHolder();
            holder.m_titleView = (TextView) view.findViewById(R.id.title);
            holder.m_urlView = (TextView) view.findViewById(R.id.url);
            holder.m_imageView = (TextView) view.findViewById(R.id.description);
            view.setOnLongClickListener(new OnCardLongClick(m_context));
            view.setTag(holder);
         }
         else
         {
            holder = (DesHolder) view.getTag();
         }

         holder.m_titleView.setText(title);
         holder.m_imageView.setText(m_items[position].m_itemDescription);
         holder.m_urlView.setText(link);
         setCardAlpha(holder.m_titleView, holder.m_urlView, holder.m_imageView, time);
      }
      /* No description or image. */
      else if(3 == viewType)
      {
         BlankHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_no_des_no_img, parent, false);
            holder = new BlankHolder();
            holder.m_titleView = (TextView) view.findViewById(R.id.title);
            holder.m_urlView = (TextView) view.findViewById(R.id.url);
            view.setOnLongClickListener(new OnCardLongClick(m_context));
            view.setTag(holder);
         }
         else
         {
            holder = (BlankHolder) view.getTag();
         }

         holder.m_titleView.setText(title);
         holder.m_urlView.setText(link);

         setCardAlpha(holder.m_titleView, holder.m_urlView, null, time);
      }

      /* The logic that tells whether the item is Read or not. */
      if(View.VISIBLE == parent.getVisibility() && position + 1 < m_items.length &&
            m_touchedScreen)
      {
         S_READ_ITEM_TIMES.add(m_items[position + 1].m_itemTime);
      }

      return view;
   }

   private
   void displayImage(ImageView imageView, int position, String imageName, long time)
   {
      imageView.setImageDrawable(new ColorDrawable(Color.WHITE));
      LayoutParams lp = imageView.getLayoutParams();

      Resources resources = m_context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      int screenWidth = displayMetrics.widthPixels;

      lp.height = (int) Math.round((double) screenWidth / m_items[position].m_imageWidth *
            m_items[position].m_imageHeight);
      imageView.setLayoutParams(lp);
      imageView.setTag(position);

      AsyncLoadImage task = new AsyncLoadImage();
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {

         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageView, position, imageName,
               m_context, time);
      }
      else
      {
         task.execute(imageView, position, imageName, m_context, time);
      }
   }

   @Override
   public
   int getItemViewType(int position)
   {
      boolean img = 0 != m_items[position].m_imageWidth;

      boolean des = null != m_items[position].m_itemDescription &&
            0 != m_items[position].m_itemDescription.length();

      if(img && des)
      {
         return 0;
      }
      if(img)
      {
         return 1;
      }
      if(des)
      {
         return 2;
      }
      return 3;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return VIEW_TYPE_COUNT;
   }

   private
   void setCardAlpha(TextView title, TextView url, TextView des, Long time)
   {
      if(S_READ_ITEM_TIMES.contains(time))
      {
         title.setTextColor(m_titleGrey);
         url.setTextColor(m_linkGrey);

         if(null != des)
         {
            des.setTextColor(m_descriptionGrey);
         }
      }
      else
      {
         title.setTextColor(m_titleBlack);
         url.setTextColor(m_linkBlack);

         if(null != des)
         {
            des.setTextColor(m_descriptionBlack);
         }
      }
   }

   boolean isScreenTouched()
   {
      return m_touchedScreen;
   }

   static
   class FullHolder
   {
      TextView  m_titleView;
      TextView  m_urlView;
      TextView  m_descriptionView;
      ImageView m_imageView;
   }

   static
   class DesHolder
   {
      TextView m_titleView;
      TextView m_urlView;
      TextView m_imageView;
   }

   static
   class ImgHolder
   {
      TextView  m_titleView;
      TextView  m_urlView;
      ImageView m_imageView;
   }

   static
   class BlankHolder
   {
      TextView m_titleView;
      TextView m_urlView;
   }
}
