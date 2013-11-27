package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public
class LightView extends View
{

   private final Paint m_paint = new Paint();
   private String m_text = "Initial Text";
   private int m_height;

   LightView(Context context)
   {
      super(context);
      m_paint.setAntiAlias(true);
   }

   public
   void setTextSize(float textSize)
   {
      m_paint.setTextSize(textSize);
   }

   public
   void setTextColor(int textColor)
   {
      m_paint.setColor(textColor);
   }

   public
   void setHeight(int height)
   {
      m_height = height;
   }

   public
   void setText(String text)
   {
      m_text = text;
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      canvas.drawText(m_text, getPaddingStart(), getPaddingTop() + 20, m_paint);
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}
