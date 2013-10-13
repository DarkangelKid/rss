package com.poloure.simplerss;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public
class FeedsActivity extends ActionBarActivity
{
   static  Handler               s_serviceHandler;
   /* Only initialized when the activity is running. */
   private Menu                  m_optionsMenu;
   private DrawerLayout          m_drawerLayout;
   private ActionBarDrawerToggle m_drawerToggle;
   private String                m_previousTitle;
   private BaseAdapter           m_navigationDrawer;

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      ActionBar actionBar = getSupportActionBar();
      actionBar.setIcon(R.drawable.rss_icon);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);

      Util.remove(Constants.DUMP_FILE, this);

      Util.mkdir(Constants.SETTINGS_DIR, this);

      /* Create the navigation drawer and set all the listeners for it. */
      m_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      ListView navigationList = (ListView) findViewById(R.id.left_drawer);

      /* Set the listeners (and save the navigation list to the public static variable). */
      AdapterView.OnItemClickListener onClickNavDrawerItem = new OnClickNavDrawerItem(
            m_drawerLayout, this);
      m_drawerToggle = new OnClickDrawerToggle(this, m_drawerLayout);
      m_navigationDrawer = new AdapterNavDrawer(this);

      m_drawerLayout.setDrawerListener(m_drawerToggle);
      navigationList.setOnItemClickListener(onClickNavDrawerItem);
      navigationList.setAdapter(m_navigationDrawer);

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      if(null == savedInstanceState)
      {
         Fragment[] mainFragments = {
               new FragmentFeeds(m_navigationDrawer),
               new FragmentManage(m_navigationDrawer),
               new FragmentSettings(),
         };

         int frame = R.id.content_frame;

         Resources resources = getResources();
         String[] navTitles = resources.getStringArray(R.array.nav_titles);
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction transaction = fragmentManager.beginTransaction();
         transaction.add(frame, mainFragments[0], navTitles[0]);
         transaction.add(frame, mainFragments[1], navTitles[1]);
         transaction.add(frame, mainFragments[2], navTitles[2]);
         transaction.hide(mainFragments[1]);
         transaction.hide(mainFragments[2]);
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
      setServiceIntent(Constants.ALARM_SERVICE_START);

      /* Save the READ_ITEMS to file. */
      Write.collection(Constants.READ_ITEMS, AdapterTag.s_readLinks, this);
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

      m_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
      boolean refresh = 0 == check.length || !Util.strbol(check[0]);

      if(refresh && Constants.ALARM_SERVICE_START.equals(state))
      {
         return;
      }

      /* Load the ManageFeedsRefresh time from settings. */
      String[] settings = Read.file(Constants.SETTINGS_DIR + fileNames[2] + Constants.TXT, this);
      if(0 != settings.length)
      {
         time = Util.stoi(settings[0]);
      }

      /* Create intent, turn into pending intent, and get the alarm manager. */
      Intent intent = Util.getServiceIntent(0, fileNames[3], this);
      PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

      /* Depending on the state string, start or stop the service. */
      if(Constants.ALARM_SERVICE_START.equals(state))
      {
         long interval = time * 60000L;
         long next = System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pendingIntent);
      }
      else if(Constants.ALARM_SERVICE_STOP.equals(state))
      {
         am.cancel(pendingIntent);
      }
   }

   Menu getOptionsMenu()
   {
      return m_optionsMenu;
   }

   void setOptionsMenu(Menu menu)
   {
      m_optionsMenu = menu;
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
      Util.updateTags(m_navigationDrawer, this);
      /*int currentPage = FragmentFeeds.s_viewPager.getCurrentItem();
      Update.page(m_navigationDrawer, currentPage);*/
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();

      /* Stop the alarm service and reset the time to 0. */
      setServiceIntent(Constants.ALARM_SERVICE_STOP);
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
   boolean onOptionsItemSelected(MenuItem item)
   {
      /* If the user has clicked the title and the title says Constants.OFFLINE. */
      String barString = getNavigationTitle();

      if(getString(R.string.offline).equals(barString))
      {
         onBackPressed();
         return true;
      }

      return m_drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
   }

   String getNavigationTitle()
   {
      ActionBar actionBar = getSupportActionBar();
      CharSequence title = actionBar.getTitle();
      return title.toString();
   }

   void setNavigationTitle(String title)
   {
      m_previousTitle = title;
      ActionBar actionBar = getSupportActionBar();
      actionBar.setTitle(title);
   }
}
