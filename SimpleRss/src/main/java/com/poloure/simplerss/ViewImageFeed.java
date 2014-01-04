package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

class ViewImageFeed extends ViewCustom
{
   ViewImageFeed(Context context, int height)
   {
      super(context, height);
      setLayerType(LAYER_TYPE_HARDWARE, null);
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      float paddingStart = getPaddingLeft();
      float paddingVertical = getPaddingTop();

      float linkHeight = LINK_PAINT.getTextSize();
      float titleHeight = TITLE_PAINT.getTextSize();
      float desHeight = DES_PAINT.getTextSize();

      float verticalPosition = paddingVertical + 20.0F;
      canvas.drawText(m_title, paddingStart, verticalPosition, TITLE_PAINT);

      verticalPosition += titleHeight;
      canvas.drawText(m_link, paddingStart, verticalPosition, LINK_PAINT);

      verticalPosition += linkHeight;
      Bitmap bitmap = m_image;
      if(null != bitmap)
      {
         canvas.drawBitmap(bitmap, 0.0F, verticalPosition, TITLE_PAINT);
      }

      verticalPosition += IMAGE_HEIGHT + 32;
      canvas.drawText(m_desLines[0], paddingStart, verticalPosition, DES_PAINT);

      verticalPosition += desHeight;
      canvas.drawText(m_desLines[1], paddingStart, verticalPosition, DES_PAINT);

      verticalPosition += desHeight;
      canvas.drawText(m_desLines[2], paddingStart, verticalPosition, DES_PAINT);
   }
}
