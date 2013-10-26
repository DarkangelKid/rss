package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

class AsyncLoadImage extends AsyncTask<Object, Void, Object[]>
{
   private static final long  IMAGE_FADE_IN_DURATION  = 240L;
   private static final float READ_ITEM_IMAGE_OPACITY = 0.66F;
   private final ImageView m_imageView;
   private       int       m_imageViewTag;

   static
   void newInstance(ImageView imageView, String imagePath, int imageViewTag, Context context,
         boolean isRead)
   {
      AsyncTask<Object, Void, Object[]> task = new AsyncLoadImage(imageView);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {

         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageViewTag, imagePath, context,
               isRead);
      }
      else
      {
         task.execute(imageViewTag, imagePath, context, isRead);
      }
   }

   private
   AsyncLoadImage(ImageView imageView)
   {
      m_imageView = imageView;
   }

   @Override
   protected
   Object[] doInBackground(Object... params)
   {
      m_imageViewTag = (Integer) params[0];
      String imagePath = (String) params[1];
      Context context = (Context) params[2];
      boolean isRead = (Boolean) params[3];

      float imageOpacity = isRead ? READ_ITEM_IMAGE_OPACITY : 1.0F;

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inSampleSize = 1;
      Animation fadeIn = new AlphaAnimation(0.0F, imageOpacity);
      fadeIn.setDuration(IMAGE_FADE_IN_DURATION);
      fadeIn.setFillAfter(true);
      fadeIn.setInterpolator(new DecelerateInterpolator());

      m_imageView.setOnClickListener(new OnClickImage(imagePath, context));
      return new Object[]{
            BitmapFactory.decodeFile(imagePath, o), fadeIn
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
         if((Integer) m_imageView.getTag() == m_imageViewTag)
         {
            m_imageView.setVisibility(View.VISIBLE);
         }
      }
   }
}
