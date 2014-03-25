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

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;

import static com.poloure.simplerss.Constants.*;

class ViewPagerDelegate implements ViewDelegate
{
    @Override
    public
    boolean isReadyForPull(View view, float x, float y)
    {
        boolean ready = false;

      /* First we check whether we're scrolled to the top of current page. */
        if(null != s_fragmentFeeds)
        {
            AbsListView absListView = getCurrentTagListView();

            if(0 == absListView.getCount())
            {
                ready = true;
            }
            else if(0 == absListView.getFirstVisiblePosition())
            {
                View firstVisibleChild = absListView.getChildAt(0);
                ready = null != firstVisibleChild && 0 <= firstVisibleChild.getTop();
            }

            if(ready && absListView.isFastScrollEnabled() && absListView.isFastScrollAlwaysVisible())
            {
                switch(absListView.getVerticalScrollbarPosition())
                {
                    case View.SCROLLBAR_POSITION_LEFT:
                        return x > absListView.getVerticalScrollbarWidth();
                    case View.SCROLLBAR_POSITION_RIGHT:
                        return x < absListView.getRight() - absListView.getVerticalScrollbarWidth();
                }
            }
        }
        return ready;
    }

    private static
    ListView getCurrentTagListView()
    {
        int currentPage = s_viewPager.getCurrentItem();
        return FragmentFeeds.getTagListView(currentPage);
    }
}

