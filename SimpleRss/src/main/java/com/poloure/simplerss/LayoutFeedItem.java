package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class LayoutFeedItem extends RelativeLayout
{
   private static final float DEFAULT_CARD_OPACITY = 0.66F;
   static         float     s_cardOpacity;
   static         int       s_titleRead;
   static         int       s_notTitleRead;
   private static int       s_titleUnread;
   private static int       s_descriptionUnread;
   private static int       s_linkUnread;
   private final  TextView  m_titleView;
   private final  TextView  m_urlView;
   private final  TextView  m_descriptionView;
   private final  ImageView m_imageView;

   LayoutFeedItem(Context context)
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

      /* Get the opacity value from file. */
      Resources resources = getResources();
      String applicationFolder = FeedsActivity.getApplicationFolder(context);
      String[] settingTitles = resources.getStringArray(R.array.settings_interface_titles);
      String opacityPath = FeedsActivity.SETTINGS_DIR + settingTitles[1] + ".txt";
      String[] opacityFile = Read.file(opacityPath, applicationFolder);

      boolean valueEmpty = 0 == opacityFile.length || 0 == opacityFile[0].length();
      s_cardOpacity = valueEmpty ? DEFAULT_CARD_OPACITY : Float.parseFloat(opacityFile[0]) / 100.0F;

      /* Get the colors for the ListView item texts. */
      s_titleRead = Color.argb(Math.round(255 * s_cardOpacity), 0, 0, 0);
      s_notTitleRead = Color.argb(Math.round(190 * s_cardOpacity), 0, 0, 0);

      s_titleUnread = Color.argb(Math.round(255), 0, 0, 0);
      s_linkUnread = Color.argb(Math.round(128), 0, 0, 0);
      s_descriptionUnread = Color.argb(Math.round(205), 0, 0, 0);

      /* Set the background color of the ListView items. */
      setBackgroundColor(Color.WHITE);
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
      m_titleView.setTextColor(isRead ? s_titleRead : s_titleUnread);
      m_urlView.setTextColor(isRead ? s_notTitleRead : s_linkUnread);

      if(null != m_descriptionView)
      {
         m_descriptionView.setTextColor(isRead ? s_notTitleRead : s_descriptionUnread);
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