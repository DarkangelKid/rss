package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.ViewGroup;

/**
 * Created by paul on 31/10/13.
 */
public
class ViewPagerStrip extends ViewPager
{
   private static final int OFF_SCREEN_PAGE_LIMIT = 128;

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
      int fourDp = Math.round(4.0F * context.getResources().getDisplayMetrics().density);
      /*TODO pagerTitleStrip.setDrawFullUnderline(true); */
      pagerTitleStrip.setGravity(Gravity.START);
      pagerTitleStrip.setPadding(0, fourDp, 0, fourDp);
      pagerTitleStrip.setTextColor(Color.WHITE);
      pagerTitleStrip.setBackgroundColor(Color.parseColor("#404040"));
      /*TODO pagerTitleStrip.setTabIndicatorColor(colorInts[pos]); */

      /* Create the ViewPager. */
      viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
      viewPager.addView(pagerTitleStrip, layoutParams);

      return viewPager;
   }
}
