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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.poloure.simplerss.adapters.AdapterFeedItems;
import com.poloure.simplerss.adapters.LinkedMapAdapter;
import com.poloure.simplerss.asynctasks.AsyncTaskSaveImage;
import com.poloure.simplerss.ui.ListViewFeeds;

class FragmentTag extends ListFragment
{
    private static final String POSITION_KEY = "POSITION";

    public static
    Fragment newInstance(int position)
    {
        Fragment fragment = new FragmentTag();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION_KEY, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public
    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.list_view_feeds, container, false);
        TextView emptyView = (TextView) view.findViewById(android.R.id.empty);
        emptyView.setText(R.string.empty_tag_list_view);
        return view;
    }

    @Override
    public
    void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        FeedsActivity activity = (FeedsActivity) getActivity();
        ListView listView = (ListView) view.findViewById(android.R.id.list);

        registerForContextMenu(listView);

        AsyncNewTagAdapters.update(activity);
    }

    @Override
    public
    void onListItemClick(ListView l, View v, int position, long id)
    {
        Utilities.showWebFragment((FeedsActivity) getActivity(), (ViewFeedItem) v);
    }

    @Override
    public
    ListViewFeeds getListView()
    {
        return (ListViewFeeds) super.getListView();
    }

    @Override
    public
    LinkedMapAdapter<Long, FeedItem> getListAdapter()
    {
        return (LinkedMapAdapter<Long, FeedItem>) super.getListAdapter();
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
    boolean onOptionsItemSelected(MenuItem menuItem)
    {
        if(R.id.jump_to_unread == menuItem.getItemId())
        {
            ListViewFeeds listView = getListView();
            if(null != listView)
            {
                FeedsActivity activity = (FeedsActivity) getActivity();
                listView.setSelectionOldestUnread(activity.getReadItemTimes());
            }
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public
    void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        boolean hasImage = ((ViewFeedItem) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView).m_hasImage;

        // Inflate the context menu from the xml file.
        Activity activity = getActivity();
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

        // Show the 'Save image' option only when the view has an image.
        menu.findItem(R.id.save_image).setVisible(hasImage);

        // Set the title of the context menu to the feed item's title.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        FeedItem view = (FeedItem) ((AdapterView<ListAdapter>) v).getAdapter()
                .getItem(info.position);
        menu.setHeaderTitle(view.m_title);
    }

    @Override
    public
    boolean onContextItemSelected(MenuItem item)
    {
        // Get the feed url from the FeedItem.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        FeedItem feedItem = ((ViewFeedItem) info.targetView).m_item;
        String url = feedItem.m_url;

        Activity activity = getActivity();

        switch(item.getItemId())
        {
            case R.id.copy:
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Url", url));

                Toast toast = Toast.makeText(activity, getString(R.string.toast_url_copied) + ' ' + url, Toast.LENGTH_SHORT);
                toast.show();
                return true;

            case R.id.open:
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;

            case R.id.favourite:
                addToFavourites(activity, feedItem);
                return true;

            case R.id.save_image:
                downloadImage(activity, feedItem.m_imageLink, feedItem.m_imageName);

            default:
                return false;
        }
    }

    private static
    void addToFavourites(Activity activity, FeedItem item)
    {
        AdapterFeedItems adapter = getFavouritesAdapter(activity);

        if(adapter.containsValue(item))
        {
            // TODO item already favourited string.
            //toast = Toast.makeText(activity, activity.getString(R.string.toast_added_feed), Toast.LENGTH_SHORT);
        }
        else
        {
            adapter.put((long) adapter.getCount(), item);
            Toast toast = Toast.makeText(activity, activity.getString(R.string.toast_added_feed, item.m_title), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private static
    void downloadImage(Activity activity, String imageUrl, String imageName)
    {
        if(null != imageUrl && !imageUrl.isEmpty())
        {
            AsyncTaskSaveImage task = new AsyncTaskSaveImage(activity, imageName, imageUrl);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    static
    AdapterFeedItems getFavouritesAdapter(Activity activity)
    {
        FragmentManager manager = activity.getFragmentManager();
        ListFragment fragment = (ListFragment) manager.findFragmentById(R.id.fragment_favourites);
        return (AdapterFeedItems) fragment.getListAdapter();
    }
}
