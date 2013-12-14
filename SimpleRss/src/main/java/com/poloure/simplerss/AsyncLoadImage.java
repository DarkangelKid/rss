package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import java.lang.ref.WeakReference;

class AsyncLoadImage extends AsyncTask<Object, Void, Bitmap>
{
   private static final int IMAGE_FADE_IN_DURATION = 666;
   private final WeakReference<View> m_imageView;
   private final int m_imageViewTag;

   private
   AsyncLoadImage(View imageView, int imageViewTag)
   {
      m_imageView = new WeakReference<View>(imageView);
      m_imageViewTag = imageViewTag;
   }

   static
   void newInstance(View imageView, String applicationFolder, String imageName, int imageViewTag,
         Context context)
   {
      AsyncTask<Object, Void, Bitmap> task = new AsyncLoadImage(imageView, imageViewTag);

      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, applicationFolder, imageName, context);
   }

   @Override
   protected
   Bitmap doInBackground(Object... params)
   {
      String imagePath = params[0] + (String) params[1];

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inSampleSize = 1;
      Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
      fadeIn.setDuration((long) IMAGE_FADE_IN_DURATION);
      fadeIn.setFillAfter(true);
      fadeIn.setInterpolator(new DecelerateInterpolator());

      View viewReference = m_imageView.get();
      if(null == viewReference)
      {
         cancel(true);
         return null;
      }

      return BitmapFactory.decodeFile(imagePath, o);
   }

   @Override
   protected
   void onPostExecute(Bitmap result)
   {
      if(isCancelled())
      {
         return;
      }

      final View imageView = m_imageView.get();
      if(null != imageView && (Integer) imageView.getTag() == m_imageViewTag && null != result)
      {
         Class type = imageView.getClass();
         if(type.equals(ViewImageFeed.class))
         {
            ((ViewImageFeed) imageView).setBitmap(result);
         }
         else
         {
            ((ViewImageSansDesFeed) imageView).setBitmap(result);
         }
         imageView.setVisibility(View.VISIBLE);
         Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
         fadeIn.setDuration(100L);

         imageView.setAnimation(fadeIn);
      }
   }
}
