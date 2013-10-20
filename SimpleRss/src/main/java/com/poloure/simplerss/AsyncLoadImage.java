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

class AsyncLoadImage extends AsyncTask<Object, Void, Object[]>
{
   private static final long  IMAGE_FADE_IN_DURATION  = 240L;
   private static final float READ_ITEM_IMAGE_OPACITY = 0.66F;
   private ImageView m_imageView;
   private int       m_imageViewTag;

   @Override
   protected
   Object[] doInBackground(Object... params)
   {
      m_imageView = (ImageView) params[0];
      m_imageViewTag = (Integer) params[1];
      String imageName = (String) params[2];
      Context context = (Context) params[3];
      long time = (Long) params[4];

      float imageOpacity = AdapterTags.S_READ_ITEM_TIMES.contains(time)
            ? READ_ITEM_IMAGE_OPACITY
            : 1.0F;

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inSampleSize = 1;
      Animation fadeIn = new AlphaAnimation(0.0F, imageOpacity);
      fadeIn.setDuration(IMAGE_FADE_IN_DURATION);
      fadeIn.setFillAfter(true);
      fadeIn.setInterpolator(new DecelerateInterpolator());

      String image = Util.getStorage(context) + imageName;
      m_imageView.setOnClickListener(new OnClickImage(image, context));
      return new Object[]{
            BitmapFactory.decodeFile(image, o), fadeIn
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
