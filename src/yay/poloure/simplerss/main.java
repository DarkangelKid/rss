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
   /* Statics without final intilisations are generally unsafe. */
   private static Menu                 optionsMenu;
   public  static ViewPager            viewpager;
   public  static Context              con;
   public  static FragmentManager      fman;
   public  static ActionBar            action_bar;
   public  static Activity             activity;
   public  static Handler              service_handler;
   public  static String[] current_groups = new String[0];

   /* These must be set straight away in onCreate. */
   public  static String storage, ALL, DELETE_DIALOG, CLEAR_DIALOG, ALL_FILE;

   /* Static final are good. */
   public static final String SEPAR = System.getProperty("file.separator");
   public static final String NL    = System.getProperty("line.separator");

   public static final String TXT           = ".txt";
   public static final String GROUPS_DIR    = "groups" + SEPAR;
   public static final String THUMBNAIL_DIR = "thumbnails" + SEPAR;
   public static final String IMAGE_DIR     = "images" + SEPAR;
   public static final String SETTINGS      = "settings" + SEPAR;
   public static final String STRIP_COLOR   = "pagertabstrip_colour" + TXT;
   public static final String DUMP_FILE     = "dump" + TXT;
   public static final String TEMP          = ".temp" + TXT;
   public static final String STORE         = ".store" + TXT;
   public static final String CONTENT       = ".content" + TXT;
   public static final String URL           = ".urls" + TXT;
   public static final String COUNT         = ".count" + TXT;
   public static final String GROUP_LIST    = "group_list" + TXT;
   public static final String FILTER_LIST   = "filter_list" + TXT;
   public static final String READ_ITEMS    = "read_items" + TXT;

   static int ver                      = VERSION.SDK_INT;
   public static boolean FROYO         = ver >= VERSION_CODES.FROYO;
   public static boolean HONEYCOMB     = ver >= VERSION_CODES.HONEYCOMB;
   public static boolean HONEYCOMB_MR2 = ver >= VERSION_CODES.HONEYCOMB_MR2;
   public static boolean JELLYBEAN     = ver >= VERSION_CODES.JELLY_BEAN;

   public static final PagerTabStrip[] strips = new PagerTabStrip[3];

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.navigation_drawer_and_content_frame);

      /* Save the other satic variables. */
      fman  = getSupportFragmentManager();
      con  = this;
      activity          = this;

      /* Form the storage path. */
      storage = util.get_storage();

      if(storage == null)
         return;

      /* Load String resources into static variables. */
      ALL           = getString(R.string.all_group);
      DELETE_DIALOG = getString(R.string.delete_dialog);
      CLEAR_DIALOG  = getString(R.string.clear_dialog);
      ALL_FILE      = storage + GROUPS_DIR + ALL + SEPAR + ALL + TXT;

      /* Delete the log file. */
      util.rm(storage + DUMP_FILE);

      /* Create the top level folders if they do not exist. */
      File folder_file;
      for(String folder : new String[]{GROUPS_DIR, SETTINGS})
      {
         folder_file = new File(storage + folder);
         if(!folder_file.exists())
            folder_file.mkdir();
      }

      /* Configure the action bar. */
      action_bar = getSupportActionBar();
      action_bar.setDisplayHomeAsUpEnabled(true);
      action_bar.setHomeButtonEnabled(true);

      /* Create the navigation drawer and set all the listeners for it. */
      new navigation_drawer(activity,
                            this,
                            (DrawerLayout) findViewById(R.id.drawer_layout),
                            (ListView) findViewById(R.id.left_drawer));

      /* Create the fragments that go inside the content frame and add them
       * to the fragment manager. Hide all but the current one. */
      if(savedInstanceState == null)
      {
         Fragment feeds    = new fragment_feeds();
         Fragment man      = new fragment_manage();
         Fragment prefs    = new fragment_settings();

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

      action_bar.setTitle(navigation_drawer.NAV_TITLES[0]);
      navigation_drawer.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
      util.set_service(this, storage, 0, "start");
      util.rm(storage + READ_ITEMS);
      write.collection(storage + READ_ITEMS, adapter_feeds_cards.read_items);
   }

   @Override
   protected void onStart()
   {
      super.onStart();
      util.set_service(this, storage, 0, "stop");
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

   private static class fragment_feeds extends Fragment
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
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b)
      {
         View v = inflater.inflate(R.layout.viewpager_feeds, container, false);

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
            public void onPageScrolled(int pos, float posOffset, int posOffsetPx)
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
         util.set_pagertabstrip_colour(storage, strips[0]);

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
            add_edit_dialog.show_add_dialog(current_groups, con);
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

   private static class fragment_manage extends Fragment
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
      public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle savedInstanceState)
      {
         View v = inf.inflate(R.layout.viewpager_manage, container, false);

         ViewPager manage_pager = (ViewPager) v.findViewById(R.id.manage_viewpager);
         manage_pager.setAdapter(new pageradapter_manage(fman));

         strips[1] = (PagerTabStrip) v.findViewById(R.id.manage_title_strip);
         strips[1].setDrawFullUnderline(true);
         util.set_pagertabstrip_colour(storage, strips[1]);

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

   public static class pageradapter_feeds extends FragmentPagerAdapter
   {
      public pageradapter_feeds(FragmentManager fm)
      {
         super(fm);
      }

      @Override
      public int getCount()
      {
         return current_groups.length;
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
         return current_groups[position];
      }
   }

   public static class pageradapter_manage extends FragmentPagerAdapter
   {
      private static final Fragment[] manage_fragments = new Fragment[]
      {
         new fragment_manage_group(),
         new fragment_manage_feed(),
         new fragment_manage_filters(),
      };
      private static final int[] manage_titles = new int[]
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
         return manage_fragments.length;
      }

      @Override
      public Fragment getItem(int position)
      {
         return manage_fragments[position];
      }

      @Override
      public String getPageTitle(int position)
      {
         return con.getString(manage_titles[position]);
      }
   }

   private static class fragment_card extends ListFragment
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
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
      {
         return inflater.inflate(R.layout.listview_feed, container, false);
      }
   }

   private static class fragment_settings extends Fragment
   {
      public fragment_settings()
      {
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
      {
         View settings_view = inflater.inflate(R.layout.viewpager_settings, container, false);
         ViewPager viewpager_settings = (ViewPager)settings_view.findViewById(R.id.settings_pager);
         pageradapter_settings adapter = new pageradapter_settings(fman);
         viewpager_settings.setAdapter(adapter);

         strips[2] = (PagerTabStrip) settings_view.findViewById(R.id.settings_title_strip);
         strips[2].setDrawFullUnderline(true);
         util.set_pagertabstrip_colour(storage, strips[2]);

         return settings_view;
      }
   }

   public static class pageradapter_settings extends FragmentPagerAdapter
   {
      private static final Fragment[] settings_fragments = new Fragment[]
      {
         new fragment_settings_function(),
         new fragment_settings_interface(),
      };

      private final String[]  settings_titles;

      public pageradapter_settings(FragmentManager fm)
      {
         super(fm);
         settings_titles = con.getResources().getStringArray(R.array.settings_pagertab_titles);
      }

      @Override
      public int getCount()
      {
         return settings_titles.length;
      }

      @Override
      public Fragment getItem(int position)
      {
         return settings_fragments[position];
      }

      @Override
      public String getPageTitle(int position)
      {
         return settings_titles[position];
      }
   }

   private static class fragment_settings_function extends ListFragment
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

   private static class fragment_settings_interface extends ListFragment
   {
      public fragment_settings_interface()
      {
      }

      @Override
      public void onCreate(Bundle savedInstanceState)
      {
         super.onCreate(savedInstanceState);
         adapter_settings_interface adapter = new adapter_settings_interface(con, storage);
         setListAdapter(adapter);
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
      {
         return inflater.inflate(R.layout.listview_settings_function, container, false);
      }
   }

   public static void set_refresh(final boolean mode)
   {
      if(optionsMenu != null)
      {
         final MenuItem refreshItem = optionsMenu.findItem(R.id.refresh);
         if(refreshItem != null)
         {
            if (mode)
               MenuItemCompat.setActionView(refreshItem, R.layout.progress_circle);
            else
               MenuItemCompat.setActionView(refreshItem, null);
         }
      }
   }

   public static void update_groups()
   {
      final int previous_size = current_groups.length;

      current_groups = read.file(storage + GROUP_LIST);
      final int size = current_groups.length;
      if(size == 0)
      {
         write.single(storage + GROUP_LIST, ALL + NL);
         current_groups = new String[]{ALL};
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

   public static int jump_to_latest_unread(String[] links, boolean update, int page)
   {
      int m;

      if(update)
         page = viewpager.getCurrentItem();

      if(links == null)
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

   private static void refresh_feeds()
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
      Intent intent = util.make_intent(con, storage, viewpager.getCurrentItem());
      con.startService(intent);
   }
}
