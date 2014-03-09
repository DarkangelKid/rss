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
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

class ViewNavItem extends View
{
   private static final Paint[] m_paints = new Paint[2];
   private static final Bitmap[] m_bitmaps_dark = new Bitmap[2];
   private static final Bitmap[] m_bitmaps_light = new Bitmap[2];

   String m_text = "";
   String m_count = "";
   int m_image = -1;

   private final int m_height;

   private static final int[] FONT_SIZES = {
         R.dimen.navigation_main, R.dimen.navigation_tag,
   };

   ViewNavItem(Context context)
   {
      super(context);
      Resources resources = context.getResources();

      if(null == m_bitmaps_dark[0])
      {
         int[] drawables_dark = {
               R.drawable.ic_action_storage, R.drawable.ic_action_settings,
         };

         int[] drawables_light = {
               R.drawable.ic_action_storage_light, R.drawable.ic_action_settings_light,
         };

         for(int i = 0; i < m_bitmaps_dark.length; i++)
         {
            m_bitmaps_dark[i] = BitmapFactory.decodeResource(resources, drawables_dark[i]);
            m_bitmaps_light[i] = BitmapFactory.decodeResource(resources, drawables_light[i]);
         }
      }

      m_height = Math.round(resources.getDimension(R.dimen.navigation_height));
      if(Build.VERSION_CODES.JELLY_BEAN > Build.VERSION.SDK_INT)
      {
         setBackgroundDrawable(resources.getDrawable(R.drawable.navigation_item_background));
      }
      else
      {
         setBackground(resources.getDrawable(R.drawable.navigation_item_background));
      }
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

      Paint paint = -1 == m_image ? m_paints[1] : m_paints[0];
      long verticalPosition = Math.round(m_height / 2.0 - (paint.descent() + paint.ascent()) / 2.0);

      boolean rtl = Utilities.isTextRtl(m_text);

      paint.setColor(isActivated() ? Color.WHITE : resources.getColor(R.color.text_navigation_main));

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
         Bitmap icon = isActivated() ? m_bitmaps_light[m_image] : m_bitmaps_dark[m_image];
         int imageWidth = icon.getWidth();
         int paddingTop = Utilities.EIGHT_DP;
         canvas.drawBitmap(icon, rtl ? width - paddingStart - imageWidth : paddingStart, Math.round(paddingTop), m_paints[0]);
         hPadding = (paddingStart << 1) + imageWidth;
      }

      /* Draw the main text. */
      canvas.drawText(m_text, rtl ? width - hPadding : hPadding, verticalPosition, paint);
   }

   private static
   void initPaints(Resources resources)
   {
      for(int i = 0; m_paints.length > i; i++)
      {
         m_paints[i] = configurePaint(resources, FONT_SIZES[i]);
      }
   }

   private static
   Paint configurePaint(Resources resources, int dimenResource)
   {
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setTextSize(resources.getDimension(dimenResource));
      return paint;
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}
