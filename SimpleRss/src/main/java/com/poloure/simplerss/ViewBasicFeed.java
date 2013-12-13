package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

class ViewBasicFeed extends View
{
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
      TITLE_PAINT.measureText("2");
   }

   private static final int SP = TypedValue.COMPLEX_UNIT_SP;
   private static final int HEIGHT = 180;
   String m_title = "Initial Text";
   String m_link = "Initial Text";
   String[] m_desLines = new String[3];

   ViewBasicFeed(Context context)
   {
      super(context);
      setLayerType(LAYER_TYPE_HARDWARE, null);
   }

   static
   View newInstance(Context context)
   {
      ViewBasicFeed view = new ViewBasicFeed(context);

      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float eightFloatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics);
      int eightDp = Math.round(eightFloatDp);

      view.setTextSizes();
      view.setBackgroundColor(Color.WHITE);
      view.setPadding(eightDp, eightDp, eightDp, eightDp);

      return view;
   }

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

   void setTexts(String title, String link, String desOne, String desTwo, String desThree)
   {
      m_title = title;
      m_link = link;
      m_desLines[0] = desOne;
      m_desLines[1] = desTwo;
      m_desLines[2] = desThree;
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      float paddingStart = (float) getPaddingLeft();
      float paddingTop = (float) getPaddingTop();

      float linkHeight = LINK_PAINT.getTextSize();
      float titleHeight = TITLE_PAINT.getTextSize();
      float desHeight = DES_PAINT.getTextSize();

      float verticalPosition = paddingTop + 20;
      canvas.drawText(m_title, paddingStart, verticalPosition, TITLE_PAINT);

      verticalPosition += titleHeight - 4;
      canvas.drawText(m_link, paddingStart, verticalPosition, LINK_PAINT);

      verticalPosition += linkHeight + 10;
      canvas.drawText(m_desLines[0], paddingStart, verticalPosition, DES_PAINT);

      verticalPosition += desHeight;
      canvas.drawText(m_desLines[1], paddingStart, verticalPosition, DES_PAINT);

      verticalPosition += desHeight;
      canvas.drawText(m_desLines[2], paddingStart, verticalPosition, DES_PAINT);
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, HEIGHT);
   }
}
