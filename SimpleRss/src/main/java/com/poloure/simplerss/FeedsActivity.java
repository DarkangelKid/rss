package com.poloure.simplerss;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;

public
class FeedsActivity extends ActionBarActivity
{
   static final         int[]  COLOR_INTS          = {
         Color.rgb(51, 181, 229), // blue
         Color.rgb(170, 102, 204), // purple
         Color.rgb(153, 204, 0), // green
         Color.rgb(255, 187, 51), // orange
         Color.rgb(255, 68, 68) // red
   };
   private static final String ALARM_SERVICE_START = "start";
   private static final String ALARM_SERVICE_STOP  = "stop";
   static         String[]      HOLO_COLORS;
   static         Menu          s_optionsMenu;
   static         Handler       s_serviceHandler;
   /* Only initialized when the activity is running. */
   private static FeedsActivity s_activity;

   static
   FeedsActivity getActivity()
   {
      return s_activity;
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
      {
         /* If the screen is now in landscape mode, we can show the
          * dialog in-line with the list so we don't need this activity. */
         finish();
         return;
      }

      setContentView(R.layout.navigation_drawer_and_content_frame);

      Util.setContext(this);
      s_activity = this;

      HOLO_COLORS = Util.getArray(R.array.settings_colours);

      ActionBar actionBar = getSupportActionBar();
      actionBar.setIcon(R.drawable.rss_icon);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);

      Util.remove(Constants.DUMP_FILE);

      Util.mkdir(Constants.SETTINGS_DIR);

      /* Create the navigation drawer and set all the listeners for it. */
      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      ListView navigationList = (ListView) findViewById(R.id.left_drawer);
      NavDrawer navDrawer = new NavDrawer(navigationList, drawerLayout);

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      BaseAdapter navigationDrawer = navDrawer.getAdapter();
      if(null == savedInstanceState)
      {
         Fragment[] mainFragments = {
               new FragmentFeeds(navigationDrawer, this),
               new FragmentManage(navigationDrawer),
               new FragmentSettings(),
         };

         int frame = R.id.content_frame;

         FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
         tran.add(frame, mainFragments[0], NavDrawer.NAV_TITLES[0])
               .add(frame, mainFragments[1], NavDrawer.NAV_TITLES[1])
               .add(frame, mainFragments[2], NavDrawer.NAV_TITLES[2])
               .hide(mainFragments[1])
               .hide(mainFragments[2])
               .commit();
      }

      Util.updateTags(navigationDrawer);
      Update.page(navigationDrawer, 0);
   }

   /* This is so the icon and text in the actionbar are selected. */
   @Override
   public
   void onConfigurationChanged(Configuration newConfig)
   {
      super.onConfigurationChanged(newConfig);
      NavDrawer.s_drawerToggle.onConfigurationChanged(newConfig);
   }

   @Override
   protected
   void onStop()
   {
      super.onStop();
      /* Set the alarm service to go of starting now. */
      setServiceIntent(ALARM_SERVICE_START);

      /* Save the READ_ITEMS to file. */
      Write.collection(Constants.READ_ITEMS, AdapterTag.s_readLinks);
      finish();
   }

   private static
   void setServiceIntent(String state)
   {
      Context con = Util.getContext();
      int time = AdapterSettingsFunctions.TIMES[3];
      String[] names = AdapterSettingsFunctions.FILE_NAMES;

      /* Load the ManageFeedsRefresh boolean value from settings. */
      String[] check = Read.file(Constants.SETTINGS_DIR + names[1] + Constants.TXT);
      boolean refresh = 0 == check.length || !Util.strbol(check[0]);

      if(refresh && ALARM_SERVICE_START.equals(state))
      {
         return;
      }

      /* Load the ManageFeedsRefresh time from settings. */
      String[] settings = Read.file(Constants.SETTINGS_DIR + names[2] + Constants.TXT);
      if(0 != settings.length)
      {
         time = Util.stoi(settings[0]);
      }

      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = Util.getServiceIntent(0);
      PendingIntent pintent = PendingIntent.getService(con, 0, intent, 0);
      String alarm = ALARM_SERVICE;
      AlarmManager am = (AlarmManager) con.getSystemService(alarm);

      /* Depending on the state string, start or stop the service. */
      if(ALARM_SERVICE_START.equals(state))
      {
         long interval = time * 60000L;
         long next = System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pintent);
      }
      else if(ALARM_SERVICE_STOP.equals(state))
      {
         am.cancel(pintent);
      }
   }

   @Override
   public
   void onBackPressed()
   {
      super.onBackPressed();
      String feeds = NavDrawer.NAV_TITLES[0];

      getSupportActionBar().setTitle(feeds);
      int lock = DrawerLayout.LOCK_MODE_UNLOCKED;
      NavDrawer.s_drawerLayout.setDrawerLockMode(lock);
      NavDrawer.s_drawerToggle.setDrawerIndicatorEnabled(true);
   }

   @Override
   protected
   void onPostCreate(Bundle savedInstanceState)
   {
      super.onPostCreate(savedInstanceState);
      // Sync the toggle state after onRestoreInstanceState has occurred.
      NavDrawer.s_drawerToggle.syncState();
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      /* If the user has clicked the title and the title says Constants.OFFLINE. */
      if(Constants.OFFLINE.equals(getSupportActionBar().getTitle().toString()))
      {
         onBackPressed();
         return true;
      }

      return NavDrawer.s_drawerToggle.onOptionsItemSelected(item) ||
            super.onOptionsItemSelected(item);
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();

      /* Stop the alarm service and reset the time to 0. */
      setServiceIntent(ALARM_SERVICE_STOP);
   }
}
