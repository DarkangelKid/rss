/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.Activity;
import android.view.View;
import android.widget.AbsListView;

class OnScrollFeedListener implements AbsListView.OnScrollListener
{
   private final int m_listViewTopPadding;
   private final String m_applicationFolder;
   private final int m_page;
   private final Activity m_activity;

   OnScrollFeedListener(Activity activity, String applicationFolder, int page,
         int listViewTopPadding)
   {
      m_activity = activity;
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
            Long time = ((FeedItem) adapterTags.getItem(0)).m_time;
            AdapterTags.READ_ITEM_TIMES.add(time);
         }

         AsyncNavigationAdapter.newInstance(m_activity, m_applicationFolder, m_page);
      }
   }

   @Override
   public
   void onScroll(AbsListView v, int fir, int visible, int total)
   {
   }
}
