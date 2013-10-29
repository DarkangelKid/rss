package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class FeedItemView extends RelativeLayout
{
   private final int       m_primaryTextLight;
   private final int       m_secondaryTextLight;
   private final int       m_tertiaryTextLight;
   private final int       m_secondaryTextDark;
   private final TextView  m_titleView;
   private final TextView  m_urlView;
   private final TextView  m_descriptionView;
   private final ImageView m_imageView;

   FeedItemView(Context context)
   {
      super(context);
      inflate(context, R.layout.card_full, this);

      /* Save the inflated views from the xml file as class fields. */
      m_titleView = (TextView) findViewById(R.id.title);
      m_urlView = (TextView) findViewById(R.id.url);
      m_descriptionView = (TextView) findViewById(R.id.description);
      m_imageView = (ImageView) findViewById(R.id.image);

      /* Set the long click listener for the item. */
      setOnLongClickListener(new OnCardLongClick(context));

      /* Set the LayoutParams to match parent, match_parent. */
      int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
      AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(matchParent,
            matchParent);
      setLayoutParams(layoutParams);

      /* Get the colors for the ListView item texts. */
      Resources resources = getResources();
      m_tertiaryTextLight = resources.getColor(android.R.color.tertiary_text_light);
      m_secondaryTextDark = resources.getColor(android.R.color.secondary_text_dark);
      m_primaryTextLight = resources.getColor(android.R.color.primary_text_light);
      m_secondaryTextLight = resources.getColor(android.R.color.secondary_text_light);

      /* Set the background color of the ListView items. */
      int backgroundColor = resources.getColor(android.R.color.background_light);
      setBackgroundColor(backgroundColor);
   }

   void showItem(FeedItem feedItem, int position, boolean isRead)
   {
      String link = feedItem.m_itemUrl;
      String title = feedItem.m_itemTitle;
      String description = feedItem.m_itemDescription;

      m_titleView.setText(title);
      m_urlView.setText(link);
      m_imageView.setImageDrawable(null);

      /* Figuring out what view type the item is. */
      boolean isImage = 0 != feedItem.m_imageWidth;
      boolean isDescription = null != description && 0 != description.length();

      if(isImage)
      {
         m_imageView.setVisibility(View.VISIBLE);
         displayImage(m_imageView, position, feedItem, isRead);
      }
      else
      {
         m_imageView.setVisibility(View.GONE);
      }

      if(isDescription)
      {
         m_descriptionView.setVisibility(View.VISIBLE);
         m_descriptionView.setText(description);
      }
      else
      {
         m_descriptionView.setVisibility(View.GONE);
      }

      /* Set the text colors based on whether the item has been read or not. */
      m_titleView.setTextColor(isRead ? m_tertiaryTextLight : m_primaryTextLight);
      m_urlView.setTextColor(isRead ? m_secondaryTextDark : m_tertiaryTextLight);

      if(null != m_descriptionView)
      {
         m_descriptionView.setTextColor(isRead ? m_secondaryTextDark : m_secondaryTextLight);
      }
   }

   private
   void displayImage(ImageView imageView, int position, FeedItem feedItem, boolean isRead)
   {
      Context context = getContext();
      Resources resources = getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();

      String imagePath = FeedsActivity.getApplicationFolder(context) + feedItem.m_itemImage;
      int imageWidth = feedItem.m_imageWidth;
      int imageHeight = feedItem.m_imageHeight;
      int screenWidth = displayMetrics.widthPixels;

      ViewGroup.LayoutParams lp = imageView.getLayoutParams();

      lp.height = (int) Math.round((double) screenWidth / imageWidth * imageHeight);
      imageView.setLayoutParams(lp);
      imageView.setTag(position);

      AsyncLoadImage.newInstance(imageView, imagePath, position, context, isRead);
   }
}