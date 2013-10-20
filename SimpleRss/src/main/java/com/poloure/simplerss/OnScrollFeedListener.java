package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

class OnScrollFeedListener implements AbsListView.OnScrollListener
{
   private static final int SIXTEEN_VALUE = 16;
   private final BaseAdapter                    m_navigationAdapter;
   private final BaseAdapter                    m_feedAdapter;
   private final Context                        m_context;
   private final ViewPager.OnPageChangeListener m_pageChange;
   private final int                            m_position;
   private final int                            m_sixteen;

   OnScrollFeedListener(BaseAdapter navigationAdapter, BaseAdapter feedAdapter,
         ViewPager.OnPageChangeListener pageChange, int pos, Context context)
   {
      m_navigationAdapter = navigationAdapter;
      m_feedAdapter = feedAdapter;
      m_context = context;
      m_position = pos;
      m_pageChange = pageChange;

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
      if(isTopView && m_sixteen == topView.getTop() &&
            View.VISIBLE == view.getVisibility() && ((AdapterTags) m_feedAdapter).isScreenTouched())
      {
         Long time = ((FeedItem) m_feedAdapter.getItem(0)).m_itemTime;
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
