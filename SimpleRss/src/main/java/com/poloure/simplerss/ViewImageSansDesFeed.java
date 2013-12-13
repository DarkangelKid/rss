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

class ViewImageSansDesFeed extends View
{
   private static final int SP = TypedValue.COMPLEX_UNIT_SP;
   private static final Paint TITLE_PAINT = new Paint();
   private static final Paint LINK_PAINT = new Paint();

   static
   {
      TITLE_PAINT.setAntiAlias(true);
      LINK_PAINT.setAntiAlias(true);
      TITLE_PAINT.setColor(Color.argb(255, 0, 0, 0));
      LINK_PAINT.setColor(Color.argb(165, 0, 0, 0));
      TITLE_PAINT.measureText("2");
   }

   private static final int HEIGHT = 500;
   String m_title = "Initial Text";
   String m_link = "Initial Text";
   Bitmap m_image;

   private
   ViewImageSansDesFeed(Context context)
   {
      super(context);
      setLayerType(LAYER_TYPE_HARDWARE, null);
   }

   static
   View newInstance(Context context)
   {
      ViewImageSansDesFeed view = new ViewImageSansDesFeed(context);

      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float eightFloatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics);
      int eightDp = Math.round(eightFloatDp);

      view.setTextSizes();
      view.setBackgroundColor(Color.WHITE);
      view.setPadding(eightDp, eightDp, eightDp, eightDp);

      return view;
   }

   private
   void setTextSizes()
   {
      Context context = getContext();
      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();

      float titleSp = TypedValue.applyDimension(SP, 16.0F, metrics);
      float linkSp = TypedValue.applyDimension(SP, 12.0F, metrics);

      TITLE_PAINT.setTextSize(titleSp);
      LINK_PAINT.setTextSize(linkSp);
   }

   void setBitmap(Bitmap bitmap)
   {
      m_image = bitmap;
      if(null != bitmap)
      {
         invalidate();
      }
   }

   void setTexts(String title, String link)
   {
      m_title = title;
      m_link = link;
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      float paddingStart = (float) getPaddingLeft();
      float paddingTop = (float) getPaddingTop();

      float linkHeight = LINK_PAINT.getTextSize();
      float titleHeight = TITLE_PAINT.getTextSize();

      float verticalPosition = paddingTop + 20;
      canvas.drawText(m_title, paddingStart, verticalPosition, TITLE_PAINT);

      verticalPosition += titleHeight;
      canvas.drawText(m_link, paddingStart, verticalPosition, LINK_PAINT);

      verticalPosition += linkHeight;
      if(null != m_image)
      {
         canvas.drawBitmap(m_image, 0, verticalPosition, TITLE_PAINT);
      }
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, HEIGHT);
   }
}
