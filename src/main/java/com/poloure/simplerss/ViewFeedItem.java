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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

class ViewFeedItem extends View
{
   static final int IMAGE_HEIGHT = 360;
   final Paint[] m_paints = new Paint[3];
   private static final int SCREEN = Resources.getSystem().getDisplayMetrics().widthPixels;

   private Bitmap m_image;
   FeedItem m_item;
   private final int m_height;

   static final int[] FONT_COLORS = {
         R.color.item_title_color, R.color.item_link_color, R.color.item_description_color,
   };
   static final int[] FONT_SIZES = {
         R.dimen.item_title_size, R.dimen.item_link_size, R.dimen.item_description_size,
   };

   ViewFeedItem(Context context, int height)
   {
      super(context);
      m_height = height;

      setLayerType(LAYER_TYPE_HARDWARE, null);
      setPadding(Utilities.EIGHT_DP, Utilities.EIGHT_DP, Utilities.EIGHT_DP, Utilities.EIGHT_DP);

      initPaints(getResources());
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      /* If the canvas is meant to draw a bitmap but it is null, draw nothing.
         NOTE: This value must change if the AdapterTags heights are changing. */

      /* TODO: This also needs a better implementation as it fails sometimes. */
      if(200 < getHeight() && null == m_image)
      {
         return;
      }

      /* If our paints have been cleared from memory, remake them. */
      if(null == m_paints[0])
      {
         initPaints(getResources());
      }

      float verticalPosition = drawBase(canvas);
      verticalPosition = drawBitmap(canvas, verticalPosition);
      if(null != m_item.m_desLines && 0 != m_item.m_desLines.length && null != m_item.m_desLines[0])
      {
         drawDes(canvas, verticalPosition);
      }
   }

   void initPaints(Resources resources)
   {
      for(int i = 0; m_paints.length > i; i++)
      {
         m_paints[i] = configurePaint(resources, FONT_SIZES[i], FONT_COLORS[i]);
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

   void setBitmap(Bitmap bitmap)
   {
      m_image = bitmap;
      if(null != bitmap)
      {
         invalidate();
      }
   }

   float drawBase(Canvas canvas)
   {
      /* Padding top. */
      float verticalPosition = getPaddingTop() + 20.0F;

      /* Draw the title. */
      if(Utilities.isTextRtl(m_item.m_title))
      {
         m_paints[0].setTextAlign(Paint.Align.RIGHT);
         m_paints[1].setTextAlign(Paint.Align.RIGHT);

         canvas.drawText(m_item.m_title, SCREEN - getPaddingRight(), verticalPosition, m_paints[0]);

         /* Draw the time. */
         m_paints[1].setTextAlign(Paint.Align.LEFT);
         canvas.drawText(getTime(m_item.m_time, getContext()), getPaddingLeft(), verticalPosition, m_paints[1]);
         m_paints[1].setTextAlign(Paint.Align.RIGHT);

         verticalPosition += m_paints[0].getTextSize();

         canvas.drawText(m_item.m_url, SCREEN - getPaddingRight(), verticalPosition, m_paints[1]);

         /* Reset the paints. */
         m_paints[0].setTextAlign(Paint.Align.LEFT);
         m_paints[1].setTextAlign(Paint.Align.LEFT);
      }
      else
      {
         canvas.drawText(m_item.m_title, getPaddingLeft(), verticalPosition, m_paints[0]);

         /* Draw the time. */
         m_paints[1].setTextAlign(Paint.Align.RIGHT);
         canvas.drawText(getTime(m_item.m_time, getContext()), SCREEN - getPaddingRight(), verticalPosition, m_paints[1]);
         m_paints[1].setTextAlign(Paint.Align.LEFT);

         verticalPosition += m_paints[0].getTextSize();

         canvas.drawText(m_item.m_url, getPaddingLeft(), verticalPosition, m_paints[1]);
      }

      return verticalPosition + m_paints[1].getTextSize();
   }

   void drawDes(Canvas canvas, float verticalPosition)
   {
      if(m_item.m_desLines[0].isEmpty())
      {
         return;
      }

      float position = verticalPosition;

      boolean rtl = Utilities.isTextRtl(m_item.m_desLines[0]);
      if(rtl)
      {
         m_paints[2].setTextAlign(Paint.Align.RIGHT);
      }
      for(int i = 0; 3 > i; i++)
      {
         canvas.drawText(m_item.m_desLines[i], rtl ? SCREEN - getPaddingRight() : getPaddingLeft(), position, m_paints[2]);
         position += m_paints[2].getTextSize();
      }
      m_paints[2].setTextAlign(Paint.Align.LEFT);
   }

   float drawBitmap(Canvas canvas, float verticalPosition)
   {
      if(null != m_image)
      {
         canvas.drawBitmap(m_image, 0.0F, verticalPosition, m_paints[0]);
         return verticalPosition + IMAGE_HEIGHT + 32;
      }
      else
      {
         return verticalPosition + Utilities.getDp(4.0F);
      }
   }

   private static
   String getTime(long time, Context context)
   {
      Long timeAgo = Calendar.getInstance().getTimeInMillis() - time;

      /* Format the time. */
      Long[] periods = {timeAgo / 86400000, timeAgo / 3600000 % 24, timeAgo / 60000 % 60};
      String[] timeStrings = context.getResources().getStringArray(R.array.time_initials);
      NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());

      StringBuilder builder = new StringBuilder(48);
      for(int i = 0; periods.length > i; i++)
      {
         if(0L != periods[i])
         {
            builder.append(format.format(periods[i]));
            builder.append(timeStrings[i]);
            builder.append(' ');
         }
      }
      builder.deleteCharAt(builder.length() - 1);
      return builder.toString();
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      setMeasuredDimension(widthMeasureSpec, m_height);
   }
}
