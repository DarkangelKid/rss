package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

class AsyncLoadImage extends AsyncTask<Object, Void, Object[]>
{
   private static final int IMAGE_FADE_IN_DURATION = 666;
   private final WeakReference<ImageView> m_imageView;
   private final int m_imageViewTag;

   private
   AsyncLoadImage(ImageView imageView, int imageViewTag)
   {
      m_imageView = new WeakReference<ImageView>(imageView);
      m_imageViewTag = imageViewTag;
   }

   static
   void newInstance(ImageView imageView, String applicationFolder, String imageName,
         int imageViewTag, Context context)
   {
      AsyncTask<Object, Void, Object[]> task = new AsyncLoadImage(imageView, imageViewTag);

      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, applicationFolder, imageName, context);
   }

   @Override
   protected
   Object[] doInBackground(Object... params)
   {
      String imagePath = params[0] + (String) params[1];
      Context context = (Context) params[2];

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inSampleSize = 1;
      Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
      fadeIn.setDuration((long) IMAGE_FADE_IN_DURATION);
      fadeIn.setFillAfter(true);
      fadeIn.setInterpolator(new DecelerateInterpolator());

      ImageView imageView = m_imageView.get();
      if(null == imageView)
      {
         cancel(true);
         return null;
      }

      imageView.setOnClickListener(new OnClickImage(imagePath, context));

      return new Object[]{
            BitmapFactory.decodeFile(imagePath, o), fadeIn
      };
   }

   @Override
   protected
   void onPostExecute(Object... result)
   {
      if(isCancelled())
      {
         return;
      }

      ImageView imageView = m_imageView.get();
      if(null != imageView && (Integer) imageView.getTag() == m_imageViewTag && null != result[0])
      {
         imageView.setVisibility(View.VISIBLE);
         imageView.setImageBitmap((Bitmap) result[0]);
         imageView.startAnimation((Animation) result[1]);
      }
   }
}
