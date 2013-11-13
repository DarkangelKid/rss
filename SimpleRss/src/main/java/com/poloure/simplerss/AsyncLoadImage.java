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
   private static final short IMAGE_FADE_IN_DURATION = (short) 240;
   private final ImageView m_imageView;
   private final int m_imageViewTag;

   private
   AsyncLoadImage(ImageView imageView, int imageViewTag)
   {
      m_imageView = imageView;
      m_imageViewTag = imageViewTag;
   }

   static
   void newInstance(ImageView imageView, String imagePath, int imageViewTag, Context context,
         boolean isRead, float opacity)
   {
      AsyncTask<Object, Void, Object[]> task = new AsyncLoadImage(imageView, imageViewTag);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {

         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath, context,
               isRead, opacity);
      }
      else
      {
         task.execute(imageViewTag, imagePath, context, isRead, opacity);
      }
   }

   @Override
   protected
   Object[] doInBackground(Object... params)
   {
      String imagePath = (String) params[0];
      Context context = (Context) params[1];
      boolean isRead = (Boolean) params[2];
      float imageOpacity = isRead ? (Float) params[3] : 1.0F;

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inSampleSize = 1;
      Animation fadeIn = new AlphaAnimation(0.0F, imageOpacity);
      fadeIn.setDuration((long) IMAGE_FADE_IN_DURATION);
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
         if((Integer) m_imageView.getTag() == m_imageViewTag)
         {
            m_imageView.setImageBitmap((Bitmap) result[0]);
            m_imageView.startAnimation((Animation) result[1]);
            m_imageView.setVisibility(View.VISIBLE);
         }
      }
   }
}
