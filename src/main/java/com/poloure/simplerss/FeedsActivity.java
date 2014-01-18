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

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

public
class FeedsActivity extends Activity
{
   private static final String READ_ITEMS = "read_items.txt";
   private static final int ALARM_SERVICE_START = 1;
   private static final int ALARM_SERVICE_STOP = 0;
   private static final int MINUTE_VALUE = 60000;
   static Handler s_serviceHandler;
   private ActionBarDrawerToggle m_drawerToggle;
   boolean m_showMenuItems = true;

   String m_currentFragment;

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

      setContentView(R.layout.navigation_drawer_and_content_frame);

      /* Load the read items to the AdapterTag class. */
      AdapterTags.READ_ITEM_TIMES.addAll(Read.longSet(this, READ_ITEMS));

      /* Configure the ActionBar. */
      ActionBar actionBar = getActionBar();
      if(null != actionBar)
      {
         Resources resources = getResources();
         String[] navigationTitles = resources.getStringArray(R.array.navigation_titles);

         actionBar.setTitle(navigationTitles[0]);
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setHomeButtonEnabled(true);

         Drawable appIcon = resources.getDrawable(R.drawable.rss_icon);
         if(null != appIcon)
         {
            appIcon.setAutoMirrored(true);
            actionBar.setIcon(appIcon);
         }
      }

      /* Create the navigation drawer and set all the listeners for it. */
      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      m_drawerToggle = new OnActionBarDrawerToggle(this, drawerLayout);
      drawerLayout.setDrawerListener(m_drawerToggle);

      ListView navigationList = (ListView) findViewById(R.id.navigation_list);
      navigationList.setAdapter(new AdapterNavigationDrawer(this));
      navigationList.setOnItemClickListener(new OnNavigationListItemClick(this));

      /* This method calls navigationList.getAdapter(). */
      AsyncNavigationAdapter.newInstance(this);

      /* Create and hide the fragments that go inside the content frame. */
      FragmentManager manager = getFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();

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

   /* Called only when the app is coming from onStop() (not visible). */
   @Override
   protected
   void onRestart()
   {
      super.onRestart();
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();
   }

   /* Load the saved state from configuration change. */
   @Override
   protected
   void onRestoreInstanceState(Bundle savedInstanceState)
   {
      super.onRestoreInstanceState(savedInstanceState);
   }

   /* Stop the alarm service and reset the time to 0 every time the user sees the activity. */
   @Override
   protected
   void onResume()
   {
      super.onResume();
      m_drawerToggle.syncState();
      setServiceIntent(ALARM_SERVICE_STOP);

      /* Update the navigation adapter. */
      AsyncNavigationAdapter.newInstance(this);
   }

   /* Activity is now running.
    *
    * These methods are for any state change post running.
    */

   /* Add information to outState that will be passed to onCreate(Bundle savedStateInstance) &
    * onRestoreInstanceState(Bundle savedInstanceState). */
   @Override
   protected
   void onSaveInstanceState(Bundle outState)
   {
      super.onSaveInstanceState(outState);
   }

   @Override
   protected
   void onPause()
   {
      super.onPause();
   }

   /* Start the alarm service every time the activity is not visible. */
   @Override
   protected
   void onStop()
   {
      super.onStop();
      setServiceIntent(ALARM_SERVICE_START);
   }

   /* Save any data stored in memory to disk before destroyed. */
   @Override
   protected
   void onDestroy()
   {
      super.onDestroy();
      Write.longSet(this, READ_ITEMS, AdapterTags.READ_ITEM_TIMES);
   }

   /* Option menu methods.
    *
    *
    */

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      return m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
   }

   @Override
   public
   boolean onPrepareOptionsMenu(Menu menu)
   {
      /* If the title is not the feeds title, disable the buttons accordingly. */
      ActionBar actionBar = getActionBar();
      String[] titles = getResources().getStringArray(R.array.navigation_titles);

      if(null != actionBar)
      {
         String title = actionBar.getTitle().toString();

         boolean[] show = titles[0].equals(title) ? new boolean[]{true, true, true}
               : new boolean[]{titles[1].equals(title), false, false};

         for(int i = 0; i < menu.size(); i++)
         {
            menu.getItem(i).setEnabled(m_showMenuItems && show[i]);
         }
      }
      return true;
   }

   @Override
   public
   boolean onCreateOptionsMenu(Menu menu)
   {
      menu.clear();

      MenuInflater menuInflater = getMenuInflater();
      menuInflater.inflate(R.menu.action_bar_menu, menu);

      /* Set the refreshItem to spin if the service is running. The handler will stop it. */
      MenuItem refreshItem = menu.findItem(R.id.refresh);

      if(isServiceRunning())
      {
         MenuItemCompat.setActionView(refreshItem, R.layout.progress_bar_refresh);
      }
      else
      {
         MenuItemCompat.setActionView(refreshItem, null);
      }

      /* Update the MenuItem in the ServiceHandler so when the service finishes, the icon changes.*/
      ServiceHandler.s_refreshItem = refreshItem;
      return true;
   }

   /* The end of overridden methods.
    *
    *
    */

   private
   boolean isServiceRunning()
   {
      ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
      for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(
            Integer.MAX_VALUE))
      {
         if(ServiceUpdate.class.getName().equals(service.service.getClassName()))
         {
            return true;
         }
      }
      return false;
   }

   private
   void setServiceIntent(int state)
   {
      /* Load the ManageFeedsRefresh boolean value from settings. */
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      boolean refreshDisabled = !preferences.getBoolean("refreshing_enabled", false);

      if(refreshDisabled && ALARM_SERVICE_START == state)
      {
         return;
      }

      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = new Intent(this, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", 0);

      PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

      /* Depending on the state string, start or stop the service. */
      if(ALARM_SERVICE_START == state)
      {
         String intervalString = preferences.getString("refresh_interval", "120");
         int refreshInterval = Integer.parseInt(intervalString);

         long interval = (long) refreshInterval * MINUTE_VALUE;
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
      ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager_tags);
      int currentPage = viewPager.getCurrentItem();

      FragmentManager manager = getFragmentManager();
      ListFragment listFragment = (ListFragment) manager.findFragmentByTag(
            Utilities.FRAGMENT_ID_PREFIX + currentPage);

      if(null != listFragment)
      {
         gotoLatestUnread(listFragment.getListView());
      }
   }

   public
   void onRefreshClick(MenuItem menuItem)
   {
      MenuItemCompat.setActionView(menuItem, R.layout.progress_bar_refresh);

      /* Set the service handler in FeedsActivity so we can check and call it from ServiceUpdate. */
      FragmentManager manager = getFragmentManager();
      ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager_tags);
      s_serviceHandler = new ServiceHandler(manager, menuItem);

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
      Adapter listAdapter = listView.getAdapter();
      for(int i = listAdapter.getCount() - 1; 0 <= i; i--)
      {
         FeedItem feedItem = (FeedItem) listAdapter.getItem(i);
         if(!AdapterTags.READ_ITEM_TIMES.contains(feedItem.m_time))
         {
            listView.setSelection(i);
            break;
         }
      }
   }
}
