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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public
class FeedsActivity extends Activity
{
   static final String READ_ITEMS = "read_items.txt";
   private static final int ALARM_SERVICE_START = 1;
   private static final int ALARM_SERVICE_STOP = 0;
   private static final int MINUTE_VALUE = 60000;
   private boolean m_stopProgressBar;
   boolean m_showMenuItems = true;

   String m_currentFragment;
   private FragmentNavigationDrawer m_FragmentNavigationDrawer;

   private static final String FEED_TAG = "Feeds";
   private static final String MANAGE_TAG = "Manage";
   private static final String SETTINGS_TAG = "Settings";
   static final String[] FRAGMENT_TAGS = {FEED_TAG, MANAGE_TAG, SETTINGS_TAG};

   /* Start of ordered state changes of the Activity.
    *
    *
    */

   /* Called only when no remnants of the Activity exist. */
   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_main);

      /* Load the read items to the AdapterTag class. */
      AdapterTags.READ_ITEM_TIMES.addAll(Read.longSet(this, READ_ITEMS));

      FragmentManager manager = getFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();

      m_FragmentNavigationDrawer = (FragmentNavigationDrawer) manager.findFragmentById(R.id.navigation_drawer);
      m_FragmentNavigationDrawer.setUp((DrawerLayout) findViewById(R.id.drawer_layout));

      /* Create and hide the fragments that go inside the content frame. */
      for(int i = 0; FRAGMENT_TAGS.length > i; i++)
      {
         Fragment fragment = getFragment(manager, FRAGMENT_TAGS[i]);
         if(!fragment.isAdded())
         {
            transaction.add(R.id.content_frame, fragment, FRAGMENT_TAGS[i]);
         }
         if(0 != i && !fragment.isHidden())
         {
            transaction.hide(fragment);
         }
      }

      transaction.commit();
      m_currentFragment = FRAGMENT_TAGS[0];
   }

   /* Stop the alarm service and reset the time to 0 every time the user sees the activity. */
   @Override
   protected
   void onResume()
   {
      super.onResume();
      setServiceIntent(ALARM_SERVICE_STOP);
      registerReceiver(Receiver, new IntentFilter(ServiceUpdate.BROADCAST_ACTION));

      /* Update the navigation adapter. */
      AsyncNavigationAdapter.update(this);
   }

   /* Activity is now running.
    *
    * These methods are for any state change post running.
    */

   @Override
   protected
   void onPause()
   {
      super.onPause();
      unregisterReceiver(Receiver);

      /* This stops the user accidentally reading items when resuming. */
      ListFragmentTag.s_hasScrolled = false;
      ListFragmentTag.s_firstLoad = true;
   }

   /* Start the alarm service every time the activity is not visible. */
   @Override
   protected
   void onStop()
   {
      super.onStop();
      Write.longSet(this, READ_ITEMS, AdapterTags.READ_ITEM_TIMES);
      setServiceIntent(ALARM_SERVICE_START);
   }

   /* Option menu methods.
    *
    *
    */

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      return m_FragmentNavigationDrawer.m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
   }

   @Override
   public
   boolean onPrepareOptionsMenu(Menu menu)
   {
      boolean[] show = new boolean[3];
      if(m_currentFragment.equals(FRAGMENT_TAGS[0]))
      {
         Arrays.fill(show, true);
      }
      else if(m_currentFragment.equals(FRAGMENT_TAGS[1]))
      {
         show[0] = true;
      }

      for(int i = 0; 3 > i; i++)
      {
         menu.getItem(i).setEnabled(m_showMenuItems && show[i]);
      }
      return true;
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

      if(m_stopProgressBar)
      {
         MenuItem item = menu.findItem(R.id.refresh);
         MenuItemCompat.setActionView(item, null);
         m_stopProgressBar = false;
         return true;
      }

      return super.onCreateOptionsMenu(menu);
   }

   /* The end of overridden methods.
    *
    *
    */

   private final BroadcastReceiver Receiver = new BroadcastReceiver()
   {
      @Override
      public
      void onReceive(Context context, Intent intent)
      {
         Activity activity = (Activity) getWindow().getDecorView().getContext();

         /* Refresh the tag page. */
         AsyncNewTagAdapters.update(activity);
         AsyncManageAdapter.update(activity);
         AsyncNavigationAdapter.update(activity);

         /* Tell the refresh icon to stop spinning. */
         m_stopProgressBar = true;
         invalidateOptionsMenu();
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

   public
   void onAddClick(MenuItem menuItem)
   {
      DialogEditFeed.newInstance(this, -1).show();
   }

   public
   void onUnreadClick(MenuItem menuItem)
   {
      ViewPager viewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);
      ListView listView = (ListView) findViewById(20000 + viewPager.getCurrentItem());

      if(null != listView)
      {
         gotoLatestUnread(listView);
      }
   }

   public
   void onRefreshClick(MenuItem menuItem)
   {
      MenuItemCompat.setActionView(menuItem, R.layout.progress_bar_refresh);

      ViewPager viewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);

      Intent intent = new Intent(this, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", viewPager.getCurrentItem());
      startService(intent);
   }

   static
   Fragment getFragment(FragmentManager manager, String tag)
   {
      Fragment fragment = manager.findFragmentByTag(tag);
      if(null == fragment)
      {
         switch(tag)
         {
            case FEED_TAG:
               return new FragmentFeeds();
            case MANAGE_TAG:
               return new ListFragmentManage();
            case SETTINGS_TAG:
               return new FragmentSettings();
         }
      }
      return fragment;
   }

   static
   void gotoLatestUnread(ListView listView)
   {
      AdapterTags listAdapter = (AdapterTags) listView.getAdapter();

      /* Create a copy of the item times. */
      List<Long> times = new ArrayList<>(listAdapter.m_times.size());
      times.addAll(listAdapter.m_times);
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
