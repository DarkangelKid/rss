package com.poloure.simplerss;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

class OnScrollFeedListener implements AbsListView.OnScrollListener
{
   private final BaseAdapter m_navigationAdapter;
   private final BaseAdapter m_feedAdapter;
   private final Context     m_context;

   OnScrollFeedListener(BaseAdapter navigationAdapter, BaseAdapter feedAdapter, Context context)
   {
      m_navigationAdapter = navigationAdapter;
      m_feedAdapter = feedAdapter;
      m_context = context;
   }

   @Override
   public
   void onScrollStateChanged(AbsListView view, int scrollState)
   {
      if(16 == view.getChildAt(0).getTop() &&
            View.VISIBLE == view.getVisibility() && ((AdapterTags) m_feedAdapter).isScreenTouched())
      {
         String time = ((FeedItem) m_feedAdapter.getItem(0)).time;
         AdapterTags.s_readItemTimes.add(time);
      }

      if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
      {
         Update.navigation(m_navigationAdapter, m_context);
      }
   }

   @Override
   public
   void onScroll(AbsListView v, int fir, int visible, int total)
   {
   }
}
