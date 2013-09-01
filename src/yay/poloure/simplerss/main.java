package yay.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;

public class main extends ActionBarActivity
{
   /* Generally unsafe. */
   static Menu                 optionsMenu;
   static ViewPager            viewpager;
   static Context              con;
   static FragmentManager      fman;
   static ActionBar            action_bar;
   static Activity             activity;
   static Handler              service_handler;
   static String[] cgroups = new String[0];

   /* These must be set straight away in onCreate. */
   static String storage, internal, ALL, DELETE_DIALOG, CLEAR_DIALOG, ALL_FILE;

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
      fman     = getSupportFragmentManager();
      con      = this;
      activity = this;

      /* Form the storage path. */
      internal = util.get_internal();
      storage  = util.get_storage();
      write.log(storage + NL);

      /* Load String resources into static variables. */
      ALL           = getString(R.string.all_group);
      DELETE_DIALOG = getString(R.string.delete_dialog);
      CLEAR_DIALOG  = getString(R.string.clear_dialog);

      ALL_FILE      = storage + GROUPS_DIR + ALL + SEPAR + ALL + TXT;

      /* Delete the log file. */
      /* util.rm(internal + DUMP_FILE); */

      /* Create the top level folders if they do not exist. */
      /* TODO only if media exists. */
      for(String folder : new String[]{GROUPS_DIR, SETTINGS})
         util.mkdir(folder);

      /* Configure the action bar. */
      action_bar = getSupportActionBar();
      action_bar.setDisplayHomeAsUpEnabled(true);
      action_bar.setHomeButtonEnabled(true);

      /*write.single(storage + "single.txt", "link|test|");
      write.collection(storage + "collection.txt", java.util.Arrays.asList(new String[]{"test1", "test2"}));
      write.single(storage + "read_file.txt", read.file(storage + "single.txt")[0]);
      write.single(storage + "count.txt", Integer.toString(read.count(storage + "collection.txt")));
      write.single(storage + "set.txt", (String)(read.set(storage + "single.txt")).toArray()[0]);
      write.single(storage + "csv.txt", read.csv(storage + "single.txt", 'l')[0][0]);*/

      /* Create the navigation drawer and set all the listeners for it. */
      new navigation_drawer(activity,
                            this,
                            (DrawerLayout) findViewById(R.id.drawer_layout),
                            (ListView) findViewById(R.id.left_drawer));

      /* Create the fragments that go inside the content frame and add them
       * to the fragment manager. Hide all but the current one. */
      if(savedInstanceState == null)
      {
         Fragment feeds = new fragment_feeds();
         Fragment man   = new fragment_manage();
         Fragment prefs = new fragment_settings();

         fman.beginTransaction()
            .add(R.id.content_frame, feeds, navigation_drawer.NAV_TITLES[0])
            .add(R.id.content_frame, man,   navigation_drawer.NAV_TITLES[1])
            .add(R.id.content_frame, prefs, navigation_drawer.NAV_TITLES[2])
            .hide(man)
            .hide(prefs)
            .commit();
      }
      else
      {
         fman.beginTransaction()
            .show(fman.findFragmentByTag(navigation_drawer.NAV_TITLES[0]))
            .hide(fman.findFragmentByTag(navigation_drawer.NAV_TITLES[2]))
            .hide(fman.findFragmentByTag(navigation_drawer.NAV_TITLES[1]))
            .commit();
      }

      /* Load the read items. */
        adapter_feeds_cards.read_items = read.set(storage + READ_ITEMS);

        update_groups();

        /* If an all_content file exists, refresh page 0. */
        if(util.exists(ALL_FILE))
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
      action_bar.setDisplayHomeAsUpEnabled(true);
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
      util.set_service(this, 0, "start");
      util.rm(storage + READ_ITEMS);
      write.collection(storage + READ_ITEMS, adapter_feeds_cards.read_items);
   }

   @Override
   protected void onStart()
   {
      super.onStart();
      util.set_service(this, 0, "stop");
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
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

   static class fragment_feeds extends Fragment
   {
      public fragment_feeds()
      {
      }

      @Override
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setRetainInstance(false);
         setHasOptionsMenu(true);
      }

      @Override
      public View onCreateView(LayoutInflater in, ViewGroup container, Bundle b)
      {
         View v = in.inflate(R.layout.viewpager_feeds, container, false);

         viewpager = (ViewPager) v.findViewById(R.id.pager);
         viewpager.setAdapter(new pageradapter_feeds(fman));
         viewpager.setOffscreenPageLimit(128);
         viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
         {
            @Override
            public void onPageScrollStateChanged(int state)
            {
            }

            @Override
            public void onPageScrolled(int pos, float offset, int offsetPx)
            {
            }

            @Override
            public void onPageSelected(int pos)
            {
               if(util.get_card_adapter(fman, viewpager, pos).getCount() == 0)
                  update.page(pos);
            }
         });

         strips[0] = (PagerTabStrip) v.findViewById(R.id.pager_title_strip);
         strips[0].setDrawFullUnderline(true);
         util.set_strip_colour(strips[0]);

         return v;
      }

      @Override
      public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
      {
         optionsMenu = menu;
         optionsMenu.clear();

         inflater.inflate(R.menu.main_overflow, optionsMenu);
         super.onCreateOptionsMenu(optionsMenu, inflater);

         set_refresh(service_update.check_service_running(getActivity()));
      }

      @Override
      public boolean onOptionsItemSelected(MenuItem item)
      {
         if(navigation_drawer.drawer_toggle.onOptionsItemSelected(item))
            return true;
         else if(item.getTitle().equals("add"))
         {
            add_edit_dialog.show_add_dialog(cgroups, con);
            return true;
         }
         else if(item.getTitle().equals("unread"))
         {
            jump_to_latest_unread(null, true, 0);
            return true;
         }
         else if(item.getTitle().equals("refresh"))
         {
            refresh_feeds();
            return true;
         }
         return super.onOptionsItemSelected(item);
      }
   }

   static class fragment_manage extends Fragment
   {
      public fragment_manage()
      {
      }

      @Override
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         setRetainInstance(false);
         setHasOptionsMenu(true);
      }

      @Override
      public View onCreateView(LayoutInflater in, ViewGroup container, Bundle b)
      {
         View v = in.inflate(R.layout.viewpager_manage, container, false);

         ViewPager pager = (ViewPager) v.findViewById(R.id.manage_viewpager);
         pager.setAdapter(new pageradapter_manage(fman));

         strips[1] = (PagerTabStrip) v.findViewById(R.id.manage_title_strip);
         strips[1].setDrawFullUnderline(true);
         util.set_strip_colour(strips[1]);

         return v;
      }

      @Override
      public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
      {
         optionsMenu = menu;
         optionsMenu.clear();

         inflater.inflate(R.menu.manage_overflow, optionsMenu);
         super.onCreateOptionsMenu(optionsMenu, inflater);
      }
   }

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
      private static final Fragment[] fragments = new Fragment[]
      {
         new fragment_manage_group(),
         new fragment_manage_feed(),
         new fragment_manage_filters(),
      };
      private static final int[] titles = new int[]
      {
         R.string.groups_manage_sub,
         R.string.feeds_manage_sub,
         R.string.filters_manage_sub,
      };

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
         return con.getString(titles[position]);
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
         pageradapter_settings adapter = new pageradapter_settings(fman);
         vp.setAdapter(adapter);

         strips[2] = (PagerTabStrip) v.findViewById(R.id.settings_title_strip);
         strips[2].setDrawFullUnderline(true);
         util.set_strip_colour(strips[2]);

         return v;
      }
   }

   static class pageradapter_settings extends FragmentPagerAdapter
   {
      private static final Fragment[] fragments = new Fragment[]
      {
         new fragment_settings_function(),
         new fragment_settings_interface(),
      };

      private final String[] titles;

      public pageradapter_settings(FragmentManager fm)
      {
         super(fm);
         titles = util.get_array(con, R.array.settings_pagertab_titles);
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
         adapter_settings_function adapter = new adapter_settings_function(con);
         setListAdapter(adapter);
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
      {
         return inflater.inflate(R.layout.listview_settings_function, container, false);
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
         adapter_settings_interface adapter = new adapter_settings_interface(con);
         setListAdapter(adapter);
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
      {
         return inflater.inflate(R.layout.listview_settings_function, container, false);
      }
   }

   static void set_refresh(final boolean mode)
   {
      if(optionsMenu != null)
      {
         MenuItem refreshItem = optionsMenu.findItem(R.id.refresh);
         if(refreshItem != null)
         {
            if (mode)
               MenuItemCompat.setActionView(refreshItem, R.layout.progress_circle);
            else
               MenuItemCompat.setActionView(refreshItem, null);
         }
      }
   }

   static void update_groups()
   {
      int previous_size = cgroups.length;

      cgroups = read.file(storage + GROUP_LIST);
      int size = cgroups.length;
      if(size == 0)
      {
         write.single(storage + GROUP_LIST, ALL + NL);
         cgroups = new String[]{ALL};
      }

      if(viewpager != null)
      {
         if(previous_size != size)
            viewpager.setAdapter(new pageradapter_feeds(fman));
         else
            viewpager.getAdapter().notifyDataSetChanged();

         /* Does not run on first update. */
         update.navigation(null);
      }
   }

   static int jump_to_latest_unread(String[] links, boolean update, int page)
   {
      int m;

      if(update)
         page = viewpager.getCurrentItem();

      if( links == null )
         links = util.get_card_adapter(fman, viewpager, page).links;

      for(m = links.length - 1; m >= 0; m--)
      {
         if(!adapter_feeds_cards.read_items.contains(links[links.length - m - 1]))
         {
            break;
         }
      }

      /* 0 is the top. links.length - 1 is the bottom.*/
      if(update)
      {
         ListView lv = util.get_listview(fman, viewpager, page);
         if(lv == null)
         {
            return -1;
         }
         lv.setSelection(m);
      }
      return m;
   }

   static void refresh_feeds()
   {
      set_refresh(true);
      service_handler = new Handler()
      {
         @Override
         public void handleMessage(Message msg)
         {
            set_refresh(false);
            int page = msg.getData().getInt("page_number");
            util.refresh_pages(page);
         }
      };
      Intent intent = util.make_intent(con, viewpager.getCurrentItem());
      con.startService(intent);
   }
}
