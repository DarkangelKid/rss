package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Canvas;

class ViewBasicSansDesFeed extends ViewCustom
{
   ViewBasicSansDesFeed(Context context, int height)
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

      float titleHeight = TITLE_PAINT.getTextSize();

      float verticalPosition = paddingVertical + 20.0F;
      canvas.drawText(m_title, paddingStart, verticalPosition, TITLE_PAINT);

      verticalPosition += titleHeight;
      canvas.drawText(m_link, paddingStart, verticalPosition, LINK_PAINT);
   }
}
