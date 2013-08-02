package yay.poloure.simplerss;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.graphics.Color;
import android.os.Debug;
import android.os.Bundle;
import android.os.AsyncTask;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.res.Resources;

import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActionBar;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.widget.DrawerLayout;

import android.view.Menu;
import android.view.MenuItem;
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

import android.webkit.WebViewFragment;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Pattern;

import java.io.File;

public class main_view extends Activity
{
	/// These are fine.
	public static DrawerLayout drawer_layout;
	private ListView navigation_list;
	private String current_title;

	/// Statics without final intilisations are generally unsafe.
	public static ActionBarDrawerToggle drawer_toggle;
	private static Menu optionsMenu;
	public static Context activity_context;
	private static ViewPager viewpager;
	public static FragmentManager fragment_manager;
	public static ActionBar action_bar;
	public static adapter_manage_feeds feed_list_adapter;
	public static adapter_manage_groups group_list_adapter;
	public static adapter_manage_filter filter_list_adapter;
	public static adapter_navigation_drawer nav_adapter;
	public static List<String> current_groups 			= new ArrayList<String>();
	public static List<Boolean> new_items 				= new ArrayList<Boolean>();
	public static String storage, ALL, FEEDS, SETTINGS, MANAGE, NAVIGATION, DELETE_DIALOG, CLEAR_DIALOG, ALL_FILE;

	/// Private static final are good.
	private static final int[] times 					= new int[]{15, 30, 45, 60, 120, 180, 240, 300, 360, 400, 480, 540, 600, 660, 720, 960, 1440, 2880, 10080, 43829};
	private static final Pattern whitespace				= Pattern.compile("\\s+");

	/// Public static final are the holy grail.
	public static final String SEPAR					= System.getProperty("file.separator");
	public static final String NL						= System.getProperty("line.separator");
	public static final String TXT						= ".txt";
	public static final String GROUPS_DIRECTORY			= "groups" + SEPAR;
	public static final String CONTENT_DIRECTORY		= "content" + SEPAR;
	public static final String THUMBNAIL_DIRECTORY		= "thumbnails" + SEPAR;
	public static final String IMAGE_DIRECTORY			= "images" + SEPAR;
	public static final String DUMP_FILE				= "dump" + TXT;
	public static final String NEW_ITEMS				= "new_items" + TXT;
	public static final String STORE_APPENDIX			= ".store" + TXT;
	public static final String CONTENT_APPENDIX			= ".content" + TXT;
	public static final String COUNT_APPENDIX			= ".count" + TXT;
	public static final String GROUP_LIST				= "group_list" + TXT;
	public static final String FILTER_LIST				= "filter_list" + TXT;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pager);

		perform_initial_operations();
		current_title = FEEDS;

		fragment_manager = getFragmentManager();

		if(savedInstanceState == null)
		{
			Fragment feed		= new fragment_feeds();
			Fragment prefs		= new fragment_preferences();
			Fragment man		= new fragment_manage();
			fragment_manager.beginTransaction()
				.add(R.id.content_frame, feed, FEEDS)
				.add(R.id.content_frame, prefs, SETTINGS)
				.add(R.id.content_frame, man, MANAGE)
				.hide(man)
				.hide(prefs)
				.commit();
		}
		else
		{
			fragment_manager.beginTransaction()
					.show(fragment_manager.findFragmentByTag(FEEDS))
					.hide(fragment_manager.findFragmentByTag(SETTINGS))
					.hide(fragment_manager.findFragmentByTag(MANAGE))
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
							switch_page(FEEDS, 0);
							invalidateOptionsMenu();
							break;
						case 1:
							switch_page(MANAGE, 1);
							invalidateOptionsMenu();
							break;
						case 2:
							switch_page(SETTINGS, 2);
							invalidateOptionsMenu();
							break;
						default:
							switch_page(FEEDS, position);
							invalidateOptionsMenu();
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
				getActionBar().setTitle(FEEDS);
			}

			@Override
			public void onDrawerOpened(View drawerView)
			{
				getActionBar().setTitle(NAVIGATION);
			}
		};

		drawer_layout.setDrawerListener(drawer_toggle);
		drawer_toggle.syncState();

		update_groups();
		action_bar = getActionBar();

		if(utilities.exists(storage + ALL_FILE))
			new refresh_page(0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void perform_initial_operations()
	{
		storage				= getExternalFilesDir(null).getAbsolutePath() + SEPAR;
		utilities.delete(storage + DUMP_FILE);

		File folder_file = new File(storage + "groups");
		if(!folder_file.exists())
			folder_file.mkdir();

		activity_context	= this;
		ALL					= getString(R.string.all_group);
		FEEDS				= getString(R.string.feeds_title);
		SETTINGS			= getString(R.string.settings_title);
		MANAGE				= getString(R.string.manage_title);
		NAVIGATION			= getString(R.string.navigation_title);
		DELETE_DIALOG		= getString(R.string.delete_dialog);
		CLEAR_DIALOG		= getString(R.string.clear_dialog);
		///					"/storage/groups/all/all.txt"
		ALL_FILE			= GROUPS_DIRECTORY + ALL + SEPAR + ALL + TXT;
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
		//if(fragment_manager.getBackStackEntryAt(0).equals("BACK"))
		{
			drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			drawer_toggle.setDrawerIndicatorEnabled(true);
			action_bar.setDisplayHomeAsUpEnabled(true);
		}
	}

	public static class refresh_manage_feeds extends AsyncTask<Void, String[], Void>
	{
		private final Animation animFadeIn = AnimationUtils.loadAnimation(activity_context, android.R.anim.fade_in);
		private final ListView listview;

		public refresh_manage_feeds()
		{
			listview = fragment_manage_feed.feed_list;
			if(feed_list_adapter.getCount() == 0)
				listview.setVisibility(View.INVISIBLE);
		}

		@Override
		protected Void doInBackground(Void... hey)
		{
			if(feed_list_adapter != null)
			{
				final String[][] content	= utilities.read_csv_to_array(storage + GROUPS_DIRECTORY + current_groups.get(0) + SEPAR + current_groups.get(0) + TXT, 'n', 'u', 'g');
				final int size				= content[0].length;
				String[] info_array			= new String[size];
				for(int i = 0; i < size; i++)
					info_array[i] = content[1][i] + NL + content[2][i] + " • " + Integer.toString(utilities.count_lines(storage + GROUPS_DIRECTORY + content[2][i] + SEPAR + content[0][i] + SEPAR + content[0][i] + CONTENT_APPENDIX)) + " items";
				publishProgress(content[0], info_array);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String[][] progress)
		{
			feed_list_adapter.set_items(progress[0], progress[1]);
			feed_list_adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Void tun)
		{
			listview.setAnimation(animFadeIn);
			listview.setVisibility(View.VISIBLE);
		}
	}

	public static class refresh_manage_groups extends AsyncTask<Void, String[], Void>
	{
		final Animation animFadeIn = AnimationUtils.loadAnimation(activity_context, android.R.anim.fade_in);
		private final ListView listview;

		public refresh_manage_groups()
		{
			listview = fragment_manage_group.manage_list;
			if(group_list_adapter.getCount() == 0)
				listview.setVisibility(View.INVISIBLE);

		}

		@Override
		protected Void doInBackground(Void... nothing)
		{
			String[][] content		= utilities.create_info_arrays(current_groups, current_groups.size(), storage);
			publishProgress(content[1], content[0]);
			return null;
		}

		@Override
		protected void onProgressUpdate(String[][] progress)
		{
			if(group_list_adapter != null)
			{
				group_list_adapter.set_items(progress[0], progress[1]);
				group_list_adapter.notifyDataSetChanged();
			}
		}

		@Override
		protected void onPostExecute(Void nothing)
		{
			listview.setAnimation(animFadeIn);
			listview.setVisibility(View.VISIBLE);
		}
	}

	protected void onStop()
	{
		super.onStop();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if(pref.getBoolean("refresh", false))
		{
			Intent intent = new Intent(this, service_update.class);
			intent.putExtra("GROUP_NUMBER", 0);
			intent.putExtra("NOTIFICATIONS", pref.getBoolean("notifications", false));
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			final long interval = (long) times[pref.getInt("refresh_time", 20)/5]*60000;
			AlarmManager alarm_refresh = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_refresh.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pend_intent);
		}
		utilities.save_positions(fragment_manager, viewpager, storage);

		utilities.write_collection_to_file(storage + NEW_ITEMS, new_items);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if(pref.getBoolean("refresh", false))
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
		storage = this.getExternalFilesDir(null).getAbsolutePath() + SEPAR;
		current_groups = utilities.read_file_to_list(storage + GROUP_LIST);
		if(new_items.size() != current_groups.size())
		{
			new_items.clear();
			for(String string : current_groups)
				new_items.add(true);
		}
	}

	private static boolean check_service_running()
	{
		ActivityManager manager = (ActivityManager) activity_context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
			if(service_update.class.getName().equals(service.service.getClassName()))
				return true;
		return false;
	}

	private void switch_page(String page_title, int position)
	{
		drawer_layout.closeDrawer(navigation_list);
		if(!current_title.equals(page_title))
		{
			fragment_manager.beginTransaction()
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
						.hide(fragment_manager.findFragmentByTag(current_title))
						.show(fragment_manager.findFragmentByTag(page_title))
						.commit();

			navigation_list.setItemChecked(position, true);
			if(position < 3)
				set_title(page_title);
			else
				set_title(FEEDS);
		}
		current_title = page_title;
	}

	private void set_title(String title)
	{
		current_title = title;
		getActionBar().setTitle(title);
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
			View feed_view = inflater.inflate(R.layout.feed_fragment, container, false);

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
							new refresh_page(position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						else if(new_items.get(position))
							new refresh_page(position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

			set_refresh(check_service_running());
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			if(drawer_toggle.onOptionsItemSelected(item))
				return true;
			else if(item.getTitle().equals("add"))
			{
				add_edit_feeds.show_add_feed_dialog(current_groups, activity_context);
				return true;
			}
			else if(item.getTitle().equals("refresh"))
			{
				set_refresh(true);
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity_context);
				utilities.save_positions(fragment_manager, viewpager, storage);
				final int page_number = viewpager.getCurrentItem();
				final Intent intent = new Intent(activity_context, service_update.class);
				intent.putExtra("GROUP_NUMBER", page_number);
				intent.putExtra("NOTIFICATIONS", pref.getBoolean("notifications", false));
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
				new refresh_page(page_number).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			}

			return super.onOptionsItemSelected(item);
		}
	}

	public static class fragment_offline extends WebViewFragment
	{
		/*@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.offline_content_layout, container, false);
		}*/
	}

	public static class fragment_preferences extends PreferenceFragment
	{

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setRetainInstance(false);
			setHasOptionsMenu(true);
			addPreferencesFromResource(R.layout.preferences);
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(Color.WHITE);
			return view;
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
		{
			optionsMenu = menu;
			optionsMenu.clear();

			super.onCreateOptionsMenu(optionsMenu, inflater);
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
			View manage_view = inflater.inflate(R.layout.manage_pager, container, false);

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

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			/*if(drawer_toggle.onOptionsItemSelected(item))
				return true;
			else if(item.getTitle().equals("add"))
			{
				utilities.show_add_feed_dialog(current_groups, activity_context);
				return true;
			}*/
			return super.onOptionsItemSelected(item);
		}
	}

	public static class fragment_manage_filters extends Fragment
	{
		public static ListView filter_list;

		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View view = inflater.inflate(R.layout.manage_filters, container, false);
			filter_list = (ListView) view.findViewById(R.id.filter_listview);
			filter_list_adapter = new adapter_manage_filter(getActivity());
			filter_list.setAdapter(filter_list_adapter);

			filter_list_adapter.set_items(utilities.read_file_to_list(storage + FILTER_LIST));

			filter_list.setOnItemLongClickListener
			(
				new OnItemLongClickListener()
				{
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(activity_context);
						builder.setCancelable(true)
						.setPositiveButton(DELETE_DIALOG, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int id)
							{
								utilities.remove_string_from_file(storage + FILTER_LIST, filter_list_adapter.getItem(position), false);
								filter_list_adapter.remove_item(position);
								filter_list_adapter.notifyDataSetChanged();
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
						return true;
					}
				}
			);
			return view;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			if(drawer_toggle.onOptionsItemSelected(item))
				return true;
			else if(item.getTitle().equals("add"))
			{
				add_edit_feeds.show_add_filter_dialog(activity_context, storage);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	public static class fragment_manage_group extends Fragment
	{
		public static ListView manage_list;

		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View view = inflater.inflate(R.layout.manage_fragment, container, false);
			manage_list = (ListView) view.findViewById(R.id.group_listview);
			group_list_adapter = new adapter_manage_groups(getActivity());
			manage_list.setAdapter(group_list_adapter);
			new refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			manage_list.setOnItemLongClickListener
			(
				new OnItemLongClickListener()
				{
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
					{
						if(position == 0)
							return false;
						AlertDialog.Builder builder = new AlertDialog.Builder(activity_context);
						builder.setCancelable(true)
						.setPositiveButton(DELETE_DIALOG, new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								group_list_adapter.remove_item(position);
								utilities.delete_group(storage, current_groups.get(position));
								group_list_adapter.notifyDataSetChanged();
							}
						});
						AlertDialog alert = builder.create();
						alert.show();
						return true;
					}
				}
			);
			return view;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			if(drawer_toggle.onOptionsItemSelected(item))
				return true;
			else if(item.getTitle().equals("add"))
			{
				add_edit_feeds.show_add_feed_dialog(current_groups, activity_context);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	public static class fragment_manage_feed extends Fragment
	{
		public static ListView feed_list;

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
			setRetainInstance(false);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View view = inflater.inflate(R.layout.manage_feeds, container, false);
			feed_list = (ListView) view.findViewById(R.id.feeds_listview);
			feed_list.setOnItemClickListener
			(
				new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						add_edit_feeds.show_edit_feed_dialog(current_groups, activity_context, storage, position);
					}
				}
			);

			feed_list_adapter = new adapter_manage_feeds(getActivity());
			feed_list.setAdapter(feed_list_adapter);

			new refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			feed_list.setOnItemLongClickListener
			(
				new OnItemLongClickListener()
				{
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id)
					{
						new AlertDialog.Builder(activity_context)
						.setCancelable(true)
						.setNegativeButton
						(
							DELETE_DIALOG,
							new DialogInterface.OnClickListener()
							{
								/// Delete the feed.
								@Override
								public void onClick(DialogInterface dialog, int id)
								{
									String group = feed_list_adapter.get_info(pos);
									group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
									final String name = feed_list_adapter.getItem(pos);

									final String group_file		= storage + GROUPS_DIRECTORY + group + SEPAR + group + TXT;
									final String group_prepend	= storage + GROUPS_DIRECTORY + group + SEPAR + group;
									final String all_file		= storage + GROUPS_DIRECTORY + ALL + SEPAR + ALL;

									utilities.delete_directory(new File(storage + GROUPS_DIRECTORY + group + SEPAR + name));
									utilities.remove_string_from_file(group_file, name, true);
									utilities.remove_string_from_file(all_file + TXT, name, true);

									utilities.delete_if_empty(group_file);
									if(!(new File(group_file).exists()))
									{
										utilities.delete_directory(new File(storage + GROUPS_DIRECTORY + group));
										utilities.remove_string_from_file(storage + GROUP_LIST, group, false);
										//getFragmentManager().beginTransaction().remove(((ListFragment) fragment_manager
										//		.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(pos))));
										new_items.set(pos, true);
									}
									else
									{
										utilities.sort_group_content_by_time(storage, group);
										utilities.delete_if_empty(group_prepend + CONTENT_APPENDIX);
										utilities.delete_if_empty(group_prepend + COUNT_APPENDIX);
										if((new File(group_prepend + CONTENT_APPENDIX)).exists())
											new_items.set(pos, true);
									}

									List<String> all_groups = utilities.read_file_to_list(storage + GROUP_LIST);
									if(all_groups.size() == 1)
									{
										utilities.delete_directory(new File(storage + GROUPS_DIRECTORY + ALL));
										new_items.set(0, true);
									}
									else if(all_groups.size() != 0)
									{
										utilities.sort_group_content_by_time(storage, ALL);
										utilities.delete_if_empty(all_file + CONTENT_APPENDIX);
										utilities.delete_if_empty(all_file + COUNT_APPENDIX);
										if((new File(all_file + CONTENT_APPENDIX)).exists())
											new_items.set(0, true);
									}

									update_groups();
									feed_list_adapter.remove_item(pos);
									feed_list_adapter.notifyDataSetChanged();
									new refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
								}
							}
						)
						.setPositiveButton
						(
							CLEAR_DIALOG,
							new DialogInterface.OnClickListener()
							{
								/// Delete the cache.
								@Override
								public void onClick(DialogInterface dialog, int id)
								{
									String group			= feed_list_adapter.get_info(pos);
									group					= group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
									final String name		= feed_list_adapter.getItem(pos);
									final String path		= storage + GROUPS_DIRECTORY + group + SEPAR + name;
									final File feed_folder	= new File(path);
									utilities.delete_directory(feed_folder);
									/// make the image and thumnail folders.
									(new File(path + SEPAR + IMAGE_DIRECTORY))		.mkdir();
									(new File(path + SEPAR + THUMBNAIL_DIRECTORY))	.mkdir();

									/// Delete the all content files.
									(new File(storage + GROUPS_DIRECTORY + ALL + SEPAR + ALL + CONTENT_APPENDIX)).delete();
									(new File(storage + GROUPS_DIRECTORY + ALL + SEPAR + ALL + COUNT_APPENDIX)).delete();

									//feed_list_adapter.notifyDataSetChanged();
									/// Refresh pages and update groups and stuff
									update_groups();
									new refresh_manage_feeds()	.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
									new refresh_manage_groups()	.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
									new_items.set(0, true);
									new_items.set(pos, true);

								}
							}
						).show();
						return true;
					}
				}
			);
			return view;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			if(drawer_toggle.onOptionsItemSelected(item))
				return true;
			else if(item.getTitle().equals("add"))
			{
				add_edit_feeds.show_add_feed_dialog(current_groups, activity_context);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		///end
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
			if(position < current_groups.size())
			{
				fragment_card f = new fragment_card();
				Bundle args = new Bundle();
				args.putInt("num", position);
				f.setArguments(args);
				return f;
			}
			return null;
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
			adapter_feeds_cards adapter = new adapter_feeds_cards(getActivity());
			setRetainInstance(false);
			setListAdapter(adapter);
			String group = current_groups.get(getArguments().getInt("num", 0));
			final List<String> count_list = utilities.read_file_to_list(storage + GROUPS_DIRECTORY + group + SEPAR + group + CONTENT_APPENDIX);
			final int sized = count_list.size();
			int i;

			for(i = sized - 1; i >= 0; i--)
			{
				if(count_list.get(i).substring(0, 9).equals("marker|1|"))
					break;
			}
			adapter.set_latest_item(i);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main_dummy, container, false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return drawer_toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	private static void set_refresh(final boolean mode)
	{
		if(optionsMenu != null)
		{
			final MenuItem refreshItem = optionsMenu.findItem(R.id.refresh);
			if(refreshItem != null)
			{
				if (mode)
					refreshItem.setActionView(R.layout.progress_circle);
				else
					refreshItem.setActionView(null);
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

	private static class refresh_page extends AsyncTask<Void, Object, Void>
	{
		int marker_position = -1, refresh_count = 0;
		final int page_number;
		Boolean markerer, waited = true;
		Animation animFadeIn;
		ListFragment l;
		adapter_feeds_cards ith;
		ListView lv;
		List<Integer> counts;

		public refresh_page(int page)
		{
			page_number = page;
		}

		@Override
		protected Void doInBackground(Void[] hey)
		{
			if(new_items.get(page_number))
			{
				while(check_service_running())
				{
					try{
						Thread.sleep(100);
					}
					catch(Exception e){
					}
				}
			}

			String group					= current_groups.get(page_number);
			final String group_path			= storage + GROUPS_DIRECTORY + group + SEPAR;
			final String group_file_path 	= group_path + group + TXT;
			final String group_content_path = group_path + group + CONTENT_APPENDIX;
			String thumbnail_path;

			if((!utilities.exists(group_file_path))||(!utilities.exists(group_content_path)))
				return null;

			String[][] contenter		= utilities.load_csv_to_array(group_content_path);
			String[] marker				= contenter[0];
			String[] titles				= contenter[1];
			String[] descriptions		= contenter[2];
			String[] links				= contenter[3];
			String[] images				= contenter[4];
			String[] widths				= contenter[5];
			String[] heights			= contenter[6];
			String[] groups				= contenter[7];
			String[] sources			= contenter[8];

			if((links[0] == null)||(links.length == 0)||(links[0].isEmpty()))
				return null;

			Set<String> existing_items = new HashSet<String>();
			try
			{
				existing_items = new HashSet<String>(utilities.get_adapter_feeds_cards(fragment_manager, viewpager, page_number).return_links());
			}
			catch(Exception e)
			{
			}

			animFadeIn = AnimationUtils.loadAnimation(activity_context, android.R.anim.fade_in);
			final int size = titles.length;
			final List<Boolean> new_markers		= new ArrayList<Boolean>();
			final List<String> new_titles		= new ArrayList<String>();
			final List<String> new_descriptions = new ArrayList<String>();
			final List<String> new_links		= new ArrayList<String>();
			final List<String> new_images		= new ArrayList<String>();
			final List<Integer> new_widths		= new ArrayList<Integer>();
			final List<Integer> new_heights		= new ArrayList<Integer>();

			int width, height;

			for(int m = 0; m < size; m++)
			{
				markerer = false;
				/// It should stop at the latest one unless there is not a newest one. So stay at 0 until it finds one.
				if(marker[m] != null)
				{
					markerer = true;
					marker_position = 0;
				}
				else if(marker_position != -1)
					marker_position++;

				if(existing_items.add(links[m]))
				{
					thumbnail_path = "";
					width = 0;
					height = 0;

					if(images[m] != null)
					{
						width = Integer.parseInt(widths[m]);
						if(width > 32)
						{
							height = Integer.parseInt(heights[m]);
							thumbnail_path = storage + GROUPS_DIRECTORY + groups[m] + SEPAR + sources[m] + SEPAR + THUMBNAIL_DIRECTORY + images[m].substring(images[m].lastIndexOf(SEPAR) + 1, images[m].length());
						}
						else
							width = 0;
					}

					if((titles[m] != null)&&(descriptions[m] != null))
					{
						if((descriptions[m].contains(titles[m]))||(whitespace.matcher(descriptions[m]).replaceAll("").length() < 8))
							descriptions[m] = "";
					}
					else if((descriptions[m] == null)||(whitespace.matcher(descriptions[m]).replaceAll("").length() < 8))
						descriptions[m] = "";
					if(titles[m] == null)
						titles[m] = "";

					new_markers		.add(markerer);
					new_titles		.add(titles[m]);
					new_links		.add(links[m]);
					new_descriptions.add(descriptions[m]);
					new_images		.add(thumbnail_path);
					new_heights		.add(height);
					new_widths		.add(width);
					refresh_count++;
				}
			}
			new_items.set(page_number, false);

			while(lv == null)
			{
				try{
					Thread.sleep(5);
				}
				catch(Exception e){
				}
				if((viewpager != null)&&(l == null))
					l = (ListFragment) fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(page_number));
				if((l != null)&&(ith == null))
					ith = ((adapter_feeds_cards) l.getListAdapter());
				if((l != null)&&(lv == null))
				{
					try
					{
						lv = l.getListView();
					}
					catch(IllegalStateException e)
					{
						lv = null;
					}
				}
				counts = get_unread_counts();
			}

			publishProgress(new_titles, new_descriptions, new_links, new_images, new_heights, new_widths, new_markers);

			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] progress)
		{
			/*int index = lv.getFirstVisiblePosition() + 1;
			View v = lv.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			if(top == 0)
				index++;
			else if (top < 0 && lv.getChildAt(1) != null)
			{
				index++;
				v = lv.getChildAt(1);
				top = v.getTop();
			}*/

			if(waited)
			{
				lv.setVisibility(View.INVISIBLE);
				waited = false;
			}

			ith.add_list((List<String>) progress[0], (List<String>) progress[1], (List<String>) progress[2], (List<String>) progress[3], (List<Integer>) progress[4], (List<Integer>) progress[5], (List<Boolean>) progress[6]);
			ith.notifyDataSetChanged();

			//lv.setSelectionFromTop(index, top - twelve);
		}

		@Override
		protected void onPostExecute(Void tun)
		{
			if(lv == null)
				return;
			if(marker_position != -1)
				lv.setSelection(marker_position);
				/// If no "marker|" s were found in the new content fall through to else,
			else
				/// refresh_count is how many new items are added.
				lv.setSelection(refresh_count);
				/// Should work.

			set_refresh(check_service_running());

			update_navigation_data(counts, false);

			lv.setAnimation(animFadeIn);
			lv.setVisibility(View.VISIBLE);
		}
	}

	public static void update_navigation_data(List<Integer> counts, Boolean update_names)
	{
		if(counts == null)
			counts = get_unread_counts();

		if(update_names)
		{
			List<String> nav = new ArrayList<String>();
			nav.addAll(Arrays.asList(FEEDS, MANAGE, SETTINGS, "Groups"));
			nav.addAll(current_groups);
			nav_adapter.add_list(nav);
		}
		nav_adapter.add_count(counts);
		nav_adapter.notifyDataSetChanged();
	}

	public static List<Integer> get_unread_counts()
	{
		List<Integer> unread_list = new ArrayList<Integer>();
		int total = 0;
		adapter_feeds_cards ith = null;
		fragment_card fc;
		final int size = current_groups.size();

		for(int j = 1; j < size; j++)
		{
			try
			{
				fc = (fragment_card) (fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(j)));
				ith = (adapter_feeds_cards) fc.getListAdapter();
			}
			catch(Exception e){
			}

			if(ith == null)
				unread_list.add(0);
			else
			{
				int most;
				String count_file = storage + GROUPS_DIRECTORY + current_groups.get(j) + SEPAR + current_groups.get(j) + COUNT_APPENDIX;
				if(utilities.exists(count_file))
					most = Integer.parseInt(utilities.read_file_to_list(count_file).get(0));
				else
					most = utilities.count_lines(storage + GROUPS_DIRECTORY + current_groups.get(j) + SEPAR + current_groups.get(j) + CONTENT_APPENDIX);
				unread_list.add(most - ith.return_unread_item_count() - 1);
			}
		}

		for(Integer un : unread_list)
			total += un;
		unread_list.add(0, total);

		return unread_list;
	}
}
