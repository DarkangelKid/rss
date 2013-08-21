package yay.poloure.simplerss;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Debug;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.widget.DrawerLayout;

import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView.MultiChoiceModeListener;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.telephony.TelephonyManager;
import android.provider.Settings;

import java.io.File;

public class main extends ActionBarActivity
{
	/// These are fine.
	private ListView navigation_list;

	/// Statics without final intilisations are generally unsafe.
	private static String					current_title;
	private static Menu						optionsMenu;
	public  static ViewPager				viewpager;
	public  static DrawerLayout			drawer_layout;
	public  static ActionBarDrawerToggle	drawer_toggle;
	public  static Context					activity_context;
	public  static FragmentManager		fragment_manager;
	public  static ActionBar				action_bar;
	public  static Activity					activity;
	public  static adapter_navigation_drawer nav_adapter;
	public  static List<String> current_groups 			= new ArrayList<String>();
	public  static List<Boolean> new_items 				= new ArrayList<Boolean>();
	public  static String storage, ALL, NAVIGATION, DELETE_DIALOG, CLEAR_DIALOG, ALL_FILE;

	/// Private static final are good.
	private static final int[] times							= new int[]{15, 30, 45, 60, 120, 180, 240, 300, 360, 400, 480, 540, 600, 660, 720, 960, 1440, 2880, 10080, 43829};
	private static final Pattern whitespace				= Pattern.compile("\\s+");

	/// Public static final are the holy grail.
	public  static final String SEPAR						= System.getProperty("file.separator");
	public  static final String NL							= System.getProperty("line.separator");
	public  static final String TXT							= ".txt";
	public  static final String GROUPS_DIRECTORY			= "groups" + SEPAR;
	public  static final String CONTENT_DIRECTORY		= "content" + SEPAR;
	public  static final String THUMBNAIL_DIRECTORY		= "thumbnails" + SEPAR;
	public  static final String IMAGE_DIRECTORY			= "images" + SEPAR;
	public  static final String DUMP_FILE					= "dump" + TXT;
	public  static final String NEW_ITEMS					= "new_items" + TXT;
	public  static final String READ_ITEMS					= "read_items" + TXT;
	public  static final String STORE_APPENDIX			= ".store" + TXT;
	public  static final String CONTENT_APPENDIX			= ".content" + TXT;
	public  static final String COUNT_APPENDIX			= ".count" + TXT;
	public  static final String GROUP_LIST					= "group_list" + TXT;
	public  static final String FILTER_LIST				= "filter_list" + TXT;
	public  static final String[] NAVIGATION_TITLES		= new String[3];

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.navigation_drawer_and_content_frame);

		perform_initial_operations();
		current_title	= NAVIGATION_TITLES[0];
		action_bar		= getSupportActionBar();
		activity			= this;

		action_bar.setDisplayShowHomeEnabled(true);
		action_bar.setDisplayHomeAsUpEnabled(true);
		action_bar.setHomeButtonEnabled(true);
		action_bar.setIcon(R.drawable.rss_icon);

		fragment_manager = getSupportFragmentManager();

		if(savedInstanceState == null)
		{
			Fragment feed		= new fragment_feeds();
			Fragment prefs		= new fragment_settings();
			Fragment man		= new fragment_manage();
			fragment_manager.beginTransaction()
				.add(R.id.content_frame, feed, NAVIGATION_TITLES[0])
				.add(R.id.content_frame, prefs, NAVIGATION_TITLES[2])
				.add(R.id.content_frame, man, NAVIGATION_TITLES[1])
				.hide(man)
				.hide(prefs)
				.commit();
		}
		else
		{
			fragment_manager.beginTransaction()
					.show(fragment_manager.findFragmentByTag(NAVIGATION_TITLES[0]))
					.hide(fragment_manager.findFragmentByTag(NAVIGATION_TITLES[2]))
					.hide(fragment_manager.findFragmentByTag(NAVIGATION_TITLES[1]))
					.commit();
		}

		nav_adapter		= new adapter_navigation_drawer(this);
		navigation_list	= (ListView) findViewById(R.id.left_drawer);
		navigation_list.setOnItemClickListener
		(
			new ListView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView parent, View view, int position, long id)
				{
					switch(position)
					{
						case 0:
							switch_page(NAVIGATION_TITLES[0], 0);
							break;
						case 1:
							switch_page(NAVIGATION_TITLES[1], 1);
							break;
						case 2:
							switch_page(NAVIGATION_TITLES[2], 2);
							break;
						default:
							switch_page(NAVIGATION_TITLES[0], position);
							viewpager.setCurrentItem(position - 4);
							break;
					}
				}
			}
		);
		navigation_list.setAdapter(nav_adapter);

		drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.END);
		drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			@Override
			public void onDrawerClosed(View view)
			{
				getSupportActionBar().setTitle(current_title);
			}

			@Override
			public void onDrawerOpened(View drawerView)
			{
				getSupportActionBar().setTitle(NAVIGATION);
			}
		};

		drawer_layout.setDrawerListener(drawer_toggle);
		drawer_toggle.syncState();

		/* Load read_items to adapter_feed_card.read_items set. */
		adapter_feeds_cards.read_items = utilities.read_file_to_set(storage + READ_ITEMS);

		update_groups();
		action_bar = getSupportActionBar();

		if(utilities.exists(storage + ALL_FILE))
		{
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				new refresh_page(0).execute();
			else
				new refresh_page(0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		utilities.log(storage, Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID));
	}

	private void perform_initial_operations()
	{
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

		utilities.delete(storage + DUMP_FILE);

		File folder_file = new File(storage + "groups");
		if(!folder_file.exists())
			folder_file.mkdir();

		activity_context		= this;
		ALL						= getString(R.string.all_group);
		NAVIGATION				= getString(R.string.navigation_title);
		DELETE_DIALOG			= getString(R.string.delete_dialog);
		CLEAR_DIALOG			= getString(R.string.clear_dialog);
		NAVIGATION_TITLES[0]	= getString(R.string.feeds_title);
		NAVIGATION_TITLES[1]	= getString(R.string.manage_title);
		NAVIGATION_TITLES[2]	= getString(R.string.settings_title);
		ALL_FILE					= GROUPS_DIRECTORY + ALL + SEPAR + ALL + TXT;
	}

	/// This is so the icon and text in the actionbar are selected.
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		drawer_toggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		//if(fragment_manager.getBackStackEntryAt(0).getName().equals("BACK"))
		{
			action_bar.setTitle(NAVIGATION_TITLES[0]);
			drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			drawer_toggle.setDrawerIndicatorEnabled(true);
			action_bar.setDisplayHomeAsUpEnabled(true);
		}
	}

	protected void onStop()
	{
		super.onStop();
		//SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if(/*pref.getBoolean("refresh", false)*/true)
		{
			Intent intent = new Intent(this, service_update.class);
			intent.putExtra("GROUP_NUMBER", 0);
			intent.putExtra("NOTIFICATIONS", /*pref.getBoolean("notifications", false)*/ true);
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			final long interval = (long) times[/*pref.getInt("refresh_time", 20)/5*/2]*60000;
			AlarmManager alarm_refresh = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_refresh.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pend_intent);
		}
		utilities.delete(storage + READ_ITEMS);
		utilities.write_collection_to_file(storage + READ_ITEMS, adapter_feeds_cards.read_items);

		utilities.write_collection_to_file(storage + NEW_ITEMS, new_items);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		/*SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);*/
		if(/*pref.getBoolean("refresh", false)*/ true)
		{
			Intent intent = new Intent(this, service_update.class);
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			AlarmManager alarm_manager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_manager.cancel(pend_intent);
		}

		List<String> strings = utilities.read_file_to_list(storage + NEW_ITEMS);
		new_items = new ArrayList<Boolean>();
		for(String string : strings)
		{
			if(string.equals("true"))
				new_items.add(true);
			else if(string.equals("false"))
				new_items.add(false);
		}

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
			storage				= getExternalFilesDir(null).getAbsolutePath() + SEPAR;
		else
		{
			String packageName	= getPackageName();
			File externalPath	= Environment.getExternalStorageDirectory();
			storage				= externalPath.getAbsolutePath() + SEPAR + "Android" + SEPAR + "data" + SEPAR + packageName + SEPAR + "files" + SEPAR;
			File storage_file	= new File(storage);
			if(!storage_file.exists())
				storage_file.mkdirs();
		}

		current_groups = utilities.read_file_to_list(storage + GROUP_LIST);
		if(new_items.size() != current_groups.size())
		{
			new_items.clear();
			for(String string : current_groups)
				new_items.add(true);
		}
	}

	private void switch_page(String page_title, int position)
	{
		drawer_layout.closeDrawer(navigation_list);
		if(!current_title.equals(page_title))
		{
			fragment_manager.beginTransaction()
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
						.hide(fragment_manager.findFragmentByTag(current_title))
						.show(fragment_manager.findFragmentByTag(page_title))
						.commit();

			navigation_list.setItemChecked(position, true);
			if(position < 3)
				set_title(page_title);
			else
				set_title(NAVIGATION_TITLES[0]);
		}
		current_title = page_title;
	}

	public static void set_title(String title)
	{
		current_title = title;
		action_bar.setTitle(title);
	}

	public static class fragment_feeds extends Fragment
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
						if(utilities.get_adapter_feeds_cards(fragment_manager, viewpager, position).getCount() == 0 || new_items.get(position))
						{
							if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
								new refresh_page(position).execute();
							else
								new refresh_page(position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}
				}
			);

			PagerTabStrip pager_tab_strip = (PagerTabStrip) feed_view.findViewById(R.id.pager_title_strip);
			pager_tab_strip.setDrawFullUnderline(true);
			pager_tab_strip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

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
			if(drawer_toggle.onOptionsItemSelected((MenuItem)item))
				return true;
			else if(item.getTitle().equals("add"))
			{
				add_edit_dialog.show_add_feed_dialog(current_groups, activity_context);
				return true;
			}
			else if(item.getTitle().equals("unread"))
			{
				jump_to_latest_unread();
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

	public static class fragment_manage extends Fragment
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

			final PagerTabStrip manage_strip = (PagerTabStrip) manage_view.findViewById(R.id.manage_title_strip);
			manage_strip.setDrawFullUnderline(true);
			manage_strip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

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
			return current_groups.size();
		}

 		@Override
		public Fragment getItem(int position)
		{
			//if(position < current_groups.size())
			{
				fragment_card f = new fragment_card();
				Bundle args = new Bundle();
				args.putInt("num", position);
				f.setArguments(args);
				return f;
			}
			//return null;
		}

		@Override
		public String getPageTitle(int position)
		{
			return current_groups.get(position);
		}
	}

	public static class pageradapter_manage extends FragmentPagerAdapter
	{
		public pageradapter_manage(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public Fragment getItem(int position)
		{
			switch(position)
			{
				case(0):
					return new fragment_manage_group();
				case(1):
					return new fragment_manage_feed();
				case(2):
					return new fragment_manage_filters();
			}
			return null;
		}

		@Override
		public String getPageTitle(int position)
		{
			switch(position)
			{
				case(0):
					return activity_context.getString(R.string.groups_manage_sub);
				case(1):
					return activity_context.getString(R.string.feeds_manage_sub);
				case(2):
					return activity_context.getString(R.string.filters_manage_sub);
			}
			return "";
		}
	}

	public static class fragment_card extends ListFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setRetainInstance(false);
			adapter_feeds_cards ith = new adapter_feeds_cards(getActivity());
			setListAdapter(ith);
			String group = current_groups.get(getArguments().getInt("num", 0));
			List<String> lines = utilities.read_file_to_list(storage + GROUPS_DIRECTORY + group + SEPAR + group + CONTENT_APPENDIX);
			int count = -1;
			for(int i = 0; i < lines.size(); i++)
			{
				if(lines.get(i).substring(7, 8).equals("0"))
				{
					count = lines.size() - i + 1;
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

	public static class fragment_settings extends Fragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
		{
			View settings_view = inflater.inflate(R.layout.viewpager_settings, container, false);
			ViewPager viewpager_settings = (ViewPager)settings_view.findViewById(R.id.settings_pager);
			pageradapter_settings adapter = new pageradapter_settings(fragment_manager);
			viewpager_settings.setAdapter(adapter);

			final PagerTabStrip settings_strip = (PagerTabStrip) settings_view.findViewById(R.id.settings_title_strip);
			settings_strip.setDrawFullUnderline(true);
			settings_strip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

			return settings_view;
		}
	}

	public static class pageradapter_settings extends FragmentPagerAdapter
	{
		public pageradapter_settings(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public Fragment getItem(int position)
		{
			switch(position)
			{
				case(0):
					return new fragment_settings_function();
				case(1):
					return new fragment_settings_interface();
			}
			return null;
		}

		@Override
		public String getPageTitle(int position)
		{
			switch(position)
			{
				case(0):
					return "Functions";
				case(1):
					return "Interface";
			}
			return "";
		}
	}

	public static class fragment_settings_function extends ListFragment
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		String title = action_bar.getTitle().toString();

		if(title.equals(NAVIGATION_TITLES[0]) || title.equals(NAVIGATION_TITLES[1]) || title.equals(NAVIGATION_TITLES[2]) || title.equals(NAVIGATION))
			return drawer_toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

		else
		{
			onBackPressed();
			return true;
		}
	}

	public static class fragment_settings_interface extends ListFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			///set adapter here
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
		final int previous_size = current_groups.size();

		current_groups = utilities.read_file_to_list(storage + GROUP_LIST);
		final int size = current_groups.size();
		if(size == 0)
		{
			utilities.append_string_to_file(storage + GROUP_LIST, ALL + NL);
			current_groups.add(ALL);
		}

		new_items.clear();
		for(String group : current_groups)
			new_items.add(false);

		/// If viewpager exists, fragment_manager != null. This must come before the nav_adapter.
		if(viewpager != null)
		{
			if(previous_size != size)
				viewpager.setAdapter(new pageradapter_feeds(fragment_manager));
			else
				viewpager.getAdapter().notifyDataSetChanged();
		}
		update_navigation_data(null, true);
	}

	private static void jump_to_latest_unread()
	{
		final int page_number = viewpager.getCurrentItem();
		int oldest_unread = -1;

		final String group_path				= storage + GROUPS_DIRECTORY + current_groups.get(page_number) + main.SEPAR;
		final String group_content_path	= group_path + current_groups.get(page_number) + CONTENT_APPENDIX;

		if(!utilities.exists(group_content_path))
			return;

		List<String> links = utilities.get_adapter_feeds_cards(fragment_manager, viewpager, page_number).content_links;

		final int size = links.size();
		for(int m = 0; m < size; m++)
		{
			if(!adapter_feeds_cards.read_items.contains(links.get(m)))
			{
				oldest_unread = m;
				break;
			}
		}
		if(oldest_unread == 0)
			oldest_unread = links.size();

		ListView lv = ((ListFragment) fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(page_number))).getListView();

		if(lv == null)
			return;

		if(oldest_unread == links.size())
		{
			lv.setSelection(links.size());
			return;
		}

		if(oldest_unread == -1)
			oldest_unread++;

		final int position = links.size() - oldest_unread - 1;
		if(position >= 0)
			lv.setSelection(position);
		else
			lv.setSelection(0);
	}

	private static void refresh_feeds()
	{
		set_refresh(true);
		/* SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity_context);*/
		/* TODO used to save positions here but now I am not sure I need too anymore. */
		final int page_number = viewpager.getCurrentItem();
		final Intent intent = new Intent(activity_context, service_update.class);
		intent.putExtra("GROUP_NUMBER", page_number);
		intent.putExtra("NOTIFICATIONS", /*pref.getBoolean("notifications", false)*/true);
		activity_context.startService(intent);
		if(page_number == 0)
		{
			for(int i = 0; i < new_items.size(); i++)
				new_items.set(i, true);
		}
		else
		{
			new_items.set(0, true);
			new_items.set(page_number, true);
		}
		/// Maybe do not refresh the page.
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new refresh_page(page_number).execute();
		else
			new refresh_page(page_number).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public static void update_navigation_data(List<Integer> counts, Boolean update_names)
	{
		if(counts == null)
			counts = get_unread_counts();

		if(update_names)
			nav_adapter.add_list(current_groups);

		nav_adapter.add_count(counts);
		nav_adapter.notifyDataSetChanged();
	}

	public static List<Integer> get_unread_counts()
	{
		List<Integer> unread_list	= new ArrayList<Integer>();
		int total = 0, unread;
		adapter_feeds_cards ith = null;
		fragment_card fc;
		final int size = current_groups.size();

		for(int i = 1; i < size; i++)
		{
			unread = 0;
			String[] urls = utilities.read_single_to_array(storage + GROUPS_DIRECTORY + current_groups.get(i) + SEPAR + current_groups.get(i) + CONTENT_APPENDIX, "link|");
			for(int j = 0; j < urls.length; j++)
			{
				if(!adapter_feeds_cards.read_items.contains(urls[j]))
						unread++;
			}
			total += unread;
			unread_list.add(unread);
		}

		unread_list.add(0, total);
		return unread_list;
	}
}
