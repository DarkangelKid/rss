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

package com.poloure.simplerss.listeners;

import android.content.res.Resources;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

import com.poloure.simplerss.FeedItem;
import com.poloure.simplerss.R;
import com.poloure.simplerss.adapters.AdapterFeedItems;

import java.util.List;

public
class MultiModeListenerFavourites extends MultiModeListener
{
    public
    MultiModeListenerFavourites(ListView listView, Resources resources)
    {
        super(listView, resources);
    }

    /* true - the action mode should be created, false - entering this mode should be aborted. */
    @Override
    public
    boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        MenuInflater inflater = mode.getMenuInflater();
        if(null != inflater)
        {
            inflater.inflate(R.menu.context_favourites, menu);
            return true;
        }
        return false;
    }

    @Override
    public
    void performActionOnItems(int actionItemClicked, List<Integer> checkedPositions)
    {
        AdapterFeedItems adapter = (AdapterFeedItems) m_listView.getAdapter();
        List<Long> itemKeys = adapter.getKeyList();

        for(int pos : checkedPositions)
        {
            if(R.id.delete_favourite == actionItemClicked)
            {
                long key = itemKeys.get(pos);
                adapter.remove(key);
            }
        }

        // Update the adapters.
        adapter.notifyDataSetChanged();
    }
}
