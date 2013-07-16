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

import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.widget.DrawerLayout;

import android.view.Display;
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
import java.util.Date;
import java.util.Locale;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Color;
import android.os.Debug;
import android.text.format.Time;

public class main_view extends Activity
{
	private DrawerLayout drawer_layout;

	private static ListView navigation_list;
	private ActionBarDrawerToggle drawer_toggle;
	private Menu optionsMenu;

	private static float density;

	private static Resources res;
	private static int positionrr, poser, twelve, check_finished, width, group_pos;
	private static List<Boolean> new_items;
	private String mTitle, feed_title, current_group, current_title;
	private static String storage;
	private static Context context, activity_context;
	private static ViewPager viewpager;

	private static List<String> current_groups, feed_titles, feed_urls, feed_groups;

	private static feed_adapter feed_list_adapter;

	private static group_adapter group_list_adapter;
	private static drawer_adapter nav_adapter;

	private static final int CONTENT_VIEW_ID = 10101010;
	private static final int[] times = new int[]{15, 30, 45, 60, 120, 180, 240, 300, 360, 400, 480, 540, 600, 660, 720, 960, 1440, 2880, 10080, 43829};
	private String feeds_string, manage_string, settings_string, navigation_string;
	private static String all_string;
	private static FragmentManager fragment_manager;
	private static final SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null)
		{
			feeds_string = getString(R.string.feeds_title);
			manage_string = getString(R.string.manage_title);
			settings_string = getString(R.string.settings_title);
			navigation_string = getString(R.string.navigation_title);
			all_string = getString(R.string.all_group);

			getActionBar().setTitle(feeds_string);
			FrameLayout frame = new FrameLayout(this);
			frame.setId(CONTENT_VIEW_ID);
			setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			storage = this.getExternalFilesDir(null).getAbsolutePath() + "/";
			if(!storage.equals(""))
			{
				delete(storage + "storage_location.txt");
				append_string_to_file(storage + "storage_location.txt", storage);
			}

			delete(storage + "dump.txt");

			String[] folders = {"images", "thumbnails", "groups", "content"};
			File folder_file;

			for(String folder : folders)
			{
				folder_file = new File(storage + folder);
				if(!folder_file.exists())
					folder_file.mkdir();
			}

			getActionBar().setIcon(R.drawable.rss_icon);

			getFragmentManager().beginTransaction()
						.add(CONTENT_VIEW_ID, new fragment_top())
						.commit();
			Fragment feed = new fragment_feed();
			Fragment pref = new fragment_preferences();
			Fragment man = new fragment_manage();
			getFragmentManager().beginTransaction()
				.add(R.id.content_frame, feed, feeds_string)
				.add(R.id.content_frame, pref, settings_string)
				.add(R.id.content_frame, man, manage_string)
				.hide(man)
				.hide(pref)
				.commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		if (savedInstanceState == null)
		{
			context = getApplicationContext();
			activity_context = this;

			navigation_list = (ListView) findViewById(R.id.left_drawer);
			navigation_list.setOnItemClickListener(new DrawerItemClickListener());

			nav_adapter = new drawer_adapter(get_context());
			navigation_list.setAdapter(nav_adapter);

			update_groups();

			viewpager = (ViewPager) findViewById(R.id.pager);
			viewpager.setAdapter(new viewpager_adapter(getFragmentManager()));
			viewpager.setOffscreenPageLimit(128);
			viewpager.setOnPageChangeListener(new page_listener());

			/// TODO: check to see if needed.
			List< List<String> > content 	= read_csv_to_list(new String[]{storage + "groups/"+ all_string + ".txt", "name", "url", "group"});
			feed_titles 					= content.get(0);
			feed_urls 						= content.get(1);
			feed_groups 					= content.get(2);

			ViewPager manage_pager = (ViewPager) findViewById(R.id.manage_viewpager);
			manage_pager.setAdapter(new manage_pager_adapter(getFragmentManager()));

			PagerTabStrip pager_tab_strip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
			pager_tab_strip.setDrawFullUnderline(true);
			pager_tab_strip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

			PagerTabStrip manage_strip = (PagerTabStrip) findViewById(R.id.manage_title_strip);
			manage_strip.setDrawFullUnderline(true);
			manage_strip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

			mTitle = feeds_string;
			drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
			drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
			{
				@Override
				public void onDrawerClosed(View view){
					getActionBar().setTitle(mTitle);
				}

				@Override
				public void onDrawerOpened(View drawerView){
					getActionBar().setTitle(navigation_string);
				}
			};

			drawer_layout.setDrawerListener(drawer_toggle);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
			//set_refresh(check_service_running());

			/// Save the width for compression
			if(!exists(storage + "width.txt"))
			{
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				width = (int) (Math.round(((size.x)*0.944)));
				delete(storage + "width.txt");
				append_string_to_file(storage + "width.txt", Integer.toString(width) + "\n");
			}
			else
				width = Integer.parseInt(read_file_to_list(storage + "width.txt").get(0));

			if(count_lines(storage + "groups/" + all_string + ".txt") > 0)
				new refresh_page(0).execute();

			drawer_toggle.syncState();
			density = getResources().getDisplayMetrics().density;
			twelve = (int) ((12 * density + 0.5f));
			res = getResources();
			fragment_manager = getFragmentManager();
		}
	}

	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		append_string_to_file(storage + "groups/" + feed_group + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|\n");
		append_string_to_file(storage + "groups/" + all_string + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|\n");

		update_feeds_list();
		update_manage_feeds();
		update_manage_groups();
	}

	public static int get_width(){
		return width;
	}

	private void update_manage_feeds()
	{
		if(feed_list_adapter != null)
		{
			feed_list_adapter.clear_list();
			final int size = feed_titles.size();
			for(int i = 0; i < size; i++)
				feed_list_adapter.add_list(feed_titles.get(i), feed_urls.get(i) + "\n" + feed_groups.get(i) + " • " + Integer.toString(count_lines(storage + "content/" + feed_titles.get(i) + ".store.txt.content.txt") - 1) + " items");
			feed_list_adapter.notifyDataSetChanged();
		}
	}

	private void update_manage_groups()
	{
		fragment_group grp_frag;
		group_adapter manage_adapter = null;
		try
		{
			grp_frag = (fragment_group) getFragmentManager().findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.manage_viewpager)).getId() + ":0");
			manage_adapter = (group_adapter)((fragment_group) getFragmentManager().findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.manage_viewpager)).getId() + ":0")).return_listview().getAdapter();
		}
		catch(Exception e){
		}
		if(manage_adapter != null)
		{
			String group, info;
			int content_size, number, j;
			List<String> content;
			manage_adapter.clear_list();

			final int size = current_groups.size();
			for(int i = 0; i < size; i++)
			{
				group = current_groups.get(i);
				content = read_csv_to_list(new String[]{storage + "groups/" + group + ".txt", "name"}).get(0);
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

					info += (content_size > 3) ? ", ..." : content.get(number - 1);
				}

				manage_adapter.add_list(group, Integer.toString(content_size) + " feeds • " + info);
			}
			manage_adapter.notifyDataSetChanged();
		}
	}

	private void edit_feed(String old_name, String new_name, String new_url, String old_group, String new_group)
	{
		/// Delete the feed info from the all group and add the new group info to the end of the all content file.
		remove_string_from_file(storage + "groups/" + all_string + ".txt", old_name, true);
		append_string_to_file(storage + "groups/" + all_string + ".txt", "name|" +  new_name + "|url|" + new_url + "|group|" + new_group + "|\n");

		/// If we have renamed the title, rename the content/title.txt file.
		if(!old_name.equals(new_name))
			(new File(storage + "content/" + old_name + ".store.txt.content.txt"))
			.renameTo((new File(storage + "content/" + new_name + ".store.txt.content.txt")));

		/// If we moved to a new group, delete the old cache file, force a refresh, and refresh the new one.
		if(!old_group.equals(new_group))
		{
			/// Remove the line from the old group file containing the old_feed_name and add to the new group file.
			remove_string_from_file(storage + "groups/" + old_group + ".txt", old_name, true);
			append_string_to_file(storage + "groups/" + new_group + ".txt", "name|" +  new_name + "|url|" + new_url + "|\n");

			/// If the above group file no longer exists because there are no lines left, remove the group from the group list.
			if(!exists("groups/" + old_group + ".txt"))
				remove_string_from_file(storage + "groups/group_list.txt", old_group, false);
		}
		/// The group is the same but the titles and urls may have changed.
		else
		{
			remove_string_from_file(storage + "groups/" + old_group + ".txt", old_name, true);
			append_string_to_file(storage + "groups/" + old_group + ".txt", "name|" +  new_name + "|url|" + new_url + "|\n");
		}

		feed_adapter temp = feed_list_adapter;

		update_feeds_list();
		/// Add the new feeds to the feed_adapter (Manage/Feeds).
		temp.remove_item(poser);
		temp.add_list_pos(poser, new_name, new_url + "\n" + new_group + " • " + Integer.toString(count_lines(storage + "content/" + new_name + ".store.txt.content.txt") - 1) + " items");
		temp.notifyDataSetChanged();

		update_groups();
		update_manage_feeds();
		update_manage_groups();

		sort_group_content_by_time(all_string);
		if(exists("groups/" + old_group + ".txt"))
			sort_group_content_by_time(old_group);
		if(exists("groups/" + new_group + ".txt"))
			sort_group_content_by_time(new_group);
	}

	private void update_feeds_list()
	{
		List< List<String> > content 	= read_csv_to_list(new String[]{storage + "groups/" + all_string + ".txt", "name", "url", "group"});
		feed_titles 		= content.get(0);
		feed_urls 			= content.get(1);
		feed_groups 		= content.get(2);
	}

	private void add_group(String group_name)
	{
		append_string_to_file(storage + "groups/group_list.txt", group_name + "\n");
		update_groups();
	}

	protected void onStop()
	{
		super.onStop();
		if((PreferenceManager.getDefaultSharedPreferences(this)).getBoolean("refresh", false))
		{
			Intent intent = new Intent(this, service_update.class);
			intent.putExtra("GROUP_NUMBER", "0");
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			long interval = (long) times[((int)(PreferenceManager.getDefaultSharedPreferences(this)).getInt("refresh_time", 20)/5)]*60000;
			AlarmManager alarm_refresh = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_refresh.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pend_intent);
		}
		save_positions();

		/// Save the new_items array to file
		delete(storage + "new_items.txt");
		for(Boolean state : new_items)
			append_string_to_file(storage + "new_items.txt", Boolean.toString(state) + "\n");
	}

	private void save_positions()
	{
		card_adapter adapter;
		BufferedWriter out;
		String url, group;
		List<String> feeds, lines;
		Boolean found_url = false;
		final int size = current_groups.size();

		for(int i = 1; i < size; i++)
		{
			try
			{
				group = current_groups.get(i);
				adapter = (card_adapter)((fragment_card) getFragmentManager().findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.pager)).getId() + ":" + Integer.toString(i))).getListView().getAdapter();
				if(adapter.getCount() > 0)
				{
					/// Read each of the content files from the group and find the line with the url.
					feeds = read_csv_to_list(new String[]{storage + "groups/" + group + ".txt", "name"}).get(0);
					found_url = false;
					url = adapter.return_latest_url();
					if(!url.equals(""))
					{
						for(String feed: feeds)
						{
							lines = read_file_to_list(storage + "content/" + feed + ".store.txt.content.txt");
							delete(storage + "content/" + feed + ".store.txt.content.txt");

							out = new BufferedWriter(new FileWriter(storage + "content/" + feed + ".store.txt.content.txt", true));
							for(String line : lines)
							{
								if(!found_url)
								{
									if(!line.contains(url))
										out.write(line + "\n");
									else if(!line.substring(0, 9).equals("marker|1|"))
									{
										out.write("marker|1|" + line + "\n");
										found_url = true;
									}
									else
										out.write(line + "\n");
								}
								else
									out.write(line + "\n");
							}
							out.close();
							if(found_url)
								break;
						}
						sort_group_content_by_time(group);
					}
				}
			}
			catch(Exception e){
			}
		}
		if(found_url)
			sort_group_content_by_time(all_string);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		//set_refresh(check_service_running());

		if((PreferenceManager.getDefaultSharedPreferences(this)).getBoolean("refresh", false))
		{
			Intent intent = new Intent(this, service_update.class);
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			AlarmManager alarm_manager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_manager.cancel(pend_intent);
		}

		List<String> strings = read_file_to_list(storage + "new_items.txt");
		new_items = new ArrayList<Boolean>();
		for(String string : strings)
		{
			if(string.equals("true"))
				new_items.add(true);
			else if(string.equals("false"))
				new_items.add(false);
		}
		storage = this.getExternalFilesDir(null).getAbsolutePath() + "/";
		current_groups = read_file_to_list(storage + "groups/group_list.txt");
		if(new_items.size() != current_groups.size())
		{
			new_items.clear();
			for(String string : current_groups)
				new_items.add(true);
		}
	}

	public static Resources get_resources(){
		return res;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		drawer_toggle.onConfigurationChanged(newConfig);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id){
			selectItem(position);
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
			((ViewPager) findViewById(R.id.pager)).setCurrentItem(page);
		}
	}

	private boolean check_service_running()
	{
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
			if(service_update.class.getName().equals(service.service.getClassName()))
				return true;
		return false;
	}

	public static float get_pixel_density(){
		return density;
	}

	private void switch_page(String page_title, int position)
	{
		if(!mTitle.equals(page_title))
		{
			getFragmentManager().beginTransaction()
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

	private final class page_listener implements ViewPager.OnPageChangeListener
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
			if(get_card_adapter(position).getCount() == 0)
				new refresh_page(position).execute();
			else if(new_items.get(position))
			{
				new refresh_page(position).execute();
				new_items.set(position, false);
			}
		}
	}

	private class fragment_top extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.pager, container, false);
		}
	}

	private class fragment_feed extends Fragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.feed_fragment, container, false);
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
		{
			//main_view.optionsMenu = menu;
			inflater.inflate(R.menu.main_overflow, menu);
			super.onCreateOptionsMenu(menu, inflater);
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
				update_group(((ViewPager) findViewById(R.id.pager)).getCurrentItem());
				log("refresh" + Integer.toString(((ViewPager) findViewById(R.id.pager)).getCurrentItem()));
				return true;
			}

			return super.onOptionsItemSelected(item);
		}
	}

	private static class fragment_preferences extends PreferenceFragment
	{

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.layout.preferences);
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(Color.WHITE);
			return view;
		}
	}

	private class fragment_manage extends Fragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.manage_pager, container, false);
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
		{
			inflater.inflate(R.menu.manage_overflow, menu);
			super.onCreateOptionsMenu(menu, inflater);
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

	public class fragment_group extends Fragment
	{
		private ListView manage_list;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.manage_fragment, container, false);
			manage_list = (ListView) view.findViewById(R.id.group_listview);
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

		private ListView return_listview(){
			return manage_list;
		}
	}

	private class feed_manage extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.manage_feeds, container, false);
			ListView feed_list = (ListView) view.findViewById(R.id.feeds_listview);


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
					AlertDialog.Builder builder = new AlertDialog.Builder(activity_context);
					builder.setCancelable(true)
							.setNegativeButton(getString(R.string.delete_dialog), new DialogInterface.OnClickListener()
					{
						/// Delete the feed.
						public void onClick(DialogInterface dialog, int id)
						{
							String group = feed_list_adapter.get_info(positionrr);
							group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
							String name = feed_list_adapter.getItem(positionrr);
							delete(storage + group + ".image_size.cache.txt");

							remove_string_from_file(storage + "groups/" + group + ".txt", name, true);
							remove_string_from_file(storage + "groups/" + all_string + ".txt", name, true);

							/// If the group file no longer exists because it was the last feed in it, delete the group from the group_list.
							if(!exists(storage + "groups/" + group + ".txt"))
							{
								remove_string_from_file(storage + "groups/group_list.txt", group, false);
								delete(storage + "groups/" + group + ".txt");
								update_groups_non_static();
							}
							else
								sort_group_content_by_time(group);

							sort_group_content_by_time(all_string);

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
							delete(storage + "content/" + name + ".store.txt.content.txt");
							delete(storage + "groups/" + group + ".txt.content.txt");
							delete(storage + group + ".image_size.cache.txt");

							sort_group_content_by_time(all_string);

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
		public viewpager_adapter(FragmentManager fm){
			super(fm);
		}

		@Override
		public int getCount(){
			return current_groups.size();
		}

 		@Override
		public Fragment getItem(int position){
			return fragment_card.newInstance(position);
		}

		@Override
		public String getPageTitle(int position){
			return current_groups.get(position);
		}
	}

	public class manage_pager_adapter extends FragmentPagerAdapter
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
		public String getPageTitle(int position){
			if(position == 0)
				return getString(R.string.groups_manage_sub);
			else
				return getString(R.string.feeds_manage_sub);
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
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setListAdapter(new card_adapter(getActivity()));
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState){
			return inflater.inflate(R.layout.fragment_main_dummy, container, false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		optionsMenu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(drawer_toggle.onOptionsItemSelected(item))
			return true;

		return super.onOptionsItemSelected(item);
	}

	private void set_refresh(final boolean mode)
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
			else
				log("refreshItem is null");
		}
		else
			log("optionsMenu is null");
	}

	public static int return_width()
	{
		return width;
	}

	private void show_add_dialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View add_rss_dialog = inflater.inflate(R.layout.add_rss_dialog, null);

		Spinner group_spinner = (Spinner) add_rss_dialog.findViewById(R.id.group_spinner);
		List<String> spinner_groups = new ArrayList<String>();
		for(int i = 1; i < current_groups.size(); i++)
		{
			spinner_groups.add(current_groups.get(i));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.group_spinner_text, spinner_groups);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner.setAdapter(adapter);

		final AlertDialog alertDialog = new AlertDialog.Builder(this, 2)
				.setTitle("Add Feed")
				.setView(add_rss_dialog)
				.setCancelable(true)
				.setPositiveButton
				(getString(R.string.add_dialog),new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.setNegativeButton
				(getString(R.string.cancel_dialog),new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialog)
			{
				Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						String new_group = ((EditText) add_rss_dialog.findViewById(R.id.group_edit)).getText().toString().trim().toLowerCase();
						String URL_check = ((EditText) add_rss_dialog.findViewById(R.id.URL_edit)).getText().toString().trim();
						String feed_name = ((EditText) add_rss_dialog.findViewById(R.id.name_edit)).getText().toString().trim();
						String spinner_group;
						try{
							spinner_group = ((Spinner) add_rss_dialog.findViewById(R.id.group_spinner)).getSelectedItem().toString();
						}
						catch(Exception e){
							spinner_group = "Unsorted";
						}
						process_user_feed(alertDialog, new_group, URL_check, feed_name, spinner_group, "add");
					}
				});
			}
		});
		alertDialog.show();
	}

	///cock
	private void show_edit_dialog(int position)
	{
		poser = position;
		LayoutInflater inflater = LayoutInflater.from(activity_context);
		final View edit_rss_dialog = inflater.inflate(R.layout.add_rss_dialog, null);
		current_group = feed_groups.get(position);
		current_title = feed_titles.get(position);

		int current_spinner_position = 0;

		Spinner group_spinner = (Spinner) edit_rss_dialog.findViewById(R.id.group_spinner);
		List<String> spinner_groups = new ArrayList<String>();
		for(int i = 1; i < current_groups.size(); i++)
		{
			spinner_groups.add(current_groups.get(i));
			if((current_groups.get(i)).equals(current_group))
				current_spinner_position = i - 1;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity_context, R.layout.group_spinner_text, spinner_groups);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner.setAdapter(adapter);
		group_spinner.setSelection(current_spinner_position);

		((EditText)edit_rss_dialog.findViewById(R.id.name_edit)).setText(current_title);
		((EditText)edit_rss_dialog.findViewById(R.id.URL_edit)).setText(feed_urls.get(position));
		group_spinner.setSelection(current_spinner_position);

		final AlertDialog edit_dialog = new AlertDialog.Builder(activity_context, 2)
				.setTitle(getString(R.string.edit_dialog_title))
				.setView(edit_rss_dialog)
				.setCancelable(true)
				.setPositiveButton
				(getString(R.string.accept_dialog), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.setNegativeButton
				(getString(R.string.cancel_dialog),new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.create();
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

						process_user_feed(edit_dialog, new_group, URL_check, feed_name, spinner_group, "edit");
					}
				});
			}
		});
		edit_dialog.show();
	}

	private void process_user_feed(AlertDialog edit_dialog, String new_group, String URL_check, String feed_name, String spinner_group, String mode)
	{
		boolean found = false, new_group_mode = false;
		if(new_group.length()>0)
		{
			new_group_mode = true;
			for(String group : current_groups)
			{
				if((group.toLowerCase()).equals(new_group))
					found = true;
			}

			String[] words = new_group.split("\\s");
			new_group = "";

			if(words.length == 1)
			{
				char cap = Character.toUpperCase(words[0].charAt(0));
				new_group +=  cap + words[0].substring(1, words[0].length());
			}
			else
			{
				for(int i = 0; i < words.length - 1; i++)
				{
					char cap = Character.toUpperCase(words[i].charAt(0));
					new_group +=  cap + words[i].substring(1, words[i].length()) + " ";
				}
				char cap = Character.toUpperCase(words[words.length - 1].charAt(0));
				new_group +=  cap + words[words.length - 1].substring(1, words[words.length - 1].length());
			}
		}
		else
			new_group = spinner_group;

		Boolean rss = false;

		check_finished = -1;
		if((!URL_check.contains("http"))&&(!URL_check.contains("https")))
		{
			new check_feed_exists().execute("http://" + URL_check);
			while(check_finished == -1){
			}
			if(check_finished == 0)
			{
				check_finished = -1;
				new check_feed_exists().execute("https://" + URL_check);
				while(check_finished == -1){
				}
				if(check_finished == 1)
					URL_check = "https://" + URL_check;
			}
			else if(check_finished == 1)
				URL_check = "http://" + URL_check;
		}
		else
		{
			new check_feed_exists().execute(URL_check);
			while(check_finished == -1){
			}
		}
		if(check_finished == 1)
			rss = true;

		if(rss!= null && !rss)
			toast_message("Invalid RSS URL", false);
		else
		{
			if((!found)&&(new_group_mode))
				add_group(new_group);

			if(feed_name.equals(""))
				feed_name = feed_title;

			feed_name = feed_name.replace("/","")
								.replace("\\", "")
								.replace("?", "")
								.replace("%", "")
								.replace("*", "")
								.replace("|", "")
								.replace("<", "")
								.replace(">", "")
								.replace(":", "");
			if(mode.equals("edit"))
				edit_feed(current_title, feed_name, URL_check, current_group, new_group);
			else
				add_feed(feed_name, URL_check, new_group);
			edit_dialog.dismiss();
		}
	}

	private static void toast_message(String message, final Boolean short_long)
	{
		Toast message_toast;
		if(short_long)
			message_toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		else
			message_toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		message_toast.show();
	}

	private class check_feed_exists extends AsyncTask<String, Void, Integer>
	{
		@Override
		protected Integer doInBackground(String... urls)
		{
			try
			{
				BufferedInputStream in;
				in = new BufferedInputStream((new URL(urls[0])).openStream());
				byte data[] = new byte[512], data2[], next[];
				in.read(data, 0, 512);
				int length;

				String line = new String(data);
				if((line.contains("rss"))||((line.contains("Atom"))||(line.contains("atom"))))
				{
					while((!line.contains("<title"))&&(!line.contains("</title>")))
					{
						length = data.length;
						data2 = new byte[512];
						in.read(data2, 0, 512);

						data = concat_byte_arrays(data, data2);
						line = new String(data);
					}
					int ind = line.indexOf(">", line.indexOf("<title")) + 1;
					feed_title = line.substring(ind, line.indexOf("</", ind));
					check_finished = 1;
				}
				else
					check_finished = 0;
			}
			catch(Exception e){
				check_finished = 0;
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer end){
		}
	}

	private byte[] concat_byte_arrays(byte[] a, byte[] b)
	{
		final int a_length = a.length;
		final int b_length = b.length;
		byte[] c = new byte[a_length + b_length];
		System.arraycopy(a, 0, c, 0, a_length);
		System.arraycopy(b, 0, c, a_length, b_length);
		return c;
	}

	public static void download_file(String urler, String file_path)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				BufferedInputStream in = null;
				FileOutputStream fout = null;
				try
				{
					in = new BufferedInputStream(new URL(urler).openStream());
					fout = new FileOutputStream(file_path);

					byte data[] = new byte[1024];
					int count;
					while ((count = in.read(data, 0, 1024)) != -1)
						fout.write(data, 0, count);
				}
				finally
				{
					if (in != null)
						in.close();
					if (fout != null)
						fout.close();
				}
			}
			catch(Exception e){
			}
		}
	}

	public static void append_string_to_file(String file_path, String string)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(file_path, true));
			out.write(string);
			out.close();
		}
		catch (Exception e)
		{
		}
	}

	private void remove_string_from_file(String file_path, String string, Boolean contains)
	{
		final List<String> list = read_file_to_list(file_path);
		delete(file_path);
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(file_path, true));
			for(String item : list)
			{
				if(contains)
				{
					if(!item.contains(string))
						out.write(item + "\n");
				}
				else
				{
					if(!item.equals(string))
						out.write(item + "\n");
				}
			}
			out.close();
		}
		catch(Exception e){
		}
	}

	private static void update_groups()
	{
		final int previous_size = current_groups.size();
		current_groups = read_file_to_list(storage + "groups/group_list.txt");
		final int size = current_groups.size();
		if(size == 0)
		{
			append_string_to_file(storage + "groups/group_list.txt", all_string + "\n");
			current_groups.add(all_string);
		}

		new_items.clear();
		for(int i = 0; i < size; i++)
			new_items.add(false);

		List<String> nav = new ArrayList<String>();
		nav.addAll(current_groups);
		nav.addAll(0, Arrays.asList("Feeds", "Manage", "Settings", "Groups"));

		nav_adapter.add_list(nav);
		nav_adapter.add_count(get_unread_counts());
		nav_adapter.notifyDataSetChanged();

		if(viewpager != null)
			viewpager.getAdapter().notifyDataSetChanged();
	}

	private void update_groups_non_static()
	{
		final int previous_size = current_groups.size();
		current_groups = read_file_to_list(storage + "groups/group_list.txt");
		final int size = current_groups.size();
		if(size == 0)
		{
			append_string_to_file(storage + "groups/group_list.txt", all_string + "\n");
			current_groups.add(all_string);
		}

		new_items.clear();
		for(int i = 0; i < size; i++)
			new_items.add(false);

		List<String> nav = new ArrayList<String>();
		nav.addAll(current_groups);
		nav.addAll(0, Arrays.asList("Feeds", "Manage", "Settings", "Groups")); ///changed this shit

		nav_adapter.add_list(nav);
		nav_adapter.add_count(get_unread_counts());
		nav_adapter.notifyDataSetChanged();

		if(previous_size != size)
			viewpager.setAdapter(new viewpager_adapter(getFragmentManager()));
		if(viewpager != null)
			viewpager.getAdapter().notifyDataSetChanged();
	}

	public static void update_group_order(List<String> new_order)
	{
		delete(storage + "groups/group_list.txt");
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

	private static Context get_context()
	{
		return context;
	}

	public static List< List<String> > read_csv_to_list(String[] type)
	{
		final String feed_path = type[0];
		final int number_of_items = type.length - 1;
		int content_start, content_index, i, bar_index, content_length;
		String line, content;

		List< List<String> > types = new ArrayList< List<String> >();
		for(i = 0; i < number_of_items; i++)
			types.add(new ArrayList< String >());

		try
		{
			BufferedReader stream = new BufferedReader(new FileReader(feed_path));
			while((line = stream.readLine()) != null)
			{
				for(i = 0; i < number_of_items; i++)
				{
					content = type[1 + i] + "|";
					content_length = content.length();
					content_index = line.indexOf(content);
					content_start = content_index + content_length;
					bar_index = line.indexOf('|', content_start);

					if((content_index == -1)||(content_start + 1 == bar_index))
						types.get(i).add("");
					else
						types.get(i).add(line.substring(content_start, bar_index));
				}
			}
		}
		catch(IOException e){
		}
		return types;
	}

	public static List<String> read_file_to_list(String file_path)
	{
		String line;
		BufferedReader stream;
		List<String> lines = new ArrayList<String>();
		try
		{
			stream = new BufferedReader(new FileReader(file_path));
			while((line = stream.readLine()) != null)
				lines.add(line);
			stream.close();
		}
		catch(IOException e){
		}
		return lines;
	}

	private static int count_lines(String file_path)
	{
		BufferedReader stream;
		int i = 0;
		try
		{
			stream = new BufferedReader(new FileReader(file_path));
			while(stream.readLine() != null)
				i++;
			stream.close();
		}
		catch(IOException e){
		}
		return i;
	}

	public static drawer_adapter return_nav_adapter(){
		return nav_adapter;
	}

	public static List<Integer> get_unread_counts()
	{
		List<Integer> unread_list = new ArrayList<Integer>();
		List<String> count_list;
		int count, sized, i, total = 0;
		card_adapter ith = null;
		fragment_card fc = null;

		final int size = current_groups.size();
		for(int j = 1; j < size; j++)
		{
			try{
				fc = (fragment_card) (fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(j)));
				ith = (card_adapter) fc.getListAdapter();
			}
			catch(Exception e){
			}
			/// TODO: Or if no items are in the list.
			if((ith == null)||(ith.getCount() == 0)||(ith.return_unread_item_count() == -1))
			{
				count = 0;
				count_list = read_file_to_list(storage + "groups/" + current_groups.get(j) + ".txt.content.txt");
				sized = count_list.size();

				for(i = sized - 1; i >= 0; i--)
				{
					if(count_list.get(i).substring(0, 9).equals("marker|1|"))
						break;
				}
				unread_list.add(sized - i - 1);
			}
			else
				unread_list.add(ith.return_unread_item_count());
		}

		for(Integer un : unread_list)
			total += un;
		unread_list.add(0, total);

		return unread_list;
	}

	private void update_group(int page_number)
	{
		save_positions();
		set_refresh(true);
		Intent intent = new Intent(this, service_update.class);
		intent.putExtra("GROUP_NUMBER", Integer.toString(page_number));
		startService(intent);
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
		new refresh_page(page_number).execute();
	}

	private class refresh_page extends AsyncTask<Void, Object, Long>
	{
		int marker_position = -1, ssize, refresh_count = 0, page_number;
		ListFragment l;
		Boolean first;
		final Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);

		public refresh_page(int page){
			page_number = page;
			first = true;
		}

		@Override
		protected void onPreExecute(){
			Debug.startMethodTracing("refresh");
		}

		@Override
		protected Long doInBackground(Void... hey)
		{
			while(check_service_running())
			{
				try{
					Thread.sleep(100);
				}
				catch(Exception e){
				}
			}

			String group 					= current_groups.get(page_number);
			final String group_file_path 	= storage + "groups/" + group + ".txt";
			final String group_content_path = group_file_path + ".content.txt";
			String image_name, thumbnail_path;

			/// If the group has no feeds  or  the content file does not exist, end.
			if((!exists(group_file_path))||(!exists(group_content_path)))
				return 0L;

			List< List<String> > contenter = read_csv_to_list(new String[]{group_content_path, "marker", "title", "image", "description", "link"});
			List<String> marker			= contenter.get(0);
			List<String> titles 		= contenter.get(1);
			List<String> images 		= contenter.get(2);
			List<String> descriptions 	= contenter.get(3);
			List<String> links			= contenter.get(4);

			if(links.get(0).length() < 1)
				return 0L;

			/// Get a set of all the pages items' urls.
			Set<String> existing_items = new HashSet<String>();
			try{
				existing_items = new HashSet<String>(get_card_adapter(page_number).return_links());
			}
			catch(Exception e){
				return 0L;
			}

			/// load the image_dimensions to a list.
			List<String> dimensions = read_file_to_list(storage + group + ".image_size.cache.txt");

			/// For each line of the group_content_file
			final int size = titles.size();
			ssize = size;
			String image_path;
			for(int m=0; m<size; m++)
			{
				thumbnail_path = "";
				Integer[] dim = {0, 0};

				if(!images.get(m).equals(""))
				{
					image_name = images.get(m).substring(images.get(m).lastIndexOf("/") + 1, images.get(m).length());
					thumbnail_path = storage + "thumbnails/" + image_name;

					if((!exists(thumbnail_path))&&(!check_service_running()))
					{
						image_path = storage + "images/" + image_name;
						if(!exists(image_path))
							download_file(images.get(m), image_path);
						dimensions.add(compress_file(storage, image_name, group, false));
					}

					dim = get_image_dimensions(dimensions, image_name);
				}

				// Checks to see if page has this item.
				if(existing_items.add(links.get(m)))
					publishProgress(page_number, titles.get(m), descriptions.get(m), links.get(m), thumbnail_path, dim[1], dim[0], marker.get(m));
			}
			return 0L;
		}

		@Override
		protected void onProgressUpdate(Object... progress)
		{
			if(l == null)
				l = (fragment_card) getFragmentManager().findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.pager)).getId() + ":" + Integer.toString((Integer) progress[0]));
			if(l != null)
			{
				card_adapter ith = ((card_adapter) l.getListAdapter());
				ListView lv = l.getListView();
				if(first)
				{
					if(refresh_count == 0)
						l.getListView().setVisibility(View.INVISIBLE);
					first = false;
				}
				Boolean marker = false;
				/// It should stop at the latest one unless there is not a newest one. So stay at 0 until it finds one.
				if(((String) progress[7]).equals("1"))
				{
					marker = true;
					marker_position = 0;
				}
				if(marker_position != -1)
					marker_position++;

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

				ith.add_list((String) progress[1], (String) progress[2], (String) progress[3], (String) progress[4], (Integer) progress[5], (Integer) progress[6], marker);
				ith.notifyDataSetChanged();
				refresh_count++;

				//lv.setSelectionFromTop(index, top - twelve);
				if(marker_position != -1)
				{
					if((refresh_count == ssize)&&(marker_position == 1))
						lv.setSelection(0);
					else
						lv.setSelection(marker_position);
				}
				else
					lv.setSelection(refresh_count);
			}
		}

		@Override
		protected void onPostExecute(Long tun)
		{
			if(l != null)
			{
				ListView lv = l.getListView();
				lv.setAnimation(animFadeIn);
				lv.setVisibility(View.VISIBLE);
			}
			//if(viewPager.getOffscreenPageLimit() > 1)
				//viewPager.setOffscreenPageLimit(1);
			set_refresh(false);
			Debug.stopMethodTracing();
		}
	}

	private card_adapter get_card_adapter(int page_index)
	{
		return ((card_adapter)((fragment_card) getFragmentManager()
						.findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.pager)).getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	/*public static void set_new_items_array(int position, Boolean value)
	{
		new_items_array[position] = value;
	}*/

	public static void sort_group_content_by_time(String group)
	{
		if(storage.equals(""))
			storage = read_file_to_list(storage + "storage_location.txt").get(0);

		final String group_path = storage + "groups/" + group + ".txt.content.txt";
		String content_path;
		Time time = new Time();
		List<String> pubDates, content;
		Map<Long, String> map = new TreeMap<Long, String>();
		int size, i;

		final List<String> feeds_array	= read_csv_to_list(new String[]{storage + "groups/" + group + ".txt", "name"}).get(0);

		for(String feed : feeds_array)
		{
			content_path = storage + "content/" + feed + ".store.txt.content.txt";
			if(exists(content_path))
			{
				content 		= read_file_to_list(content_path);
				pubDates		= read_csv_to_list(new String[]{content_path, "published"}).get(0);

				if(pubDates.get(0).length() < 8)
					pubDates 	= read_csv_to_list(new String[]{content_path, "pubDate"}).get(0);

				size = pubDates.size();
				for(i = 0; i < size; i++)
				{
					try{
						time.parse3339(pubDates.get(i));
					}
					catch(Exception e){
						main_view.log("BUG : Meant to be 3339 but looks like: " + pubDates.get(i));
						return;
					}

					map.put(time.toMillis(false), content.get(i));
				}
			}
		}

		delete(group_path);
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(group_path, true));
			for(Map.Entry<Long, String> entry : map.entrySet())
				out.write(entry.getValue() + "\n");
			out.close();
		}
		catch(Exception e){
		}
	}

	public static void log(String text)
	{
		append_string_to_file(storage + "dump.txt", text + "\n");
	}

	public static void delete(String file_path)
	{
		(new File(file_path)).delete();
	}

	public static Boolean exists(String file_path)
	{
		return (new File(file_path)).exists();
	}

	public static String compress_file(String path, String image_name, String group, Boolean skip_save)
	{
		int insample;
		if(!skip_save)
		{
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path + "images/" + image_name, o);

			int width_tmp = o.outWidth;
			if(width < 2)
				width = Integer.parseInt(main_view.read_file_to_list(storage + "width.txt").get(0));

			insample = (width_tmp > width) ? (Math.round((float) width_tmp / (float) width)) : 1;
		}
		else
			insample = 1;

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = insample;
		Bitmap bitmap = BitmapFactory.decodeFile(path + "images/" + image_name, o2);
		if(o2.outWidth > 9)
			append_string_to_file(path + group + ".image_size.cache.txt", image_name + "|" + o2.outWidth + "|" + o2.outHeight + "\n");

		if(!skip_save)
		{
			try
			{
				FileOutputStream out = new FileOutputStream(path + "thumbnails/" + image_name);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			}
			catch (Exception e){
			}
		}

		return image_name + "|" + o2.outWidth + "|" + o2.outHeight;
	}

	private Integer[] get_image_dimensions(List<String> dim_list, final String image_name)
	{
		Integer[] size = {0, 0};
		final int sizer = dim_list.size();
		String line;
		for(int i = 0 ; i < sizer; i++)
		{
			line = dim_list.get(i);
			if(line.contains(image_name))
			{
				int first = line.indexOf('|') + 1;
				int second = line.indexOf('|', first + 1) + 1;
				size[0] = Integer.parseInt(line.substring(first, second - 1));
				size[1] = Integer.parseInt(line.substring(second, line.length()));
				dim_list.remove(i);
				return size;
			}
		}
		return size;
	}
}
