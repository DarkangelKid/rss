package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public
class ItemView extends RelativeLayout
{
   private TextView  m_titleView;
   private TextView  m_urlView;
   private TextView  m_descriptionView;
   private ImageView m_imageView;

   private final int m_titleBlack;
   private final int m_linkBlack;
   private final int m_descriptionBlack;
   private final int m_titleGrey;
   private final int m_linkGrey;
   private final int m_descriptionGrey;

   private final Context m_context;

   public
   ItemView(Context context, AttributeSet attributeSet)
   {
      super(context, attributeSet);

      Resources resources = context.getResources();
      m_titleGrey = resources.getColor(R.color.title_grey);
      m_linkGrey = resources.getColor(R.color.link_grey);
      m_descriptionGrey = resources.getColor(R.color.des_grey);

      m_titleBlack = resources.getColor(R.color.title_black);
      m_linkBlack = resources.getColor(R.color.link_black);
      m_descriptionBlack = resources.getColor(R.color.des_black);

      m_context = context;
   }

   @Override
   protected
   void onFinishInflate()
   {
      super.onFinishInflate();
      m_titleView = (TextView) findViewById(R.id.title);
      m_urlView = (TextView) findViewById(R.id.url);
      m_descriptionView = (TextView) findViewById(R.id.description);
      m_imageView = (ImageView) findViewById(R.id.image);

      /* Set the long click listener for the item. */
      setOnLongClickListener(new OnCardLongClick(m_context));
   }

   public
   void showItem(FeedItem feedItem, int position)
   {
      String link = feedItem.m_itemUrl;
      String title = feedItem.m_itemTitle;
      String description = feedItem.m_itemDescription;
      Long time = feedItem.m_itemTime;

      m_titleView.setText(title);
      m_urlView.setText(link);
      m_imageView.setImageDrawable(null);

      /* Figuring out what view type the item is. */
      boolean isImage = 0 != feedItem.m_imageWidth;
      boolean isDescription = null != description && 0 != description.length();

      if(isImage)
      {
         displayImage(m_imageView, position, feedItem, time);
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

      setCardAlpha(m_titleView, m_urlView, m_descriptionView, time);
   }

   private
   void displayImage(ImageView imageView, int position, FeedItem feedItem, long time)
   {
      Resources resources = m_context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();

      String imageName = feedItem.m_itemImage;
      int imageWidth = feedItem.m_imageWidth;
      int imageHeight = feedItem.m_imageHeight;
      int screenWidth = displayMetrics.widthPixels;

      ViewGroup.LayoutParams lp = imageView.getLayoutParams();

      lp.height = (int) Math.round((double) screenWidth / imageWidth * imageHeight);
      imageView.setLayoutParams(lp);
      imageView.setTag(position);

      AsyncTask<Object, Void, Object[]> task = new AsyncLoadImage();
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {

         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageView, position,
               feedItem.m_itemImage, m_context, time);
      }
      else
      {
         task.execute(imageView, position, imageName, m_context, time);
      }
   }

   private
   void setCardAlpha(TextView title, TextView url, TextView des, Long time)
   {
      if(AdapterTags.S_READ_ITEM_TIMES.contains(time))
      {
         title.setTextColor(m_titleGrey);
         url.setTextColor(m_linkGrey);

         if(null != des)
         {
            des.setTextColor(m_descriptionGrey);
         }
      }
      else
      {
         title.setTextColor(m_titleBlack);
         url.setTextColor(m_linkBlack);

         if(null != des)
         {
            des.setTextColor(m_descriptionBlack);
         }
      }
   }

}