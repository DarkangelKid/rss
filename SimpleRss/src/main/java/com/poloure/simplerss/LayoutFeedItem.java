package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class LayoutFeedItem extends LinearLayout
{
   private static final int COLOR_TITLE_UNREAD = Color.argb(255, 0, 0, 0);
   private static final int COLOR_DESCRIPTION_UNREAD = Color.argb(205, 0, 0, 0);
   private static final float TEXT_SIZE_DESCRIPTION = 14.0F;
   private static final float EIGHT_PADDING = 8.0F;
   private static final Typeface SERIF = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
   private static final AbsListView.LayoutParams LAYOUT_PARAMS = new AbsListView.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
   private static final ViewGroup.LayoutParams IMAGE_PARAMS = new ViewGroup.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
   private final TextView m_titleView;
   private final TextView m_descriptionView;
   private final ImageView m_imageView;

   LayoutFeedItem(Context context)
   {
      super(context);

      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float eightFloatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EIGHT_PADDING,
            metrics);
      int eightDp = Math.round(eightFloatDp);

      m_titleView = new TextView(context);
      m_titleView.setTextColor(COLOR_TITLE_UNREAD);
      m_titleView.setTypeface(SERIF);
      m_titleView.setPadding(eightDp, eightDp, eightDp, eightDp);

      m_descriptionView = new TextView(context);
      m_descriptionView.setTextColor(COLOR_DESCRIPTION_UNREAD);
      m_descriptionView.setTypeface(SERIF);
      m_descriptionView.setPadding(eightDp, eightDp, eightDp, eightDp);
      m_descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_DESCRIPTION);

      m_imageView = new ImageView(context);
      m_imageView.setLayoutParams(IMAGE_PARAMS);
      m_imageView.setAdjustViewBounds(true);
      m_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

      setLayoutParams(LAYOUT_PARAMS);
      setOrientation(VERTICAL);

      addView(m_titleView);
      addView(m_imageView);
      addView(m_descriptionView);
   }

   @Override
   public
   boolean hasOverlappingRendering()
   {
      return false;
   }

   void showItem(FeedItem feedItem, String applicationFolder, int position,
         CharSequence editableTitle)
   {
      String description = feedItem.m_itemDescription;

      m_titleView.setText(editableTitle);
      m_imageView.setImageDrawable(null);

      /* Figuring out what view type the item is. */
      boolean isImage = 0 != feedItem.m_EffImageHeight;
      boolean isDescription = null != description && 0 != description.length();

      m_imageView.setVisibility(isImage ? VISIBLE : GONE);
      m_descriptionView.setVisibility(isDescription ? VISIBLE : GONE);

      if(isImage)
      {
         Context context = getContext();

         ViewGroup.LayoutParams params = m_imageView.getLayoutParams();
         if(null != params)
         {
            params.height = feedItem.m_EffImageHeight;
            m_imageView.setLayoutParams(params);
         }

         m_imageView.setTag(position);

         AsyncLoadImage.newInstance(m_imageView, applicationFolder, feedItem.m_imageName, position,
               context);
      }
      if(isDescription) m_descriptionView.setText(description);
   }

}