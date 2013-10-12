package com.poloure.simplerss;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import java.io.File;

class OnClickImage implements View.OnClickListener
{
   private final String m_imagePath;

   OnClickImage(String im)
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
