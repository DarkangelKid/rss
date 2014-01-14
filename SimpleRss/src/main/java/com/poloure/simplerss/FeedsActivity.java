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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

public
class FeedsActivity extends Activity
{
   private static final String READ_ITEMS = "read_items.txt";
   private static final int ALARM_SERVICE_START = 1;
   private static final int ALARM_SERVICE_STOP = 0;
   private static final int MINUTE_VALUE = 60000;
   static Handler s_serviceHandler;
   private ViewPager m_feedsViewPager;
   private ActionBarDrawerToggle m_drawerToggle;
   private String m_applicationFolder;
   private boolean m_showMenuItems = true;

   String m_currentFragment;

   static
   String getApplicationFolder(Context context)
   {
      /* Check the media state for the desirable state. */
      String state = Environment.getExternalStorageState();

      if(!Environment.MEDIA_MOUNTED.equals(state))
      {
         return null;
      }

      File externalFilesDir = context.getExternalFilesDir(null);
      return externalFilesDir.getAbsolutePath() + File.separatorChar;
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

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      m_applicationFolder = getApplicationFolder(this);

      /* Load the read items to the AdapterTag class. */
      AdapterTags.READ_ITEM_TIMES.addAll(Read.longSet(READ_ITEMS, m_applicationFolder));

      /* Get the navigation drawer titles. */
      Resources resources = getResources();
      final FeedsActivity activity = this;
      final String[] navigationTitles = resources.getStringArray(R.array.navigation_titles);

      Drawable appIcon = resources.getDrawable(R.drawable.rss_icon);
      appIcon.setAutoMirrored(true);

      /* Configure the ActionBar. */
      ActionBar actionBar = getActionBar();
      actionBar.setIcon(appIcon);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setTitle(navigationTitles[0]);

      final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

      /* Create the navigation drawer and set all the listeners for it. */
      m_drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer,
            R.string.drawer_open, R.string.drawer_close)
      {
         @Override
         public
         void onDrawerSlide(View drawerView, float slideOffset)
         {
            super.onDrawerSlide(drawerView, slideOffset);
            m_showMenuItems = 0.0F == slideOffset;
            invalidateOptionsMenu();
         }
      };
      drawerLayout.setDrawerListener(m_drawerToggle);

      /* The R.id.content_frame is child 0, the R.id.navigation_list is child 1. */
      ListView navigationList = (ListView) drawerLayout.getChildAt(1);
      navigationList.setAdapter(new AdapterNavigationDrawer(this));

      navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
         @Override
         public
         void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {
            drawerLayout.closeDrawers();

            /* Set the title and switch fragment. */
            String selectedTitle = navigationTitles[2 < position ? 0 : position];
            Utilities.switchFragments(activity, m_currentFragment, selectedTitle);
            getActionBar().setTitle(selectedTitle);

            if(2 < position)
            {
               m_feedsViewPager.setCurrentItem(position - 3);
            }
            else
            {
               Utilities.updateSubtitle(activity,
                     0 == position ? m_feedsViewPager.getCurrentItem() : -1);
            }
         }
      });

      /* Create and hide the fragments that go inside the content frame. */
      if(null == savedInstanceState)
      {
         Fragment[] fragments = {
               new FragmentFeeds(), new ListFragmentManage(), new FragmentSettings()
         };

         FragmentTransaction transaction = getFragmentManager().beginTransaction();
         for(int i = 0; 3 > i; i++)
         {
            transaction.add(R.id.content_frame, fragments[i], navigationTitles[i])
                       .hide(fragments[i]);
         }
         transaction.show(fragments[0]);
         transaction.commit();
         m_currentFragment = navigationTitles[0];
      }
   }

   @Override
   protected
   void onPostCreate(Bundle savedInstanceState)
   {
      super.onPostCreate(savedInstanceState);
      // Sync the toggle state after onRestoreInstanceState has occurred.
      m_drawerToggle.syncState();

      AsyncNavigationAdapter.newInstance(this, m_applicationFolder, 0);

      m_feedsViewPager = (ViewPager) findViewById(R.id.view_pager_tags);
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();

      /* Stop the alarm service and reset the time to 0. */
      setServiceIntent(ALARM_SERVICE_STOP);
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

   @Override
   protected
   void onStop()
   {
      super.onStop();

      Write.longSet(READ_ITEMS, AdapterTags.READ_ITEM_TIMES, m_applicationFolder);

      /* Set the alarm service to go off starting now. */
      setServiceIntent(ALARM_SERVICE_START);
   }

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
      String title = getActionBar().getTitle().toString();
      String[] titles = getResources().getStringArray(R.array.navigation_titles);

      boolean[] show = titles[0].equals(title) ? new boolean[]{true, true, true}
            : new boolean[]{titles[1].equals(title), false, false};

      for(int i = 0; i < menu.size(); i++)
      {
         menu.getItem(i).setEnabled(m_showMenuItems && show[i]);
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

      View refreshIcon = isServiceRunning() ? Utilities.makeProgressBar(this) : null;
      MenuItemCompat.setActionView(refreshItem, refreshIcon);

      /* Update the MenuItem in the ServiceHandler so when the service finishes, the icon changes.*/
      ServiceHandler.s_refreshItem = refreshItem;
      return true;
   }

   private
   boolean isServiceRunning()
   {
      ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
      for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(
            Integer.MAX_VALUE))
      {
         String className = service.service.getClassName();
         String serviceName = ServiceUpdate.class.getName();
         if(serviceName.equals(className))
         {
            return true;
         }
      }
      return false;
   }

   public
   void onAddClick(MenuItem menuItem)
   {
      DialogEditFeed.newInstance(this, -1).show();
   }

   public
   void onUnreadClick(MenuItem menuItem)
   {
      int currentPage = m_feedsViewPager.getCurrentItem();

      FragmentManager manager = getFragmentManager();
      ListFragment listFragment = (ListFragment) manager.findFragmentByTag(
            Utilities.FRAGMENT_ID_PREFIX + currentPage);

      ListView listViewTags = listFragment.getListView();

      gotoLatestUnread(listViewTags);
   }

   public
   void onRefreshClick(MenuItem menuItem)
   {
      MenuItemCompat.setActionView(menuItem, Utilities.makeProgressBar(this));

      /* Set the service handler in FeedsActivity so we can check and call it from ServiceUpdate. */
      FragmentManager manager = getFragmentManager();
      s_serviceHandler = new ServiceHandler(manager, menuItem, m_applicationFolder);

      Intent intent = new Intent(this, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", m_feedsViewPager.getCurrentItem());
      startService(intent);
   }
}
