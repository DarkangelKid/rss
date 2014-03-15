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
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public
class FeedsActivity extends Activity
{
   static final String READ_ITEMS = "read_items.txt";
   private static final int ALARM_SERVICE_START = 1;
   private static final int ALARM_SERVICE_STOP = 0;
   private static final int MINUTE_VALUE = 60000;
   boolean m_showMenuItems = true;

   String m_previousTag;
   String m_currentTag;

   FragmentNavigationDrawer m_FragmentDrawer;

   List<IndexItem> m_index;

   static final String WEB_TAG = "Web";
   static final String FEED_TAG = "Feeds";
   static final String FAVOURITES_TAG = "Favourites";
   static final String MANAGE_TAG = "Manage";
   static final String SETTINGS_TAG = "Settings";
   static final String[] FRAGMENT_TAGS = {FAVOURITES_TAG, MANAGE_TAG, SETTINGS_TAG, FEED_TAG};

   static
   class SettingsFragment extends PreferenceFragment
   {
      @Override
      public
      void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
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

      /* Load the read items to the tags Adapter. */
      AdapterTags.READ_ITEM_TIMES.addAll(Utilities.loadReadItems(this));

      m_currentTag = FEED_TAG;

      FragmentManager manager = getFragmentManager();

      m_FragmentDrawer = (FragmentNavigationDrawer) manager.findFragmentById(R.id.navigation_drawer);
      m_FragmentDrawer.setUp((DrawerLayout) findViewById(R.id.drawer_layout));

      /* Create and hide the fragments that go inside the content frame. */
      FragmentUtils.addAllFragments(this);
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
      if(!m_currentTag.equals(WEB_TAG))
      {
         AsyncNavigationAdapter.update(this);
      }
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
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(android.R.id.home == item.getItemId() && m_currentTag.equals(WEB_TAG))
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
      boolean web = m_currentTag.equals(WEB_TAG);
      boolean feed = m_currentTag.equals(FEED_TAG);
      boolean manage = m_currentTag.equals(MANAGE_TAG);

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

         ((PullToRefreshLayout) findViewById(R.id.ptr_layout)).setRefreshComplete();
      }
   };

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

      if(m_currentTag.equals(WEB_TAG))
      {
         Utilities.setTitlesAndDrawerAndPage(this, m_previousTag, -10);

         /* Switch back to our old tag. */
         m_currentTag = m_previousTag;
         m_previousTag = WEB_TAG;

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
}
