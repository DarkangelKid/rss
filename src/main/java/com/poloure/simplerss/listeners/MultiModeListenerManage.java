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

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;

import com.poloure.simplerss.AsyncManageAdapter;
import com.poloure.simplerss.AsyncNavigationAdapter;
import com.poloure.simplerss.AsyncNewTagAdapters;
import com.poloure.simplerss.FeedsActivity;
import com.poloure.simplerss.IndexItem;
import com.poloure.simplerss.PagerAdapterTags;
import com.poloure.simplerss.R;
import com.poloure.simplerss.ServiceUpdate;

import java.util.ArrayList;
import java.util.List;

public
class MultiModeListenerManage extends MultiModeListener
{
    private final FeedsActivity m_activity;

    public
    MultiModeListenerManage(ListView listView, FeedsActivity activity)
    {
        super(listView, activity.getResources());
        m_activity = activity;
    }

    /* true - the action mode should be created, false - entering this mode should be aborted. */
    @Override
    public
    boolean onCreateActionMode(ActionMode mode, Menu menu)
    {
        MenuInflater inflater = mode.getMenuInflater();
        if(null != inflater)
        {
            inflater.inflate(R.menu.context_manage, menu);
            return true;
        }
        return false;
    }

    @Override
    public
    void performActionOnItems(int actionItemClicked, List<Integer> checkedPositions)
    {
        // Make a copy since we call m_index.get() and .remove() in a loop with absolute positions.
        List<IndexItem> indexCopy = new ArrayList<IndexItem>(m_activity.m_index);

        for(int pos : checkedPositions)
        {
            IndexItem indexItem = indexCopy.get(pos);

            switch(actionItemClicked)
            {
                case R.id.delete_feed:
                    m_activity.m_index.remove(indexItem);
                case R.id.delete_content:
                    m_activity.deleteFile(indexItem.m_uid + ServiceUpdate.ITEM_LIST);
                    m_activity.deleteFile(indexItem.m_uid + ServiceUpdate.CONTENT_FILE);
            }
        }

        // Update all our adapters.
        PagerAdapterTags.run(m_activity);
        AsyncManageAdapter.run(m_activity);
        AsyncNewTagAdapters.update(m_activity);
        AsyncNavigationAdapter.run(m_activity);
    }
}
