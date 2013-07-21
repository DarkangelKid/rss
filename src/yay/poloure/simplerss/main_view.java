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

	private static int positionrr, poser, check_finished, group_pos;
	private static String mTitle, feed_title;
	private static String storage;
	private static Context application_context, activity_context;
	private static ViewPager viewpager;

	private static feed_adapter feed_list_adapter;
	private static group_adapter group_list_adapter;
	private static drawer_adapter nav_adapter;

	private static final Fragment feed		= new fragment_feed();
	private static final Fragment prefs		= new fragment_preferences();
	private static final Fragment man		= new fragment_manage();


	private static List<String> current_groups 			= new ArrayList<String>();
	private static List<Boolean> new_items 				= new ArrayList<Boolean>();
	private static final int[] times 					= new int[]{15, 30, 45, 60, 120, 180, 240, 300, 360, 400, 480, 540, 600, 660, 720, 960, 1440, 2880, 10080, 43829};
	private static final String[] folders 				= {"images", "thumbnails", "groups", "content"};
	private static final Pattern illegal_file_chars		= Pattern.compile("[/\\?%*|<>:]");

	private static String feeds_string, manage_string, settings_string, navigation_string;
	private static String all_string;

	private static FragmentManager fragment_manager;
	private static SharedPreferences pref;
	private static LayoutInflater inf;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//Debug.startMethodTracing("boot");

		setContentView(R.layout.pager);

		perform_initial_operations();

		fragment_manager = getFragmentManager();
		fragment_manager.beginTransaction()
			.add(R.id.content_frame, feed, feeds_string)
			.add(R.id.content_frame, prefs, settings_string)
			.add(R.id.content_frame, man, manage_string)
			.hide(man)
			.hide(prefs)
			.commit();

		nav_adapter		= new drawer_adapter(this);
		navigation_list	= (ListView) findViewById(R.id.left_drawer);
		navigation_list.setOnItemClickListener
		(
			new ListView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView parent, View view, int position, long id){
					selectItem(position);
				}
			}
		);
		navigation_list.setAdapter(nav_adapter);

		drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
		drawer_toggle	= new ActionBarDrawerToggle(this, drawer_layout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			@Override
			public void onDrawerClosed(View view)
			{
				getActionBar().setTitle(mTitle);
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
		mTitle				= feeds_string;
		pref				= PreferenceManager.getDefaultSharedPreferences(this);
		application_context	= getApplicationContext();
		activity_context	= this;
		inf					= getLayoutInflater();
	}

	/// This is so the feeds is selected too.

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		drawer_toggle.onConfigurationChanged(newConfig);
	}
	///

	private static void add_feed(String feed_name, String feed_url, String feed_group)
	{
		utilities.append_string_to_file(storage + "groups/" + feed_group + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|\n");
		utilities.append_string_to_file(storage + "groups/" + all_string + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|\n");

		update_manage_feeds();
		update_manage_groups();
	}

	private static void update_manage_feeds()
	{
		if(feed_list_adapter != null)
		{
			feed_list_adapter.clear_list();
			final List< List<String> > content 	= utilities.read_csv_to_list(new String[]{storage + "groups/"+ all_string + ".txt", "name|", "url|", "group|"});
			final List<String> feed_titles 		= content.get(0);
			final List<String> feed_urls 		= content.get(1);
			final List<String> feed_groups 		= content.get(2);
			final int size 						= feed_titles.size();
			for(int i = 0; i < size; i++)
				feed_list_adapter.add_list(feed_titles.get(i), feed_urls.get(i) + "\n" + feed_groups.get(i) + " • " + Integer.toString(utilities.count_lines(storage + "content/" + feed_titles.get(i) + ".store.txt.content.txt")) + " items");
			feed_list_adapter.notifyDataSetChanged();
		}
	}

	private static void update_manage_groups()
	{
		if(group_list_adapter != null)
		{
			String group, info;
			int content_size, number, j;
			List<String> content;
			group_list_adapter.clear_list();

			final int size = current_groups.size();
			for(int i = 0; i < size; i++)
			{
				group = current_groups.get(i);
				content = utilities.read_csv_to_list(new String[]{storage + "groups/" + group + ".txt", "name|"}).get(0);
				content_size = content.size();
				if(i == 0)
					info = (size == 1) ? "1 group" :  size + " groups";
				else
				{
					info = "";
					number = 3;
					if(content_size < 3)
						number = content_size;
					for(j = 0; j < number - 1; j++)
						info += content.get(j) + ", ";

					if(content_size > 3)
						info += "...";
					else if(number > 0)
						info += content.get(number - 1);
				}

				group_list_adapter.add_list(group, Integer.toString(content_size) + " feeds • " + info);
			}
			group_list_adapter.notifyDataSetChanged();
		}
	}

	private static void edit_feed(String old_name, String new_name, String new_url, String old_group, String new_group)
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
		feed_list_adapter.remove_item(poser);
		feed_list_adapter.add_list_pos(poser, new_name, new_url + "\n" + new_group + " • " + Integer.toString(utilities.count_lines(storage + "content/" + new_name + ".store.txt.content.txt") - 1) + " items");
		feed_list_adapter.notifyDataSetChanged();

		update_groups();
		update_manage_feeds();
		update_manage_groups();

		utilities.sort_group_content_by_time(storage, all_string);
		if(utilities.exists("groups/" + old_group + ".txt"))
			utilities.sort_group_content_by_time(storage, old_group);
		if(utilities.exists("groups/" + new_group + ".txt"))
			utilities.sort_group_content_by_time(storage, new_group);
	}

	private static void add_group(String group_name)
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
		if(position == 2)
			switch_page(settings_string, position);
		else if(position == 1)
			switch_page(manage_string, position);
		else if(position == 0)
			switch_page(feeds_string, position);
		else if(position > 3)
		{
			switch_page(feeds_string, position);
			int page = position - 4;
			viewpager.setCurrentItem(page);
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
		if(!mTitle.equals(page_title))
		{
			fragment_manager.beginTransaction()
						.setTransition(4099)
						.hide(getFragmentManager().findFragmentByTag(mTitle))
						.show(getFragmentManager().findFragmentByTag(page_title))
						.commit();

			navigation_list.setItemChecked(position, true);
			if(position < 3)
				set_title(page_title);
			else
				set_title(feeds_string);
		}
		drawer_layout.closeDrawer(navigation_list);
		mTitle = page_title;
	}

	private void set_title(String title)
	{
		mTitle = title;
		getActionBar().setTitle(title);
	}

	public static class fragment_feed extends Fragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState){
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
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View view = inflater.inflate(R.layout.manage_fragment, container, false);
			final ListView manage_list = (ListView) view.findViewById(R.id.group_listview);
			group_list_adapter = new group_adapter(getActivity());
			manage_list.setAdapter(group_list_adapter);
			update_manage_groups();
			manage_list.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
				{
					if(position == 0)
						return false;
					group_pos = position;
					AlertDialog.Builder builder = new AlertDialog.Builder(activity_context);
					builder.setCancelable(true)
							.setPositiveButton(getString(R.string.delete_dialog), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							group_list_adapter.remove_item(group_pos);
							group_list_adapter.notifyDataSetChanged();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
					return true;
				}
			});
			return view;
		}
	}

	public static class feed_manage extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final View view = inflater.inflate(R.layout.manage_feeds, container, false);
			final ListView feed_list = (ListView) view.findViewById(R.id.feeds_listview);
			feed_list.setOnItemClickListener(new OnItemClickListener()
						{
							public void onItemClick(AdapterView<?> parent, View view, int position, long id)
							{
								show_edit_dialog(position);
							}
						});

			feed_list_adapter = new feed_adapter(getActivity());
			feed_list.setAdapter(feed_list_adapter);

			update_manage_feeds();

			feed_list.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
				{
					positionrr = pos;
					final AlertDialog.Builder builder = new AlertDialog.Builder(activity_context);
					builder.setCancelable(true)
							.setNegativeButton(getString(R.string.delete_dialog), new DialogInterface.OnClickListener()
					{
						/// Delete the feed.
						public void onClick(DialogInterface dialog, int id)
						{
							String group = feed_list_adapter.get_info(positionrr);
							group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
							String name = feed_list_adapter.getItem(positionrr);
							utilities.delete(storage + group + ".image_size.cache.txt");

							utilities.remove_string_from_file(storage + "groups/" + group + ".txt", name, true);
							utilities.remove_string_from_file(storage + "groups/" + all_string + ".txt", name, true);

							/// If the group file no longer exists because it was the last feed in it, delete the group from the group_list.
							if(!utilities.exists(storage + "groups/" + group + ".txt"))
							{
								utilities.remove_string_from_file(storage + "groups/group_list.txt", group, false);
								utilities.delete(storage + "groups/" + group + ".txt");
								update_groups();
							}
							else
								utilities.sort_group_content_by_time(storage, group);

							utilities.sort_group_content_by_time(storage, all_string);

							/// remove deleted files content from groups that it was in
							feed_list_adapter.remove_item(positionrr);
							feed_list_adapter.notifyDataSetChanged();
							update_manage_groups();
						}
					})
					.setPositiveButton(getString(R.string.clear_dialog), new DialogInterface.OnClickListener()
					{
						/// Delete the feed.
						public void onClick(DialogInterface dialog, int id)
						{
							String group = feed_list_adapter.get_info(positionrr);
							group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
							String name = feed_list_adapter.getItem(positionrr);
							utilities.delete(storage + "content/" + name + ".store.txt.content.txt");
							utilities.delete(storage + "groups/" + group + ".txt.content.txt");
							utilities.delete(storage + group + ".image_size.cache.txt");

							update_manage_feeds();
							utilities.sort_group_content_by_time(storage, all_string);

							/// remove deleted files content from groups that it was in
							/// TODO: update item info
							//feed_list_adapter.notifyDataSetChanged();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
					return true;
				}
			});
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
		public manage_pager_adapter(FragmentManager fm){
			super(fm);
		}

		@Override
		public int getCount(){
			return 2;
		}
		@Override
		public Fragment getItem(int position){
			if(position == 0)
				return new fragment_group();
			else
				return new feed_manage();
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
						new check_feed_exists(alertDialog, new_group, feed_name, "add", spinner_group, "", "").execute(URL_check);
					}
				});
				alertDialog.show();
	}

	private static void show_edit_dialog(final int position)
	{
		final LayoutInflater inflater 		= LayoutInflater.from(activity_context);
		final View edit_rss_dialog 			= inflater.inflate(R.layout.add_rss_dialog, null);
		final List< List<String> > content 	= utilities.read_csv_to_list(new String[]{storage + "groups/"+ all_string + ".txt", "name|", "url|", "group|"});
		final String current_title			= content.get(0).get(position);
		final String current_url			= content.get(1).get(position);
		final String current_group  		= content.get(2).get(position);

		int current_spinner_position = 0;

		final Spinner group_spinner = (Spinner) edit_rss_dialog.findViewById(R.id.group_spinner);
		List<String> spinner_groups = new ArrayList<String>();
		for(int i = 1; i < current_groups.size(); i++)
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
				.setPositiveButton
				(activity_context.getString(R.string.accept_dialog), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.setNegativeButton
				(activity_context.getString(R.string.cancel_dialog),new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.show();
		edit_dialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialog)
			{
				Button b = edit_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						String new_group 		= ((EditText) edit_rss_dialog.findViewById(R.id.group_edit)).getText().toString().trim().toLowerCase();
						String URL_check 		= ((EditText) edit_rss_dialog.findViewById(R.id.URL_edit)).getText().toString().trim();
						String feed_name 		= ((EditText) edit_rss_dialog.findViewById(R.id.name_edit)).getText().toString().trim();
						String spinner_group 	= ((Spinner) edit_rss_dialog.findViewById(R.id.group_spinner)).getSelectedItem().toString();

						new check_feed_exists(edit_dialog, new_group, feed_name, "edit", spinner_group, current_group, current_title).execute(URL_check);
					}
				});
			}
		});
	}

	private static class check_feed_exists extends AsyncTask<String, Void, Integer>
	{
		private Boolean existing_group = false, real = false;
		AlertDialog dialog;
		String group, name, mode, url, feed_title, spinner_group, current_group, current_title;
		Button button;

		public check_feed_exists(AlertDialog edit_dialog, String new_group, String feed_name, String moder, String spin_group, String current_tit, String current_grop)
		{
			dialog			= edit_dialog;
			group			= new_group;
			name			= feed_name;
			mode			= moder;
			spinner_group	= spin_group;
			current_group	= current_grop;
			current_title	= current_tit;
			button			= dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			button.setEnabled(false);
		}

		@Override
		protected Integer doInBackground(String... urler)
		{
			/// If the group entry has text, check to see if it is an old group or if it is new.
			if(group.length()>0)
			{
				for(String gro : current_groups)
				{
					if((gro.toLowerCase()).equals(group.toLowerCase()))
					{
						group = gro;
						existing_group = true;
					}
				}
				if(!existing_group)
				{
					String[] words = group.split(" ");
					group = "";

					for(String word: words)
						group += (word.substring(0, 1).toUpperCase()).concat(word.substring(1).toLowerCase()) + " ";
					group = group.substring(0, group.length() - 1);
				}

			}
			else
				group = spinner_group;

			List<String> check_list = new ArrayList<String>();
			if(!urler[0].contains("http"))
			{
				check_list.add("http://" + urler[0]);
				check_list.add("https://" + urler[0]);
			}
			else
				check_list.add(urler[0]);

			try
			{
				for(String check : check_list)
				{
					final BufferedInputStream in = new BufferedInputStream((new URL(check)).openStream());
					byte data[] = new byte[512], data2[];
					in.read(data, 0, 512);

					String line = new String(data);
					if((line.contains("rss"))||((line.contains("Atom"))||(line.contains("atom"))))
					{
						while((!line.contains("<title"))&&(!line.contains("</title>")))
						{
							data2 = new byte[512];
							in.read(data2, 0, 512);

							data = utilities.concat_byte_arrays(data, data2);
							line = new String(data);
						}
						final int ind = line.indexOf(">", line.indexOf("<title")) + 1;
						feed_title = line.substring(ind, line.indexOf("</", ind));
						real = true;
						url = check;
						break;
					}
				}
			}
			catch(Exception e){
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer end)
		{
			if(!real)
			{
				utilities.toast_message(activity_context, "Invalid RSS URL", false);
				button.setEnabled(true);
			}
			else
			{
				if(!existing_group)
					add_group(group);
				if(name.isEmpty())
					name = feed_title;

				name = illegal_file_chars.matcher(name).replaceAll("");

				if(mode.equals("edit"))
					/// current title and group are pulled from the air.
					edit_feed(current_title, name, url, current_group, group);
				else
					add_feed(name, url, group);

				dialog.dismiss();
			}
		}
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

	public static void update_group_order(List<String> new_order)
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

	public static drawer_adapter get_nav_adapter()
	{
		return nav_adapter;
	}

	public static FragmentManager get_fragment_manager()
	{
		return fragment_manager;
	}

	public static ViewPager get_viewpager()
	{
		return viewpager;
	}

	public static String get_storage()
	{
		return storage;
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

	private static class refresh_page extends AsyncTask<Void, Object, Long>
	{
		int marker_position = -1, ssize, refresh_count = 0, page_number;
		Boolean markerer, waited = true;
		final Animation animFadeIn = AnimationUtils.loadAnimation(activity_context, android.R.anim.fade_in);
		ListFragment l;
		card_adapter ith;
		ListView lv;

		public refresh_page(int page)
		{
			//Debug.startMethodTracing("refresh2");
			page_number = page;
		}

		@Override
		protected void onPreExecute()
		{
		}

		@Override
		protected Long doInBackground(Void... hey)
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
			final String group_content_path = group_file_path + ".content.txt";
			String thumbnail_path;

			/// If the group has no feeds  or  the content file does not exist, end.
			if((!utilities.exists(group_file_path))||(!utilities.exists(group_content_path)))
				return 0L;

			List< List<String> > contenter 	= utilities.read_csv_to_list(new String[]{group_content_path, "marker|", "title|", "description|", "link|" , "image|", "width|", "height|"});
			List<String> marker				= contenter.get(0);
			List<String> titles				= contenter.get(1);
			List<String> descriptions		= contenter.get(2);
			List<String> links				= contenter.get(3);
			List<String> images				= contenter.get(4);
			List<String> widths				= contenter.get(5);
			List<String> heights			= contenter.get(6);

			if((links.size() == 0)||(links.get(0).isEmpty()))
				return 0L;

			/// Get a set of all the pages items' urls.
			Set<String> existing_items = new HashSet<String>();
			try{
				existing_items = new HashSet<String>(utilities.get_card_adapter(fragment_manager, viewpager, page_number).return_links());
			}
			catch(Exception e){
			}

			/// For each line of the group_content_file
			final int size = titles.size();
			int width, height;
			ssize = size;
			String image;
			String tag;

			while(lv == null)
			{
				try{
					Thread.sleep(50);
				}
				catch(Exception e){
				}
				if((viewpager != null)&&(l == null))
					l = (fragment_card) fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(page_number));
				if((l != null)&&(ith == null))
					ith = ((card_adapter) l.getListAdapter());
				if((l != null)&&(lv == null))
					lv = l.getListView();
			}

			for(int m = 0; m < size; m++)
			{
				thumbnail_path = "";
				width = 0;
				height = 0;
				image = images.get(m);

				if(!image.isEmpty())
				{
					width = Integer.parseInt(widths.get(m));
					if(width > 32)
					{
						height = Integer.parseInt(heights.get(m));
						thumbnail_path = storage + "thumbnails/" + image.substring(image.lastIndexOf("/") + 1, image.length());
					}
					else
						width = 0;
				}

				markerer = false;
				/// It should stop at the latest one unless there is not a newest one. So stay at 0 until it finds one.
				if(marker.get(m).equals("1"))
				{
					markerer = true;
					marker_position = 0;
				}
				else if(marker_position != -1)
					marker_position++;

				// Checks to see if page has this item.
				if(existing_items.add(links.get(m)))
					publishProgress(titles.get(m), descriptions.get(m), links.get(m), thumbnail_path, height, width, markerer);
			}
			new_items.set(page_number, false);
			return 0L;
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

			ith.add_list((String) progress[0], (String) progress[1], (String) progress[2], (String) progress[3], (Integer) progress[4], (Integer) progress[5], (Boolean) progress[6]);
			ith.notifyDataSetChanged();
			refresh_count++;

			//lv.setSelectionFromTop(index, top - twelve);
			if(marker_position != -1)
					lv.setSelection(marker_position);
			else
				lv.setSelection(refresh_count);
		}

		@Override
		protected void onPostExecute(Long tun)
		{
			if(lv == null)
				return;
			lv.setAnimation(animFadeIn);
			lv.setVisibility(View.VISIBLE);
			set_refresh(check_service_running());

			List<String> nav = new ArrayList<String>();
			nav.addAll(Arrays.asList("Feeds", "Manage", "Settings", "Groups"));
			nav.addAll(current_groups);
			nav_adapter.add_list(nav);
			nav_adapter.add_count(utilities.get_unread_counts(fragment_manager, viewpager, storage));
			nav_adapter.notifyDataSetChanged();
			//if(viewPager.getOffscreenPageLimit() > 1)
				//viewPager.setOffscreenPageLimit(1);
			//Debug.stopMethodTracing();
		}
	}
}
