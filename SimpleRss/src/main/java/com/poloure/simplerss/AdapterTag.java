package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Set;

class AdapterTag extends BaseAdapter
{
   static final         Set<String> s_readLinks             = Read.set(Constants.READ_ITEMS);
   private static final int         SCREEN_WIDTH            = Util.getScreenWidth();
   private static final int         VIEW_TYPE_COUNT         = 4;
   private static final float       READ_ITEM_IMAGE_OPACITY = 0.5f;
   private final LayoutInflater m_inflater;
   boolean    m_touchedScreen = true;
   FeedItem[] m_items         = new FeedItem[0];

   AdapterTag(Context context)
   {
      m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   void prependArray(FeedItem... items)
   {
      m_items = concat(items, m_items);
   }

   private static
   <T> T[] concat(T[] first, T[]... rest)
   {
      int length = first.length;
      for(T[] array : rest)
      {
         length += array.length;
      }

      @SuppressWarnings("unchecked") T[] result = (T[]) Array.newInstance(
            first.getClass().getComponentType(), length);
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

      String link = m_items[position].url;
      String title = m_items[position].title;

      /* card_full.xml img && m_des. */
      if(0 == viewType)
      {
         FullHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_full, parent, false);
            holder = new FullHolder();
            holder.m_title = (TextView) view.findViewById(R.id.title);
            holder.m_url = (TextView) view.findViewById(R.id.url);
            holder.m_des = (TextView) view.findViewById(R.id.description);
            holder.m_imageView = (ImageView) view.findViewById(R.id.image);
            view.setOnLongClickListener(new OnCardLongClick());
            view.setTag(holder);
         }
         else
         {
            holder = (FullHolder) view.getTag();
         }

         displayImage(holder.m_imageView, position, m_items[position].image);

         holder.m_title.setText(title);
         holder.m_des.setText(m_items[position].description);
         holder.m_url.setText(link);
         setCardAlpha(holder.m_title, holder.m_url, holder.m_imageView, holder.m_des, link);
      }
      /* card_no_des_img.xml no description, image, m_title. */
      else if(1 == viewType)
      {
         ImgHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_no_des_img, parent, false);
            holder = new ImgHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.url = (TextView) view.findViewById(R.id.url);
            holder.image = (ImageView) view.findViewById(R.id.image);
            view.setOnLongClickListener(new OnCardLongClick());
            view.setTag(holder);
         }
         else
         {
            holder = (ImgHolder) view.getTag();
         }

         displayImage(holder.image, position, m_items[position].image);

         holder.title.setText(title);
         holder.url.setText(link);
         setCardAlpha(holder.title, holder.url, holder.image, null, link);
      }
      /* card_des_no_img.xml no image, description, title. */
      else if(2 == viewType)
      {
         DesHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_des_no_img, parent, false);
            holder = new DesHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.url = (TextView) view.findViewById(R.id.url);
            holder.des = (TextView) view.findViewById(R.id.description);
            view.setOnLongClickListener(new OnCardLongClick());
            view.setTag(holder);
         }
         else
         {
            holder = (DesHolder) view.getTag();
         }

         holder.title.setText(title);
         holder.des.setText(m_items[position].description);
         holder.url.setText(link);
         setCardAlpha(holder.title, holder.url, null, holder.des, link);
      }
      /* No description or image. */
      else if(3 == viewType)
      {
         BlankHolder holder;
         if(null == view)
         {
            view = m_inflater.inflate(R.layout.card_no_des_no_img, parent, false);
            holder = new BlankHolder();
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.url = (TextView) view.findViewById(R.id.url);
            view.setOnLongClickListener(new OnCardLongClick());
            view.setTag(holder);
         }
         else
         {
            holder = (BlankHolder) view.getTag();
         }

         holder.title.setText(title);
         holder.url.setText(link);

         setCardAlpha(holder.title, holder.url, null, null, link);
      }

      /* The logic that tells whether the item is Read or not. */
      if(View.VISIBLE == parent.getVisibility() && position + 1 < m_items.length &&
            m_touchedScreen)
      {
         s_readLinks.add(m_items[position + 1].url);
      }

      return view;
   }

   private static
   void setCardAlpha(TextView title, TextView url, ImageView image, TextView des, String link)
   {
      Resources res = Util.getContext().getResources();

      if(s_readLinks.contains(link))
      {
         title.setTextColor(res.getColor(R.color.title_grey));
         url.setTextColor(res.getColor(R.color.link_grey));

         if(null != des)
         {
            des.setTextColor(res.getColor(R.color.des_grey));
         }

         if(null != image)
         {
            image.setAlpha(READ_ITEM_IMAGE_OPACITY);
         }
      }
      else
      {
         Write.log("Not read.");
         title.setTextColor(res.getColor(R.color.title_black));
         url.setTextColor(res.getColor(R.color.link_black));

         if(null != des)
         {
            des.setTextColor(res.getColor(R.color.des_black));
         }

         if(null != image)
         {
            image.setAlpha(1.0f);
         }
      }
   }

   @Override
   public
   int getItemViewType(int position)
   {
      boolean img = 0 != m_items[position].width;

      boolean des = null != m_items[position].description &&
            0 != m_items[position].description.length();

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
   void displayImage(ImageView v, int p, String imageName)
   {
      v.setImageDrawable(new ColorDrawable(Color.WHITE));
      LayoutParams lp = v.getLayoutParams();

      lp.height = (int) Math.round((double) SCREEN_WIDTH / m_items[p].width * m_items[p].height);
      v.setLayoutParams(lp);
      v.setTag(p);

      if(Constants.HONEYCOMB)
      {

         new AsyncLoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, v, v.getTag(),
               imageName);
      }
      else
      {
         new AsyncLoadImage().execute(v, v.getTag(), imageName);
      }
   }

   boolean isScreenTouched()
   {
      return m_touchedScreen;
   }

   static
   class FullHolder
   {
      TextView  m_title;
      TextView  m_url;
      TextView  m_des;
      ImageView m_imageView;
   }

   static
   class DesHolder
   {
      TextView title;
      TextView url;
      TextView des;
   }

   static
   class ImgHolder
   {
      TextView  title;
      TextView  url;
      ImageView image;
   }

   static
   class BlankHolder
   {
      TextView title;
      TextView url;
   }

}
