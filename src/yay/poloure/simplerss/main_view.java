package yay.poloure.simplerss;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;

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

import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;

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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.FrameLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.net.URL;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.regex.Pattern;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;

import android.graphics.Color;
import android.os.Debug;
import android.text.format.Time;

public class main_view extends Activity
{
	private static DrawerLayout drawer_layout;

	private static ListView navigation_list;
	private static ActionBarDrawerToggle drawer_toggle;
	private static Menu optionsMenu;

	private static String current_title, feed_title;
	public static String storage;
	public static Context application_context, activity_context;
	public static ViewPager viewpager;

	private static feed_adapter feed_list_adapter;
	private static group_adapter group_list_adapter;
	private static fragment_group fragment_group_store;
	private static feed_manage fragment_feed_store;
	public static drawer_adapter nav_adapter;

	private static List<String> current_groups 			= new ArrayList<String>();
	private static List<Boolean> new_items 				= new ArrayList<Boolean>();
	private static final int[] times 					= new int[]{15, 30, 45, 60, 120, 180, 240, 300, 360, 400, 480, 540, 600, 660, 720, 960, 1440, 2880, 10080, 43829};
	private static final String[] folders 				= {"images", "thumbnails", "groups", "content"};
	private static final Pattern whitespace				= Pattern.compile("\\s+");

	private static String feeds_string, manage_string, settings_string, navigation_string, all_string;

	public static FragmentManager fragment_manager;
	private static SharedPreferences pref;
	private static LayoutInflater inf;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pager);

		perform_initial_operations();

		fragment_manager = getFragmentManager();

		if(savedInstanceState == null)
		{
			Fragment feed		= new fragment_feed();
			Fragment prefs		= new fragment_preferences();
			Fragment man			= new fragment_manage();
			fragment_manager.beginTransaction()
				.add(R.id.content_frame, feed, feeds_string)
				.add(R.id.content_frame, prefs, settings_string)
				.add(R.id.content_frame, man, manage_string)
				.hide(man)
				.hide(prefs)
				.commit();
		}
		else
		{
			fragment_manager.beginTransaction()
				.show(fragment_manager.findFragmentByTag(feeds_string))
				.hide(fragment_manager.findFragmentByTag(settings_string))
				.hide(fragment_manager.findFragmentByTag(manage_string))
				.commit();
		}

		nav_adapter		= new drawer_adapter(this);
		navigation_list	= (ListView) findViewById(R.id.left_drawer);
		navigation_list.setOnItemClickListener
		(
			new ListView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView parent, View view, int position, long id)
				{
					selectItem(position);
				}
			}
		);
		navigation_list.setAdapter(nav_adapter);

		drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
		drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			@Override
			public void onDrawerClosed(View view)
			{
				getActionBar().setTitle(current_title);
			}

			@Override
			public void onDrawerOpened(View drawerView)
			{
				getActionBar().setTitle(navigation_string);
			}
		};

		drawer_layout.setDrawerListener(drawer_toggle);
		drawer_toggle.syncState();

		update_groups();

		if(utilities.exists(storage + "groups/" + all_string + ".txt"))
			new refresh_page(0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void perform_initial_operations()
	{
		storage				= getExternalFilesDir(null).getAbsolutePath() + "/";
		utilities.delete(storage + "dump.txt");

		File folder_file;
		for(String folder : folders)
		{
			folder_file = new File(storage.concat(folder));
			if(!folder_file.exists())
				folder_file.mkdir();
		}

		feeds_string		= getString(R.string.feeds_title);
		manage_string		= getString(R.string.manage_title);
		settings_string		= getString(R.string.settings_title);
		navigation_string	= getString(R.string.navigation_title);
		all_string			= getString(R.string.all_group);
		current_title				= feeds_string;
		pref				= PreferenceManager.getDefaultSharedPreferences(this);
		application_context	= getApplicationContext();
		activity_context	= this;
		inf					= getLayoutInflater();
	}

	/// This is so the icon and text in the actionbar are selected.
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		drawer_toggle.onConfigurationChanged(newConfig);
	}
	///

	public static void add_feed(String feed_name, String feed_url, String feed_group)
	{
		utilities.append_string_to_file(storage + "groups/" + feed_group + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|\n");
		utilities.append_string_to_file(storage + "groups/" + all_string + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|\n");

		if(feed_list_adapter != null)
			new refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		if(group_list_adapter != null)
			new refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private static class refresh_manage_feeds extends AsyncTask<Void, String[], Long>
	{
		final Animation animFadeIn = AnimationUtils.loadAnimation(activity_context, android.R.anim.fade_in);
		private ListView listview;

		public refresh_manage_feeds()
		{
			listview = fragment_feed_store.getListView();
			if(feed_list_adapter.getCount() == 0)
				listview.setVisibility(View.INVISIBLE);
		}

		@Override
		protected Long doInBackground(Void... hey)
		{
			if(feed_list_adapter != null)
			{
				final String[][] content 	= utilities.read_csv_to_array(storage + "groups/"+ all_string + ".txt", new char[]{'n', 'u', 'g'});
				final String[] feed_titles 	= content[0];
				final String[] feed_urls 	= content[1];
				final String[] feed_groups 	= content[2];
				final int size 				= feed_titles.length;
				String[] info_array			= new String[size];
				for(int i = 0; i < size; i++)
					info_array[i] = feed_urls[i] + "\n" + feed_groups[i] + " • " + Integer.toString(utilities.count_lines(storage + "content/" + feed_titles[i] + ".store.txt.content.txt")) + " items";
				publishProgress(feed_titles, info_array);
			}
			return 0L;
		}

		@Override
		protected void onProgressUpdate(String[]... progress)
		{
			feed_list_adapter.set_items(progress[0], progress[1]);
			feed_list_adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Long tun)
		{
			listview.setAnimation(animFadeIn);
			listview.setVisibility(View.VISIBLE);
		}
	}

	private static class refresh_manage_groups extends AsyncTask<Void, String[], Long>
	{
		final Animation animFadeIn = AnimationUtils.loadAnimation(activity_context, android.R.anim.fade_in);
		private ListView listview;

		public refresh_manage_groups()
		{
			listview = fragment_group_store.getListView();
			if(group_list_adapter.getCount() == 0)
				listview.setVisibility(View.INVISIBLE);

		}

		@Override
		protected Long doInBackground(Void... hey)
		{
			if(group_list_adapter != null)
			{
				String info, count_path;
				int number, j, total = 0;
				String[] content;

				final int size = current_groups.size();
				String[] group_array = new String[size];
				String[] info_array = new String[size];

				for(int i = 0; i < size; i++)
				{
					group_array[i] = current_groups.get(i);
					content = utilities.read_single_to_array(storage + "groups/" + group_array[i] + ".txt", "name|");
					count_path = storage + "groups/" + group_array[i] + ".txt.content.txt.count.txt";
					if(utilities.exists(count_path))
						total += Integer.parseInt(utilities.read_file_to_list(count_path).get(0));
					if(i == 0)
						info = (size == 1) ? "1 group" :  size + " groups";
					else
					{
						info = "";
						number = 3;
						if(content.length < 3)
							number = content.length;
						for(j = 0; j < number - 1; j++)
							info += content[j].concat(", ");

						if(content.length > 3)
							info += "...";
						else if(number > 0)
							info += content[number - 1];
					}
					info_array[i] = Integer.toString(content.length) + " feeds • " + info;
				}
				info_array[0] =  total + " items • " + info_array[0];
				publishProgress(group_array, info_array);
			}
			return 0L;
		}

		@Override
		protected void onProgressUpdate(String[]... progress)
		{
			group_list_adapter.set_items(progress[0], progress[1]);
			group_list_adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Long tun)
		{
			listview.setAnimation(animFadeIn);
			listview.setVisibility(View.VISIBLE);
		}
	}

	public static void edit_feed(String old_name, String new_name, String new_url, String old_group, String new_group, int position)
	{
		/// Delete the feed info from the all group and add the new group info to the end of the all content file.
		utilities.remove_string_from_file(storage + "groups/" + all_string + ".txt", old_name, true);
		utilities.append_string_to_file(storage + "groups/" + all_string + ".txt", "name|" +  new_name + "|url|" + new_url + "|group|" + new_group + "|\n");

		/// If we have renamed the title, rename the content/title.txt file.
		if(!old_name.equals(new_name))
			(new File(storage + "content/" + old_name + ".store.txt.content.txt"))
			.renameTo((new File(storage + "content/" + new_name + ".store.txt.content.txt")));

		/// If we moved to a new group, delete the old cache file, force a refresh, and refresh the new one.
		if(!old_group.equals(new_group))
		{
			/// Remove the line from the old group file containing the old_feed_name and add to the new group file.
			utilities.remove_string_from_file(storage + "groups/" + old_group + ".txt", old_name, true);
			utilities.append_string_to_file(storage + "groups/" + new_group + ".txt", "name|" +  new_name + "|url|" + new_url + "|\n");

			/// If the above group file no longer exists because there are no lines left, remove the group from the group list.
			utilities.delete_if_empty("groups/" + old_group + ".txt");
			if(!utilities.exists("groups/" + old_group + ".txt"))
				utilities.remove_string_from_file(storage + "groups/group_list.txt", old_group, false);
		}
		/// The group is the same but the titles and urls may have changed.
		else
		{
			utilities.remove_string_from_file(storage + "groups/" + old_group + ".txt", old_name, true);
			utilities.append_string_to_file(storage + "groups/" + old_group + ".txt", "name|" +  new_name + "|url|" + new_url + "|\n");
		}

		/// Add the new feeds to the feed_adapter (Manage/Feeds).
		feed_list_adapter.set_position(position, new_name, new_url + "\n" + new_group + " • " + Integer.toString(utilities.count_lines(storage + "content/" + new_name + ".store.txt.content.txt") - 1) + " items");
		feed_list_adapter.notifyDataSetChanged();

		update_groups();
		new refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		new refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		utilities.sort_group_content_by_time(storage, all_string);
		if(utilities.exists("groups/" + old_group + ".txt"))
			utilities.sort_group_content_by_time(storage, old_group);
		if(utilities.exists("groups/" + new_group + ".txt"))
			utilities.sort_group_content_by_time(storage, new_group);
	}

	public static void add_group(String group_name)
	{
		utilities.append_string_to_file(storage + "groups/group_list.txt", group_name + "\n");
		update_groups();
	}

	protected void onStop()
	{
		super.onStop();
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

		/// Save the new_items array to file
		utilities.delete(storage + "new_items.txt");
		for(Boolean state : new_items)
			utilities.append_string_to_file(storage + "new_items.txt", Boolean.toString(state) + "\n");
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if(pref.getBoolean("refresh", false))
		{
			Intent intent = new Intent(this, service_update.class);
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			AlarmManager alarm_manager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_manager.cancel(pend_intent);
		}

		List<String> strings = utilities.read_file_to_list(storage + "new_items.txt");
		new_items = new ArrayList<Boolean>();
		for(String string : strings)
		{
			if(string.equals("true"))
				new_items.add(true);
			else if(string.equals("false"))
				new_items.add(false);
		}
		storage = this.getExternalFilesDir(null).getAbsolutePath() + "/";
		current_groups = utilities.read_file_to_list(storage + "groups/group_list.txt");
		if(new_items.size() != current_groups.size())
		{
			new_items.clear();
			for(String string : current_groups)
				new_items.add(true);
		}
	}

	private void selectItem(int position)
	{
		switch(position)
		{
			case 0:
				switch_page(feeds_string, 0);
				break;
			case 1:
				switch_page(manage_string, 1);
				break;
			case 2:
				switch_page(settings_string, 2);
				break;
			default:
				switch_page(feeds_string, position);
				viewpager.setCurrentItem(position - 4);
				break;
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
				set_title(feeds_string);
		}
		current_title = page_title;
	}

	private void set_title(String title)
	{
		current_title = title;
		getActionBar().setTitle(title);
	}

	public static class fragment_feed extends Fragment
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
			viewpager.setAdapter(new viewpager_adapter(fragment_manager));
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
						if(utilities.get_card_adapter(fragment_manager, viewpager, position).getCount() == 0)
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
				show_add_dialog();
				return true;
			}
			else if(item.getTitle().equals("refresh"))
			{
				update_group(viewpager.getCurrentItem());
				return true;
			}

			return super.onOptionsItemSelected(item);
		}
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
			manage_pager.setAdapter(new manage_pager_adapter(fragment_manager));

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
			if(drawer_toggle.onOptionsItemSelected(item))
				return true;
			else if(item.getTitle().equals("add"))
			{
				show_add_dialog();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	public static class fragment_group extends Fragment
	{
		private static ListView manage_list;

		public static ListView getListView()
		{
			return manage_list;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View view = inflater.inflate(R.layout.manage_fragment, container, false);
			manage_list = (ListView) view.findViewById(R.id.group_listview);
			group_list_adapter = new group_adapter(getActivity());
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
						.setPositiveButton(getString(R.string.delete_dialog), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int id)
							{
								group_list_adapter.remove_item(position);
								/// delete the group
								String group = current_groups.get(position);
								utilities.delete_group(storage, group);
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
	}

	public static class feed_manage extends Fragment
	{
		private static ListView feed_list;

		public static ListView getListView()
		{
			return feed_list;
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
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
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						show_edit_dialog(position);
					}
				}
			);

			feed_list_adapter = new feed_adapter(getActivity());
			feed_list.setAdapter(feed_list_adapter);

			new refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			feed_list.setOnItemLongClickListener
			(
				new OnItemLongClickListener()
				{
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id)
					{
						AlertDialog alert = new AlertDialog.Builder(activity_context)
						.setCancelable(true)
						.setNegativeButton
						(
							getString(R.string.delete_dialog),
							new DialogInterface.OnClickListener()
							{
								/// Delete the feed.
								public void onClick(DialogInterface dialog, int id)
								{
									String group = feed_list_adapter.get_info(pos);
									group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
									final String name = feed_list_adapter.getItem(pos);

									utilities.remove_string_from_file(storage + "groups/" + group + ".txt", name, true);
									utilities.remove_string_from_file(storage + "groups/" + all_string + ".txt", name, true);

									/// If the group file no longer exists because it was the last feed in it, delete the group from the group_list.
									utilities.delete_if_empty(storage + "groups/" + group + ".txt");
									if(!utilities.exists(storage + "groups/" + group + ".txt"))
									{
										utilities.delete(storage + "groups/" + group + ".txt.content.txt");
										utilities.delete(storage + "groups/" + group + ".txt.content.txt.count.txt");
										utilities.remove_string_from_file(storage + "groups/group_list.txt", group, false);
										update_groups();
									}
									else
										utilities.sort_group_content_by_time(storage, group);

									utilities.sort_group_content_by_time(storage, all_string);

									/// remove deleted files content from groups that it was in
									feed_list_adapter.remove_item(pos);
									feed_list_adapter.notifyDataSetChanged();
									new refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
								}
							}
						)
						.setPositiveButton
						(
							getString(R.string.clear_dialog),
							new DialogInterface.OnClickListener()
							{
								/// Delete the feed.
								public void onClick(DialogInterface dialog, int id)
								{
									String group = feed_list_adapter.get_info(pos);
									group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
									String name = feed_list_adapter.getItem(pos);
									utilities.delete(storage + "content/" + name + ".store.txt.content.txt");
									utilities.delete(storage + "groups/" + group + ".txt.content.txt");
									utilities.delete(storage + group + ".image_size.cache.txt");

									new refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
									utilities.sort_group_content_by_time(storage, all_string);

									/// remove deleted files content from groups that it was in
									/// TODO: update item info
									//feed_list_adapter.notifyDataSetChanged();
								}
							}
						).show();
						return true;
					}
				}
			);
			return view;
		}
	}

	public static class viewpager_adapter extends FragmentPagerAdapter
	{
		public viewpager_adapter(FragmentManager fm)
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
			return fragment_card.newInstance(position);
		}

		@Override
		public String getPageTitle(int position)
		{
			return current_groups.get(position);
		}
	}

	public static class manage_pager_adapter extends FragmentPagerAdapter
	{
		public manage_pager_adapter(FragmentManager fm)
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
			if(position == 0)
			{
				fragment_group_store	= new fragment_group();
				return fragment_group_store;
			}
			else
			{
				fragment_feed_store		= new feed_manage();
				return fragment_feed_store;
			}
		}

		@Override
		public String getPageTitle(int position)
		{
			if(position == 0)
				return activity_context.getString(R.string.groups_manage_sub);
			else
				return activity_context.getString(R.string.feeds_manage_sub);
		}
	}

	public static class fragment_card extends ListFragment
	{
		static fragment_card newInstance(int num)
		{
			fragment_card f = new fragment_card();
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);
			return f;
		}

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			card_adapter adapter = new card_adapter(getActivity());
			setRetainInstance(false);
			setListAdapter(adapter);
			final List<String> count_list = utilities.read_file_to_list(storage + "groups/" + current_groups.get(getArguments().getInt("num", 0)) + ".txt.content.txt");
			final int sized = count_list.size();
			int i;

			for(i = sized - 1; i >= 0; i--)
			{
				if(count_list.get(i).substring(0, 9).equals("marker|1|"))
					break;
			}
			/// Min = 0, max = sized - 1 but it should be sized
			/// If I have read three, i = 2;
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

	private static void show_add_dialog()
	{
		final View add_rss_dialog = inf.inflate(R.layout.add_rss_dialog, null);
		final Spinner group_spinner = (Spinner) add_rss_dialog.findViewById(R.id.group_spinner);

		/// TODO: This is a manual array copy.
		final int size = current_groups.size();
		String[] spinner_groups = new String[size - 1];
		for(int i = 0; i < size - 1; i++)
			spinner_groups[i] = current_groups.get(i + 1);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity_context, R.layout.group_spinner_text, spinner_groups);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner.setAdapter(adapter);

		final AlertDialog alertDialog = new AlertDialog.Builder(activity_context, 2)
				.setTitle("Add Feed")
				.setView(add_rss_dialog)
				.setCancelable(true)
				.setNegativeButton
				(activity_context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.create();

				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, (activity_context.getString(R.string.add_dialog)),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						final String new_group = ((EditText) add_rss_dialog.findViewById(R.id.group_edit)).getText().toString().trim().toLowerCase();
						final String URL_check = ((EditText) add_rss_dialog.findViewById(R.id.URL_edit)).getText().toString().trim();
						final String feed_name = ((EditText) add_rss_dialog.findViewById(R.id.name_edit)).getText().toString().trim();
						String spinner_group;
						try{
							spinner_group = ((Spinner) add_rss_dialog.findViewById(R.id.group_spinner)).getSelectedItem().toString();
						}
						catch(Exception e){
							spinner_group = "Unsorted";
						}
						new utilities.check_feed_exists(alertDialog, new_group, feed_name, "add", spinner_group, "", "", 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
					}
				});
				alertDialog.show();
	}

	private static void show_edit_dialog(final int position)
	{
		final LayoutInflater inflater 		= LayoutInflater.from(activity_context);
		final View edit_rss_dialog 			= inflater.inflate(R.layout.add_rss_dialog, null);
		final String[][] content 			= utilities.read_csv_to_array(storage + "groups/"+ all_string + ".txt", new char[]{'n', 'u', 'g'});
		final String current_title			= content[0][position];
		final String current_url			= content[1][position];
		final String current_group  		= content[2][position];

		int current_spinner_position = 0;

		final Spinner group_spinner = (Spinner) edit_rss_dialog.findViewById(R.id.group_spinner);
		final List<String> spinner_groups = new ArrayList<String>();
		final int size = current_groups.size();
		for(int i = 1; i < size; i++)
		{
			spinner_groups.add(current_groups.get(i));
			if((current_groups.get(i)).equals(current_group))
				current_spinner_position = i - 1;
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity_context, R.layout.group_spinner_text, spinner_groups);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner.setAdapter(adapter);

		((EditText)edit_rss_dialog.findViewById(R.id.name_edit)).setText(current_title);
		((EditText)edit_rss_dialog.findViewById(R.id.URL_edit)).setText(current_url);
		group_spinner.setSelection(current_spinner_position);

		final AlertDialog edit_dialog = new AlertDialog.Builder(activity_context, 2)
				.setTitle(activity_context.getString(R.string.edit_dialog_title))
				.setView(edit_rss_dialog)
				.setCancelable(true)
				.setNegativeButton
				(activity_context.getString(R.string.cancel_dialog),new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.create();

				edit_dialog.setButton(edit_dialog.BUTTON_POSITIVE, (activity_context.getString(R.string.accept_dialog)),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
							String new_group 		= ((EditText) edit_rss_dialog.findViewById(R.id.group_edit)).getText().toString().trim().toLowerCase();
							String URL_check 		= ((EditText) edit_rss_dialog.findViewById(R.id.URL_edit)).getText().toString().trim();
							String feed_name 		= ((EditText) edit_rss_dialog.findViewById(R.id.name_edit)).getText().toString().trim();
							String spinner_group 	= ((Spinner) edit_rss_dialog.findViewById(R.id.group_spinner)).getSelectedItem().toString();

							new utilities.check_feed_exists(edit_dialog, new_group, feed_name, "edit", spinner_group, current_group, current_title, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
					}
				});

				edit_dialog.show();
	}

	private static void update_groups()
	{
		final int previous_size = current_groups.size();

		current_groups = utilities.read_file_to_list(storage + "groups/group_list.txt");
		final int size = current_groups.size();
		if(size == 0)
		{
			utilities.append_string_to_file(storage + "groups/group_list.txt", all_string + "\n");
			current_groups.add(all_string);
		}

		new_items.clear();
		for(String group : current_groups)
			new_items.add(false);

		/// If viewpager exists, fragment_manager != null. This must come before the nav_adapter.
		if(viewpager != null)
		{
			if(previous_size != size)
				viewpager.setAdapter(new viewpager_adapter(fragment_manager));
			else
				viewpager.getAdapter().notifyDataSetChanged();
		}

		List<String> nav = new ArrayList<String>();
		nav.addAll(Arrays.asList("Feeds", "Manage", "Settings", "Groups"));
		nav.addAll(current_groups);

		nav_adapter.add_list(nav);
		nav_adapter.add_count(utilities.get_unread_counts(fragment_manager, viewpager, storage));
		nav_adapter.notifyDataSetChanged();
	}

	public static void update_group_order(String[] new_order)
	{
		utilities.delete(storage + "groups/group_list.txt");
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(storage + "groups/group_list.txt", true));
			for(String group : new_order)
				out.write(group + "\n");
			out.close();
		}
		catch(Exception e){
		}
		update_groups();
	}

	private static void update_group(int page_number)
	{
		utilities.save_positions(fragment_manager, viewpager, storage);
		set_refresh(true);
		Intent intent = new Intent(activity_context, service_update.class);
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
		new refresh_page(page_number).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private static class refresh_page extends AsyncTask<Void, Object, Void>
	{
		int marker_position = -1, ssize, refresh_count = 0, page_number;
		Boolean markerer, waited = true;
		Animation animFadeIn;
		ListFragment l;
		card_adapter ith;
		ListView lv;
		List<String> nav;
		List<Integer> counts;

		public refresh_page(int page)
		{
			page_number = page;
		}

		@Override
		protected Void doInBackground(Void... hey)
		{
			/// while the service is running on new_items and this page is refreshing.
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
			final String group_file_path 	= storage + "groups/" + group + ".txt";
			final String group_content_path = group_file_path.concat(".content.txt");
			String thumbnail_path;

			/// If the group has no feeds  or  the content file does not exist, end.
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

			if((links[0] == null)||(links.length == 0)||(links[0].isEmpty()))
				return null;

			/// Get a set of all the pages items' urls.
			Set<String> existing_items = new HashSet<String>();
			try{
				existing_items = new HashSet<String>(utilities.get_card_adapter(fragment_manager, viewpager, page_number).return_links());
			}
			catch(Exception e){
			}

			/// For each line of the group_content_file
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
			ssize = size;
			String tag;

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
							thumbnail_path = storage.concat("thumbnails/".concat(images[m].substring(images[m].lastIndexOf("/") + 1, images[m].length())));
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

			nav = new ArrayList<String>();
			nav.addAll(Arrays.asList("Feeds", "Manage", "Settings", "Groups"));
			nav.addAll(current_groups);

			while(lv == null)
			{
				try{
					Thread.sleep(5);
				}
				catch(Exception e){
				}
				if((viewpager != null)&&(l == null))
					l = (fragment_card) fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(page_number));
				if((l != null)&&(ith == null))
					ith = ((card_adapter) l.getListAdapter());
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
				counts = utilities.get_unread_counts(fragment_manager, viewpager, storage);
			}

			publishProgress(new_titles, new_descriptions, new_links, new_images, new_heights, new_widths, new_markers);

			return null;
		}

		@Override
		protected void onProgressUpdate(Object... progress)
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

			nav_adapter.add_list(nav);
			nav_adapter.add_count(counts);
			nav_adapter.notifyDataSetChanged();

			lv.setAnimation(animFadeIn);
			lv.setVisibility(View.VISIBLE);
		}
	}
}
