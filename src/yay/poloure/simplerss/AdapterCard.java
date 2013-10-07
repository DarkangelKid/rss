package yay.poloure.simplerss;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Set;
import java.util.regex.Pattern;

class AdapterCard extends BaseAdapter
{
   static final         int         EIGHT              = Math.round(
         8.0F * Util.getContext().getResources().getDisplayMetrics().density + 0.5f);
   static final         Set<String> s_readLinks        = Read.set(Constants.READ_ITEMS);
   private static final Pattern     PATTERN_THUMBNAILS = Pattern.compile(Constants.THUMBNAILS);
   private static final int         SCREEN_WIDTH       = Util.getScreenWidth();
   private static final int         VIEW_TYPE_COUNT    = 4;
   private final BaseAdapter m_navigationAdapter;
   boolean    m_touchedScreen = true;
   FeedItem[] m_items         = new FeedItem[0];
   private boolean m_firstGetItem = true;
   private ListView m_listView;

   AdapterCard(BaseAdapter navigationAdapter)
   {
      m_navigationAdapter = navigationAdapter;
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
   String getItem(int position)
   {
      return m_items[position].title;
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View cv, ViewGroup parent)
   {
      View cv1 = cv;
      int viewType = getItemViewType(position);

      if(m_firstGetItem)
      {
         m_listView = (ListView) parent;
         m_listView.setScrollingCacheEnabled(false);
         m_listView.setOnScrollListener(new CardScrollListener());
         m_firstGetItem = false;
      }

      LayoutInflater inflater = Util.getLayoutInflater();
      String link = m_items[position].url;
      String title = m_items[position].title;

      /* card_full.xml img && m_des. */
      if(0 == viewType)
      {
         FullHolder holder;
         if(null == cv1)
         {
            cv1 = inflater.inflate(R.layout.card_full, parent, false);
            holder = new FullHolder();
            holder.m_title = (TextView) cv1.findViewById(R.id.title);
            holder.m_url = (TextView) cv1.findViewById(R.id.url);
            holder.m_des = (TextView) cv1.findViewById(R.id.description);
            holder.m_imageView = (ImageView) cv1.findViewById(R.id.image);
            cv1.setOnLongClickListener(new OnCardLongClick());
            cv1.setTag(holder);
         }
         else
         {
            holder = (FullHolder) cv1.getTag();
         }

         displayImage(holder.m_imageView, position);

         holder.m_title.setText(title);
         holder.m_des.setText(m_items[position].description);
         holder.m_url.setText(link);
         setCardAlpha(holder.m_title, holder.m_url, holder.m_imageView, holder.m_des, link);
      }
      /* card_no_des_img.xml no description, image, m_title. */
      else if(1 == viewType)
      {
         ImgHolder holder;
         if(null == cv1)
         {
            cv1 = inflater.inflate(R.layout.card_no_des_img, parent, false);
            holder = new ImgHolder();
            holder.title = (TextView) cv1.findViewById(R.id.title);
            holder.url = (TextView) cv1.findViewById(R.id.url);
            holder.image = (ImageView) cv1.findViewById(R.id.image);
            cv1.setOnLongClickListener(new OnCardLongClick());
            cv1.setTag(holder);
         }
         else
         {
            holder = (ImgHolder) cv1.getTag();
         }

         displayImage(holder.image, position);

         holder.title.setText(title);
         holder.url.setText(link);
         setCardAlpha(holder.title, holder.url, holder.image, null, link);
      }
      /* card_des_no_img.xml no image, description, title. */
      else if(2 == viewType)
      {
         DesHolder holder;
         if(null == cv1)
         {
            cv1 = inflater.inflate(R.layout.card_des_no_img, parent, false);
            holder = new DesHolder();
            holder.title = (TextView) cv1.findViewById(R.id.title);
            holder.url = (TextView) cv1.findViewById(R.id.url);
            holder.des = (TextView) cv1.findViewById(R.id.description);
            cv1.setOnLongClickListener(new OnCardLongClick());
            cv1.setTag(holder);
         }
         else
         {
            holder = (DesHolder) cv1.getTag();
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
         if(null == cv1)
         {
            cv1 = inflater.inflate(R.layout.card_no_des_no_img, parent, false);
            holder = new BlankHolder();
            holder.title = (TextView) cv1.findViewById(R.id.title);
            holder.url = (TextView) cv1.findViewById(R.id.url);
            cv1.setOnLongClickListener(new OnCardLongClick());
            cv1.setTag(holder);
         }
         else
         {
            holder = (BlankHolder) cv1.getTag();
         }

         holder.title.setText(title);
         holder.url.setText(link);

         setCardAlpha(holder.title, holder.url, null, null, link);
      }

      /* The logic that tells whether the item is Read or not. */
      if(View.VISIBLE == m_listView.getVisibility() && position + 1 < m_items.length &&
            m_touchedScreen)
      {
         s_readLinks.add(m_items[position + 1].url);
      }

      return cv1;
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
   void displayImage(ImageView v, int p)
   {
      v.setImageDrawable(new ColorDrawable(Color.WHITE));
      LayoutParams lp = v.getLayoutParams();

      lp.height = (int) Math.round((double) SCREEN_WIDTH / m_items[p].width * m_items[p].height);
      v.setLayoutParams(lp);
      v.setTag(p);

      if(Constants.HONEYCOMB)
      {

         new AsyncLoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, v, v.getTag());
      }
      else
      {
         new AsyncLoadImage().execute(v, v.getTag());
      }
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
            image.setAlpha(0.5f);
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

   static
   class FullHolder
   {
      TextView  m_title;
      TextView  m_url;
      TextView  m_des;
      ImageView m_imageView;
   }

   private static
   class DesHolder
   {
      TextView title;
      TextView url;
      TextView des;
   }

   private static
   class ImgHolder
   {
      TextView  title;
      TextView  url;
      ImageView image;
   }

   private static
   class BlankHolder
   {
      TextView title;
      TextView url;
   }

   private static
   class ImageClick implements View.OnClickListener
   {
      final String m_imagePath;

      ImageClick(String im)
      {
         m_imagePath = im;
      }

      @Override
      public
      void onClick(View v)
      {
         Intent intent = new Intent();
         intent.setAction(Intent.ACTION_VIEW);
         int index = m_imagePath.lastIndexOf('.') + 1;
         String type = m_imagePath.substring(index, m_imagePath.length());

         Uri uri = Uri.fromFile(new File(m_imagePath));

         if(Constants.JELLYBEAN)
         {
            intent.setDataAndTypeAndNormalize(uri, Constants.IMAGE_TYPE + type);
         }
         else
         {
            intent.setDataAndType(uri, Constants.IMAGE_TYPE + type);
         }

         Util.getContext().startActivity(intent);
      }
   }

   private
   class AsyncLoadImage extends AsyncTask<Object, Void, Object[]>
   {
      ImageView m_imageView;
      int       m_imageViewTag;

      @Override
      protected
      Object[] doInBackground(Object... params)
      {
         m_imageView = (ImageView) params[0];
         m_imageViewTag = (Integer) params[1];
         BitmapFactory.Options o = new BitmapFactory.Options();
         o.inSampleSize = 1;
         Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
         fadeIn.setDuration(240L);
         fadeIn.setInterpolator(new DecelerateInterpolator());
         String image = Util.getStorage() +
               PATTERN_THUMBNAILS.matcher(m_items[m_imageViewTag].image)
                     .replaceAll(Constants.IMAGES);
         m_imageView.setOnClickListener(new ImageClick(image));
         return new Object[]{
               BitmapFactory.decodeFile(Util.getStorage() + m_items[m_imageViewTag].image, o),
               fadeIn
         };
      }

      @Override
      protected
      void onPostExecute(Object... result)
      {
         if((Integer) m_imageView.getTag() != m_imageViewTag)
         {
            return;
         }
         if(null != m_imageView && null != result[0])
         {
            m_imageView.setImageBitmap((Bitmap) result[0]);
            m_imageView.startAnimation((Animation) result[1]);
            if((Integer) m_imageView.getTag() != m_imageViewTag)
            {
               return;
            }
            m_imageView.setVisibility(View.VISIBLE);
         }
      }
   }

   private
   class CardScrollListener implements AbsListView.OnScrollListener
   {
      @Override
      public
      void onScrollStateChanged(AbsListView view, int scrollState)
      {
         if(m_listView.getChildAt(0).getTop() == EIGHT &&
               View.VISIBLE == m_listView.getVisibility() && m_touchedScreen)
         {
            s_readLinks.add(m_items[0].url);
         }

         if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
         {
            Update.navigation(m_navigationAdapter);
         }
      }

      @Override
      public
      void onScroll(AbsListView v, int fir, int visible, int total)
      {
      }
   }
}
