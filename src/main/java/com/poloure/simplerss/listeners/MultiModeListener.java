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
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import com.poloure.simplerss.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract
class MultiModeListener implements AbsListView.MultiChoiceModeListener
{
    final ListView m_listView;
    private final Resources m_resources;
    private int m_itemCount;
    static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());

    public
    MultiModeListener(ListView listView, Resources resources)
    {
        m_listView = listView;
        m_resources = resources;
    }

    @Override
    public
    void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
    {
        m_itemCount += checked ? 1 : -1;
        updateTitle(mode);
    }

    private
    void updateTitle(ActionMode mode)
    {
        String count = NUMBER_FORMAT.format(m_itemCount);
        mode.setTitle(m_resources.getQuantityString(R.plurals.items_selected, m_itemCount, count));
    }

    /* Called to refresh an action mode's action menu whenever it is invalidated.
     * true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public
    boolean onPrepareActionMode(ActionMode mode, Menu menu)
    {
        return false;
    }

    @Override
    public
    boolean onActionItemClicked(ActionMode mode, MenuItem item)
    {
        int itemId = item.getItemId();

        if(R.id.select_all == itemId)
        {
            for(int i = 0; m_listView.getCount() > i; i++)
            {
                if(!m_listView.isItemChecked(i))
                {
                    m_listView.setItemChecked(i, true);
                }
            }
            updateTitle(mode);
        }
        else
        {
            SparseBooleanArray checked = m_listView.getCheckedItemPositions();

            List<Integer> positionsChecked = new ArrayList<Integer>(checked.size());
            for(int i = 0; checked.size() > i; i++)
            {
                if(checked.valueAt(i))
                {
                    positionsChecked.add(checked.keyAt(i));
                }
            }

            performActionOnItems(itemId, positionsChecked);

            mode.finish();
        }
        return true;
    }

    public
    abstract
    void performActionOnItems(int itemId, List<Integer> checkedPositions);

    @Override
    public
    void onDestroyActionMode(ActionMode mode)
    {
        m_itemCount = 0;
    }
}
