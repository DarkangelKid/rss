package com.poloure.simplerss;

import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AbsListView;

class OnScrollFeedListener implements AbsListView.OnScrollListener
{
   private final int m_listViewTopPadding;
   private final String m_applicationFolder;
   private final int m_page;
   private final AdapterNavigationDrawer m_adapterNavigationDrawer;
   private final ActionBar m_actionBar;

   OnScrollFeedListener(AdapterNavigationDrawer adapterNavigationDrawer, ActionBar actionBar,
         String applicationFolder, int page, int listViewTopPadding)
   {
      m_adapterNavigationDrawer = adapterNavigationDrawer;
      m_actionBar = actionBar;
      m_applicationFolder = applicationFolder;
      m_page = page;
      m_listViewTopPadding = listViewTopPadding;
   }

   @Override
   public
   void onScrollStateChanged(AbsListView view, int scrollState)
   {
      if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
      {
         AdapterTags adapterTags = (AdapterTags) view.getAdapter();
         View topView = view.getChildAt(0);
         boolean isTopView = null != topView;
         boolean isVeryTop = isTopView && m_listViewTopPadding == topView.getTop();
         boolean isListViewShown = view.isShown();
         boolean readingItems = adapterTags.m_isReadingItems;

         if(isTopView && isVeryTop && isListViewShown && readingItems)
         {
            Long time = ((FeedItem) adapterTags.getItem(0)).m_itemTime;
            AdapterTags.READ_ITEM_TIMES.add(time);
         }

         AsyncRefreshNavigationAdapter.newInstance(m_adapterNavigationDrawer, m_actionBar,
               m_applicationFolder, m_page);
      }
   }

   @Override
   public
   void onScroll(AbsListView v, int fir, int visible, int total)
   {
   }
}
