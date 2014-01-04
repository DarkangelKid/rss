package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

class ViewCustom extends View
{
   static final int IMAGE_HEIGHT = 360;
   private static final int SP = TypedValue.COMPLEX_UNIT_SP;
   static final Paint TITLE_PAINT = new Paint();
   static final Paint LINK_PAINT = new Paint();
   static final Paint DES_PAINT = new Paint();

   static
   {
      TITLE_PAINT.setAntiAlias(true);
      LINK_PAINT.setAntiAlias(true);
      DES_PAINT.setAntiAlias(true);
      TITLE_PAINT.setColor(Color.argb(255, 0, 0, 0));
      LINK_PAINT.setColor(Color.argb(165, 0, 0, 0));
      DES_PAINT.setColor(Color.argb(205, 0, 0, 0));
   }

   String m_title = "Initial Text";
   String m_link = "Initial Text";
   String m_linkFull = "Initial Text";
   final String[] m_desLines = new String[3];
   Bitmap m_image;
   private final int m_height;

   ViewCustom(Context context, int height)
   {
      super(context);
      setLayerType(LAYER_TYPE_HARDWARE, null);

      m_height = height;

      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float eightFloatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics);
      int eightDp = Math.round(eightFloatDp);

      setTextSizes();
      setBackgroundColor(Color.WHITE);
      setPadding(eightDp, eightDp, eightDp, eightDp);
   }

   private
   void setTextSizes()
   {
      Context context = getContext();
      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();

      float titleSp = TypedValue.applyDimension(SP, 16.0F, metrics);
      float linkSp = TypedValue.applyDimension(SP, 12.0F, metrics);
      float descriptionSp = TypedValue.applyDimension(SP, 14.0F, metrics);

      TITLE_PAINT.setTextSize(titleSp);
      LINK_PAINT.setTextSize(linkSp);
      DES_PAINT.setTextSize(descriptionSp);
   }

   void setBitmap(Bitmap bitmap)
   {
      m_image = bitmap;
      if(null != bitmap)
      {
         invalidate();
      }
   }

   void setTexts(String title, String link, String linkFull, String desOne, String desTwo,
         String desThree)
   {
      m_title = title;
      m_link = link;
      m_linkFull = linkFull;
      m_desLines[0] = desOne;
      m_desLines[1] = desTwo;
      m_desLines[2] = desThree;
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}
