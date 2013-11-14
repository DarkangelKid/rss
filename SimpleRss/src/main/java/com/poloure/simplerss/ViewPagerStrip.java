package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

class ViewPagerStrip extends ViewPager
{
   //private static final int OFF_SCREEN_PAGE_LIMIT = 128;
   private static final int VIEWPAGER_BACKGROUND_COLOR = Color.parseColor("#404040");
   private static final float TEXT_VERTICAL_PADDING = 4.0F;
   private static final float VIEWPAGER_TEXT_SIZE = 14.0F;
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
   ViewPager newInstance(Context context, int pagerTitleStripId)
   {
      /* Create the PagerTitleStrip. */
      PagerTitleStrip pagerTitleStrip = new PagerTitleStrip(context);
      pagerTitleStrip.setId(pagerTitleStripId);
      pagerTitleStrip.setGravity(Gravity.START);

      /* Configure the PagerTitleStrip. */
      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      float textVerticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            TEXT_VERTICAL_PADDING, displayMetrics);
      int textPadding = Math.round(textVerticalPadding);
      pagerTitleStrip.setGravity(Gravity.START);
      pagerTitleStrip.setPadding(0, textPadding, 0, textPadding);
      pagerTitleStrip.setTextColor(Color.WHITE);
      pagerTitleStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, VIEWPAGER_TEXT_SIZE);
      pagerTitleStrip.setBackgroundColor(VIEWPAGER_BACKGROUND_COLOR);

      /* Create the ViewPager. */
      ViewPager viewPager = new ViewPagerStrip(context);
      //viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      viewPager.addView(pagerTitleStrip, LAYOUT_PARAMS);

      return viewPager;
   }
}
