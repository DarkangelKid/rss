package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Canvas;

class ViewBasicFeed extends ViewCustom
{
   ViewBasicFeed(Context context, int height)
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
      canvas.drawText(m_desLines[0], paddingStart, verticalPosition, DES_PAINT);

      verticalPosition += desHeight;
      canvas.drawText(m_desLines[1], paddingStart, verticalPosition, DES_PAINT);

      verticalPosition += desHeight;
      canvas.drawText(m_desLines[2], paddingStart, verticalPosition, DES_PAINT);
   }
}
