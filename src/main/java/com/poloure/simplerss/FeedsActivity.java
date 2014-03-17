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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public
class FeedsActivity extends Activity
{
   static final String READ_ITEMS = "read_items.txt";
   private static final int ALARM_SERVICE_START = 1;
   private static final int ALARM_SERVICE_STOP = 0;
   private static final int MINUTE_VALUE = 60000;
   private final BroadcastReceiver Receiver = new BroadcastReceiver()
   {
      @Override
      public
      void onReceive(Context context, Intent intent)
      {
         FeedsActivity activity = (FeedsActivity) getWindow().getDecorView().getContext();

         AsyncNewTagAdapters.update(activity);

         /* Manage adapter is updated every time it is shown but in case the user switched to the
            manage fragment mid refresh. */
         AsyncManageAdapter.update(activity);
         AsyncNavigationAdapter.update(activity);

         ((PullToRefreshLayout) activity.findViewById(R.id.viewpager)
               .getParent()).setRefreshComplete();
      }
   };
   boolean m_showMenuItems = true;
   int m_previousFragmentId;
   int m_currentFragmentId;
   FragmentNavigationDrawer m_FragmentDrawer;
   List<IndexItem> m_index;

   static
   Collection<Long> loadReadItems(Context context)
   {
      Set<Long> set = (Set<Long>) Read.object(context, READ_ITEMS);
      return null == set ? new HashSet<Long>(0) : set;
   }

   static
   void setTopOffset(Activity activity)
   {
      if(!ViewConfiguration.get(activity)
            .hasPermanentMenuKey() && Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT)
      {
         Resources resources = activity.getResources();
         TypedValue value = new TypedValue();

         activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true);
         int actionBar = activity.getResources().getDimensionPixelSize(value.resourceId);
         int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
         int statusBar = resources.getDimensionPixelSize(resourceId);

         activity.findViewById(android.R.id.content).setPadding(0, actionBar + statusBar, 0, 0);
      }
   }

   static
   void gotoLatestUnread(ListView listView)
   {
      AdapterTags listAdapter = (AdapterTags) listView.getAdapter();

      /* Create a copy of the item times. */
      List<Long> times = new ArrayList<Long>(listAdapter.m_times);
      times.removeAll(AdapterTags.READ_ITEM_TIMES);

      if(times.isEmpty())
      {
         listView.setSelection(0);
      }
      else
      {
         int index = listAdapter.m_times.indexOf(times.get(times.size() - 1));
         listView.setSelection(index);
      }
   }

   /* Called only when no remnants of the Activity exist. */
   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_main);

      /* Load the index. */
      m_index = Utilities.loadIndexList(this);
      m_currentFragmentId = R.id.fragment_feeds;

      /* Load the read items to the tags Adapter. */
      AdapterTags.READ_ITEM_TIMES.addAll(loadReadItems(this));

      FragmentManager manager = getFragmentManager();
      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

      m_FragmentDrawer = (FragmentNavigationDrawer) manager.findFragmentById(R.id.fragment_navigation_drawer);
      m_FragmentDrawer.setUp(drawerLayout);

      setTopOffset(this);

      if(null == savedInstanceState)
      {
         /* Create and hide the fragments that go inside the content frame. */
         FragmentTransaction trans = manager.beginTransaction();

         trans.hide(manager.findFragmentById(R.id.fragment_favourites))
               .hide(manager.findFragmentById(R.id.fragment_settings))
               .hide(manager.findFragmentById(R.id.fragment_manage))
               .hide(manager.findFragmentById(R.id.fragment_web))
               .commit();
      }
   }

   /* Stop the alarm service and reset the time to 0 every time the user sees the activity. */
   @Override
   protected
   void onResume()
   {
      super.onResume();
      setServiceIntent(ALARM_SERVICE_STOP);
      registerReceiver(Receiver, new IntentFilter(ServiceUpdate.BROADCAST_ACTION));

      /* Update the navigation adapter. This updates the subtitle and title. */
      if(R.id.fragment_web != m_currentFragmentId)
      {
         AsyncNavigationAdapter.update(this);
      }


      PullToRefreshLayout layout = (PullToRefreshLayout) findViewById(R.id.viewpager).getParent();
      if(!layout.isRefreshing() && isServiceRunning())
      {
         ((PullToRefreshLayout) findViewById(R.id.viewpager).getParent()).setRefreshing(true);
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

   @Override
   protected
   void onPause()
   {
      super.onPause();
      unregisterReceiver(Receiver);
   }

   /* Start the alarm service every time the activity is not visible. */
   @Override
   protected
   void onStop()
   {
      super.onStop();
      Write.object(this, READ_ITEMS, AdapterTags.READ_ITEM_TIMES);
      Write.object(this, Read.INDEX, m_index);
      Write.object(this, Read.FAVOURITES, ListFragmentTag.getFavouritesAdapter(this).m_feedItems);
      setServiceIntent(ALARM_SERVICE_START);
   }

   @Override
   public
   void onConfigurationChanged(Configuration newConfig)
   {
      super.onConfigurationChanged(newConfig);

      FragmentManager manager = getFragmentManager();
      FragmentTransaction trans = manager.beginTransaction();

      if(600 <= newConfig.screenWidthDp && Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation)
      {
         trans.show(manager.findFragmentById(R.id.fragment_web)).commit();
      }
      else if(R.id.fragment_web != m_currentFragmentId && Configuration.ORIENTATION_PORTRAIT == newConfig.orientation)
      {
         trans.hide(manager.findFragmentById(R.id.fragment_web));
      }

      /* Update the padding of the content view. */
      setTopOffset(this);
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(android.R.id.home == item.getItemId() && R.id.fragment_web == m_currentFragmentId)
      {
         onBackPressed();
         return true;
      }
      return m_FragmentDrawer.m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
   }

   @Override
   public
   boolean onPrepareOptionsMenu(Menu menu)
   {
      boolean web = R.id.fragment_web == m_currentFragmentId;
      boolean feed = R.id.fragment_feeds == m_currentFragmentId;
      boolean manage = R.id.fragment_manage == m_currentFragmentId;

      menu.getItem(0).setVisible(!web).setEnabled(m_showMenuItems && (feed || manage));
      menu.getItem(1).setVisible(!web).setEnabled(m_showMenuItems && feed);
      menu.getItem(2).setVisible(web);

      return super.onPrepareOptionsMenu(menu);
   }

   /* TODO: Needs a method of checking if the service is running. */
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

   private
   void setServiceIntent(int state)
   {
      /* Load the ManageFeedsRefresh boolean value from settings. */
      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

      if(!pref.getBoolean("refreshing_enabled", false) && ALARM_SERVICE_START == state)
      {
         return;
      }

      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = new Intent(this, ServiceUpdate.class);

      PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

      /* Depending on the state string, start or stop the service. */
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

   @Override
   public
   void onBackPressed()
   {
      super.onBackPressed();

      if(R.id.fragment_web == m_currentFragmentId)
      {
         Utilities.setTitlesAndDrawerAndPage(this, m_previousFragmentId, -10);

         /* Switch back to our old tag. */
         m_currentFragmentId = m_previousFragmentId;
         m_previousFragmentId = R.id.fragment_web;

         m_FragmentDrawer.m_drawerToggle.setDrawerIndicatorEnabled(true);
         invalidateOptionsMenu();
      }
   }

   public
   void onAddClick(MenuItem menuItem)
   {
      DialogEditFeed.newInstance(this, -1).show();
   }

   public
   void onUnreadClick(MenuItem menuItem)
   {
      ListView listView = Utilities.getCurrentTagListView(this);

      if(null != listView)
      {
         gotoLatestUnread(listView);
      }
   }
}
