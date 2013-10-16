package com.poloure.simplerss;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

class OnScrollFeedListener implements AbsListView.OnScrollListener
{
   private final BaseAdapter m_navigationAdapter;
   private final BaseAdapter m_feedAdapter;
   private final Context     m_context;
   private final ViewPager.OnPageChangeListener m_pageChange;
   private final int m_position;

   OnScrollFeedListener(BaseAdapter navigationAdapter, BaseAdapter feedAdapter, ViewPager.OnPageChangeListener pageChange, int pos, Context context)
   {
      m_navigationAdapter = navigationAdapter;
      m_feedAdapter = feedAdapter;
      m_context = context;
      m_position = pos;
      m_pageChange = pageChange;
   }

   @Override
   public
   void onScrollStateChanged(AbsListView view, int scrollState)
   {
      if(16 == view.getChildAt(0).getTop() &&
            View.VISIBLE == view.getVisibility() && ((AdapterTags) m_feedAdapter).isScreenTouched())
      {
         Long time = ((FeedItem) m_feedAdapter.getItem(0)).time;
         AdapterTags.S_READ_ITEM_TIMES.add(time);
      }

      if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
      {
         Update.navigation(m_navigationAdapter, m_pageChange, m_position, m_context);
      }
   }

   @Override
   public
   void onScroll(AbsListView v, int fir, int visible, int total)
   {
   }
}
