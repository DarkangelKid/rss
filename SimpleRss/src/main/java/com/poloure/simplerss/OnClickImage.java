package com.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import java.io.File;

class OnClickImage implements View.OnClickListener
{
   private static final String IMAGE_TYPE = "image" + File.separatorChar;
   private static final char WEB_FILE_SUFFIX_SEPARATOR = '.';
   private final String m_imagePath;
   private final Context m_context;

   OnClickImage(String imagePath, Context context)
   {
      m_context = context;
      m_imagePath = imagePath;
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

      intent.setDataAndType(uri, IMAGE_TYPE + type);

      m_context.startActivity(intent);
   }
}
