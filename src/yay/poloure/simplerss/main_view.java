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

public class main_view extends Activity
{
	private DrawerLayout drawer_layout;

	private static ListView navigation_list;
	private ActionBarDrawerToggle drawer_toggle;
	private Menu optionsMenu;

	private static float density;

	private static Resources res;
	private static int positionrr, poser, twelve, check_finished, width, group_pos;
	private Boolean new_items = false, refreshing = false;
	private String mTitle, feed_title;
	private static String storage;
	private static Context context, activity_context;
	private static ViewPager viewpager;

	private static List<String> current_groups, feed_titles, feed_urls, feed_groups;

	private static feed_adapter feed_list_adapter;

	private static group_adapter group_list_adapter;

	private static final int CONTENT_VIEW_ID = 10101010;
	private static final int[] times = new int[]{15, 30, 45, 60, 120, 180, 240, 300, 360, 400, 480, 540, 600, 660, 720, 960, 1440, 2880, 10080, 43829};


	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		append_string_to_file("groups/" + feed_group + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|\n");
		append_string_to_file("groups/All.txt", "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|\n");
		update_feeds_list();
		update_manage();
	}

	private void update_manage()
	{
		update_manage_feeds();
		update_manage_groups();
	}

	private void update_manage_feeds()
	{
		if(feed_list_adapter != null)
		{
			feed_list_adapter.clear_list();
			final int size = feed_titles.size();
			for(int i = 0; i < size; i++)
				feed_list_adapter.add_list(feed_titles.get(i), feed_urls.get(i) + "\n" + feed_groups.get(i) + " • " + Integer.toString(count_lines("content/" + feed_titles.get(i) + ".store.txt.content.txt") - 1) + " items");
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
			manage_adapter.clear_list();
			final int size = current_groups.size();
			for(int i = 0; i < size; i++)
			{
				List<String> content = read_csv_to_list(new String[]{storage + "groups/" + current_groups.get(i) + ".txt", "0", "name"}).get(0);
				String info = "";
				int number = 3;
				if(content.size() < 3)
					number = content.size();
				for(int j = 0; j < number - 1; j++)
					info = info + content.get(j) + ", ";
				if(content.size() > 0)
					info = info + content.get(number - 1);
				if(content.size() > 3)
					info = info + ", ...";
				if(i == 0)
				{
					if(size == 1)
						info = "1 group";
					else
						info = current_groups.size() + " groups";
				}
				manage_adapter.add_list(current_groups.get(i), Integer.toString(content.size()) + " feeds • " + info);
			}
			manage_adapter.notifyDataSetChanged();
		}
	}

	private void edit_feed(String old_name, String new_name, String new_url, String old_group, String new_group)
	{
		/// Delete the feed info from the all group and add the new group info to the end of the all content file.
		remove_string_from_file("groups/All.txt", old_name, true);
		append_string_to_file("groups/All.txt", "name|" +  new_name + "|url|" + new_url + "|group|" + new_group + "|\n");

		/// If we have renamed the title, rename the content/title.txt file.
		if(!old_name.equals(new_name))
			(new File(storage + "content/" + old_name + ".store.txt.content.txt"))
			.renameTo((new File(storage + "content/" + new_name + ".store.txt.content.txt")));

		/// If we moved to a new group, delete the old cache file, force a refresh, and refresh the new one.
		if(!old_group.equals(new_group))
		{
			/// Remove the line from the old group file containing the old_feed_name and add to the new group file.
			remove_string_from_file("groups/" + old_group + ".txt", old_name, true);
			append_string_to_file("groups/" + new_group + ".txt", "name|" +  new_name + "|url|" + new_url + "|\n");

			/// If the above group file no longer exists because there are no lines left, remove the group from the group list.
			if(!exists("groups/" + old_group + ".txt"))
				remove_string_from_file("groups/group_list.txt", old_group, false);
		}
		/// The group is the same but the titles and urls may have changed.
		else
		{
			remove_string_from_file("groups/" + old_group + ".txt", old_name, true);
			append_string_to_file("groups/" + old_group + ".txt", "name|" +  new_name + "|url|" + new_url + "|\n");
		}

		feed_adapter temp = feed_list_adapter;

		update_feeds_list();
		/// Add the new feeds to the feed_adapter (Manage/Feeds).
		temp.remove_item(poser);
		temp.add_list_pos(poser, new_name, new_url + "\n" + new_group + " • " + Integer.toString(count_lines("content/" + new_name + ".store.txt.content.txt") - 1) + " items");
		temp.notifyDataSetChanged();

		update_groups();
		update_manage();

		sort_group_content_by_time("All");
		if(exists("groups/" + old_group + ".txt"))
			sort_group_content_by_time(old_group);
		if(exists("groups/" + new_group + ".txt"))
			sort_group_content_by_time(new_group);
	}

	private void update_feeds_list()
	{
		List< List<String> > content 	= read_csv_to_list(new String[]{storage + "groups/All.txt", "0", "name", "url", "group"});
		feed_titles 		= content.get(0);
		feed_urls 			= content.get(1);
		feed_groups 		= content.get(2);
	}

	private void add_group(String group_name)
	{
		append_string_to_file("groups/group_list.txt", group_name + "\n");
		update_groups();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null)
		{
			getActionBar().setTitle("Feeds");
			FrameLayout frame = new FrameLayout(this);
			frame.setId(CONTENT_VIEW_ID);
			setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			storage = this.getExternalFilesDir(null).getAbsolutePath() + "/";

			delete("dump.txt");

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
				.add(R.id.content_frame, feed, "Feeds")
				.add(R.id.content_frame, pref, "Settings")
				.add(R.id.content_frame, man, "Manage")
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

			update_groups();

			viewpager = (ViewPager) findViewById(R.id.pager);
			viewpager.setAdapter(new viewpager_adapter(getFragmentManager()));
			viewpager.setOffscreenPageLimit(128);
			viewpager.setOnPageChangeListener(new page_listener());

			/// TODO: check to see if needed.
			List< List<String> > content 	= read_csv_to_list(new String[]{storage + "groups/All.txt", "0", "name", "url", "group"});
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

			mTitle = "Feeds";
			drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
			drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
			{
				public void onDrawerClosed(View view){
					getActionBar().setTitle(mTitle);
				}

				public void onDrawerOpened(View drawerView){
					getActionBar().setTitle("Navigation");
				}
			};

			drawer_layout.setDrawerListener(drawer_toggle);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);

			/// Save the width for compression
			if(!exists(storage + "width.txt"))
			{
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				width = (int) Math.round(((float)size.x)*0.80);
				append_string_to_file("width.txt", Integer.toString(width) + "\n");
			}
			else
				width = Integer.parseInt(read_file_to_list("width.txt", 0).get(0));

			if(read_file_to_list("groups/All.txt", 0).size()>0)
				new refresh_page().execute(0);

			drawer_toggle.syncState();
			density = getResources().getDisplayMetrics().density;
			twelve = (int) ((12 * density + 0.5f));
			res = getResources();
		}
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
			log(Long.toString(interval));
			AlarmManager alarm_refresh = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_refresh.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, pend_intent);
		}
		/// Save the positions.
		for(int i = 0; i < current_groups.size(); i++)
		{
			String group = current_groups.get(i);
			try
			{
				card_adapter adapter = (card_adapter)((fragment_card) getFragmentManager().findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.pager)).getId() + ":" + Integer.toString(i))).getListView().getAdapter();
				if(adapter.getCount() > 0)
				{
					List<String> lines = read_file_to_list("groups/" + group + ".txt.content.txt", 0);
					delete("groups/" + group + ".txt.content.txt");
					BufferedWriter out = new BufferedWriter(new FileWriter(storage + "groups/" + group + ".txt.content.txt", true));
					String url = adapter.return_latest_url();
					for(String line : lines)
					{
						if((url.equals("")||(!line.contains(url))))
							out.write(line + "\n");
						else if(!line.contains("marker|1|"))
							out.write(line + "marker|1|\n");
						else
							out.write(line + "\n");
					}
					out.close();
				}
			}
			catch(Exception e){
			}
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if(!refreshing)
			set_refresh(check_service_running());
		if((PreferenceManager.getDefaultSharedPreferences(this)).getBoolean("refresh", false))
		{
			Intent intent = new Intent(this, service_update.class);
			PendingIntent pend_intent = PendingIntent.getService(this, 0, intent, 0);
			AlarmManager alarm_manager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
			alarm_manager.cancel(pend_intent);
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
			switch_page("Settings", position);
		else if(position == 1)
			switch_page("Manage", position);
		else if(position == 0)
			switch_page("Feeds", position);
		else
		{
			switch_page("Feeds", position);
			int page = position - 3;
			((ViewPager) findViewById(R.id.pager)).setCurrentItem(page);
		}
	}

	private boolean check_service_running()
	{
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if(service_update.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
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
				set_title("Feeds");
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
			/// Add a global new feeds downloaded so update all method here.
			/// Replace 0 with the index of all.
			
			if((position == 0)&&(new_items))
			{
				new refresh_page().execute(0);
				new_items = false;
			}
			else if(get_card_adapter(position).getCount() == 0)
				new refresh_page().execute(position);
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
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.feed_fragment, container, false);
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
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.manage_pager, container, false);
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
							.setPositiveButton("Delete", new DialogInterface.OnClickListener()
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
							.setNegativeButton("Delete", new DialogInterface.OnClickListener()
					{
						/// Delete the feed.
						public void onClick(DialogInterface dialog, int id)
						{
							String group = feed_list_adapter.get_info(positionrr);
							group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
							String name = feed_list_adapter.getItem(positionrr);
							delete(group + ".image_size.cache.txt");
							/// Perhaps regen the all_image.cache.txt

							remove_string_from_file("groups/" + group + ".txt", name, true);
							remove_string_from_file("groups/All.txt", name, true);

							/// If the group file no longer exists because it was the last feed in it, delete the group from the group_list.
							if(!exists("groups/" + group + ".txt"))
							{
								remove_string_from_file("groups/group_list.txt", group, false);
								update_groups();
							}

							sort_group_content_by_time("All");
							if(exists("groups/" + group + ".txt"))
								sort_group_content_by_time(group);

							/// remove deleted files content from groups that it was in
							feed_list_adapter.remove_item(positionrr);
							feed_list_adapter.notifyDataSetChanged();
							update_manage_groups();
						}
					})
					.setPositiveButton("Clear Content", new DialogInterface.OnClickListener()
					{
						/// Delete the feed.
						public void onClick(DialogInterface dialog, int id)
						{
							String group = feed_list_adapter.get_info(positionrr);
							group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
							String name = feed_list_adapter.getItem(positionrr);
							delete("content/" + name + ".store.txt.content.txt");
							delete("groups/" + group + ".txt.content.txt");
							delete(group + ".image_size.cache.txt");

							sort_group_content_by_time("All");

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
				return "Groups";
			else
				return "Feeds";
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
		this.optionsMenu = menu;
		MenuInflater menu_inflater = getMenuInflater();
		menu_inflater.inflate(R.menu.main_overflow, menu);
		return true;
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
			return true;
		}

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
				{
					refreshItem.setActionView(null);
					refreshing = false;
				}
			}
		}
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
				("Add",new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.setNegativeButton
				("Cancel",new DialogInterface.OnClickListener()
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
						boolean found = false;
						boolean new_group_mode = false;
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
							new_group = ((Spinner) add_rss_dialog.findViewById(R.id.group_spinner)).getSelectedItem().toString();

						Boolean rss = false;
						String URL_check = ((EditText) add_rss_dialog.findViewById(R.id.URL_edit)).getText().toString().trim();
						String feed_name = ((EditText) add_rss_dialog.findViewById(R.id.name_edit)).getText().toString().trim();

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

						if(!rss)
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

							add_feed(feed_name, URL_check, new_group);
							alertDialog.dismiss();
						}
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

		final String current_group = feed_groups.get(position);
		final String current_title = feed_titles.get(position);

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
				.setTitle("Edit Feed")
				.setView(edit_rss_dialog)
				.setCancelable(true)
				.setPositiveButton
				("Accept",new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.setNegativeButton
				("Cancel",new DialogInterface.OnClickListener()
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
						String new_group = ((EditText) edit_rss_dialog.findViewById(R.id.group_edit)).getText().toString().trim().toLowerCase();
						boolean found = false;
						boolean new_group_mode = false;
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
							new_group = ((Spinner) edit_rss_dialog.findViewById(R.id.group_spinner)).getSelectedItem().toString();

						Boolean rss = false;
						String URL_check = ((EditText) edit_rss_dialog.findViewById(R.id.URL_edit)).getText().toString().trim();
						String feed_name = ((EditText) edit_rss_dialog.findViewById(R.id.name_edit)).getText().toString().trim();

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

							edit_feed(current_title, feed_name, URL_check, current_group, new_group);
							edit_dialog.dismiss();
						}
					}
				});
			}
		});
		edit_dialog.show();
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
				byte data[] = new byte[512];
				byte data2[] = new byte[512];
				in.read(data, 0, 512);
				String line = new String(data);
				if((line.contains("rss"))||((line.contains("Atom"))||(line.contains("atom"))))
				{
					int count = 0;
					while((!line.contains("<title"))&&(!line.contains("</title>")))
					{
						in.read(data, count, 512 + count);
						byte[] next = new byte[data.length + 512];
						System.arraycopy(data, 0, next, 0, data.length);
						System.arraycopy(data2, 0, next, data.length, 512);
						data = next;
						line = new String(data);
						count = count + 512;
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

	private void download_file(String urler, String file_name)
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
					fout = new FileOutputStream(storage + file_name);

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

	public static void append_string_to_file(String file_name, String string)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(storage + file_name, true));
			out.write(string);
			out.close();
		}
		catch (Exception e)
		{
		}
	}

	/// Relies on read_file_to_list && storage.
	private void remove_string_from_file(String file_name, String string, Boolean contains)
	{
		final List<String> list = read_file_to_list(file_name, 0);
		delete(file_name);
		for(String item : list)
		{
			if(contains)
			{
				if(!item.contains(string))
					append_string_to_file(file_name, item + "\n");
			}
			else
			{
				if(!item.equals(string))
					append_string_to_file(file_name, item + "\n");
			}
		}
	}

	private static void update_groups()
	{
		current_groups = read_file_to_list("groups/group_list.txt", 0);

		if(current_groups.size() == 0)
		{
			append_string_to_file("groups/group_list.txt", "All\n");
			current_groups.add("All");
		}

		List<String> nav = new ArrayList<String>();
		nav.addAll(current_groups);
		nav.addAll(0, Arrays.asList("Feeds", "Manage", "Settings"));
		String[] navigation_bar_data = nav.toArray(new String[nav.size()]);

		navigation_list.setAdapter(new ArrayAdapter<String>(get_context(), R.layout.drawer_list_item, navigation_bar_data));
		if(viewpager != null)
			viewpager.getAdapter().notifyDataSetChanged();
		/// TODO: add an element to the list for new items.
	}

	public static void update_group_order(List<String> new_order)
	{
		delete("groups/group_list.txt");
		for(String group : new_order)
			append_string_to_file("groups/group_list.txt", group + "\n");
		update_groups();
	}

	private static Context get_context()
	{
		return context;
	}

	private List< List<String> > read_csv_to_list(String[] type)
	{
		final String feed_path = type[0];
		final int lines_to_skip = Integer.parseInt(type[1]);
		final int number_of_items = type.length - 2;

		String line;
		BufferedReader stream;
		List< List<String> > types = new ArrayList< List<String> >();
		for(int i = 0; i < number_of_items; i++)
			types.add(new ArrayList< String >());

		String content;
		try
		{
			stream = new BufferedReader(new FileReader(feed_path));

			/// Skip lines.
			for(int i = 0; i < lines_to_skip; i++)
				stream.readLine();

			while((line = stream.readLine()) != null)
			{
				for(int i = 0; i < number_of_items; i++)
				{
					content = type[2 + i] + "|";
					if((!line.contains(content))||(line.contains(content + '|')))
						types.get(i).add("");
					else
					{
						int content_start = line.indexOf(content) + content.length();
						types.get(i).add(line.substring(content_start, line.indexOf('|', content_start)));
					}
				}
			}
		}
		catch(IOException e){
		}
		return types;
	}

	public static List<String> read_file_to_list(String file_name, int lines_to_skip)
	{
		String line;
		BufferedReader stream;
		List<String> lines = new ArrayList<String>();
		try
		{
			stream = new BufferedReader(new FileReader(storage + file_name));
			for(int i=0; i<lines_to_skip; i++)
				stream.readLine();
			while((line = stream.readLine()) != null)
				lines.add(line);
			stream.close();
		}
		catch(IOException e){
		}
		return lines;
	}

	private static int count_lines(String file_name)
	{
		BufferedReader stream;
		int i = 0;
		try
		{
			stream = new BufferedReader(new FileReader(storage + file_name));
			while(stream.readLine() != null)
				i++;
			stream.close();
		}
		catch(IOException e){
		}
		return i;
	}

	private void update_group(int page_number)
	{
		set_refresh(true);
		Intent intent = new Intent(this, service_update.class);
		intent.putExtra("GROUP_NUMBER", Integer.toString(page_number));
		startService(intent);
	}

	private class refresh_page extends AsyncTask<Integer, Object, Long>
	{
		@Override
		protected void onPreExecute(){
			set_refresh(true);
			refreshing = true;
		}

		@Override
		protected Long doInBackground(Integer... ton)
		{
			/// ton[0] = page number or position in current_groups.
			/// TODO: setRecyclerListener(AbsListView.RecyclerListener listener);
			int page_number = ton[0];

			String group = current_groups.get(page_number);
			String group_file_path 			= storage + "groups/" + group + ".txt";
			String partial_thumbnail_path 	= storage + "thumbnails/";

			List< List<String> > content 		= read_csv_to_list(new String[]{group_file_path, "0", "name", "url"});
			List<String> group_feeds_names 		= content.get(0);

			String image_name, thumbnail_path;

			if(group_feeds_names.size() < 1)
				return 0L;

			/// If we should download and update the feeds inside that group.
			for(String feed : group_feeds_names)
			{
				if(!exists("content/" + feed + ".store.txt.content.txt"))
					return 0L;
			}

			/// Make group content file
			String group_content_path = storage + "groups/" + group + ".txt.content.txt";
			File group_content_file = new File(group_content_path);

			/// If we have skipped the download, and either the page number is zero (which it only is if new data had been made since) or the group content file does not exist yet.
			new_items = true;
			if((!group_content_file.exists())||(new_items))
			{
				for(String feed : group_feeds_names)
				{
					if(exists("content/" + feed + ".store.txt.content.txt"))
					{
						sort_group_content_by_time(group);
						break;
					}
				}
			}

			String[] passer = {group_content_path, "0", "title", "image", "description", "link", "marker"};
			List< List<String> > contenter = read_csv_to_list(passer);
			List<String> titles 		= contenter.get(0);
			List<String> images 		= contenter.get(1);
			List<String> descriptions 	= contenter.get(2);
			List<String> links			= contenter.get(3);
			List<String> marker			= contenter.get(4);
			
			if(titles.get(0).length() < 1)
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
			List<String> dimensions = read_file_to_list(group + ".image_size.cache.txt", 0);

			/// For each line of the group_content_file
			final int size = titles.size();
			for(int m=0; m<size; m++)
			{
				thumbnail_path = "";
				Integer[] dim = {0, 0};

				if(!images.get(m).equals(""))
				{
					image_name = images.get(m).substring(images.get(m).lastIndexOf("/") + 1, images.get(m).length());

					if((!exists("thumbnails/" + image_name))&&(!check_service_running()))
					{
						if(!exists("images" + image_name))
							download_file(images.get(m), "images/" + image_name);
						dimensions.add(compress_file(image_name, group, false));
					}

					dim = get_image_dimensions(dimensions, image_name);
					if(dim[0] < 6)
					{
						dimensions.add(compress_file(image_name, group, true));
						dim = get_image_dimensions(dimensions, image_name);
					}
					thumbnail_path = partial_thumbnail_path + image_name;
				}

				// Checks to see if page has this item.
				if(existing_items.add(links.get(m)))
				{
					publishProgress(page_number, titles.get(m), descriptions.get(m), links.get(m), thumbnail_path, dim[1], dim[0], marker.get(m));
					/// If we have downloaded new data on other pages bar 0, and it gets here, there is new data for 0 to refresh with.
					if(page_number != 0)
					// Make this an array for the group pages.
					new_items = true;
				}
			}
			return 0L;
		}

		private int marker_position = -1;

		@Override
		protected void onProgressUpdate(Object... progress)
		{
			ListFragment l = ((fragment_card) getFragmentManager().findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.pager)).getId() + ":" + Integer.toString((Integer) progress[0])));
			if(l != null)
			{
				card_adapter ith = ((card_adapter) l.getListAdapter());
				ListView lv = l.getListView();
				Boolean marker = false;
				/// It should stop at the latest one unless there is not a newest one. So stay at 0 until it finds one.
				if(((String) progress[7]).equals("1"))
				{
					marker = true;
					marker_position = 0;
				}
				else if(marker_position != -1)
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

				//lv.setSelectionFromTop(index, top - twelve);
				if(marker_position != -1)
					lv.setSelection(marker_position);
				else
					lv.setSelection(lv.getCount() - 1);
			}
		}

		@Override
		protected void onPostExecute(Long tun)
		{
			//if(viewPager.getOffscreenPageLimit() > 1)
				//viewPager.setOffscreenPageLimit(1);
			set_refresh(check_service_running());
		}
	}

	private card_adapter get_card_adapter(int page_index)
	{
		return ((card_adapter)((fragment_card) getFragmentManager()
						.findFragmentByTag("android:switcher:" + ((ViewPager) findViewById(R.id.pager)).getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	private void sort_group_content_by_time(String group)
	{
		Date time;

		List<String> feeds_array	= read_csv_to_list(new String[]{storage + "groups/" + group + ".txt", "0", "name"}).get(0);
		List<Date> dates 			= new ArrayList<Date>();
		List<String> links_ordered 	= new ArrayList<String>();
		List<String> content_all 	= new ArrayList<String>();
		List<String> links, pubDates, content;

		for(String feed : feeds_array)
		{
			String content_path = storage + "content/" + feed + ".store.txt.content.txt";
			File test = new File(content_path);
			if(test.exists())
			{

				List< List<String> > contenter	= read_csv_to_list(new String[]{content_path, "1", "link", "pubDate"});
				links 							= contenter.get(0);
				pubDates						= contenter.get(1);
				content 						= read_file_to_list("content/" + feed + ".store.txt.content.txt", 1);

				if(pubDates.get(0).length()<8)
					pubDates 					= read_csv_to_list(new String[]{content_path, "1", "published"}).get(0);
				if(pubDates.get(0).length()<8)
					pubDates 					= read_csv_to_list(new String[]{content_path, "1", "updated"}).get(0);

				final int size = pubDates.size();
				for(int i=0; i<size; i++)
				{
					content_all.add(content.get(i));
					try{
						time 					= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH)).parse(pubDates.get(i));
					}
					catch(Exception e){
						try{
							time 				= (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)).parse(pubDates.get(i));
						}
						catch(Exception t){
							try{
								time 			= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)).parse(pubDates.get(i));
							}
							catch(Exception c){
								try{
									time 		= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)).parse(pubDates.get(i));
								}
								catch(Exception n){
									try{
										time 	= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)).parse(pubDates.get(i));
									}
									catch(Exception r){
										try{
											time 	= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)).parse(pubDates.get(i));
										}
										catch(Exception r){
											try{
												time = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)).parse(pubDates.get(i));
											}
											catch(Exception x){
												main_view.log("BUG : Format not found and date looks like: " + pubDates.get(i));
												time = new Date();
											}
										}
									}
								}
							}
						}
					}
					
					final int sizer = dates.size();
					for(int j=0; j<sizer; j++)
					{
						if(time.before(dates.get(j)))
						{
							dates.add(j, time);
							links_ordered.add(j, links.get(i));
							break;
						}
						else if((j == dates.size() - 1)&&(time.after(dates.get(j))))
						{
							dates.add(time);
							links_ordered.add(links.get(i));
							break;
						}
					}
					if(dates.size() == 0)
					{
						dates.add(time);
						links_ordered.add(links.get(i));
					}
				}
			}
		}

		final String group_content_path = "groups/" + group + ".txt.content.txt";
		String last_url = "";
		if(exists("groups/" + group + ".txt.content.txt"))
		{
			List< List<String> > bonne = read_csv_to_list(new String[]{storage + "groups/" + group + ".txt.content.txt", "0", "link", "marker"});
			List<String> urls = bonne.get(0);
			List<String> marks = bonne.get(1);
			int sized = marks.size();
			for(int i = sized - 1; i >= 0; i--)
			{
				if(marks.get(i).equals("1"))
				{
					last_url = urls.get(i);
					break;
				}
			}
			if((last_url.equals(""))&&(sized > 0))
				last_url = urls.get(0);
		}
		if(last_url.equals(""))
			last_url = links_ordered.get(0);

		delete(group_content_path);

		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(storage + group_content_path, true));

			if(links_ordered.size()>0)
			{
				for(String link : links_ordered)
				{
					for(String line : content_all)
					{
						if(line.contains(link))
						{
							if(link.equals(last_url))
								out.write(line + "marker|1|\n");
							else
								out.write(line + "\n");
							break;
						}
					}
				}
			}
			out.close();
		}
		catch(Exception e){
		}
	}

	public static void log(String text)
	{
		append_string_to_file("dump.txt", text + "\n");
	}

	public static void delete(String file_name)
	{
		(new File(storage + file_name)).delete();
	}

	public static Boolean exists(String file_name)
	{
		return (new File(storage + file_name)).exists();
	}

	private String compress_file(String image_name, String group, Boolean skip_save)
	{
		int insample;
		if(!skip_save)
		{
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(storage + "images/" + image_name, o);

			int width_tmp = o.outWidth;

			if(width_tmp > width)
				insample =  Math.round((float) width_tmp / (float) width);
			else
				insample = 1;
		}
		else
			insample = 1;

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = insample;
		Bitmap bitmap = BitmapFactory.decodeFile(storage + "images/" + image_name, o2);
		if(o2.outWidth > 9)
			append_string_to_file(group + ".image_size.cache.txt", image_name + "|" + o2.outWidth + "|" + o2.outHeight + "\n");

		if(!skip_save)
		{
			try
			{
				FileOutputStream out = new FileOutputStream(storage + "thumbnails/" + image_name);
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
		for(String dimer : dim_list)
		{
			if(dimer.contains(image_name))
			{
				int first = dimer.indexOf('|') + 1;
				int second = dimer.indexOf('|', first + 1) + 1;
				size[0] = Integer.parseInt(dimer.substring(first, second - 1));
				size[1] = Integer.parseInt(dimer.substring(second, dimer.length()));
				return size;
			}
		}
		return size;
	}
}
