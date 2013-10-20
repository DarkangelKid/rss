package com.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import java.io.File;

class OnClickImage implements View.OnClickListener
{
   private static final String IMAGE_TYPE = "image" + Constants.SEPAR;
   private final String  m_imagePath;
   private final Context m_context;
   private static final char WEB_FILE_SUFFIX_SEPARATOR = '.';

   OnClickImage(String im, Context context)
   {
      m_context = context;
      m_imagePath = im;
   }

   @Override
   public
   void onClick(View v)
   {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_VIEW);
      int index = m_imagePath.lastIndexOf(WEB_FILE_SUFFIX_SEPARATOR) + 1;
      String type = m_imagePath.substring(index);

      Uri uri = Uri.fromFile(new File(m_imagePath));

      if(Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT)
      {
         intent.setDataAndTypeAndNormalize(uri, IMAGE_TYPE + type);
      }
      else
      {
         intent.setDataAndType(uri, IMAGE_TYPE + type);
      }

      m_context.startActivity(intent);
   }
}
