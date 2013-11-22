package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class LayoutFeedItem extends LinearLayout
{
   private static final int COLOR_TITLE_UNREAD = Color.argb(255, 0, 0, 0);
   private static final int COLOR_DESCRIPTION_UNREAD = Color.argb(205, 0, 0, 0);
   private static final int COLOR_LINK_UNREAD = Color.argb(128, 0, 0, 0);
   private static final AbsListView.LayoutParams LAYOUT_PARAMS = new AbsListView.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
   private static float s_opacity = -1.0F;
   private static int s_titleRead = Color.argb(168, 0, 0, 0);
   private static int s_notTitleRead = Color.argb(125, 0, 0, 0);
   private static float s_screenWidth;
   private final TextView m_titleView;
   private final TextView m_descriptionView;
   private final ImageView m_imageView;

   LayoutFeedItem(Context context)
   {
      super(context);
      inflate(context, R.layout.feed_item, this);

      if(0.0F == s_screenWidth)
      {
         Resources resources = context.getResources();
         DisplayMetrics metrics = resources.getDisplayMetrics();
         s_screenWidth = (float) metrics.widthPixels;
      }

      /* Save the inflated views from the xml file as class fields. */
      m_titleView = (TextView) findViewById(R.id.title);
      m_descriptionView = (TextView) findViewById(R.id.description);
      m_imageView = (ImageView) findViewById(R.id.image);

      setLayoutParams(LAYOUT_PARAMS);
      setOrientation(VERTICAL);
   }

   static
   float getReadItemOpacity()
   {
      return s_opacity;
   }

   static
   void setReadItemOpacity(float opacity)
   {
      s_opacity = opacity;
      s_titleRead = Color.argb(Math.round(255.0F * opacity), 0, 0, 0);
      s_notTitleRead = Color.argb(Math.round(190.0F * opacity), 0, 0, 0);
   }

   void showItem(FeedItem feedItem, String applicationFolder, int position, boolean isRead,
         CharSequence editableTitle)
   {
      String description = feedItem.m_itemDescription;

      m_titleView.setText(editableTitle);

      /* Set the text colors based on whether the item has been read or not. */
      m_titleView.setTextColor(isRead ? s_titleRead : COLOR_TITLE_UNREAD);
      m_imageView.setImageDrawable(null);

      /* Figuring out what view type the item is. */
      boolean isImage = 0 != feedItem.m_imageWidth;
      boolean isDescription = null != description && 0 != description.length();

      m_imageView.setVisibility(isImage ? VISIBLE : GONE);
      m_descriptionView.setVisibility(isDescription ? VISIBLE : GONE);

      if(isImage)
      {
         Context context = getContext();

         short imageWidth = feedItem.m_imageWidth;
         short imageHeight = feedItem.m_imageHeight;

         ViewGroup.LayoutParams lp = m_imageView.getLayoutParams();

         lp.height = Math.round((float) s_screenWidth / (float) imageWidth * (float) imageHeight);
         m_imageView.setLayoutParams(lp);
         m_imageView.setTag(position);

         AsyncLoadImage.newInstance(m_imageView, applicationFolder, feedItem.m_imageName, position,
               context, isRead, s_opacity);
      }
      if(isDescription)
      {
         m_descriptionView.setText(description);
         m_descriptionView.setTextColor(isRead ? s_notTitleRead : COLOR_DESCRIPTION_UNREAD);
      }
   }

}