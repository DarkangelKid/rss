package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class LayoutFeedItem extends RelativeLayout
{
   private static final int COLOR_TITLE_UNREAD = Color.argb(255, 0, 0, 0);
   private static final int COLOR_DESCRIPTION_UNREAD = Color.argb(205, 0, 0, 0);
   private static final int COLOR_LINK_UNREAD = Color.argb(128, 0, 0, 0);
   private static final AbsListView.LayoutParams LAYOUT_PARAMS = new AbsListView.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
   private static float s_opacity = -1.0F;
   private static int s_titleRead = Color.argb(168, 0, 0, 0);
   private static int s_notTitleRead = Color.argb(125, 0, 0, 0);
   private final int m_eightDp;
   private final TextView m_titleView;
   private final TextView m_urlView;
   private final TextView m_descriptionView;
   private final ImageView m_imageView;
   private final ImageView m_leftShadow;
   private final ImageView m_rightShadow;

   LayoutFeedItem(Context context)
   {
      super(context);
      inflate(context, R.layout.card_full, this);

      /* Save the inflated views from the xml file as class fields. */
      m_titleView = (TextView) findViewById(R.id.title);
      m_urlView = (TextView) findViewById(R.id.url);
      m_descriptionView = (TextView) findViewById(R.id.description);
      m_imageView = (ImageView) findViewById(R.id.image);
      m_leftShadow = (ImageView) findViewById(R.id.white_left_shadow);
      m_rightShadow = (ImageView) findViewById(R.id.white_right_shadow);

      /* Set the long click listener for the item. */
      setOnLongClickListener(new OnCardLongClick(context));

      setLayoutParams(LAYOUT_PARAMS);

      /* Save DP values. */
      Resources resources = getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      m_eightDp = Math.round(displayMetrics.density * 8.0F);

      /* Set the background color of the ListView items. */
      setBackgroundColor(Color.WHITE);
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

   void showItem(FeedItem feedItem, String applicationFolder, int position, boolean isRead)
   {
      String link = feedItem.m_itemUrl;
      String title = feedItem.m_itemTitle;
      String description = feedItem.m_itemDescription;

      m_titleView.setText(title);
      m_urlView.setText(link);

      /* Set the text colors based on whether the item has been read or not. */
      m_titleView.setTextColor(isRead ? s_titleRead : COLOR_TITLE_UNREAD);
      m_urlView.setTextColor(isRead ? s_notTitleRead : COLOR_LINK_UNREAD);

      m_imageView.setImageDrawable(null);

      /* Figuring out what view type the item is. */
      boolean isImage = 0 != feedItem.m_imageWidth;
      boolean isDescription = null != description && 0 != description.length();

      m_imageView.setVisibility(isImage ? VISIBLE : GONE);
      m_descriptionView.setVisibility(isDescription ? VISIBLE : GONE);

      if(isImage)
      {
         displayImage(m_imageView, applicationFolder, position, feedItem, isRead);
      }
      if(isDescription)
      {
         int topPadding = isImage ? m_eightDp : Math.round((float) m_eightDp / 4.0F);
         m_descriptionView.setPadding(m_eightDp, topPadding, m_eightDp, m_eightDp);
         m_descriptionView.setText(description);
         m_descriptionView.setTextColor(isRead ? s_notTitleRead : COLOR_DESCRIPTION_UNREAD);
      }

      /* Set whether the white shadows should show. */
      int shadowVisibility = isImage && !isDescription ? GONE : VISIBLE;
      m_leftShadow.setVisibility(shadowVisibility);
      m_rightShadow.setVisibility(shadowVisibility);
   }

   private
   void displayImage(ImageView imageView, String applicationFolder, int position, FeedItem feedItem,
         boolean isRead)
   {
      Context context = getContext();
      Resources resources = getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();

      String imagePath = applicationFolder + feedItem.m_imagePath;
      short imageWidth = feedItem.m_imageWidth;
      short imageHeight = feedItem.m_imageHeight;
      int screenWidth = displayMetrics.widthPixels;

      ViewGroup.LayoutParams lp = imageView.getLayoutParams();

      lp.height = Math.round((float) screenWidth / (float) imageWidth * (float) imageHeight);
      imageView.setLayoutParams(lp);
      imageView.setTag(position);

      AsyncLoadImage.newInstance(imageView, imagePath, position, context, isRead, s_opacity);
   }
}