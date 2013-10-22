package com.poloure.simplerss;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;

public
class FeedsActivity extends ActionBarActivity
{
   private static final String ALARM_SERVICE_START = "start";
   private static final String ALARM_SERVICE_STOP  = "stop";
   private static final long   MINUTE_VALUE        = 60000L;
   static Handler s_serviceHandler;
   /* Only initialized when the activity is running. */ private Menu m_optionsMenu;
   private ActionBarDrawerToggle m_drawerToggle;
   private String                m_previousTitle;

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      /* Get the navigation drawer titles. */
      Resources resources = getResources();
      String[] navTitles = resources.getStringArray(R.array.nav_titles);

      /* Configure the ActionBar. */
      ActionBar actionBar = getSupportActionBar();
      actionBar.setIcon(R.drawable.rss_icon);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setTitle(navTitles[0]);

      /* Delete the log file. */
      Util.remove(Constants.LOG_FILE, this);

      /* Make the settings directory. */
      String storage = Util.getStorage(this);
      File folder = new File(storage + Constants.SETTINGS_DIR);
      folder.mkdirs();

      /* Create the navigation drawer and set all the listeners for it. */
      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      ListView navigationList = (ListView) findViewById(R.id.left_drawer);

      /* Set the listeners. */
      m_drawerToggle = new OnClickDrawerToggle(this, drawerLayout);
      ListAdapter adapterNavDrawer = new AdapterNavDrawer(this);

      drawerLayout.setDrawerListener(m_drawerToggle);
      navigationList.setAdapter(adapterNavDrawer);

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      if(null == savedInstanceState)
      {
         Fragment feedFragment = FragmentFeeds.newInstance();
         Fragment fragmentManage = FragmentManage.newInstance();
         Fragment fragmentSettings = FragmentSettings.newInstance();

         AdapterView.OnItemClickListener onClickNavDrawerItem = new OnClickNavDrawerItem(
               drawerLayout, this);
         navigationList.setOnItemClickListener(onClickNavDrawerItem);

         int frame = R.id.content_frame;

         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.add(frame, feedFragment, navTitles[0]);
         transaction.add(frame, fragmentManage, navTitles[1]);
         transaction.add(frame, fragmentSettings, navTitles[2]);
         transaction.hide(fragmentManage);
         transaction.hide(fragmentSettings);
         transaction.commit();
      }
   }

   /* This is so the icon and text in the actionbar are selected. */
   @Override
   public
   void onConfigurationChanged(Configuration newConfig)
   {
      super.onConfigurationChanged(newConfig);
      m_drawerToggle.onConfigurationChanged(newConfig);
   }

   @Override
   protected
   void onStop()
   {
      super.onStop();
      /* Set the alarm service to go of starting now. */
      setServiceIntent(ALARM_SERVICE_START);

      /* Save the READ_ITEMS to file. */
      Write.longSet(Constants.READ_ITEMS, AdapterTags.S_READ_ITEM_TIMES, this);
   }

   @Override
   public
   void onBackPressed()
   {
      super.onBackPressed();

      Resources resources = getResources();

      String feeds = resources.getStringArray(R.array.nav_titles)[0];
      ActionBar actionBar = getSupportActionBar();
      actionBar.setTitle(feeds);

      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
      m_drawerToggle.setDrawerIndicatorEnabled(true);
   }

   private
   void setServiceIntent(String state)
   {
      Resources resources = getResources();
      int time = resources.getIntArray(R.array.refresh_integers)[3];
      String[] fileNames = resources.getStringArray(R.array.settings_names);

      /* Load the ManageFeedsRefresh boolean value from settings. */
      String[] check = Read.file(Constants.SETTINGS_DIR + fileNames[1] + Constants.TXT, this);
      boolean refresh = 0 == check.length || !Boolean.parseBoolean(check[0]);

      if(refresh && ALARM_SERVICE_START.equals(state))
      {
         return;
      }

      /* Load the ManageFeedsRefresh time from settings. */
      String[] settings = Read.file(Constants.SETTINGS_DIR + fileNames[2] + Constants.TXT, this);
      if(0 != settings.length)
      {

         time = null == settings[0] || 0 == settings[0].length()
               ? 0
               : Integer.parseInt(settings[0]);
      }

      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = getServiceIntent(0, this);
      PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

      /* Depending on the state string, start or stop the service. */
      if(ALARM_SERVICE_START.equals(state))
      {
         long interval = time * MINUTE_VALUE;
         long next = System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pendingIntent);
      }
      else if(ALARM_SERVICE_STOP.equals(state))
      {
         am.cancel(pendingIntent);
      }
   }

   private static
   Intent getServiceIntent(int page, Context context)
   {
      Resources resources = context.getResources();
      String notificationName = resources.getStringArray(R.array.settings_names)[3];

      /* Load notification boolean. */
      String path = Constants.SETTINGS_DIR + notificationName + Constants.TXT;
      String[] check = Read.file(path, context);

      boolean notificationsEnabled = 0 != check.length && Boolean.parseBoolean(check[0]);
      Intent intent = new Intent(context, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notificationsEnabled);
      return intent;
   }

   String getPreviousNavigationTitle()
   {
      return m_previousTitle;
   }

   @Override
   protected
   void onResume()
   {
      super.onResume();
      Util.updateTags(this);
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();

      /* Stop the alarm service and reset the time to 0. */
      setServiceIntent(ALARM_SERVICE_STOP);
   }

   @Override
   protected
   void onPostCreate(Bundle savedInstanceState)
   {
      super.onPostCreate(savedInstanceState);

      // Sync the toggle state after onRestoreInstanceState has occurred.
      m_drawerToggle.syncState();
   }

   @Override
   public
   boolean onCreateOptionsMenu(Menu menu)
   {
      menu.clear();

      MenuInflater menuInflater = getMenuInflater();
      menuInflater.inflate(R.menu.activity, menu);

      m_optionsMenu = menu;

      boolean serviceRunning = isServiceRunning(this);
      Util.setRefreshingIcon(serviceRunning, menu);
      return true;
   }

   private static
   boolean isServiceRunning(Activity activity)
   {
      ActivityManager manager = (ActivityManager) activity.getSystemService(ACTIVITY_SERVICE);
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

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      ListView navigationList = (ListView) findViewById(R.id.left_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      /* If the user has clicked the title and the title says R.string.offline. */
      String navigationTitle = getNavigationTitle();
      CharSequence menuText = item.getTitle();

      String offline = getString(R.string.offline);
      String addFeed = getString(R.string.add_feed);
      String jumpTo = getString(R.string.unread);
      String refresh = getString(R.string.refresh);

      if(offline.equals(navigationTitle))
      {
         onBackPressed();
      }
      else if(menuText.equals(addFeed))
      {
         FragmentManageFeeds.showEditDialog(navigationAdapter, this, FragmentManageFeeds.MODE_ADD);
      }
      else if(menuText.equals(jumpTo))
      {
         FragmentManager fragmentManager = getSupportFragmentManager();
         ViewPager viewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);
         int currentPage = viewPager.getCurrentItem();
         Util.gotoLatestUnread(fragmentManager, currentPage);
      }
      else if(menuText.equals(refresh))
      {
         refreshFeeds(this, navigationAdapter);
      }
      else
      {
         return m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
      }

      return true;
   }

   /* Updates and refreshes the tags with any new content. */
   private
   void refreshFeeds(ActionBarActivity activity, BaseAdapter navigationAdapter)
   {
      Util.setRefreshingIcon(true, m_optionsMenu);

      /* Set the service handler in FeedsActivity so we can check and call it
       * from ServiceUpdate. */
      s_serviceHandler = new OnFinishServiceHandler(activity, navigationAdapter, m_optionsMenu);

      ViewPager viewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);
      int currentPage = viewPager.getCurrentItem();

      Intent intent = getServiceIntent(currentPage, activity);
      activity.startService(intent);
   }

   String getNavigationTitle()
   {
      ActionBar actionBar = getSupportActionBar();
      CharSequence title = actionBar.getTitle();
      return title.toString();
   }

   void setNavigationTitle(CharSequence title, boolean saveTitle)
   {
      if(saveTitle)
      {
         m_previousTitle = getNavigationTitle();
      }
      ActionBar actionBar = getSupportActionBar();
      actionBar.setTitle(title);
   }
}
