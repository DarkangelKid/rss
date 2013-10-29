package com.poloure.simplerss;

import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.AbsListView;

class OnScrollFeedListener implements AbsListView.OnScrollListener
{
   private final int              m_sixteenDp;
   private final String           m_applicationFolder;
   private final int              m_page;
   private final AdapterNavDrawer m_adapterNavDrawer;
   private final ActionBar        m_actionBar;

   OnScrollFeedListener(AdapterNavDrawer adapterNavDrawer, ActionBar actionBar,
         String applicationFolder, int page, int sixteenDp)
   {
      m_adapterNavDrawer = adapterNavDrawer;
      m_actionBar = actionBar;
      m_applicationFolder = applicationFolder;
      m_page = page;
      m_sixteenDp = sixteenDp;
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
         boolean isSixteenGap = isTopView && m_sixteenDp == topView.getTop();
         boolean isListViewShown = view.isShown();
         boolean readingItems = adapterTags.m_isReadingItems;

         if(isTopView && isSixteenGap && isListViewShown && readingItems)
         {
            Long time = ((FeedItem) adapterTags.getItem(0)).m_itemTime;
            AdapterTags.S_READ_ITEM_TIMES.add(time);
         }

         AsyncRefreshNavigationAdapter.newInstance(m_adapterNavDrawer, m_actionBar,
               m_applicationFolder, m_page);
      }
   }

   @Override
   public
   void onScroll(AbsListView v, int fir, int visible, int total)
   {
   }
}
