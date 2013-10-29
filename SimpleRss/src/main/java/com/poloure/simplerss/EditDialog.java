package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

class EditDialog extends Dialog
{
   private final String           m_oldFeedTitle;
   private final String           m_applicationFolder;
   private final String           m_allTag;
   private final FragmentActivity m_activity;

   private
   EditDialog(Context context, String oldFeedTitle, String applicationFolder, String allTag)
   {
      super(context);
      m_activity = (FragmentActivity) context;
      m_oldFeedTitle = oldFeedTitle;
      m_applicationFolder = applicationFolder;
      m_allTag = allTag;
   }

   static
   Dialog newInstance(Context context, String oldFeedTitle, String applicationFolder, String allTag)
   {
      return new EditDialog(context, oldFeedTitle, applicationFolder, allTag);
   }

   @Override
   protected
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.add_rss_dialog);

      Button buttonNegative = (Button) findViewById(R.id.negative_button);
      buttonNegative.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public
         void onClick(View v)
         {
            dismiss();
         }
      });

      Button buttonPositive = (Button) findViewById(R.id.positive_button);
      buttonPositive.setOnClickListener(new OnClickPositive(this));
   }

   private
   class OnClickPositive implements View.OnClickListener
   {
      private final Dialog m_dialog;

      OnClickPositive(Dialog dialog)
      {
         m_dialog = dialog;
      }

      @Override
      public
      void onClick(View v)
      {
         ViewPager feedPager = (ViewPager) m_activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
         FragmentPagerAdapter pagerAdapterFeeds = (FragmentPagerAdapter) feedPager.getAdapter();

         ListView navigationDrawer = (ListView) m_activity.findViewById(R.id.navigation_drawer);
         BaseAdapter navigationAdapter = (BaseAdapter) navigationDrawer.getAdapter();

         AsyncCheckFeed.newInstance(m_dialog, pagerAdapterFeeds, navigationAdapter, m_oldFeedTitle,
               m_applicationFolder, m_allTag);
      }
   }
}
