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
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class main extends ActionBarActivity
{
   /* Generally unsafe. */
   static Menu                 optionsMenu;
   static ViewPager            viewpager;
   static Context              con;
   static FragmentManager      fman;
   static ActionBar            action_bar;
   static Handler              service_handler;

   /* These must be set straight away in onCreate. */
   /* UI related strings. */
   static String storage, ALL;
   static String[] cgroups;

   static Fragment[] main_fragments;

   /* Static final are best only when type is a primitive or a String.
    * Static is also better than not static when primitive or String.
    * Final is only desirable when complicated logic is in place or it
    * must be accessed within an inner class.
    *
    * Nothing should be public, replace with nothing. Nothing is
    * package-private which is basically public. Public is avialable ouside
    * the package but all our classes are inside the same package.
    *
    * Unless you really do not want another class accessing a variable,
    * there is no need to make it private, it makes no difference after
    * compiling.
    *
    * Overriden functions from android have public because they are in an
    * android package and we want to access them.
    *
    * A final method means it can not be overriden/overided but again,
    * since we are not coding a library, there is no need.
    *
    * If an field is not initilised when static finaled, it can cause a
    * NullPointerException and be carefull relying on it being not null. */

   static final String SEPAR = System.getProperty("file.separator");
   static final String NL    = System.getProperty("line.separator");

   /* File related names. */
   static final String TXT           = ".txt";
   static final String GROUPS_DIR    = "groups" + SEPAR;
   static final String THUMBNAIL_DIR = "thumbnails" + SEPAR;
   static final String IMAGE_DIR     = "images" + SEPAR;
   static final String SETTINGS      = "settings" + SEPAR;
   static final String INT_STORAGE   = "internal" + TXT;
   static final String STRIP_COLOR   = "pagertabstrip_colour" + TXT;
   static final String DUMP_FILE     = "dump" + TXT;
   static final String TEMP          = ".temp" + TXT;
   static final String STORE         = ".store" + TXT;
   static final String CONTENT       = ".content" + TXT;
   static final String URL           = ".urls" + TXT;
   static final String COUNT         = ".count" + TXT;
   static final String GROUP_LIST    = "group_list" + TXT;
   static final String FILTER_LIST   = "filter_list" + TXT;
   static final String READ_ITEMS    = "read_items" + TXT;

   static final int v                 = VERSION.SDK_INT;
   static final boolean FROYO         = v >= VERSION_CODES.FROYO;
   static final boolean HONEYCOMB     = v >= VERSION_CODES.HONEYCOMB;
   static final boolean HONEYCOMB_MR2 = v >= VERSION_CODES.HONEYCOMB_MR2;
   static final boolean JELLYBEAN     = v >= VERSION_CODES.JELLY_BEAN;

   static final PagerTabStrip[] strips = new PagerTabStrip[3];

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      /* Save the other satic variables. */
      fman    = getSupportFragmentManager();
      con     = this;

      /* Form the storage path. */
      storage = util.get_storage();
      cgroups = read.file(storage + GROUP_LIST);

      /* Load String resources into static variables. */
      ALL     = getString(R.string.all_group);

      /* Configure the action bar. */
      action_bar = getSupportActionBar();
      action_bar.setDisplayHomeAsUpEnabled(true);
      action_bar.setHomeButtonEnabled(true);

      /* Create the navigation drawer and set all the listeners for it. */
      DrawerLayout dl      = (DrawerLayout) findViewById(R.id.drawer_layout);
      ListView left_drawer = (ListView)     findViewById(R.id.left_drawer);
      new navigation_drawer(dl, left_drawer);

      /* Create the fragments that go inside the content frame. */
      if(savedInstanceState == null)
      {
         main_fragments = new Fragment[]
         {
            new fragment_feeds(),
            new fragment_manage(),
            new fragment_settings(),
         };

         int frame = R.id.content_frame;

         FragmentTransaction tran = main.fman.beginTransaction();
         tran.add(frame, main_fragments[0])
             .add(frame, main_fragments[1])
             .add(frame, main_fragments[2])
             .hide(main_fragments[1]).hide(main_fragments[2]).commit();
      }

      util.update_groups();
      update.page(0);
   }

   @Override
   public void onBackPressed()
   {
      super.onBackPressed();
      int    lock  = DrawerLayout.LOCK_MODE_UNLOCKED;
      String feeds = navigation_drawer.NAV_TITLES[0];

      action_bar.setTitle(feeds);
      navigation_drawer.drawer_layout.setDrawerLockMode(lock);
      navigation_drawer.drawer_toggle.setDrawerIndicatorEnabled(true);
   }

   /* This is so the icon and text in the actionbar are selected. */
   @Override
   public void onConfigurationChanged(Configuration config)
   {
      super.onConfigurationChanged(config);
      navigation_drawer.drawer_toggle.onConfigurationChanged(config);
   }

   @Override
   protected void onStop()
   {
      super.onStop();
      /* Set the alarm service to go of starting now. */
      util.set_service(this, 0, "start");

      /* Save the read_items to file. */
      write.collection(storage + READ_ITEMS, adapter_feeds_cards.read_items);
   }

   @Override
   protected void onStart()
   {
      super.onStart();

      /* Stop the alarmservice and reset the time to 0. */
      util.set_service(this, 0, "stop");
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      /* If the user has clicked the title and the tilte says "Offline". */
      if(action_bar.getTitle().toString().equals("Offline"))
      {
         onBackPressed();
         return true;
      }

      if(navigation_drawer.drawer_toggle.onOptionsItemSelected(item))
         return true;

      return super.onOptionsItemSelected(item);
   }

   /* END OF OVERRIDES */

   static class pageradapter_feeds extends FragmentPagerAdapter
   {
      public pageradapter_feeds(FragmentManager fm)
      {
         super(fm);
      }

      @Override
      public int getCount()
      {
         return cgroups.length;
      }

      @Override
      public Fragment getItem(int position)
      {
         fragment_card f = new fragment_card();
         Bundle args = new Bundle();
         args.putInt("num", position);
         f.setArguments(args);
         return f;
      }

      @Override
      public String getPageTitle(int position)
      {
         return cgroups[position];
      }
   }

   static class pageradapter_manage extends FragmentPagerAdapter
   {
      static final Fragment[] fragments = new Fragment[]
      {
         new fragment_manage_group(),
         new fragment_manage_feed(),
         new fragment_manage_filters(),
      };

      static final String[] titles = util.get_array(R.array.manage_titles);

      public pageradapter_manage(FragmentManager fm)
      {
         super(fm);
      }

      @Override
      public int getCount()
      {
         return fragments.length;
      }

      @Override
      public Fragment getItem(int position)
      {
         return fragments[position];
      }

      @Override
      public String getPageTitle(int position)
      {
         return titles[position];
      }
   }

   static class fragment_card extends ListFragment
   {
      public fragment_card()
      {
      }

      @Override
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setListAdapter(new adapter_feeds_cards(getActivity()));
      }

      @Override
      public View onCreateView(LayoutInflater in, ViewGroup container, Bundle b)
      {
         return in.inflate(R.layout.listview_feed, container, false);
      }
   }

   static class fragment_settings extends Fragment
   {
      public fragment_settings()
      {
      }

      @Override
      public View onCreateView(LayoutInflater in, ViewGroup container, Bundle b)
      {
         View v = in.inflate(R.layout.viewpager_settings, container, false);
         ViewPager vp = (ViewPager) v.findViewById(R.id.settings_pager);
         vp.setAdapter(new pageradapter_settings(fman));

         strips[2] = (PagerTabStrip) v.findViewById(R.id.settings_title_strip);
         strips[2].setDrawFullUnderline(true);
         util.set_strip_colour(strips[2]);

         return v;
      }
   }

   static class pageradapter_settings extends FragmentPagerAdapter
   {
      static final Fragment[] fragments = new Fragment[]
      {
         new fragment_settings_function(),
         new fragment_settings_interface(),
      };

      static final String[] titles = util.get_array(R.array.settings_titles);

      public pageradapter_settings(FragmentManager fm)
      {
         super(fm);
      }

      @Override
      public int getCount()
      {
         return titles.length;
      }

      @Override
      public Fragment getItem(int position)
      {
         return fragments[position];
      }

      @Override
      public String getPageTitle(int position)
      {
         return titles[position];
      }
   }

   static class fragment_settings_function extends ListFragment
   {
      public fragment_settings_function()
      {
      }

      @Override
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setListAdapter(new adapter_settings_function());
      }

      @Override
      public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
      {
         return inf.inflate(R.layout.listview_settings_function, cont, false);
      }
   }

   static class fragment_settings_interface extends ListFragment
   {
      public fragment_settings_interface()
      {
      }

      @Override
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setListAdapter(new adapter_settings_UI());
      }

      @Override
      public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
      {
         return inf.inflate(R.layout.listview_settings_function, cont, false);
      }
   }
}
