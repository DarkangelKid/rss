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
   private static final int OFF_SCREEN_PAGE_LIMIT = 128;

   private
   ViewPagerStrip(Context context)
   {
      super(context);
   }

   static
   ViewPager newInstance(Context context, int pagerTitleStripId)
   {
      ViewPager viewPager = new ViewPagerStrip(context);

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
      int fourDp = Math.round(4.0F * displayMetrics.density);
      pagerTitleStrip.setGravity(Gravity.START);
      pagerTitleStrip.setPadding(0, fourDp, 0, fourDp);
      pagerTitleStrip.setTextColor(Color.WHITE);
      pagerTitleStrip.setBackgroundColor(Color.parseColor("#404040"));

      /* Create the ViewPager. */
      viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      viewPager.addView(pagerTitleStrip, layoutParams);

      return viewPager;
   }
}
