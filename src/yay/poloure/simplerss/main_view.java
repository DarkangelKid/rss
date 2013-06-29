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
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.io.StringWriter;
import java.io.PrintWriter;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class main_view extends Activity
{
	private DrawerLayout drawer_layout;

	private static ListView navigation_list;
	private ActionBarDrawerToggle drawer_toggle;
	private Menu optionsMenu;

	private String mTitle, feed_title;
	private static float density;
	private int width;

	private static int download_finished;
	private static ViewPager viewPager;
	private ViewPager manage_pager;
	private static Resources res;
	private static int twelve;
	private static int first_height;
	private int check_finished;
	private Boolean new_items;
	public static String storage;
	public static Context context;

	public static String[] current_groups;
	private static String[] feed_titles, feed_urls, feed_groups;
	
	private static final int CONTENT_VIEW_ID = 10101010;
	private static String[] nav_items, nav_final;

	private Fragment man, pref, feed;


	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		append_string_to_file("groups/" + feed_group + ".txt", "name|" +  feed_name + "|url|" + feed_url + "|\n");
		append_string_to_file("groups/All.txt", "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|\n");
					
		feed_titles = read_csv_to_array("name", "groups/All.txt", 0);
		feed_urls = read_csv_to_array("url", "groups/All.txt", 0);
		feed_groups = read_csv_to_array("group", "groups/All.txt", 0);
		
		
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
						
			feed_titles = read_csv_to_array("name", storage + "groups/All.txt", 0);
			feed_urls = read_csv_to_array("url", storage + "groups/All.txt", 0);
			feed_groups = read_csv_to_array("group", storage + "groups/All.txt", 0);
			
			manage_pager = (ViewPager) findViewById(R.id.manage_viewpager);
			manage_pager.setAdapter(new manage_pager_adapter(getFragmentManager()));
			context = getApplicationContext();
			update_groups();
			if(read_file_to_list("groups/All.txt", 0).size()>0)
				new refresh_feeds().execute(true, 0);

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
			view.setBackgroundColor(getResources().getColor(R.color.white));
			return view;
		}
	}

	private class manage_fragment extends Fragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			//setRetainInstance(true);
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
			for(String group : current_groups)
				manage_adapter.add_list(group);
			manage_list.setAdapter(manage_adapter);
			manage_adapter.notifyDataSetChanged();
			return view;
		}
	}
	
	public static class feed_manage extends Fragment
	{
		private ListView feed_list;
		public feed_adapter feed_list_adapter;
		
		static feed_manage manage_feeds_instance(int num)
		{
			feed_manage m = new feed_manage();
			Bundle args = new Bundle();
			args.putInt("num", num);
			m.setArguments(args);
			return m;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.manage_feeds, container, false);
			feed_list = (ListView) view.findViewById(R.id.feeds_listview);
			feed_list_adapter = new feed_adapter(getActivity());
			feed_list.setAdapter(feed_list_adapter);
			
			for(int i = 0; i < feed_titles.length; i++)
			{
				feed_list_adapter.add_list(feed_titles[i], feed_urls[i] + "\n" + feed_groups[i]);
				feed_list_adapter.notifyDataSetChanged();
			}
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

							add_feed(feed_name, URL_check, new_group);
							alertDialog.dismiss();
						}
					}
				});
			}
		});
		alertDialog.show();
	}

	private void toast_message(String message, int zero_or_one)
	{
		Context context = getApplicationContext();
		Toast message_toast = Toast.makeText(context, message, zero_or_one);
		message_toast.show();
	}

	class check_feed_exists extends AsyncTask<String, Void, Integer>
	{
		@Override
		protected Integer doInBackground(String... urls)
		{
			try
			{
				BufferedInputStream in = null;
				in = new BufferedInputStream((new URL(urls[0])).openStream());
				byte data[] = new byte[512];
				in.read(data, 0, 512);
				String line = new String(data);
				log(line);
				if((line.contains("rss"))||((line.contains("Atom"))||(line.contains("atom"))))
				{
					while((!line.contains("<title>"))&&(!line.contains("</title>")))
					{
						in.read(data, 0, 512);
						line = new String(data);
					}
					int ind = line.indexOf("<title>") + 7;
					feed_title = line.substring(ind, line.indexOf("</", ind));
					log(feed_title);
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

	private static void append_string_to_file(String file_name, String string)
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
		List<String> gs = read_file_to_list("groups/group_list.txt", 0);
		current_groups = gs.toArray(new String[gs.size()]);
		ArrayAdapter<String> nav_adapter;
		if((current_groups.length+3)>nav_final.length)
		{
			String[] nav_finaler = new String[nav_final.length + 1];
			System.arraycopy(nav_final, 0, nav_finaler, 0, nav_final.length);
			nav_finaler[nav_finaler.length - 1] = current_groups[current_groups.length - 1];
			nav_final = nav_finaler;
			nav_adapter = new ArrayAdapter<String>(get_context(), R.layout.drawer_list_item, nav_final);
		}
		else
		{
			for(int i=0; i<current_groups.length; i++)
				nav_final[i+3] = current_groups[i];
			nav_adapter = new ArrayAdapter<String>(get_context(), R.layout.drawer_list_item, nav_final);
		}
		navigation_list.setAdapter(nav_adapter);
		viewPager.getAdapter().notifyDataSetChanged();
	}

	public static void update_group_order(List<String> new_order)
	{
		(new File(storage + "groups/group_list.txt")).delete();
		for(String group : new_order)
			append_string_to_file("groups/group_list.txt", group + "\n");
		update_groups();
	}

	public static Context get_context()
	{
		return context;
	}

	private String return_first_line_containing(String file_name, String content)
	{
		String line = "";

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				File in = new File(storage + file_name);
				BufferedReader reader = new BufferedReader(new FileReader(in));
								
				while(!line.contains(content))
					line = reader.readLine();
			}
			catch (Exception e){
				line = "";
			}
		}
		return line;
	}

	private String[] read_csv_to_array(String content_type, String feed_path, int lines_to_skip)
	{
		String line = null;
		BufferedReader stream = null;
		List<String> lines = new ArrayList<String>();
		content_type = content_type + "|";
		try
		{
			stream = new BufferedReader(new FileReader(feed_path));
			for(int i=0; i<lines_to_skip; i++)
				stream.readLine();
			while((line = stream.readLine()) != null)
			{
				if((!line.contains(content_type))||(line.contains(content_type + '|')))
					lines.add("");
				else
				{
					int content_start = line.indexOf(content_type) + content_type.length();
					line = line.substring(content_start, line.indexOf('|', content_start));
					lines.add(line);
				}
			}
		}
		catch(IOException e){
		}
		return lines.toArray(new String[lines.size()]);
	}

	private static List<String> read_file_to_list(String file_name, int lines_to_skip)
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
			String group_file_path = storage + "groups/" + group + ".txt";
			String partial_image_path = storage + "images/";
			String partial_thumbnail_path = storage + "thumbnails/";
			String[] group_feeds_names = read_csv_to_array("name", group_file_path, 0);
			String[] group_feeds_urls = read_csv_to_array("url", group_file_path, 0);
			String image_name = "", thumbnail_path = "", feed_path = "";
			File image_file, thumbnail_file;

			if(group_feeds_names.length < 1)
				return 0L;

			/// If we should download and update the feeds inside that group.
			if(!skip_download)
			{
				for(int i=0; i<group_feeds_names.length; i++)
				{
					feed_path = storage + "content/" + group_feeds_names[i]; /// mariam_feed.txt
					download_file(group_feeds_urls[i], "content/" + group_feeds_names[i] + ".store.txt"); /// Downloads file as mariam_feed.store.txt
					new parsered(feed_path + ".store.txt"); /// Parses the file and makes other files like mariam_feed.store.txt.content.txt
					(new File(feed_path + ".store.txt")).delete();
					remove_duplicates(feed_path + ".store.txt.content.txt"); /// Finally we have the feeds content files.
				}
			}

			/// Make group content file
			String group_content_path = storage + "groups/" + group + ".txt.content.txt";
			File group_content_file = new File(group_content_path);

			/// if we have updated the feeds OR if the group content file does not exist, make the group content file.
			if((!skip_download))
				sort_group_content_by_time(group);
			else if((!group_content_file.exists())||(page_number == 0))
			{
				for(String feed : group_feeds_names)
				{
					if((new File(storage + "content/" + feed + ".store.txt.content.txt").exists()))
					{
						sort_group_content_by_time(group);
						break;
					}
				}
			}

			String [] titles = 			read_csv_to_array("title", group_content_path, 0);

			if(titles.length < 1)
				return 0L;

			String [] images = 			read_csv_to_array("image", group_content_path, 0);
			String [] descriptions = 	read_csv_to_array("description", group_content_path, 0);
			String [] links = 			read_csv_to_array("link", group_content_path, 0);

			List<String> ith_list = get_card_adapter(page_number).return_links();

			/// For each line of the group_content_file
			for(int m=0; m<titles.length; m++)
			{
				thumbnail_path = "";
				Integer[] dim = {0, 0};
				
				if(!images[m].equals(""))
				{
					image_name = images[m].substring(images[m].lastIndexOf("/") + 1, images[m].length());
					image_file = new File(partial_image_path + image_name);
					thumbnail_file = new File(partial_thumbnail_path + image_name);

					if(!image_file.exists())
						download_file(images[m], "images/" + image_name);
					if(!thumbnail_file.exists())
						compress_file(partial_image_path + image_name, partial_thumbnail_path + image_name, image_name);

					dim = get_image_dimensions(image_name);
					thumbnail_path = partial_thumbnail_path + image_name;
				}

				if((!ith_list.contains(links[m]))||(ith_list.size() == 0))
				{
					publishProgress(page_number, titles[m], descriptions[m], links[m], thumbnail_path, dim[1], dim[0]);
					ith_list.add(links[m]);
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
		String[] links, pubDates;
		Date time;

		String[] feeds_array = read_csv_to_array("name", storage + "groups/" + group + ".txt", 0);
		List<Date> dates = new ArrayList<Date>();
		List<String> links_ordered = new ArrayList<String>();
		List<String> content_all = new ArrayList<String>();
		List<String> content = new ArrayList<String>();
		
		for(String feed : feeds_array)
		{
			String content_path = storage + "content/" + feed + ".store.txt.content.txt";
			File test = new File(content_path);
			if(test.exists())
			{
				links = 			read_csv_to_array("link", content_path, 1);
				content = read_file_to_list("content/" + feed + ".store.txt.content.txt", 1);
				pubDates = 			read_csv_to_array("pubDate", content_path, 1);
				if(pubDates[0].length()<8)
					pubDates = 		read_csv_to_array("published", content_path, 1);
				if(pubDates[0].length()<8)
					pubDates = 		read_csv_to_array("updated", content_path, 1);

				for(int i=0; i<pubDates.length; i++)
				{
					content_all.add(content.get(i));
					try{
						time = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH)).parse(pubDates[i]);
					}
					catch(Exception e){
						try{
							time = (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)).parse(pubDates[i]);
						}
						catch(Exception t){
							try{
								time = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)).parse(pubDates[i]);
							}
							catch(Exception c){
								try{
									time = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)).parse(pubDates[i]);
								}
								catch(Exception n){
									try{
										time = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)).parse(pubDates[i]);
									}
									catch(Exception o){
										log("Format not found and date looks like: " + pubDates[i]);
										time = new Date();
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
							links_ordered.add(j , links[i]);
							break;
						}
						else if((j == dates.size() - 1)&&(time.after(dates.get(j))))
						{
							dates.add(time);
							links_ordered.add(links[i]);
							break;
						}
					}
					if(dates.size() == 0)
					{
						dates.add(time);
						links_ordered.add(links[i]);
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

	private boolean update_feed(String feed_name, String url)
	{
		boolean found = false;
		try{
			String line = "";
			String content_name = "content/" + feed_name + ".store.txt.content.txt";
			String store_name = feed_name + ".store.txt";
			String store_path = storage + "content/" + store_name;
				
			download_file(url, "content/" + store_name);
			new parsered(store_path);
			(new File(store_path)).delete();

			remove_duplicates(content_name);
			return true;
		}
		catch(Exception e){
			return false;
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

	private void log(String text)
	{
		append_string_to_file("dump.txt", text + "\n");
	}

	void compress_file(String image_path, String thumbnail_path, String image_name)
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
		int insample;

		if(width_tmp > width)
			insample =  Math.round((float) width_tmp / (float) width);
		else
			insample = 1;
			
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = insample;
		Bitmap bitmap = BitmapFactory.decodeFile(image_path, o2);
		append_string_to_file("image_size.cache.txt", image_name + "|" + o2.outWidth + "|" + o2.outHeight + "\n");

		try
		{
			FileOutputStream out = new FileOutputStream(thumbnail_path);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		}
		catch (Exception e){
		}
	}

	private Integer[] get_image_dimensions(final String image_name)
	{
		Integer[] size = new Integer[2];
		String line = return_first_line_containing("image_size.cache.txt", image_name);

		int first = line.indexOf('|') + 1;
		int second = line.indexOf('|', first + 1) + 1;
		size[0] = Integer.parseInt(line.substring(first, second - 1));
		size[1] = Integer.parseInt(line.substring(second, line.length()));
		return size;
	}
}
