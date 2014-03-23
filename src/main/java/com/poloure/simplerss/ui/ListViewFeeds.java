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
import android.widget.ListView;

import com.poloure.simplerss.AdapterTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public
class ListViewFeeds extends ListView
{
    public
    ListViewFeeds(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
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
    void gotoLatestUnread(final Collection<Long> readItemTimes)
    {
        AdapterTags listAdapter = getAdapter();

        // Create a List with all the item times in this ListView.
        List<Long> times = new ArrayList<Long>(listAdapter.m_itemTimes);

        // Removes all the read item times from the list just made.
        times.removeAll(readItemTimes);

        if(times.isEmpty())
        {
            setSelection(0);
        }
        else
        {
            int index = listAdapter.m_itemTimes.indexOf(times.get(times.size() - 1));
            setSelection(index);
        }
    }

    @Override
    public
    AdapterTags getAdapter()
    {
        return (AdapterTags) super.getAdapter();
    }
}
