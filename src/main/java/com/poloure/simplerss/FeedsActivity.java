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
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;

import com.poloure.simplerss.adapters.AdapterFeedItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.poloure.simplerss.Constants.*;

public
class FeedsActivity extends Activity
{
    static final String READ_ITEMS = "read_items.txt";
    static final String INDEX = "index.txt";
    static final String FAVOURITES = "favourites.txt";
    private static final int ALARM_SERVICE_START = 1;
    private static final int ALARM_SERVICE_STOP = 0;
    private static final int MINUTE_VALUE = 60000;
    private final Set<Long> mReadItemTimes = Collections.synchronizedSet(new HashSet<Long>(0));
    private final BroadcastReceiver m_broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public
        void onReceive(Context context, Intent intent)
        {
            if(null != s_activity)
            {
                AsyncNewTagAdapters.update(s_activity);

                // Manage adapter is updated every time it is shown but in case the user switched to the
                // manage fragment mid refresh.
                AsyncManageAdapter.run(s_activity);
                AsyncNavigationAdapter.run(s_activity);

                s_pullToRefreshLayout.setRefreshComplete();
            }
        }
    };
    boolean m_showMenuItems = true;
    List<IndexItem> m_index;

    /* Called only when no remnants of the Activity exist. */
    @Override
    public
    void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        saveInitialConstants(this);

        RssLogger.setup();

        // Load the index.
        ObjectIO indexReader = new ObjectIO(this, INDEX);
        m_index = (List<IndexItem>) indexReader.readCollection(ArrayList.class);

        /* Load the read items to the tags Adapter. */
        ObjectIO readItemReader = new ObjectIO(this, READ_ITEMS);
        Collection<Long> set = (HashSet<Long>) readItemReader.readCollection(HashSet.class);
        mReadItemTimes.addAll(set);

        s_fragmentDrawer.setUp(s_drawerLayout);

        setTopOffset(this);

        if(null == savedInstanceState)
        {
            /* Create and hide the fragments that go inside the content frame. */
            if(!canFitTwoFragments())
            {
                hideFragments(s_fragmentWeb);
            }

            hideFragments(s_fragmentFavourites, s_fragmentManage, s_fragmentSettings);
            s_fragmentManager.executePendingTransactions();
        }
    }

    /**
     * Checks to see if the Activity is using (or should be using) a two pane fragment layout.
     *
     * @return true if the display is rotated horizontally and the activity is using the w600dp
     * res folder for layout resources.
     */
    static
    boolean canFitTwoFragments()
    {
        return 600 <= s_displayMetrics.widthPixels / s_displayMetrics.density && isDisplayHorizontal();
    }

    /**
     * Checks to see if the default Display in the WindowManager is rotated horizontally.
     *
     * @return true if the display is currently rotated horizontally, false otherwise.
     */
    private static
    boolean isDisplayHorizontal()
    {
        Display display = s_windowManager.getDefaultDisplay();
        int rotation = display.getRotation();
        return Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation;
    }

    @Override
    protected
    void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        saveViews();
    }

    /* Stop the alarm service and reset the time to 0 every time the user sees the activity. */
    @Override
    protected
    void onResume()
    {
        super.onResume();
        setServiceIntent(ALARM_SERVICE_STOP);
        registerReceiver(m_broadcastReceiver, new IntentFilter(ServiceUpdate.BROADCAST_ACTION));

        // Update the navigation adapter. This updates the subtitle and title.
        if(s_fragmentWeb.isHidden())
        {
            AsyncNavigationAdapter.run(this);
        }

        if(!s_pullToRefreshLayout.isRefreshing() && isServiceRunning())
        {
            s_pullToRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    protected
    void onPause()
    {
        super.onPause();
        unregisterReceiver(m_broadcastReceiver);
    }

    /* Start the alarm service every time the activity is not visible. */
    @Override
    protected
    void onStop()
    {
        super.onStop();

        // Write the read items set to file.
        ObjectIO out = new ObjectIO(this, READ_ITEMS);
        out.write(mReadItemTimes);

        // Write the index file to disk.
        out.setNewFileName(INDEX);
        out.write(m_index);

        // Write the favourites list to file.
        AdapterFeedItems adapter = FragmentTag.getFavouritesAdapter(this);
        Collection<FeedItem> favourites = adapter.getSet();

        out.setNewFileName(FAVOURITES);
        out.write(favourites);

        setServiceIntent(ALARM_SERVICE_START);
    }

    @Override
    public
    void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if(600 <= newConfig.screenWidthDp && Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation)
        {
            showFragments(s_fragmentWeb);
        }
        else if(600 <= newConfig.screenWidthDp && Configuration.ORIENTATION_PORTRAIT == newConfig.orientation)
        {
            hideFragments(s_fragmentWeb);
        }

        // Update the padding of the content view.
        setTopOffset(this);
    }

    @Override
    public
    void onBackPressed()
    {
        if(s_fragmentWeb.isVisible() && !canFitTwoFragments())
        {
            super.onBackPressed();

            s_fragmentManager.executePendingTransactions();
            Utilities.setTitlesAndDrawerAndPage(null, -10);

            s_drawerToggle.setDrawerIndicatorEnabled(true);
            s_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            invalidateOptionsMenu();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public
    boolean onCreateOptionsMenu(Menu menu)
    {
        if(0 == menu.size())
        {
            getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public
    boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean web = s_fragmentWeb.isVisible() && !canFitTwoFragments();
        boolean feed = s_fragmentFeeds.isVisible();
        boolean manage = s_fragmentManage.isVisible();

        menu.getItem(0).setVisible(!web).setEnabled(m_showMenuItems && (feed || manage));
        menu.getItem(1).setVisible(!web).setEnabled(m_showMenuItems && feed);
        menu.getItem(2).setVisible(web);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public
    boolean onOptionsItemSelected(MenuItem item)
    {
        return s_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private
    void setServiceIntent(int state)
    {
        // Load the ManageFeedsRefresh boolean value from settings.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if(!pref.getBoolean("refreshing_enabled", false) && ALARM_SERVICE_START == state)
        {
            return;
        }

        // Create intent, turn into pending intent, and get the alarm manager.
        Intent intent = new Intent(this, ServiceUpdate.class);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Depending on the state string, start or stop the service.
        if(ALARM_SERVICE_START == state)
        {
            String intervalString = pref.getString("refresh_interval", "120");

            long interval = Long.parseLong(intervalString) * MINUTE_VALUE;
            long next = System.currentTimeMillis() + interval;
            am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pendingIntent);
        }
        else if(ALARM_SERVICE_STOP == state)
        {
            am.cancel(pendingIntent);
        }
    }

    private
    boolean isServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(ServiceUpdate.class.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Mark a FeedItem as read.
     *
     * @param itemTime to add to collection of read items.
     */
    public
    void readItem(long itemTime)
    {
        mReadItemTimes.add(itemTime);
    }

    /**
     * Checks if an item with this time is in the read collection.
     *
     * @param itemTime the time of the item to check
     *
     * @return true if there exists a FeedItem in the collection with this itemTime vaule.
     */
    public
    boolean isItemRead(long itemTime)
    {
        return mReadItemTimes.contains(itemTime);
    }

    public
    Collection<Long> getReadItemTimes()
    {
        return mReadItemTimes;
    }

    public
    void onAddClick(MenuItem menuItem)
    {
        DialogEditFeed.newInstance(this, -1).show();
    }
}
