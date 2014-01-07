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
import android.graphics.Color;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

class ViewPagerStrip extends ViewPager
{
   private static final int OFF_SCREEN_PAGE_LIMIT = 128;
   private static final int PAGER_TAB_STRIP_BACKGROUND = Color.parseColor("#404040");
   private static final float TEXT_VERTICAL_PADDING = 4.0F;
   private static final float PAGER_TAB_STRIP_TEXT_SIZE = 14.0F;
   private static final int PAGER_TITLE_STRIP_ID = 165143;
   private static final LayoutParams LAYOUT_PARAMS = new LayoutParams();

   static
   {
      LAYOUT_PARAMS.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      LAYOUT_PARAMS.width = ViewGroup.LayoutParams.MATCH_PARENT;
      LAYOUT_PARAMS.gravity = Gravity.TOP;
   }

   private
   ViewPagerStrip(Context context)
   {
      super(context);
   }

   static
   ViewPager newInstance(Context context)
   {
      /* Create the PagerTitleStrip. */
      PagerTitleStrip pagerTitleStrip = new PagerTitleStrip(context);
      pagerTitleStrip.setId(PAGER_TITLE_STRIP_ID);
      pagerTitleStrip.setGravity(Gravity.START);

      /* Configure the PagerTitleStrip. */
      int textPadding = Utilities.getDp(TEXT_VERTICAL_PADDING);

      pagerTitleStrip.setGravity(Gravity.START);
      pagerTitleStrip.setPadding(0, textPadding, 0, textPadding);
      pagerTitleStrip.setTextColor(Color.WHITE);
      pagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, PAGER_TAB_STRIP_TEXT_SIZE);
      pagerTitleStrip.setBackgroundColor(PAGER_TAB_STRIP_BACKGROUND);

      /* Create the ViewPager. */
      ViewPager viewPager = new ViewPagerStrip(context);
      viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      viewPager.addView(pagerTitleStrip, LAYOUT_PARAMS);

      return viewPager;
   }
}
