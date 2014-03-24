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

package com.poloure.simplerss.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ListView;

import com.poloure.simplerss.AsyncNavigationAdapter;
import com.poloure.simplerss.FeedItem;
import com.poloure.simplerss.FeedsActivity;
import com.poloure.simplerss.adapters.AdapterFeedItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public
class ListViewFeeds extends ListView implements AbsListView.OnScrollListener
{
    public
    ListViewFeeds(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        setOnScrollListener(this);
    }

    /**
     * Finds the item lowest in the list that is unread and sets the ListView selection
     * to this position. If no items are unread, sets the selection to 0.
     *
     * @param readItemTimes a Collection of longs that correspond to the unix times that the
     *                      FeedItems were made.
     *
     * @see com.poloure.simplerss.FeedItem
     */
    public
    void setSelectionOldestUnread(final Collection<Long> readItemTimes)
    {
        AdapterFeedItems listAdapter = getAdapter();
        final int pos = getPositionOfLastItemOnlyInB(readItemTimes, listAdapter.m_itemTimes);

        // If all items are read, set the selection to 0, else the position.
        // Must be post because this function is called very early during making the ListView.
        clearFocus();
        post(new Runnable()
        {

            @Override
            public
            void run()
            {

                setSelection(-1 == pos ? 0 : pos);
            }
        });
    }

    @Override
    public
    AdapterFeedItems getAdapter()
    {
        return (AdapterFeedItems) super.getAdapter();
    }

    /**
     * Finds the last item of B that is not in A.
     *
     * @param a Collection to remove all of its elements from b.
     * @param b List that we want the last item of.
     *
     * @return -1 if B is a subset of A, else position in b of last member of B exclusive of A.
     */
    private static
    int getPositionOfLastItemOnlyInB(Collection<Long> a, List<Long> b)
    {
        // Create a copy of the B list.
        List<Long> times = new ArrayList<Long>(b);

        // Removes all the items from B that are in both collections.
        times.removeAll(a);

        // If B is a subset of A, return -1.
        if(times.isEmpty())
        {
            return -1;
        }

        // Get the last item of the new List.
        int last = times.size() - 1;
        long l = times.get(last);

        // Return the position of the last item of the new List in B.
        return b.indexOf(l);
    }

    @Override
    public
    void onScrollStateChanged(AbsListView view, int scrollState)
    {
        FeedsActivity activity = (FeedsActivity) view.getContext();

        if(SCROLL_STATE_TOUCH_SCROLL == scrollState || SCROLL_STATE_IDLE == scrollState)
        {
            Adapter adapter = view.getAdapter();
            int first = view.getFirstVisiblePosition();
            int last = view.getLastVisiblePosition();

            for(int i = 0; last - first >= i; i++)
            {
                View viewItem = view.getChildAt(i);

                if(null != viewItem && viewItem.isShown() && 0 <= viewItem.getTop())
                {
                    FeedItem item = (FeedItem) adapter.getItem(first + i);
                    activity.readItem(item.m_time);
                }
            }
        }
        if(SCROLL_STATE_IDLE == scrollState)
        {
            AsyncNavigationAdapter.run(activity);
        }
    }

    @Override
    public
    void onScroll(AbsListView v, int fir, int visible, int total)
    {
    }
}
