package com.poloure.simplerss;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

class DialogEditFeed extends Dialog
{
   private final String m_oldFeedTitle;
   private final String m_applicationFolder;
   private final String m_allTag;
   private final Activity m_activity;
   private final ListView m_listView;

   private
   DialogEditFeed(Context context, ListView listView, String oldFeedTitle, String applicationFolder,
         String allTag)
   {
      super(context);
      m_activity = (Activity) context;
      m_listView = listView;
      m_oldFeedTitle = oldFeedTitle;
      m_applicationFolder = applicationFolder;
      m_allTag = allTag;
   }

   static
   Dialog newInstance(Context context, ListView listView, String oldFeedTitle,
         String applicationFolder, String allTag)
   {
      return new DialogEditFeed(context, listView, oldFeedTitle, applicationFolder, allTag);
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
      buttonPositive.setOnClickListener(new OnClickPositive(this, m_listView));
   }

   private
   class OnClickPositive implements View.OnClickListener
   {
      private final Dialog m_dialog;
      private final ListView m_innerListView;

      OnClickPositive(Dialog dialog, ListView listView)
      {
         m_dialog = dialog;
         m_innerListView = listView;
      }

      @Override
      public
      void onClick(View v)
      {
         ViewPager feedPager = (ViewPager) m_activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
         FragmentPagerAdapter pagerAdapterFeeds = (FragmentPagerAdapter) feedPager.getAdapter();

         ListView navigationDrawer = (ListView) m_activity.findViewById(R.id.navigation_drawer);
         BaseAdapter navigationAdapter = (BaseAdapter) navigationDrawer.getAdapter();

         AsyncCheckFeed.newInstance(m_dialog, m_innerListView, pagerAdapterFeeds, navigationAdapter,
               m_oldFeedTitle, m_applicationFolder, m_allTag);
      }
   }

}
