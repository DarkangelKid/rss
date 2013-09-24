package yay.poloure.simplerss;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public
class FeedsActivity extends ActionBarActivity
{
   private static final String ALARM_SERVICE_START = "start";
   private static final String ALARM_SERVICE_STOP  = "stop";

   /* Only initialized when the activity is running. */
   static Menu            s_optionsMenu;
   static ViewPager       s_ViewPager;
   static FragmentManager s_fragmentManager;
   static ActionBar       s_actionBar;
   static Handler         s_serviceHandler;
   static String[]        s_currentTags;

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      /* Save the other static variables. */
      Util.setContext(this);
      s_fragmentManager = getSupportFragmentManager();
      s_actionBar = getSupportActionBar();
      s_actionBar.setDisplayHomeAsUpEnabled(true);
      s_actionBar.setHomeButtonEnabled(true);

      s_currentTags = Read.file(Constants.TAG_LIST);
      Util.remove(Constants.DUMP_FILE);

      /* Create the navigation drawer and set all the listeners for it. */
      new NavDrawer((ListView) findViewById(R.id.left_drawer),
            (DrawerLayout) findViewById(R.id.drawer_layout));

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      if(null == savedInstanceState)
      {
         Fragment[] mainFragments = new Fragment[]{
               new FragmentFeeds(), new FragmentManage(), new FragmentSettings(),
         };

         int frame = R.id.content_frame;

         FragmentTransaction tran = s_fragmentManager.beginTransaction();
         tran.add(frame, mainFragments[0], NavDrawer.NAV_TITLES[0])
               .add(frame, mainFragments[1], NavDrawer.NAV_TITLES[1])
               .add(frame, mainFragments[2], NavDrawer.NAV_TITLES[2])
               .hide(mainFragments[1])
               .hide(mainFragments[2])
               .commit();
      }

      Util.updateTags();
      Update.page(0);
   }

   static
   Fragment getFragmentByTag(String fragmentTag)
   {
      return s_fragmentManager.findFragmentByTag(fragmentTag);
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
      Write.collection(Constants.READ_ITEMS, Util.SetHolder.READ_ITEMS);
   }

   @Override
   public
   void onBackPressed()
   {
      super.onBackPressed();
      String feeds = NavDrawer.NAV_TITLES[0];

      s_actionBar.setTitle(feeds);
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
      /* If the user has clicked the m_title and the tilte says Constants.OFFLINE. */
      if(Constants.OFFLINE.equals(s_actionBar.getTitle().toString()))
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

      /* Stop the alarmservice and reset the time to 0. */
      setServiceIntent(ALARM_SERVICE_STOP);
   }

   private static
   void setServiceIntent(String state)
   {
      Context con = Util.getContext();
      int time = AdapterSettingsFunctions.TIMES[3];
      String[] names = AdapterSettingsFunctions.FILE_NAMES;

      /* Load the ManageRefresh boolean value from settings. */
      String[] check = Read.file(Constants.SETTINGS_DIR + names[1] + Constants.TXT);
      boolean refresh = 0 == check.length || !Util.strbol(check[0]);

      if(refresh && ALARM_SERVICE_START.equals(state))
      {
         return;
      }

      /* Load the ManageRefresh time from settings. */
      String[] settings = Read.file(Constants.SETTINGS_DIR + names[2] + Constants.TXT);
      if(0 != settings.length)
      {
         time = Util.stoi(settings[0]);
      }

      /* Create intent, turn into pending intent, and get the alarmmanager. */
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
}
