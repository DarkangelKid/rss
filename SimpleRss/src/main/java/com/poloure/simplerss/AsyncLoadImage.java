package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;

import java.lang.ref.WeakReference;

class AsyncLoadImage extends AsyncTask<Object, Void, Bitmap>
{
   private static final int IMAGE_FADE_IN_DURATION = 166;
   private final WeakReference<ViewCustom> m_view;
   private final int m_viewTag;

   private
   AsyncLoadImage(ViewCustom view, int viewTag)
   {
      m_view = new WeakReference<ViewCustom>(view);
      m_viewTag = viewTag;
   }

   static
   void newInstance(ViewCustom view, String applicationFolder, String imageName, int viewTag,
         Context context)
   {
      AsyncTask<Object, Void, Bitmap> task = new AsyncLoadImage(view, viewTag);

      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, applicationFolder, imageName, context);
   }

   @Override
   protected
   Bitmap doInBackground(Object... params)
   {
      String imagePath = params[0] + (String) params[1];

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inSampleSize = 1;

      View viewReference = m_view.get();
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

      ViewCustom view = m_view.get();
      if(null != view && (Integer) view.getTag() == m_viewTag && null != result)
      {
         AlphaAnimation animation = new AlphaAnimation(0.0F, 1.0F);
         animation.setDuration(IMAGE_FADE_IN_DURATION);
         animation.setFillAfter(true);
         view.setBitmap(result);
         view.setAnimation(animation);
      }
   }
}
