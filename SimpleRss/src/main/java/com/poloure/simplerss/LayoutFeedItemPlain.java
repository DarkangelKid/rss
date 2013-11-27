package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

class LayoutFeedItemPlain extends LinearLayout
{
   private static final int COLOR_TITLE_UNREAD = Color.argb(255, 0, 0, 0);
   private static final int COLOR_DESCRIPTION_UNREAD = Color.argb(205, 0, 0, 0);
   private static final int COLOR_LINK_UNREAD = Color.argb(165, 0, 0, 0);
   private static final float EIGHT_PADDING = 8.0F;
   private static final Typeface SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
   /* TODO DP */
   private static final AbsListView.LayoutParams LAYOUT_PARAMS = new AbsListView.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, 178);
   private final LightView m_titleView;
   private final LightView m_linkView;
   private final TextView m_descriptionView;

   LayoutFeedItemPlain(Context context)
   {
      super(context);

      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float eightFloatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EIGHT_PADDING,
            metrics);
      int eightDp = Math.round(eightFloatDp);

      m_titleView = new LightView(context);
      m_titleView.setTextSize(
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16.0F, metrics));
      m_titleView.setTextColor(COLOR_TITLE_UNREAD);
      m_titleView.setBackgroundColor(Color.WHITE);
      m_titleView.setPadding(eightDp, eightDp, eightDp, 0);
      m_titleView.setHeight(44);

      m_linkView = new LightView(context);
      m_linkView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.0F, metrics));
      m_linkView.setTextColor(COLOR_LINK_UNREAD);
      m_linkView.setPadding(eightDp, 0, eightDp, 0);
      m_linkView.setBackgroundColor(Color.WHITE);
      m_titleView.setId(100);
      m_linkView.setHeight(30);

      m_descriptionView = new TextView(context);
      m_descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0F);
      m_descriptionView.setTextColor(COLOR_DESCRIPTION_UNREAD);
      m_descriptionView.setTypeface(SERIF);
      m_descriptionView.setPadding(eightDp, 0, eightDp, eightDp);
      m_descriptionView.setBackgroundColor(Color.WHITE);
      m_descriptionView.setLayoutParams(
            new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 104));

      setLayoutParams(LAYOUT_PARAMS);
      setOrientation(VERTICAL);

      addView(m_titleView);
      addView(m_linkView);
      addView(m_descriptionView);
   }

   @Override
   public
   boolean hasOverlappingRendering()
   {
      return false;
   }

   void showItem(String title, String link, String description)
   {
      m_titleView.setText(title);
      m_linkView.setText(link);
      m_descriptionView.setText(description);
   }
}