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
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public
class FeedsActivity extends Activity
{
   static final String READ_ITEMS = "read_items.txt";
   private static final int ALARM_SERVICE_START = 1;
   private static final int ALARM_SERVICE_STOP = 0;
   private static final int MINUTE_VALUE = 60000;
   boolean m_showMenuItems = true;

   String m_currentFragment;
   private FragmentNavigationDrawer m_FragmentNavigationDrawer;

   static List<IndexItem> s_index;

   static final String FEED_TAG = "Feeds";
   static final String FAVOURITES_TAG = "Favourites";
   static final String MANAGE_TAG = "Manage";
   static final String SETTINGS_TAG = "Settings";
   static final String[] FRAGMENT_TAGS = {FAVOURITES_TAG, MANAGE_TAG, SETTINGS_TAG, FEED_TAG};

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

      /* Load the index. */
      s_index = (List<IndexItem>) Read.object(this, Read.INDEX);
      if(null == s_index)
      {
         s_index = new ArrayList<IndexItem>(0);
      }

      /* Load the read items to the AdapterTag class. */
      Collection<Long> set = (Collection<Long>) Read.object(this, READ_ITEMS);
      if(null != set)
      {
         AdapterTags.READ_ITEM_TIMES.addAll(set);
      }

      m_currentFragment = FEED_TAG;

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
         if(3 != i && !fragment.isHidden())
         {
            transaction.hide(fragment);
         }
      }
      transaction.commit();
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
      ListFragmentTag.s_firstLoad = true;
   }

   /* Start the alarm service every time the activity is not visible. */
   @Override
   protected
   void onStop()
   {
      super.onStop();
      Write.object(this, READ_ITEMS, AdapterTags.READ_ITEM_TIMES);
      Write.object(this, Read.INDEX, s_index);
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
      boolean feed = m_currentFragment.equals(FEED_TAG);
      boolean manage = m_currentFragment.equals(MANAGE_TAG);

      menu.getItem(0).setEnabled(m_showMenuItems && (feed || manage));
      menu.getItem(1).setEnabled(m_showMenuItems && feed);

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

   /* Large section that declares the three main fragments. */
   static
   Fragment getFragment(FragmentManager manager, String tag)
   {
      Fragment fragment = manager.findFragmentByTag(tag);
      if(null == fragment)
      {
         if(tag.equals(FEED_TAG))
         {
            return new Fragment()
            {
               private static final float PULL_DISTANCE = 0.5F;

               @Override
               public
               View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
               {
                  super.onCreateView(inflater, container, savedInstanceState);

                  PullToRefreshLayout layout = (PullToRefreshLayout) inflater.inflate(R.layout.viewpager, null, false);
                  final Activity activity = getActivity();

                  final ViewPager pager = (ViewPager) layout.findViewById(R.id.viewpager);

                  ActionBarPullToRefresh.from(activity)
                        .allChildrenArePullable()
                        .options(Options.create().scrollDistance(PULL_DISTANCE).build())
                        .useViewDelegate(ViewPager.class, new ViewPagerDelegate())
                        .listener(new OnRefreshListener()
                        {
                           @Override
                           public
                           void onRefreshStarted(View view)
                           {
                              Intent intent = new Intent(activity, ServiceUpdate.class);
                              intent.putExtra("GROUP_NUMBER", pager.getCurrentItem());
                              activity.startService(intent);
                           }
                        })
                        .setup(layout);

                  /* Inflate and configure the ViewPager. */
                  pager.setOffscreenPageLimit(128);
                  pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
                  {
                     @Override
                     public
                     void onPageSelected(int position)
                     {
                        /* Set the item to be checked in the navigation drawer. */
                        Utilities.setNavigationTagSelection(activity, position);

                        /* Set the subtitle to the unread count. */
                        Utilities.updateTagTitle(activity);
                     }
                  });
                  return layout;
               }

               @Override
               public
               void onActivityCreated(Bundle savedInstanceState)
               {
                  super.onActivityCreated(savedInstanceState);

                  ViewPager pager = (ViewPager) getView().findViewById(R.id.viewpager);
                  Activity activity = getActivity();

                  pager.setAdapter(new PagerAdapterTags(getFragmentManager(), activity));
                  Utilities.setNavigationTagSelection(activity, 0);
               }
            };
         }
         if(tag.equals(FAVOURITES_TAG))
         {
            return new ListFragmentFavourites();
         }
         if(tag.equals(MANAGE_TAG))
         {
            return new ListFragmentManage();
         }
         if(tag.equals(SETTINGS_TAG))
         {
            return new PreferenceFragment()
            {
               @Override
               public
               void onCreate(Bundle savedInstanceState)
               {
                  super.onCreate(savedInstanceState);
                  addPreferencesFromResource(R.xml.preferences);
               }
            };
         }
      }
      return fragment;
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
