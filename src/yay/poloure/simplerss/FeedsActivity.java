package yay.poloure.simplerss;

import android.content.Context;
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

class FeedsActivity extends ActionBarActivity
{
   /* Generally unsafe. */
   static Menu            optionsMenu;
   static ViewPager       viewpager;
   static Context         con;
   static FragmentManager fman;
   static ActionBar       bar;
   static Handler         service_handler;

   /* These must be set straight away in onCreate. */
   /* UI related strings. */
   static String   storage;
   static String   all;
   static String[] ctags;

   static Fragment[] main_fragments;

   static final String SEPAR = System.getProperty("file.separator");
   static final String NL    = System.getProperty("line.separator");

   /* Appends */
   static final String TXT   = ".txt";
   static final String TEMP  = ".temp" + TXT;
   static final String COUNT = ".count" + TXT;
   static final String STORE = ".store" + TXT;

   /* Folders */
   static final String GROUPS_DIR    = "tags" + SEPAR;
   static final String THUMBNAIL_DIR = "thumbnails" + SEPAR;
   static final String IMAGE_DIR     = "images" + SEPAR;
   static final String SETTINGS      = "settings" + SEPAR;

   /* Files */
   static final String INT_STORAGE = "internal" + TXT;
   static final String STRIP_COLOR = "pagertabstrip_colour" + TXT;
   static final String DUMP_FILE   = "dump" + TXT;
   static final String INDEX       = "index" + TXT;
   static final String CONTENT     = "content" + TXT;
   static final String URL         = "urls" + TXT;
   static final String GROUP_LIST  = "tag_list" + TXT;
   static final String FILTER_LIST = "filter_list" + TXT;
   static final String READ_ITEMS  = "read_items" + TXT;

   private static final int     VER           = VERSION.SDK_INT;
   static final         boolean FROYO         = VERSION_CODES.FROYO <= VER;
   static final         boolean HONEYCOMB     = VERSION_CODES.HONEYCOMB <= VER;
   static final         boolean HONEYCOMB_MR2 = VERSION_CODES.HONEYCOMB_MR2 <= VER;
   static final         boolean JELLYBEAN     = VERSION_CODES.JELLY_BEAN <= VER;

   static final PagerTabStrip[] PAGER_TAB_STRIPS = new PagerTabStrip[3];

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      /* Save the other satic variables. */
      fman = getSupportFragmentManager();
      bar = getSupportActionBar();
      bar.setDisplayHomeAsUpEnabled(true);
      bar.setHomeButtonEnabled(true);
      con = this;

      /* Form the storage path. */
      storage = Util.getStorage();
      ctags = Read.file(GROUP_LIST);

      Util.remove(DUMP_FILE);

      /* Load String resources into static variables. */
      all = getString(R.string.all_tag);

      /* Create the navigation drawer and set all the listeners for it. */
      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      ListView navDrawerListView = (ListView) findViewById(R.id.left_drawer);
      new NavDrawer(drawerLayout, navDrawerListView);

      /* Create the MANAGE_FRAGMENTS that go inside the content frame. */
      if(null == savedInstanceState)
      {
         main_fragments = new Fragment[]{
               new FragmentFeeds(), new FragmentManage(), new FragmentSettings(),
         };

         int frame = R.id.content_frame;

         FragmentTransaction tran = fman.beginTransaction();
         tran.add(frame, main_fragments[0]).add(frame, main_fragments[1])
             .add(frame, main_fragments[2]).hide(main_fragments[1]).hide(main_fragments[2])
             .commit();
      }

      Util.updateTags();
      Update.page(0);
   }

   @Override
   protected void onPostCreate(Bundle savedInstanceState)
   {
      super.onPostCreate(savedInstanceState);
      // Sync the toggle state after onRestoreInstanceState has occurred.
      NavDrawer.drawer_toggle.syncState();
   }


   @Override
   public void onBackPressed()
   {
      super.onBackPressed();
      String feeds = NavDrawer.NAV_TITLES[0];

      bar.setTitle(feeds);
      int lock = DrawerLayout.LOCK_MODE_UNLOCKED;
      NavDrawer.drawer_layout.setDrawerLockMode(lock);
      NavDrawer.drawer_toggle.setDrawerIndicatorEnabled(true);
   }

   /* This is so the icon and text in the actionbar are selected. */
   @Override
   public void onConfigurationChanged(Configuration config)
   {
      super.onConfigurationChanged(config);
      NavDrawer.drawer_toggle.onConfigurationChanged(config);
   }

   @Override
   protected void onStop()
   {
      super.onStop();
      /* Set the alarm service to go of starting now. */
      Util.setServiceIntent("start");

      /* Save the read_items to file. */
      Write.collection(READ_ITEMS, AdapterCard.read_items);
   }

   @Override
   protected void onStart()
   {
      super.onStart();

      /* Stop the alarmservice and reset the time to 0. */
      Util.setServiceIntent("stop");
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      /* If the user has clicked the m_title and the tilte says "Offline". */
      if("Offline".equals(bar.getTitle().toString()))
      {
         onBackPressed();
         return true;
      }

      return NavDrawer.drawer_toggle.onOptionsItemSelected(item) ||
             super.onOptionsItemSelected(item);
   }
}
