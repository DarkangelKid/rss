/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

class ViewNavItem extends View
{
   final Paint[] m_paints = new Paint[2];
   private static final Bitmap[] m_bitmaps = new Bitmap[3];

   String m_text = "";
   String m_count = "";
   int m_image = -1;

   private final int m_height;

   static final int[] FONT_SIZES = {
         R.dimen.navigation_main, R.dimen.navigation_tag,
   };

   ViewNavItem(Context context)
   {
      super(context);
      Resources resources = context.getResources();

      if(null == m_bitmaps[0])
      {
         int[] drawables = {
               R.drawable.action_feeds, R.drawable.action_manage, R.drawable.action_settings
         };
         for(int i = 0; i < m_bitmaps.length; i++)
         {
            m_bitmaps[i] = BitmapFactory.decodeResource(resources, drawables[i]);
         }
      }

      m_height = Math.round(resources.getDimension(R.dimen.navigation_height));
      setLayerType(LAYER_TYPE_HARDWARE, null);
      initPaints(resources);
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      Resources resources = getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();

      /* If our paints have been cleared from memory, remake them. */
      if(null == m_paints[0])
      {
         initPaints(resources);
      }

      /* Padding top. */
      float width = getWidth();

      int hPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16.0F, metrics));
      int paddingStart = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics));
      int paddingTop = paddingStart;

      Paint paint = -1 == m_image ? m_paints[1] : m_paints[0];
      float verticalPosition = Math.round((m_height + paint.getTextSize()) / 2.0);

      boolean rtl = Utilities.isTextRtl(m_text);

      /* Draw the count. */
      if(-1 == m_image)
      {
         paint.setTextAlign(rtl ? Paint.Align.LEFT : Paint.Align.RIGHT);
         canvas.drawText(m_count, rtl ? hPadding : width - hPadding, verticalPosition, paint);
      }

      paint.setTextAlign(rtl ? Paint.Align.RIGHT : Paint.Align.LEFT);

      /* Draw the image. */
      if(-1 != m_image)
      {
         int imageWidth = m_bitmaps[m_image].getWidth();
         canvas.drawBitmap(m_bitmaps[m_image], rtl ? width - paddingStart - imageWidth : paddingStart, Math
               .round(paddingTop), m_paints[0]);
         hPadding += paddingStart + imageWidth;
      }

      /* Draw the main text. */
      canvas.drawText(m_text, rtl ? width - hPadding : hPadding, verticalPosition, paint);
   }

   void initPaints(Resources resources)
   {
      for(int i = 0; m_paints.length > i; i++)
      {
         m_paints[i] = configurePaint(resources, FONT_SIZES[i], android.R.color.white);
      }
   }

   static
   Paint configurePaint(Resources resources, int dimenResource, int colorResource)
   {
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setTextSize(resources.getDimension(dimenResource));
      paint.setColor(resources.getColor(colorResource));
      return paint;
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}
