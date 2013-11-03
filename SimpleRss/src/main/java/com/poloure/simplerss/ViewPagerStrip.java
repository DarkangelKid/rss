package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;

class ViewPagerStrip extends ViewPager
{
   private static final int   OFF_SCREEN_PAGE_LIMIT = 128;
   private static final float TEXT_VERTICAL_PADDING = 4.0F;

   private
   ViewPagerStrip(Context context)
   {
      super(context);
   }

   static
   ViewPager newInstance(Context context, int pagerTitleStripId)
   {
      /* Create the LayoutParams to put the PagerTitleStrip in the ViewPager correctly. */
      LayoutParams layoutParams = new LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      /* Create the PagerTitleStrip. */
      PagerTitleStrip pagerTitleStrip = new PagerTitleStrip(context);
      pagerTitleStrip.setId(pagerTitleStripId);
      pagerTitleStrip.setGravity(Gravity.START);

      /* Configure the PagerTitleStrip. */
      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      int textVerticalPadding = Math.round(TEXT_VERTICAL_PADDING * displayMetrics.density);
      pagerTitleStrip.setGravity(Gravity.START);
      pagerTitleStrip.setPadding(0, textVerticalPadding, 0, textVerticalPadding);
      pagerTitleStrip.setTextColor(Color.WHITE);
      pagerTitleStrip.setBackgroundColor(Color.parseColor("#404040"));

      /* Create the ViewPager. */
      ViewPager viewPager = new ViewPagerStrip(context);
      viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      viewPager.addView(pagerTitleStrip, layoutParams);

      return viewPager;
   }
}
