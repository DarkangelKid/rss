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

import android.app.Dialog;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.poloure.simplerss.adapters.AdapterManageItems;
import com.poloure.simplerss.listeners.MultiModeListenerManage;

public
class ListFragmentManage extends ListFragment
{
    @Override
    public
    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.list_view, container, false);
        TextView emptyView = (TextView) view.findViewById(android.R.id.empty);
        emptyView.setText(R.string.empty_manage_list_view);
        return view;
    }

    @Override
    public
    void onListItemClick(ListView l, View v, int position, long id)
    {
        DialogEditFeed.newInstance((FeedsActivity) getActivity(), position).show();
    }

    @Override
    public
    void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public
    void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_manage, menu);
    }

    @Override
    public
    boolean onOptionsItemSelected(MenuItem menuItem)
    {
        if(R.id.add_feed == menuItem.getItemId())
        {
            FeedsActivity activity = (FeedsActivity) getActivity();
            Dialog dialog = DialogEditFeed.newInstance(activity, -1);
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public
    void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if(!hidden)
        {
            AsyncManageAdapter task = new AsyncManageAdapter((FeedsActivity) getActivity(), this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public
    void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        FeedsActivity activity = (FeedsActivity) getActivity();
        ListView listView = getListView();

        setListAdapter(new AdapterManageItems(activity));

        registerForContextMenu(listView);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiModeListenerManage(listView, activity));
    }
}
