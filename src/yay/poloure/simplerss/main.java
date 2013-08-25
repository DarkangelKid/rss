package yay.poloure.simplerss;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
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
	private static Menu						optionsMenu;
	public  static ViewPager				viewpager;
	public  static Context					activity_context;
	public  static FragmentManager		fragment_manager;
	public  static ActionBar				action_bar;
	public  static Activity					activity;
	public  static Handler					service_handler;
	public  static String[] current_groups = new String[0];
	public  static String storage, ALL, NAVIGATION, DELETE_DIALOG, CLEAR_DIALOG, ALL_FILE;

	/* Static final are good. */
	public  static final String SEPAR					= System.getProperty("file.separator");
	public  static final String NL						= System.getProperty("line.separator");
	public  static final String TXT						= ".txt";
	public  static final String GROUPS_DIRECTORY		= "groups" + SEPAR;
	public  static final String THUMBNAIL_DIRECTORY	= "thumbnails" + SEPAR;
	public  static final String IMAGE_DIRECTORY		= "images" + SEPAR;
	public  static final String SETTINGS				= "settings" + SEPAR;
	public  static final String PAGERTABSTRIPCOLOUR	= "pagertabstrip_colour" + TXT;
	public  static final String DUMP_FILE				= "dump" + TXT;
	public  static final String STORE_APPENDIX		= ".store" + TXT;
	public  static final String CONTENT_APPENDIX		= ".content" + TXT;
	public  static final String COUNT_APPENDIX		= ".count" + TXT;
	public  static final String GROUP_LIST				= "group_list" + TXT;
	public  static final String FILTER_LIST			= "filter_list" + TXT;
	public  static final PagerTabStrip[] strips		= new PagerTabStrip[3];

	private static final String[] folders				= new String[]{GROUPS_DIRECTORY, SETTINGS};
	private static final String READ_ITEMS				= "read_items" + TXT;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.navigation_drawer_and_content_frame);

		/* Form the storage path. */
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
			storage					= getExternalFilesDir(null).getAbsolutePath() + SEPAR;
		else
		{
			String packageName	= getPackageName();
			File externalPath		= Environment.getExternalStorageDirectory();
			storage					= externalPath.getAbsolutePath() + SEPAR + "Android" + SEPAR + "data" + SEPAR + packageName + SEPAR + "files" + SEPAR;
			File storage_file		= new File(storage);
			if(!storage_file.exists())
				storage_file.mkdirs();
		}

		/* Delete the log file. */
		utilities.delete(storage + DUMP_FILE);

		/* Create the top level folders if they do not exist. */
		File folder_file;
		for(String folder : folders)
		{
			folder_file = new File(storage + folder);
			if(!folder_file.exists())
				folder_file.mkdir();
		}

		/* Load String resources into static variables. */
		ALL						= getString(R.string.all_group);
		NAVIGATION				= getString(R.string.navigation_title);
		DELETE_DIALOG			= getString(R.string.delete_dialog);
		CLEAR_DIALOG			= getString(R.string.clear_dialog);
		ALL_FILE					= GROUPS_DIRECTORY + ALL + SEPAR + ALL + TXT;

		/* Save the other satic variables. */
		fragment_manager	= getSupportFragmentManager();
		activity_context	= this;
		activity				= this;

		/* Configure the action bar. */
		action_bar = getSupportActionBar();
		action_bar.setDisplayShowHomeEnabled(true);
		action_bar.setDisplayHomeAsUpEnabled(true);
		action_bar.setHomeButtonEnabled(true);
		action_bar.setIcon(R.drawable.rss_icon);

		/* Create the navigation drawer and set all the listeners for it. */
		new navigation_drawer(activity, this, (DrawerLayout) findViewById(R.id.drawer_layout), (ListView) findViewById(R.id.left_drawer));

		/* Create the fragments that go inside the content frame and add them to the fragment manager. */
		if(savedInstanceState == null)
		{
			Fragment prefs		= new fragment_settings();
			Fragment man		= new fragment_manage();
			fragment_manager.beginTransaction()
				.add(R.id.content_frame, new fragment_feeds(),	navigation_drawer.NAVIGATION_TITLES[0])
				.add(R.id.content_frame, prefs,						navigation_drawer.NAVIGATION_TITLES[2])
				.add(R.id.content_frame, man,							navigation_drawer.NAVIGATION_TITLES[1])
				.hide(man)
				.hide(prefs)
				.commit();
		}
		else
		{
			fragment_manager.beginTransaction()
					.show(fragment_manager.findFragmentByTag(navigation_drawer.NAVIGATION_TITLES[0]))
					.hide(fragment_manager.findFragmentByTag(navigation_drawer.NAVIGATION_TITLES[2]))
					.hide(fragment_manager.findFragmentByTag(navigation_drawer.NAVIGATION_TITLES[1]))
					.commit();
		}

		/* Load the read items. */
		adapter_feeds_cards.read_items = utilities.read_file_to_set(storage + READ_ITEMS);

		update_groups();

		/* If an all_content file exists, refresh page 0. */
		if(utilities.exists(storage + ALL_FILE))
		{
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				new refresh_page(0).execute();
			else
				new refresh_page(0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		action_bar.setTitle(navigation_drawer.NAVIGATION_TITLES[0]);
		navigation_drawer.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		navigation_drawer.drawer_toggle.setDrawerIndicatorEnabled(true);
		action_bar.setDisplayHomeAsUpEnabled(true);
	}

	/* This is so the icon and text in the actionbar are selected. */
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		navigation_drawer.drawer_toggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		/* Load the refresh boolean value from settings. */
		Boolean refresh = false;
		String[] check = utilities.read_file_to_array(main.storage + main.SETTINGS + main.SEPAR + adapter_settings_function.file_names[1] + main.TXT);
		if(check.length != 0)
			refresh = Boolean.parseBoolean(check[0]);

		if(refresh)
		{
			/* Load the refresh time from settings. */
			int refresh_time = adapter_settings_function.times[3];
			check = utilities.read_file_to_array(main.storage + main.SETTINGS + main.SEPAR + adapter_settings_function.file_names[2] + main.TXT);
			if(check.length != 0)
				refresh_time = Integer.parseInt(check[0]);

			/* Load notification boolean. */
			Boolean notifications = false;
			check = utilities.read_file_to_array(main.storage + main.SETTINGS + main.SEPAR + adapter_settings_function.file_names[3] + main.TXT);
			if(check.length != 0)
				notifications = Boolean.parseBoolean(check[0]);

			/* Create the pendingIntent and set the alarmservice times for it. */
			Intent intent = new Intent(this, service_update.class);
			intent.putExtra("GROUP_NUMBER", 0);
			intent.putExtra("NOTIFICATIONS", notifications);
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			final long interval = (long) refresh_time*60000;
			AlarmManager alarm_refresh = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_refresh.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pend_intent);
		}
		utilities.delete(storage + READ_ITEMS);
		utilities.write_collection_to_file(storage + READ_ITEMS, adapter_feeds_cards.read_items);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Boolean refresh = false;
		/* Load the refresh boolean value from settings. */
		String[] check = utilities.read_file_to_array(main.storage + main.SETTINGS + main.SEPAR + adapter_settings_function.file_names[1] + main.TXT);
		if(check.length != 0)
			refresh = Boolean.parseBoolean(check[0]);

		if(refresh)
		{
			Intent intent = new Intent(this, service_update.class);
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			AlarmManager alarm_manager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_manager.cancel(pend_intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		String title = action_bar.getTitle().toString();

		if(utilities.index_of(navigation_drawer.NAVIGATION_TITLES, title) != -1)
			return navigation_drawer.drawer_toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
		else
		{
			onBackPressed();
			return true;
		}
	}

	/* END OF OVERRIDES */

	private static class fragment_feeds extends Fragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setRetainInstance(false);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View feed_view = inflater.inflate(R.layout.viewpager_feeds, container, false);

			viewpager = (ViewPager) feed_view.findViewById(R.id.pager);
			viewpager.setAdapter(new pageradapter_feeds(fragment_manager));
			viewpager.setOffscreenPageLimit(128);
			viewpager.setOnPageChangeListener
			(
				new ViewPager.OnPageChangeListener()
				{
					@Override
					public void onPageScrollStateChanged(int state)
					{
					}

					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
					{
					}

					@Override
					public void onPageSelected(int position)
					{
						if(utilities.get_adapter_feeds_cards(fragment_manager, viewpager, position).getCount() == 0)
						{
							if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
								new refresh_page(position).execute();
							else
								new refresh_page(position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}
				}
			);

			strips[0] = (PagerTabStrip) feed_view.findViewById(R.id.pager_title_strip);
			strips[0].setDrawFullUnderline(true);
			utilities.set_pagertabstrip_colour(storage, strips[0]);

			return feed_view;
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
				add_edit_dialog.show_add_feed_dialog(current_groups, activity_context);
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
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setRetainInstance(false);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View manage_view = inflater.inflate(R.layout.viewpager_manage, container, false);

			ViewPager manage_pager = (ViewPager) manage_view.findViewById(R.id.manage_viewpager);
			manage_pager.setAdapter(new pageradapter_manage(fragment_manager));

			strips[1] = (PagerTabStrip) manage_view.findViewById(R.id.manage_title_strip);
			strips[1].setDrawFullUnderline(true);
			utilities.set_pagertabstrip_colour(storage, strips[1]);

			return manage_view;
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
		private static final Fragment[] manage_fragments	= new Fragment[]{new fragment_manage_group(), new fragment_manage_feed(), new fragment_manage_filters()};
		private static final int[] manage_titles = new int[]{R.string.groups_manage_sub, R.string.feeds_manage_sub, R.string.filters_manage_sub};

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
			return activity_context.getString(manage_titles[position]);
		}
	}

	private static class fragment_card extends ListFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setRetainInstance(false);
			adapter_feeds_cards ith = new adapter_feeds_cards(getActivity());
			setListAdapter(ith);
			String group = current_groups[getArguments().getInt("num", 0)];
			String[] lines = utilities.read_file_to_array(storage + GROUPS_DIRECTORY + group + SEPAR + group + CONTENT_APPENDIX);
			int count = -1;
			for(int i = 0; i < lines.length; i++)
			{
				if(lines[i].substring(7, 8).equals("0"))
				{
					count = lines.length - i + 1;
					break;
				}
			}
			if(count == -1)
				ith.unread_count = 0;
			else
				ith.unread_count = count;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
		{
			return inflater.inflate(R.layout.listview_feed, container, false);
		}
	}

	private static class fragment_settings extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
		{
			View settings_view = inflater.inflate(R.layout.viewpager_settings, container, false);
			ViewPager viewpager_settings = (ViewPager)settings_view.findViewById(R.id.settings_pager);
			pageradapter_settings adapter = new pageradapter_settings(fragment_manager);
			viewpager_settings.setAdapter(adapter);

			strips[2] = (PagerTabStrip) settings_view.findViewById(R.id.settings_title_strip);
			strips[2].setDrawFullUnderline(true);
			utilities.set_pagertabstrip_colour(storage, strips[2]);

			return settings_view;
		}
	}

	public static class pageradapter_settings extends FragmentPagerAdapter
	{
		private static final Fragment[] settings_fragments	= new Fragment[]{new fragment_settings_function(), new fragment_settings_interface()};
		private static final String[]   settings_titles		= new String[]{"Functions", "Interface"};

		public pageradapter_settings(FragmentManager fm)
		{
			super(fm);
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
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			adapter_settings_function adapter = new adapter_settings_function(activity_context);
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
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			adapter_settings_interface adapter = new adapter_settings_interface(activity_context, storage);
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

		current_groups = utilities.read_file_to_array(storage + GROUP_LIST);
		final int size = current_groups.length;
		if(size == 0)
		{
			utilities.append_string_to_file(storage + GROUP_LIST, ALL + NL);
			current_groups = new String[]{"All"};
		}

		if(viewpager != null)
		{
			if(previous_size != size)
				viewpager.setAdapter(new pageradapter_feeds(fragment_manager));
			else
				viewpager.getAdapter().notifyDataSetChanged();
		}
		navigation_drawer.update_navigation_data(null, true);
	}

	public static int jump_to_latest_unread(String[] links, boolean update, int page_number)
	{
		int m;

		if(update)
			page_number = viewpager.getCurrentItem();

		if(links == null)
			links = utilities.get_adapter_feeds_cards(fragment_manager, viewpager, page_number).links;

		for(m = links.length - 1; m >= 0; m--)
		{
			if(!adapter_feeds_cards.read_items.contains(links[links.length - m - 1]))
				break;
		}

		/* 0 is the top. links.length - 1 is the bottom.*/
		if(update)
		{
			ListView lv = utilities.get_listview(fragment_manager, viewpager, page_number);
			if(lv == null)
				return -1;
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
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				{
					new refresh_page(0).execute();
					if(page != 0)
						new refresh_page(page).execute();
					else
					{
						for(int i = 1; i < current_groups.length; i++)
							new refresh_page(i).execute();
					}
				}
				else
				{
					new refresh_page(0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					if(page != 0)
						new refresh_page(page).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					else
					{
						for(int i = 1; i < current_groups.length; i++)
							new refresh_page(i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				}
			}
		};

		/* Load notification boolean. */
		Boolean notifications = false;
		String[] check = utilities.read_file_to_array(main.storage + main.SETTINGS + main.SEPAR + adapter_settings_function.file_names[3] + main.TXT);
		if(check.length != 0)
			notifications = Boolean.parseBoolean(check[0]);

		final int page_number = viewpager.getCurrentItem();
		final Intent intent = new Intent(activity_context, service_update.class);
		intent.putExtra("GROUP_NUMBER", page_number);
		intent.putExtra("NOTIFICATIONS", notifications);
		activity_context.startService(intent);
	}
}
