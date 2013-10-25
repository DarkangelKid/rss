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
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;

public
class FeedsActivity extends ActionBarActivity
{
   static final         String READ_ITEMS          = "read_items" + ".txt";
   static final         String SETTINGS_DIR        = "settings" + File.separatorChar;
   static final         String FILTER_LIST         = "filter_list.txt";
   private static final int    ALARM_SERVICE_START = 1;
   private static final int    ALARM_SERVICE_STOP  = 0;
   private static final long   MINUTE_VALUE        = 60000L;
   static Handler s_serviceHandler;
   private static String s_applicationFolder = "";
   private Menu                  m_optionsMenu;
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
      File file = new File(Write.LOG_FILE);
      file.delete();

      /* Make the settings directory. */
      String applicationFolder = getApplicationFolder(this);
      File folder = new File(applicationFolder + SETTINGS_DIR);
      folder.mkdirs();

      /* Create the navigation drawer and set all the listeners for it. */
      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      ListView navigationList = (ListView) findViewById(R.id.left_drawer);

      /* Set the listeners. */
      m_drawerToggle = new OnClickDrawerToggle(this, drawerLayout);

      String[] navigationTitles = resources.getStringArray(R.array.nav_titles);
      LayoutInflater layoutInflater = getLayoutInflater();
      ListAdapter adapterNavDrawer = new AdapterNavDrawer(navigationTitles, /* TODO DP */ 24,
            layoutInflater);

      drawerLayout.setDrawerListener(m_drawerToggle);
      navigationList.setAdapter(adapterNavDrawer);

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      if(null == savedInstanceState)
      {
         Fragment feedFragment = FragmentFeeds.newInstance();
         Fragment fragmentManage = FragmentManage.newInstance();
         Fragment fragmentSettings = FragmentSettings.newInstance();

         AdapterView.OnItemClickListener onClickNavDrawerItem = new OnClickNavDrawerItem(this);
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

   static
   String getApplicationFolder(Context context)
   {
      /* Check the media state for the desirable state. */
      String state = Environment.getExternalStorageState();

      String mounted = Environment.MEDIA_MOUNTED;
      if(!mounted.equals(state))
      {
         return null;
      }

      File externalFilesDir = context.getExternalFilesDir(null);
      return externalFilesDir.getAbsolutePath() + File.separatorChar;
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
      String applicationFolder = getApplicationFolder(this);
      Write.longSet(READ_ITEMS, AdapterTags.S_READ_ITEM_TIMES, applicationFolder);
   }

   private
   void setServiceIntent(int state)
   {
      Resources resources = getResources();
      String applicationFolder = getApplicationFolder(this);
      int time = resources.getIntArray(R.array.refresh_integers)[3];
      String[] fileNames = resources.getStringArray(R.array.settings_function_names);

      /* Load the ManageFeedsRefresh boolean value from settings. */
      String[] check = Read.file(SETTINGS_DIR + fileNames[1] + ".txt", applicationFolder);
      boolean refresh = 0 == check.length || !Boolean.parseBoolean(check[0]);

      if(refresh && ALARM_SERVICE_START == state)
      {
         return;
      }

      /* Load the ManageFeedsRefresh time from settings. */
      String[] settings = Read.file(SETTINGS_DIR + fileNames[2] + ".txt", applicationFolder);
      if(0 != settings.length)
      {

         time = null == settings[0] || 0 == settings[0].length()
               ? 0
               : Integer.parseInt(settings[0]);
      }

      String[] settingNames = resources.getStringArray(R.array.settings_function_names);
      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = new Intent(this, ServiceUpdate.class);
      intent = configureServiceIntent(intent, 0, settingNames, applicationFolder);

      PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

      /* Depending on the state string, start or stop the service. */
      if(ALARM_SERVICE_START == state)
      {
         long interval = time * MINUTE_VALUE;
         long next = System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pendingIntent);
      }
      else if(ALARM_SERVICE_STOP == state)
      {
         am.cancel(pendingIntent);
      }
   }

   private static
   Intent configureServiceIntent(Intent intent, int page, String[] settingNames,
         String applicationFolder)
   {
      /* Load notification boolean. */
      String settingFileName = SETTINGS_DIR + settingNames[3] + ".txt";
      String[] check = Read.file(settingFileName, applicationFolder);

      boolean notificationsEnabled = 0 != check.length && Boolean.parseBoolean(check[0]);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notificationsEnabled);
      return intent;
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

   String getPreviousNavigationTitle()
   {
      return m_previousTitle;
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

      ListView navigationList = (ListView) findViewById(R.id.left_drawer);
      AdapterNavDrawer adapterNavDrawer = (AdapterNavDrawer) navigationList.getAdapter();
      ActionBar actionBar = getSupportActionBar();
      String applicationFolder = getApplicationFolder(this);

      AsyncRefreshNavigationAdapter.newInstance(adapterNavDrawer, actionBar, applicationFolder, 0);
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
      setRefreshingIcon(serviceRunning, menu);
      return true;
   }

   /* Changes the ManageFeedsRefresh menu item to an animation if m_mode = true. */
   static
   void setRefreshingIcon(boolean mode, Menu menu)
   {
      /* Find the menu item by ID called ManageFeedsRefresh. */
      MenuItem item = menu.findItem(R.id.refresh);
      if(null == item)
      {
         return;
      }

      /* Change it depending on the m_mode. */
      if(mode)
      {
         MenuItemCompat.setActionView(item, R.layout.progress_circle);
      }
      else
      {
         MenuItemCompat.setActionView(item, null);
      }
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
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
         String applicationFolder = getApplicationFolder(this);
         LayoutInflater layoutInflater = getLayoutInflater();
         FragmentManageFeeds.showEditDialog(-1, applicationFolder, layoutInflater, this);
      }
      else if(menuText.equals(jumpTo))
      {
         FragmentManager fragmentManager = getSupportFragmentManager();
         ViewPager viewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);
         int currentPage = viewPager.getCurrentItem();

         String fragmentTag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + currentPage;

         ListFragment listFragment = (ListFragment) fragmentManager.findFragmentByTag(fragmentTag);
         Adapter adapterTags = listFragment.getListAdapter();
         ListView listViewTags = listFragment.getListView();

         gotoLatestUnread(adapterTags, listViewTags);
      }
      else if(menuText.equals(refresh))
      {
         refreshFeeds(this);
      }
      else
      {
         return m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
      }

      return true;
   }

   static
   void gotoLatestUnread(Adapter cardAdapter, ListView listView)
   {
      int itemCount = cardAdapter.getCount() - 1;
      for(int i = itemCount; 0 <= i; i--)
      {
         FeedItem feedItem = (FeedItem) cardAdapter.getItem(i);
         if(!AdapterTags.S_READ_ITEM_TIMES.contains(feedItem.m_itemTime))
         {
            listView.setSelection(i);
            break;
         }
      }

      if(!listView.isShown() || 0 == itemCount)
      {
         Animation animation = new AlphaAnimation(0.0F, 1.0F);
         animation.setDuration(240);
         listView.setAnimation(animation);
         listView.setVisibility(View.VISIBLE);
      }
   }

   String getNavigationTitle()
   {
      ActionBar actionBar = getSupportActionBar();
      CharSequence title = actionBar.getTitle();
      return title.toString();
   }

   /* Updates and refreshes the tags with any new content. */
   private
   void refreshFeeds(ActionBarActivity activity)
   {
      setRefreshingIcon(true, m_optionsMenu);

      /* Set the service handler in FeedsActivity so we can check and call it from ServiceUpdate. */

      String applicationFolder = getApplicationFolder(this);
      FragmentManager fragmentManager = getSupportFragmentManager();

      s_serviceHandler = new ServiceHandler(fragmentManager, m_optionsMenu, applicationFolder,
      /* TODO */ 24);

      ViewPager viewPager = (ViewPager) findViewById(FragmentFeeds.VIEW_PAGER_ID);
      int currentPage = viewPager.getCurrentItem();

      Resources resources = getResources();
      String[] settingNames = resources.getStringArray(R.array.settings_function_names);

      Intent intent = new Intent(this, ServiceUpdate.class);
      intent = configureServiceIntent(intent, currentPage, settingNames, applicationFolder);
      activity.startService(intent);
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
