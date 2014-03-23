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
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.text.NumberFormat;
import java.util.Locale;

import static com.poloure.simplerss.Constants.s_displayMetrics;

class ViewFeedItem extends View
{
   private static final Paint[] m_paints = new Paint[3];
   private static final int SCREEN = Resources.getSystem().getDisplayMetrics().widthPixels;
   private static final NumberFormat TIME_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());
   private static final int[] FONT_COLORS = {
         R.color.item_title_color, R.color.item_link_color, R.color.item_description_color,
   };
   private static final int[] FONT_SIZES = {
         R.dimen.item_title_size, R.dimen.item_link_size, R.dimen.item_description_size,
   };
   private final int m_height;
   private final String[] m_timeInitials;
   boolean m_hasImage;
   FeedItem m_item;
   private Bitmap m_image;

   ViewFeedItem(Context context, int type)
   {
      super(context);
      Resources resources = context.getResources();

      float titleSize = resources.getDimension(R.dimen.item_title_size);
      float linkSize = resources.getDimension(R.dimen.item_link_size);
      float desSize = resources.getDimension(R.dimen.item_description_size);
      float imageSize = resources.getDimension(R.dimen.max_image_height);

      /* Calculate the size of the view. */
      float base = Constants.s_eightDp + titleSize * 2 + linkSize;
      switch(type)
      {
         case AdapterTags.TYPE_PLAIN:
            base += (float) (3.6 * desSize + getDp(4.0F));
         case AdapterTags.TYPE_PLAIN_SANS_DESCRIPTION:
            base += getDp(4.0F);
            break;
         case AdapterTags.TYPE_IMAGE:
            base += (float) (3.6 * desSize + getDp(20.0F));
         case AdapterTags.TYPE_IMAGE_SANS_DESCRIPTION:
            base += imageSize;
      }
      m_height = Math.round(base);

      m_timeInitials = resources.getStringArray(R.array.time_initials);

      initPaints(resources);
   }

   private static
   void initPaints(Resources resources)
   {
      for(int i = 0; m_paints.length > i; i++)
      {
         m_paints[i] = configurePaint(resources, FONT_SIZES[i], FONT_COLORS[i]);
         if(2 == i)
         {
            m_paints[2].setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
         }
      }
   }

   static
   Paint configurePaint(Resources resources, int dimenResource, int colorResource)
   {
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setTextSize(resources.getDimension(dimenResource));
      paint.setColor(resources.getColor(colorResource));
      paint.setHinting(Paint.HINTING_ON);
      return paint;
   }

   private static
   int getDp(float pixels)
   {
      float floatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, s_displayMetrics);
      return Math.round(floatDp);
   }

   @Override
   public
   void onDraw(Canvas canvas)
   {
      /* If the canvas is meant to draw a bitmap but it is null, draw nothing. */
      if(m_hasImage && null == m_image)
      {
         return;
      }

      float verticalPosition = drawBase(canvas);
      verticalPosition = drawBitmap(canvas, verticalPosition);
      if(null != m_item.m_desLines && 0 != m_item.m_desLines.length && null != m_item.m_desLines[0])
      {
         if(m_hasImage)
         {
            verticalPosition += getDp(4.0F);
         }
         drawDes(canvas, verticalPosition);
      }
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
      boolean rtl = Utilities.isTextRtl(m_item.m_title);
      float verticalPosition = m_paints[0].getTextSize() + Constants.s_eightDp;

      int startPadding = rtl ? SCREEN - Constants.s_eightDp : Constants.s_eightDp;
      int endPadding = rtl ? Constants.s_eightDp : SCREEN - Constants.s_eightDp;

      Paint.Align start = rtl ? Paint.Align.RIGHT : Paint.Align.LEFT;
      Paint.Align end = rtl ? Paint.Align.LEFT : Paint.Align.RIGHT;

      /* Draw the time. */
      m_paints[1].setTextAlign(end);
      canvas.drawText(getTime(m_item.m_time), endPadding, verticalPosition, m_paints[1]);

      String[] info = {m_item.m_title, m_item.m_urlTrimmed};

      /* Draw the title and the url. */
      for(int i = 0; 2 > i; i++)
      {
         m_paints[i].setTextAlign(start);
         canvas.drawText(info[i], startPadding, verticalPosition, m_paints[i]);
         verticalPosition += m_paints[i].getTextSize();
      }

      return m_hasImage ? verticalPosition : verticalPosition + getDp(4.0F);
   }

   void drawDes(Canvas canvas, float verticalPos)
   {
      if(!m_item.m_desLines[0].isEmpty())
      {
         boolean rtl = Utilities.isTextRtl(m_item.m_desLines[0]);

         m_paints[2].setTextAlign(rtl ? Paint.Align.RIGHT : Paint.Align.LEFT);
         int horizontalPos = rtl ? SCREEN - Constants.s_eightDp : Constants.s_eightDp;

         for(String des : m_item.m_desLines)
         {
            canvas.drawText(des, horizontalPos, verticalPos, m_paints[2]);
            verticalPos += (float) (m_paints[2].getTextSize() * 1.2);
         }
      }
   }

   float drawBitmap(Canvas canvas, float verticalPosition)
   {
      if(null != m_image)
      {
         canvas.drawBitmap(m_image, 0.0F, verticalPosition, m_paints[0]);
         return verticalPosition + m_image.getHeight() + getDp(16.0F);
      }
      else
      {
         return verticalPosition + getDp(4.0F);
      }
   }

   private
   String getTime(long time)
   {
      Long timeAgo = System.currentTimeMillis() - time;

      /* Format the time. */
      Long[] periods = {
            timeAgo / 31556952000L,
            timeAgo / 604800000 % 365,
            timeAgo / 3600000 % 24,
            timeAgo / 60000 % 60,
      };

      StringBuilder builder = new StringBuilder(48);

      /* Display the two highest non zero values. */
      int start = 0;
      while(periods.length > start && 0L == periods[start])
      {
         start++;
      }

      int end = Math.min(start + 2, periods.length);

      for(int i = start; end > i; i++)
      {
         if(0L != periods[i])
         {
            builder.append(TIME_FORMAT.format(periods[i]));
            builder.append(m_timeInitials[i]);
            builder.append(' ');
         }
      }
      if(0 < builder.length())
      {
         builder.deleteCharAt(builder.length() - 1);
         return builder.toString();
      }
      else
    	  return "";
   }

   @Override
   public
   boolean hasOverlappingRendering()
   {
      return false;
   }

   @Override
   protected
   void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
   {
      Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
      int orientation = display.getRotation();
      switch(orientation)
      {
         case Surface.ROTATION_0:
         case Surface.ROTATION_180:
            setMeasuredDimension(Resources.getSystem().getDisplayMetrics().widthPixels, m_height);
            break;
         case Surface.ROTATION_90:
         case Surface.ROTATION_270:
            //noinspection SuspiciousNameCombination
            setMeasuredDimension(Resources.getSystem().getDisplayMetrics().heightPixels, m_height);
      }
   }
}
