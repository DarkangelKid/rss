package yay.poloure.simplerss;

import android.preference.PreferenceFragment;

import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;

import android.app.AlertDialog;
import java.util.List;

import android.os.Bundle;

import android.app.Activity;

import android.app.ListFragment;
import android.view.Display;
import android.graphics.Point;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.widget.DrawerLayout;

import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.content.res.Configuration;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import android.os.Environment;
import java.io.File;
import java.net.URL;

import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class main_view extends Activity
{
	private DrawerLayout drawer_layout;

	private static ListView navigation_list;
	private ActionBarDrawerToggle drawer_toggle;
	private Menu optionsMenu;

	private static String mTitle, feed_title;
	private static float density;
	private int width;

	private static ViewPager viewPager;
	private ViewPager manage_pager;
	private static Resources res;
	public static int positionrr, poser;
	private static int twelve;
	private static int check_finished;
	private Boolean new_items;
	public static String storage;
	public static Context context, activity_context;

	public static String[] current_groups;
	private static List<String> feed_titles, feed_urls, feed_groups;

	private static final int CONTENT_VIEW_ID = 10101010;
	private static String[] nav_final;

	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		append_string_to_file("groups/" + feed_group + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|\n");
		append_string_to_file("groups/All.txt", "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|\n");
		update_feeds_list();
	}

	private static void edit_feed(int pos, String old_feed_name, String feed_name, String feed_url, String old_group, String feed_group)
	{
		/// Update the All.txt file
		List<String> feeds = read_file_to_list_static("groups/All.txt", 0);
		(new File(storage + "groups/All.txt")).delete();
		for(int i=0; i<feeds.size(); i++)
		{
			if(feeds.get(i).contains(old_feed_name))
				append_string_to_file_static("groups/All.txt", "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|\n");
			else
				append_string_to_file_static("groups/All.txt", feeds.get(i) + "\n");
		}

		/// If we have renamed the title, rename the content/title.txt file.
		if(!old_feed_name.equals(feed_name))
		{
			(new File(storage + "content/" + old_feed_name + ".store.txt.content.txt"))
			.renameTo((new File(storage + "content/" + feed_name + ".store.txt.content.txt")));
		}

		/// If we moved to a new group, delete the old cache file, force a refresh, and refresh the new one.
		if(!old_group.equals(feed_group))
		{
			/// Update the group file
			File group_file = new File(storage + "groups/" + feed_group + ".txt");
			append_string_to_file_static("groups/" + feed_group + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|\n");

			/// Then delete from old group file
			feeds = read_file_to_list_static("groups/" + old_group + ".txt", 0);
			group_file = new File(storage + "groups/" + old_group + ".txt");
			group_file.delete();
			Boolean append = false;
			for(int i=0; i<feeds.size(); i++)
			{
				if(!feeds.get(i).contains(old_feed_name))
				{
					append_string_to_file_static("groups/" + old_group + ".txt", feeds.get(i) + "\n");
					append = true;
				}
			}
			if(append == false)
			{
				feeds = read_file_to_list_static("groups/group_list.txt", 0);
				group_file = new File(storage + "groups/group_list.txt");
				group_file.delete();
				for(int i=0; i<feeds.size(); i++)
				{
					if(!feeds.get(i).contains(old_group))
						append_string_to_file_static("groups/group_list.txt", feeds.get(i) + "\n");
				}
			}
			(new File(storage + "groups/" + old_group + ".txt.content.txt")).delete();
			//sort_group_content_by_time();
			//new refresh_feeds().execute(true, 0);
			//new refresh_feeds().execute(true, 0);
		}
		
		(new File(storage + "groups/All.txt.content.txt")).delete();
		feed_adapter temp = feed_manage.return_feed_adapter();

		/// Update the feed_titles, feed_urls, and feed_group lists from the groups/All.txt file.
		update_feeds_list();
		/// Add the new feeds to the feed_adapter (Manage/Feeds).
		temp.remove_item(poser);
		temp.add_list_pos(poser, feed_name, feed_url + "\n" + feed_group + " • " + Integer.toString(count_lines("content/" + feed_name + ".store.txt.content.txt") - 1) + " items");
		temp.notifyDataSetChanged();

		update_groups();
	}

	private static void update_feeds_list()
	{
		String[] pass 					= {storage + "groups/All.txt", "0", "name", "url", "group"};
		List< List<String> > content 	= read_csv_to_list_static(pass);
		feed_titles 		= content.get(0);
		feed_urls 			= content.get(1);
		feed_groups 		= content.get(2);
	}
	private static void add_group(String group_name)
	{
		append_string_to_file_static("groups/group_list.txt", group_name + "\n");
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

			(new File(storage + "dump.txt")).delete();

			String[] folders = {"images", "thumbnails", "groups", "content"};
			File folder_file;

			for(String folder : folders)
			{
				folder_file = new File(storage + folder);
				if(!folder_file.exists())
					folder_file.mkdir();
			}

			List<String> gs = read_file_to_list("groups/group_list.txt", 0);
			current_groups = gs.toArray(new String[gs.size()]);
			if(current_groups.length == 0)
				append_string_to_file("groups/group_list.txt", "All\n");

			getActionBar().setIcon(R.drawable.rss_icon);

				getFragmentManager().beginTransaction()
							.add(CONTENT_VIEW_ID, new top_fragment())
							.commit();
				Fragment feed = new the_feed_fragment();
				Fragment pref = new PrefsFragment();
				Fragment man = new manage_fragment();
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

			String[] nav_items = new String[]{"Feeds", "Manage", "Settings"};
			nav_final = new String[current_groups.length + nav_items.length];
			System.arraycopy(nav_items, 0, nav_final, 0, nav_items.length);
			System.arraycopy(current_groups, 0, nav_final, nav_items.length, current_groups.length);

			navigation_list = (ListView) findViewById(R.id.left_drawer);
			ArrayAdapter<String> nav_adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, nav_final);
			navigation_list.setAdapter(nav_adapter);
			navigation_list.setOnItemClickListener(new DrawerItemClickListener());

			viewPager = (ViewPager) findViewById(R.id.pager);
			viewPager.setAdapter(new viewpager_adapter(getFragmentManager()));
			viewPager.setOffscreenPageLimit(128);
			viewPager.setOnPageChangeListener(new page_listener());

			String[] pass 					= {storage + "groups/All.txt", "0", "name", "url", "group"};
			List< List<String> > content 	= read_csv_to_list(pass);
			feed_titles 		= content.get(0);
			feed_urls 			= content.get(1);
			feed_groups 		= content.get(2);

			manage_pager = (ViewPager) findViewById(R.id.manage_viewpager);
			manage_pager.setAdapter(new manage_pager_adapter(getFragmentManager()));
			context = getApplicationContext();
			activity_context = this;
			new_items = false;
			update_groups();
			
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

			if(read_file_to_list("groups/All.txt", 0).size()>0)
				new refresh_feeds().execute(true, 0);

			drawer_toggle.syncState();
			density = getResources().getDisplayMetrics().density;
			twelve = (int) ((12 * density + 0.5f));
			res = getResources();
		}
	}

	public static Resources get_resources(){
		return res;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
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
			viewPager.setCurrentItem(page);
		}
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
			if(position < 3){
				set_title(page_title);
			}
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
				new refresh_feeds().execute(true, 0);
				new_items = false;
			}
			else if(get_card_adapter(position).getCount() == 0)
				new refresh_feeds().execute(true, position);
		}
	}

	private class top_fragment extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.pager, container, false);
		}
	}

	private class the_feed_fragment extends Fragment
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

	private static class PrefsFragment extends PreferenceFragment
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

	private class manage_fragment extends Fragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.manage_pager, container, false);
		}
	}

	public static class group_fragment extends Fragment
	{
		private ListView manage_list;
		public group_adapter manage_adapter;

		static group_fragment manage_groups_instance(int num)
		{
			group_fragment m = new group_fragment();
			Bundle args = new Bundle();
			args.putInt("num", num);
			m.setArguments(args);
			return m;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.manage_fragment, container, false);
			manage_list = (ListView) view.findViewById(R.id.group_listview);
			manage_adapter = new group_adapter(getActivity());
			for(int i = 0; i < current_groups.length; i++)
			{
				List<String> content = read_csv_to_list_static(new String[]{storage + "groups/" + current_groups[i] + ".txt", "0", "name"}).get(0);
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
					info = Integer.toString(current_groups.length) + " groups";

				manage_adapter.add_list(current_groups[i], Integer.toString(content.size()) + " feeds • " + info);
			}
			manage_list.setAdapter(manage_adapter);
			manage_adapter.notifyDataSetChanged();
			return view;
		}
	}

	public static class feed_manage extends Fragment
	{
		private ListView feed_list;
		public static feed_adapter feed_list_adapter;

		static feed_manage manage_feeds_instance(int num)
		{
			feed_manage m = new feed_manage();
			Bundle args = new Bundle();
			args.putInt("num", num);
			m.setArguments(args);
			return m;
		}

		public static feed_adapter return_feed_adapter()
		{
			return feed_list_adapter;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.manage_feeds, container, false);
			feed_list = (ListView) view.findViewById(R.id.feeds_listview);

			feed_list.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					///show dialog
					show_edit_dialog(position);
				}
			});

			feed_list_adapter = new feed_adapter(getActivity());
			feed_list.setAdapter(feed_list_adapter);

			for(int i = 0; i < feed_titles.size(); i++)
			{
				/// Read the feed_title content file to see how many readLines it will take.
				feed_list_adapter.add_list(feed_titles.get(i), feed_urls.get(i) + "\n" + feed_groups.get(i) + " • " + Integer.toString(count_lines("content/" + feed_titles.get(i) + ".store.txt.content.txt") - 1) + " items");
				feed_list_adapter.notifyDataSetChanged();
			}

			feed_list.setOnItemLongClickListener(new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
				{
					positionrr = pos;
					AlertDialog.Builder builder = new AlertDialog.Builder(activity_context);
					builder.setCancelable(true)
							.setPositiveButton("Delete", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							String details = feed_list_adapter.get_info(positionrr);
							String title = feed_list_adapter.getItem(positionrr);
							details = details.substring(details.indexOf('\n') + 1, details.indexOf(' '));
							(new File(storage + "content/" + title + ".store.txt.content.txt")).delete();
							(new File(storage + "groups/All.txt.content.txt")).delete();
							(new File(storage + "groups/" + details + ".txt.content.txt")).delete();
							(new File(storage + details + ".image_size.cache.txt")).delete();
							/// Perhaps regen the all_image.cache.txt
							
							File all_file = new File(storage + "groups/" + details + ".txt");
							List<String> feeds = read_file_to_list_static("groups/" + details + ".txt", 0);
							all_file.delete();
							for(int i = 0; i < feeds.size(); i++)
							{
								if(!feeds.get(i).contains(title))
									append_string_to_file_static("groups/" + details + ".txt", feeds.get(i) + "\n");
							}
							
							if(!(new File(storage + "groups/" + details + ".txt")).exists())
							{
								all_file = new File(storage + "groups/group_list.txt");
								feeds = read_file_to_list_static("groups/group_list.txt", 0);
								all_file.delete();
								for(int i = 0; i < feeds.size(); i++)
								{
									if(!feeds.get(i).contains(details))
										append_string_to_file_static("groups/group_list.txt", feeds.get(i) + "\n");
								}
							}

							all_file = new File(storage + "groups/All.txt");
							feeds = read_file_to_list_static("groups/All.txt", 0);
							all_file.delete();
							for(int i = 0; i < feeds.size(); i++)
							{
								if(!feeds.get(i).contains(title))
									append_string_to_file_static("groups/All.txt", feeds.get(i) + "\n");
							}
							
							/// remove deleted files content from groups that it was in

							feed_list_adapter.remove_item(positionrr);
							feed_list_adapter.notifyDataSetChanged();
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
			return current_groups.length;
		}

 		@Override
		public Fragment getItem(int position){
			return card_fragment.newInstance(position);
		}

		@Override
		public String getPageTitle(int position){
			for(int i = 0; i < current_groups.length; i++)
			{
				if(position == i)
					return current_groups[position];
			}
			return "";
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
				return group_fragment.manage_groups_instance(position);
			else
				return feed_manage.manage_feeds_instance(position);
		}
		@Override
		public String getPageTitle(int position){
			if(position == 0)
				return "Groups";
			else
				return "Feeds";
		}
	}

	public static class card_fragment extends ListFragment
	{
		static card_fragment newInstance(int num)
		{
			card_fragment f = new card_fragment();
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
		//set_refresh(true);
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
			new refresh_feeds().execute(false, ((ViewPager) findViewById(R.id.pager)).getCurrentItem());
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	void set_refresh(final boolean refreshing)
	{
		if(optionsMenu != null)
		{
			final MenuItem refreshItem = optionsMenu.findItem(R.id.refresh);
			if(refreshItem != null)
			{
				if (refreshing)
					refreshItem.setActionView(R.layout.progress_circle);
				else
					refreshItem.setActionView(null);
			}
		}
	}

	private void show_add_dialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View add_rss_dialog = inflater.inflate(R.layout.add_rss_dialog, null);

		check_for_no_groups();

		Spinner group_spinner = (Spinner) add_rss_dialog.findViewById(R.id.group_spinner);
		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.group_spinner_text, current_groups);
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

						if(rss!= null && !rss)
							toast_message("Invalid RSS URL", 0);
						else
						{
							if((!found)&&(new_group_mode))
								add_group(new_group);

							if(feed_name.equals(""))
								feed_name = feed_title;

							feed_name = feed_name.replace("/","");

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
	private static void show_edit_dialog(int position)
	{
		poser = position;
		LayoutInflater inflater = LayoutInflater.from(activity_context);
		final View edit_rss_dialog = inflater.inflate(R.layout.add_rss_dialog, null);

		final String current_group = feed_groups.get(position);
		final String current_title = feed_titles.get(position);

		Spinner group_spinner = (Spinner) edit_rss_dialog.findViewById(R.id.group_spinner);
		ArrayAdapter adapter = new ArrayAdapter(activity_context, R.layout.group_spinner_text, current_groups);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner.setAdapter(adapter);

		int i;
		for(i = 0; i < current_groups.length; i++)
		{
			if(current_groups[i].equals(current_group))
				break;
		}

		((EditText)edit_rss_dialog.findViewById(R.id.name_edit)).setText(current_title);
		((EditText)edit_rss_dialog.findViewById(R.id.URL_edit)).setText(feed_urls.get(position));
		group_spinner.setSelection(i);

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
							toast_message("Invalid RSS URL", 0);
						else
						{
							if((!found)&&(new_group_mode))
								add_group(new_group);

							if(feed_name.equals(""))
								feed_name = feed_title;

							edit_feed(poser, current_title, feed_name, URL_check, current_group, new_group);
							edit_dialog.dismiss();
						}
					}
				});
			}
		});
		edit_dialog.show();
	}
	///cock

	static private void toast_message(String message, int zero_or_one)
	{
		Toast message_toast = Toast.makeText(context, message, zero_or_one);
		message_toast.show();
	}

	static class check_feed_exists extends AsyncTask<String, Void, Integer>
	{
		@Override
		protected Integer doInBackground(String... urls)
		{
			try
			{
				BufferedInputStream in = null;
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
						{
							fout.write(data, 0, count);
						}
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

	private void append_string_to_file(String file_name, String string)
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

	private static void append_string_to_file_static(String file_name, String string)
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

	private void check_for_no_groups()
	{
		List<String> groups = read_file_to_list("groups/group_list.txt", 0);
		if(!groups.contains("All"))
			add_group("All");
	}

	private static void update_groups()
	{
		List<String> gs = read_file_to_list_static("groups/group_list.txt", 0);
		current_groups = gs.toArray(new String[gs.size()]);
		ArrayAdapter<String> nav_adapter;
		if((current_groups.length+3)>nav_final.length)
		{
			String[] nav_finaler = new String[nav_final.length + 1];
			System.arraycopy(nav_final, 0, nav_finaler, 0, nav_final.length);
			nav_finaler[nav_finaler.length - 1] = current_groups[current_groups.length - 1];
			nav_final = nav_finaler;
		}
		else if((current_groups.length+3)<nav_final.length)
		{
			String[] nav_finaler = new String[nav_final.length - 1];
			nav_finaler[0] = nav_final[0];	nav_finaler[1] = nav_final[1];	nav_finaler[2] = nav_final[2];
			for(int i=0; i<current_groups.length; i++)
				nav_final[i+3] = current_groups[i];
			nav_final = nav_finaler;
		}
		else
		{
			for(int i=0; i<current_groups.length; i++)
			{
				log(current_groups[i]);
				nav_final[i+3] = current_groups[i];
			}
		}
		nav_adapter = new ArrayAdapter<String>(get_context(), R.layout.drawer_list_item, nav_final);
		navigation_list.setAdapter(nav_adapter);
		viewPager.getAdapter().notifyDataSetChanged();
	}

	public static void update_group_order(List<String> new_order)
	{
		(new File(storage + "groups/group_list.txt")).delete();
		for(String group : new_order)
			append_string_to_file_static("groups/group_list.txt", group + "\n");
		update_groups();
	}

	public static Context get_context()
	{
		return context;
	}

	private List< List<String> > read_csv_to_list(String[] type)
	{
		String feed_path = type[0];
		int lines_to_skip = Integer.parseInt(type[1]);
		int number_of_items = type.length - 2;

		String line = null;
		BufferedReader stream = null;
		List< List<String> > types = new ArrayList< List<String> >();
		for(int i = 0; i < number_of_items; i++)
			types.add(new ArrayList< String >());

		String content = "";

		try
		{
			stream = new BufferedReader(new FileReader(feed_path));

			/// Skip lines.
			for(int i=0; i<lines_to_skip; i++)
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

	private static List< List<String> > read_csv_to_list_static(String[] type)
	{
		String feed_path = type[0];
		int lines_to_skip = Integer.parseInt(type[1]);
		int number_of_items = type.length - 2;

		String line = null;
		BufferedReader stream = null;
		List< List<String> > types = new ArrayList< List<String> >();
		for(int i = 0; i < number_of_items; i++)
			types.add(new ArrayList< String >());

		String content = "";

		try
		{
			stream = new BufferedReader(new FileReader(feed_path));

			/// Skip lines.
			for(int i=0; i<lines_to_skip; i++)
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


	private List<String> read_file_to_list(String file_name, int lines_to_skip)
	{
		String line = null;
		BufferedReader stream = null;
		List<String> lines = new ArrayList<String>();
		try
		{
			stream = new BufferedReader(new FileReader(storage + file_name));
			for(int i=0; i<lines_to_skip; i++)
				stream.readLine();
			while((line = stream.readLine()) != null)
				lines.add(line);
			if (stream != null)
				stream.close();
		}
		catch(IOException e){
		}
		return lines;
	}

	private static int count_lines(String file_name)
	{
		BufferedReader stream = null;
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

	private static List<String> read_file_to_list_static(String file_name, int lines_to_skip)
	{
		String line = null;
		BufferedReader stream = null;
		List<String> lines = new ArrayList<String>();
		try
		{
			stream = new BufferedReader(new FileReader(storage + file_name));
			for(int i=0; i<lines_to_skip; i++)
				stream.readLine();
			while((line = stream.readLine()) != null)
				lines.add(line);
			if (stream != null)
				stream.close();
		}
		catch(IOException e){
		}
		return lines;
	}

	private class refresh_feeds extends AsyncTask<Object, Object, Long>
	{
		@Override
		protected void onPreExecute(){
			set_refresh(true);
		}

		@Override
		protected Long doInBackground(Object... ton)
		{
			/// ton[1] = page number or position in current_groups.
			/// ton[0] = if true, do not download new data.
			Boolean skip_download = ((Boolean) ton[0]);
			int page_number = ((Integer) ton[1]);

			String group = current_groups[page_number];
			String group_file_path 			= storage + "groups/" + group + ".txt";
			String partial_image_path 		= storage + "images/";
			String partial_thumbnail_path 	= storage + "thumbnails/";

			List< List<String> > content 		= read_csv_to_list(new String[]{group_file_path, "0", "name", "url"});
			List<String> group_feeds_names 		= content.get(0);
			List<String> group_feeds_urls 		= content.get(1);

			String image_name = "", thumbnail_path = "", feed_path = "";

			if(group_feeds_names.size() < 1)
				return 0L;

			/// If we should download and update the feeds inside that group.
			if(!skip_download)
			{
				for(int i=0; i<group_feeds_names.size(); i++)
				{
					feed_path = storage + "content/" + group_feeds_names.get(i); /// mariam_feed.txt
					download_file(group_feeds_urls.get(i), "content/" + group_feeds_names.get(i) + ".store.txt"); /// Downloads file as mariam_feed.store.txt
					new parsered(feed_path + ".store.txt"); /// Parses the file and makes other files like mariam_feed.store.txt.content.txt
					(new File(feed_path + ".store.txt")).delete();
					remove_duplicates("content/" + group_feeds_names.get(i) + ".store.txt.content.txt"); /// Finally we have the feeds content files.
				}
			}
			else
			{
				for(String feed : group_feeds_names)
				{
					if(!(new File(storage + "content/" + feed + ".store.txt.content.txt")).exists())
						return 0L;
				}
			}

			/// Make group content file
			String group_content_path = storage + "groups/" + group + ".txt.content.txt";
			File group_content_file = new File(group_content_path);

			/// If we have updated the feeds, resort the group_content file.
			if((!skip_download))
				sort_group_content_by_time(group);
			/// If we have skipped the download, and either the page number is zero (which it only is if new data had been made since) or the group content file does not exist yet.
			else if((!group_content_file.exists())||((page_number == 0)&&(new_items)))
			{
				for(String feed : group_feeds_names)
				{
					if((new File(storage + "content/" + feed + ".store.txt.content.txt")).exists())
					{
						sort_group_content_by_time(group);
						break;
					}
				}
			}

			String[] passer = {group_content_path, "0", "title", "image", "description", "link"};
			List< List<String> > contenter = read_csv_to_list(passer);
			List<String> titles 		= contenter.get(0);
			List<String> images 		= contenter.get(1);
			List<String> descriptions 	= contenter.get(2);
			List<String> links			= contenter.get(3);
			
			if(titles.get(0).length() < 1)
				return 0L;

			/// Get a list of all the pages items' urls.
			List<String> ith_list;
			try{
				ith_list = get_card_adapter(page_number).return_links();
			}
			catch(Exception e){
				log("BUG : Card_adapter returned null for page : " + Integer.toString(page_number));
				return 0L;
			}

			/// load the image_dimensions to a list.
			List<String> dimensions = read_file_to_list(group + ".image_size.cache.txt", 0);

			/// For each line of the group_content_file
			for(int m=0; m<titles.size(); m++)
			{
				thumbnail_path = "";
				Integer[] dim = {0, 0};

				if(!images.get(m).equals(""))
				{
					image_name = images.get(m).substring(images.get(m).lastIndexOf("/") + 1, images.get(m).length());

					/// If the image_name does not exist in images/ then download the file at url (images[m]) to images with name image_name
					if(!(new File(partial_image_path + image_name)).exists())
						download_file(images.get(m), "images/" + image_name);
					/// If the thumbnail does not exist in thumbnails/, compress the image in images/ to thumbnails with image_name.
					if(!(new File(partial_thumbnail_path + image_name)).exists())
						dimensions = compress_file(partial_image_path + image_name, partial_thumbnail_path + image_name, image_name, group, dimensions, false);

					dim = get_image_dimensions(dimensions, image_name);
					if(dim[0] == 0)
					{
						dimensions = compress_file(partial_image_path + image_name, partial_thumbnail_path + image_name, image_name, group, dimensions, true);
						dim = get_image_dimensions(dimensions, image_name);
					}

					thumbnail_path = partial_thumbnail_path + image_name;
				}

				// Perhaps shorten descriptions to eight lines max here so that parsing is faster.

				if((!ith_list.contains(links.get(m)))||(ith_list.size() == 0))
				{
					publishProgress(page_number, titles.get(m), descriptions.get(m), links.get(m), thumbnail_path, dim[1], dim[0]);
					/// Add this url to the card list we have stored instead of reasking for the new list.
					ith_list.add(links.get(m));
					/// If we have downloaded new data on other pages bar 0, and it gets here, there is new data for 0 to refresh with.
					if((skip_download == false)&&(page_number != 0))
						new_items = true;
				}
			}
			return 0L;
		}

		@Override
		protected void onProgressUpdate(Object... progress)
		{
			ListFragment l = ((card_fragment) getFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + Integer.toString((Integer) progress[0])));
			if(l != null)
			{
				card_adapter ith = ((card_adapter) l.getListAdapter());

				ListView lv = l.getListView();
				int index = lv.getFirstVisiblePosition() + 1;
				View v = lv.getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();
				if(top == 0)
					index++;
				else if (top < 0 && lv.getChildAt(1) != null)
				{
					index++;
					v = lv.getChildAt(1);
					top = v.getTop();
				}

				ith.add_list((String) progress[1], (String) progress[2], (String) progress[3], (String) progress[4], (Integer) progress[5], (Integer) progress[6]);
				ith.notifyDataSetChanged();

				lv.setSelectionFromTop(index, top - twelve);
			}
			else
				toast_message("ListFragment " + Integer.toString((Integer) progress[0]) + "NullPointerException", 1);
		}

		@Override
		protected void onPostExecute(Long tun)
		{
			//if(viewPager.getOffscreenPageLimit() > 1)
				//viewPager.setOffscreenPageLimit(1);
			set_refresh(false);
		}
	}

	private card_adapter get_card_adapter(int page_index)
	{
		return ((card_adapter)((card_fragment) getFragmentManager()
						.findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	private void sort_group_content_by_time(String group)
	{
		Date time;

		List<String> feeds_array	= read_csv_to_list(new String[]{storage + "groups/" + group + ".txt", "0", "name"}).get(0);
		List<Date> dates 			= new ArrayList<Date>();
		List<String> links			= new ArrayList<String>();
		List<String> pubDates		= new ArrayList<String>();
		List<String> links_ordered 	= new ArrayList<String>();
		List<String> content_all 	= new ArrayList<String>();
		List<String> content 		= new ArrayList<String>();

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

				for(int i=0; i<pubDates.size(); i++)
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
											time = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)).parse(pubDates.get(i));
										}
										catch(Exception x){
											log("BUG : Format not found and date looks like: " + pubDates.get(i));
											time = new Date();
										}
									}
								}
							}
						}
					}

					for(int j=0; j<dates.size(); j++)
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

		String group_content_path = "groups/" + group + ".txt.content.txt";
		File group_content = new File(storage + group_content_path);
		group_content.delete();

		if(links_ordered.size()>0)
		{
			for(String link : links_ordered)
			{
				for(String line : content_all)
				{
					if(line.contains(link))
					{
						append_string_to_file(group_content_path, line + "\n");
						break;
					}
				}
			}
		}
	}

	private static String[] get_groups()
	{
		return current_groups;
	}

	private void remove_duplicates(String content_name)
	{
		Set<String> set = new LinkedHashSet<String>(read_file_to_list(content_name, 0));
		(new File(storage + content_name)).delete();

		String[] feeds = set.toArray(new String[set.size()]);

		for(String feed : feeds)
			append_string_to_file(content_name, feed + "\n");
	}

	private static void log(String text)
	{
		append_string_to_file_static("dump.txt", text + "\n");
	}

	List<String> compress_file(String image_path, String thumbnail_path, String image_name, String group, List<String> dimensions, Boolean skip_save)
	{
		int insample;
		if(!skip_save)
		{
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(image_path, o);

			if(width < 10)
			{
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				width = (int) Math.round(((float)size.x)*0.80);
			}

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
		Bitmap bitmap = BitmapFactory.decodeFile(image_path, o2);
		append_string_to_file(group + ".image_size.cache.txt", image_name + "|" + o2.outWidth + "|" + o2.outHeight + "\n");
		dimensions.add(image_name + "|" + o2.outWidth + "|" + o2.outHeight);

		if(!skip_save)
		{
			try
			{
				FileOutputStream out = new FileOutputStream(thumbnail_path);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			}
			catch (Exception e){
			}
		}

		return dimensions;
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
