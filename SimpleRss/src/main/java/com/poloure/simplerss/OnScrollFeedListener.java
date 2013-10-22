package com.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

class OnScrollFeedListener implements AbsListView.OnScrollListener
{
   private static final int SIXTEEN_VALUE = 16;
   private final BaseAdapter m_feedAdapter;
   private final Context     m_context;
   private final int         m_sixteen;

   OnScrollFeedListener(BaseAdapter feedAdapter, Context context)
   {
      m_feedAdapter = feedAdapter;
      m_context = context;

      Resources resources = context.getResources();
      DisplayMetrics displayMetrics = resources.getDisplayMetrics();
      m_sixteen = Math.round(SIXTEEN_VALUE * displayMetrics.density);
   }

   @Override
   public
   void onScrollStateChanged(AbsListView view, int scrollState)
   {
      View topView = view.getChildAt(0);
      boolean isTopView = null != topView;
      boolean isSixteenGap = isTopView && m_sixteen == topView.getTop();
      boolean isListViewShown = view.isShown();
      boolean readingItems = ((AdapterTags) m_feedAdapter).m_readingItems;

      if(isTopView && isSixteenGap && isListViewShown && readingItems)
      {
         Long time = ((FeedItem) m_feedAdapter.getItem(0)).m_itemTime;
         AdapterTags.S_READ_ITEM_TIMES.add(time);
      }

      if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
      {
         Update.navigation((Activity) m_context);
      }
   }

   @Override
   public
   void onScroll(AbsListView v, int fir, int visible, int total)
   {
   }
}
