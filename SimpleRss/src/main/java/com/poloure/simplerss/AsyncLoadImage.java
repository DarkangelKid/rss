package com.poloure.simplerss;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.util.regex.Pattern;

class AsyncLoadImage extends AsyncTask<Object, Void, Object[]>
{
   private static final long    IMAGE_FADE_IN_DURATION = 240L;
   private static final Pattern PATTERN_THUMBNAILS     = Pattern.compile(Constants.THUMBNAILS);
   private ImageView m_imageView;
   private int       m_imageViewTag;

   @Override
   protected
   Object[] doInBackground(Object... params)
   {
      m_imageView = (ImageView) params[0];
      m_imageViewTag = (Integer) params[1];
      String imageName = (String) params[2];

      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inSampleSize = 1;
      Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
      fadeIn.setDuration(IMAGE_FADE_IN_DURATION);
      fadeIn.setInterpolator(new DecelerateInterpolator());

      String image = Util.getStorage() +
            PATTERN_THUMBNAILS.matcher(imageName).replaceAll(Constants.IMAGES);
      m_imageView.setOnClickListener(new OnClickImage(image));
      return new Object[]{
            BitmapFactory.decodeFile(Util.getStorage() + imageName, o), fadeIn
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
