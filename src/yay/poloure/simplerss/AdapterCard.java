package yay.poloure.simplerss;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
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
import java.util.Arrays;
import java.util.regex.Pattern;

class AdapterCard extends BaseAdapter
{
   private static final Pattern PATTERN_THUMBNAILS = Pattern.compile(Constants.THUMBNAILS);
   private static final int     SCREEN_WIDTH       = Util.getScreenWidth();
   static final         int     EIGHT              = Math.round(
         8.0F * Util.getContext().getResources().getDisplayMetrics().density + 0.5f);

   boolean m_touchedScreen = true;
   FeedItem[] m_items;
   private boolean m_firstGetItem = true;
   private ListView m_listview;

   private static final int VIEW_TYPE_COUNT = 4;

   private static
   <T> T[] concat(T[] first, T... second)
   {
      if(null == first)
      {
         return second;
      }
      if(null == second)
      {
         return first;
      }
      T[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   void prependArray(FeedItem... items)
   {
      m_items = concat(items, m_items);
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
         m_listview = (ListView) parent;
         m_listview.setScrollingCacheEnabled(false);
         m_listview.setOnScrollListener(new CardScrollListener());
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
            cv1.setOnClickListener(new WebviewMode());
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
            cv1.setOnClickListener(new WebviewMode());
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
      }
      /* card_des_no_img.xml no image, descirition, m_title. */
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
            cv1.setOnClickListener(new WebviewMode());
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
            cv1.setOnClickListener(new WebviewMode());
            cv1.setOnLongClickListener(new OnCardLongClick());
            cv1.setTag(holder);
         }
         else
         {
            holder = (BlankHolder) cv1.getTag();
         }

         holder.title.setText(title);
         holder.url.setText(link);
      }

      /* The logic that tells whether the item is Read or not. */
      if(View.VISIBLE == m_listview.getVisibility() && 0 <= position - 1 && m_touchedScreen)
      {
         Util.SetHolder.READ_ITEMS.add(m_items[position - 1].url);
      }

      return cv1;
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

   @Override
   public
   int getItemViewType(int position)
   {
      boolean img = 0 != m_items[position].width;

      boolean des = null != m_items[position].description &&
            !m_items[position].description.isEmpty();

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

   private static
   class WebviewMode implements View.OnClickListener
   {
      WebviewMode()
      {
      }

      @Override
      public
      void onClick(View v)
      {
         FeedsActivity.s_actionBar.setTitle(Constants.OFFLINE);
         NavDrawer.s_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
         NavDrawer.s_drawerToggle.setDrawerIndicatorEnabled(false);
         FeedsActivity.s_actionBar.setDisplayHomeAsUpEnabled(true);
         FeedsActivity.s_fragmentManager
               .beginTransaction()
               .hide(FeedsActivity.s_fragmentManager.findFragmentByTag(NavDrawer.NAV_TITLES[0]))
               .add(R.id.drawer_layout, new FragmentWebView(), Constants.OFFLINE)
               .addToBackStack("BACK")
               .commit();
      }
   }

   static
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
      CardScrollListener()
      {
      }

      @Override
      public
      void onScroll(AbsListView v, int fir, int visible, int total)
      {
      }

      @Override
      public
      void onScrollStateChanged(AbsListView view, int scrollState)
      {
         if(m_listview.getChildAt(0).getTop() == EIGHT &&
               View.VISIBLE == m_listview.getVisibility() && m_touchedScreen)
         {
            Util.SetHolder.READ_ITEMS.add(m_items[0].url);
         }

         if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
         {
            Update.navigation();
         }
      }
   }
}
