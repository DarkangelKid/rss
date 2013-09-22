package yay.poloure.simplerss;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.Locale;

public
class FeedsActivity extends ActionBarActivity
{
   static final String          SEPAR            = System.getProperty("file.separator");
   static final String          NL               = System.getProperty("line.separator");
   static final Locale          locale           = Locale.getDefault();
   /* Formats */
   static final String          INDEX_FORMAT     = "feed|%s|url|%s|tag|%s|";
   static final String          FRAGMENT_TAG     = "android:switcher:%s:%d";
   /* Appends */
   static final String          TXT              = ".txt";
   static final String          TEMP             = ".temp" + TXT;
   static final String          COUNT            = ".count" + TXT;
   static final String          STORE            = ".store" + TXT;
   /* Parser saves */
   static final String          IMAGE            = "image|";
   static final String          HEIGHT           = "height|";
   static final String          WIDTH            = "width|";
   static final String          TAG_TITLE        = "<title";
   static final String          ENDTAG_TITLE     = "</title>";
   /* Folders */
   static final String          GROUPS_DIR       = "tags" + SEPAR;
   static final String          THUMBNAIL_DIR    = "thumbnails" + SEPAR;
   static final String          IMAGE_DIR        = "images" + SEPAR;
   static final String          SETTINGS         = "settings" + SEPAR;
   /* Files */
   static final String          INT_STORAGE      = "internal" + TXT;
   static final String          STRIP_COLOR      = "pagertabstrip_colour" + TXT;
   static final String          DUMP_FILE        = "dump" + TXT;
   static final String          INDEX            = "index" + TXT;
   static final String          CONTENT          = "content" + TXT;
   static final String          URL              = "urls" + TXT;
   static final String          GROUP_LIST       = "tag_list" + TXT;
   static final String          FILTER_LIST      = "filter_list" + TXT;
   static final String          READ_ITEMS       = "read_items" + TXT;
   /* Else */
   static final int             VER              = VERSION.SDK_INT;
   static final String          IMAGE_TYPE       = "image" + SEPAR;
   static final boolean         FROYO            = VERSION_CODES.FROYO <= VER;
   static final boolean         HONEYCOMB        = VERSION_CODES.HONEYCOMB <= VER;
   static final boolean         HONEYCOMB_MR2    = VERSION_CODES.HONEYCOMB_MR2 <= VER;
   static final boolean         JELLYBEAN        = VERSION_CODES.JELLY_BEAN <= VER;
   static final PagerTabStrip[] PAGER_TAB_STRIPS = new PagerTabStrip[3];
   /* Generally unsafe. */
   static Menu            optionsMenu;
   static ViewPager       viewpager;
   static Context         con;
   static FragmentManager fman;
   static ActionBar       bar;
   static Handler         service_handler;
   /* These must be set straight away in onCreate. */
   /* UI related strings. */
   static String          all;
   static String[]        ctags;
   static Fragment[]      main_fragments;

   static
   void setServiceIntent(String state)
   {
      Context con = Util.getContext();
      int time = AdapterSettingsFunctions.TIMES[3];
      String[] names = AdapterSettingsFunctions.FILE_NAMES;

      /* Load the ManageRefresh boolean value from settings. */
      String[] check = Read.file(SETTINGS + names[1] + TXT);
      boolean refresh = 0 == check.length || !Util.strbol(check[0]);

      if(refresh && "start".equals(state))
      {
         return;
      }

      /* Load the ManageRefresh time from settings. */
      String[] settings = Read.file(SETTINGS + names[2] + TXT);
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
      if("start".equals(state))
      {
         long interval = time * 60000L;
         long next = System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pintent);
      }
      else if("stop".equals(state))
      {
         am.cancel(pintent);
      }
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      /* Save the other satic variables. */
      fman = getSupportFragmentManager();
      bar = getSupportActionBar();
      bar.setDisplayHomeAsUpEnabled(true);
      bar.setHomeButtonEnabled(true);
      con = this;

      ctags = Read.file(GROUP_LIST);
      Util.remove(DUMP_FILE);

      /* Load String resources into static variables. */
      all = getString(R.string.all_tag);

      /* Create the navigation drawer and set all the listeners for it. */
      new NavDrawer((ListView) findViewById(R.id.left_drawer),
            (DrawerLayout) findViewById(R.id.drawer_layout));

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      if(null == savedInstanceState)
      {
         main_fragments = new Fragment[]{
               new FragmentFeeds(), new FragmentManage(), new FragmentSettings(),
         };

         int frame = R.id.content_frame;

         FragmentTransaction tran = fman.beginTransaction();
         tran.add(frame, main_fragments[0])
               .add(frame, main_fragments[1])
               .add(frame, main_fragments[2])
               .hide(main_fragments[1])
               .hide(main_fragments[2])
               .commit();
      }

      Util.updateTags();
      Update.page(0);
   }

   /* This is so the icon and text in the actionbar are selected. */
   @Override
   public
   void onConfigurationChanged(Configuration config)
   {
      super.onConfigurationChanged(config);
      NavDrawer.DRAWER_TOGGLE.onConfigurationChanged(config);
   }

   @Override
   protected
   void onStop()
   {
      super.onStop();
      /* Set the alarm service to go of starting now. */
      setServiceIntent("start");

      /* Save the read_items to file. */
      Write.collection(READ_ITEMS, Util.SetHolder.read_items);
   }

   @Override
   public
   void onBackPressed()
   {
      super.onBackPressed();
      String feeds = NavDrawer.NAV_TITLES[0];

      bar.setTitle(feeds);
      int lock = DrawerLayout.LOCK_MODE_UNLOCKED;
      NavDrawer.s_drawerLayout.setDrawerLockMode(lock);
      NavDrawer.DRAWER_TOGGLE.setDrawerIndicatorEnabled(true);
   }

   @Override
   protected
   void onPostCreate(Bundle savedInstanceState)
   {
      super.onPostCreate(savedInstanceState);
      // Sync the toggle state after onRestoreInstanceState has occurred.
      NavDrawer.DRAWER_TOGGLE.syncState();
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      /* If the user has clicked the m_title and the tilte says "Offline". */
      if("Offline".equals(bar.getTitle().toString()))
      {
         onBackPressed();
         return true;
      }

      return NavDrawer.DRAWER_TOGGLE.onOptionsItemSelected(item) ||
            super.onOptionsItemSelected(item);
   }

   @Override
   protected
   void onStart()
   {
      super.onStart();

      /* Stop the alarmservice and reset the time to 0. */
      setServiceIntent("stop");
   }
}
