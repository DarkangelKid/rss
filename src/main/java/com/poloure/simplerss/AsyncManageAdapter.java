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

import android.app.ListFragment;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import com.poloure.simplerss.adapters.AdapterManageItems;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static com.poloure.simplerss.Constants.*;

public
class AsyncManageAdapter extends AsyncTask<String, String[][], Void>
{
    private final ListFragment m_listFragment;
    private final FeedsActivity m_activity;

    public
    AsyncManageAdapter(FeedsActivity activity, ListFragment listFragment)
    {
        m_activity = activity;
        m_listFragment = listFragment;
    }

    /**
     * Run when you want to update the manage ListView.
     *
     * @param activity required for access to m_index.
     */
    public static
    void run(FeedsActivity activity)
    {
        // Only run when the manage fragment is visible since this runs onHiddenChanged(false).
        if(null != s_fragmentManage && s_fragmentManage.isVisible())
        {
            AsyncManageAdapter task = new AsyncManageAdapter(activity, s_fragmentManage);
            task.executeOnExecutor(THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    protected
    Void doInBackground(String... applicationFolder)
    {
        // ObjectIO the index file for names, urls, and tags.
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        List<IndexItem> indexItems = m_activity.m_index;

        String[][] strings = new String[indexItems.size()][3];

        for(int i = 0; i < indexItems.size(); ++i)
        {
            // Append the url to the next line.
            IndexItem item = indexItems.get(i);

            ObjectIO reader = new ObjectIO(m_activity, item.m_uid + ServiceUpdate.ITEM_LIST);
            int itemCount = reader.getElementCount();

            strings[i][0] = format.format(itemCount);
            strings[i][1] = item.m_url;
            strings[i][2] = Utilities.formatTags(item.m_tags);
        }
        publishProgress(strings);
        return null;
    }

    @Override
    protected
    void onProgressUpdate(String[][]... values)
    {
        // Creating a new adapter seems to fix items not updating.
        ArrayAdapter<String[]> adapterManage = new AdapterManageItems(m_activity);
        m_listFragment.setListAdapter(adapterManage);
        adapterManage.addAll(values[0]);
    }
}
