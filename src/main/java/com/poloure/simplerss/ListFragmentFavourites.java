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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.poloure.simplerss.adapters.AdapterFeedItems;
import com.poloure.simplerss.listeners.MultiModeListener;
import com.poloure.simplerss.listeners.MultiModeListenerFavourites;

import org.apache.commons.collections.map.LinkedMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public
class ListFragmentFavourites extends ListFragment
{
    @Override
    public
    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.list_view, container, false);
        TextView emptyView = (TextView) view.findViewById(android.R.id.empty);
        emptyView.setText(R.string.empty_favourites_list_view);
        return view;
    }

    @Override
    public
    void onListItemClick(ListView l, View v, int position, long id)
    {
        Utilities.showWebFragment((FeedsActivity) getActivity(), (ViewFeedItem) v);
    }

    @Override
    public
    void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        FeedsActivity activity = (FeedsActivity) getActivity();
        ListView listView = getListView();

        // Read the favourites set to memory.
        ObjectIO reader = new ObjectIO(activity, FeedsActivity.FAVOURITES);

        Map<Long, FeedItem> map;

        // This object used to be a LinkedHashSet<FeedItem>, if it still is, convert to a map.
        try
        {
            map = (Map<Long, FeedItem>) reader.read();
        }
        catch(ClassCastException ignored)
        {
            // This is the old format. Convert to a Map.
            Collection<FeedItem> set = (Collection<FeedItem>) reader.read();
            FeedItem[] items = set.toArray(new FeedItem[set.size()]);

            map = new LinkedHashMap<Long, FeedItem>(set.size());

            for(int i = 0; i < items.length; i++)
            {
                map.put((long) i, items[i]);
            }
        }

        // Add the favourites to the adapter and set the ListView adapter.
        setListAdapter(new AdapterFeedItems(activity, map));

        registerForContextMenu(listView);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiModeListenerFavourites(listView, getResources()));
    }
}
