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
	private DrawerLayout mDrawerLayout;

	private ListView navigation_list;
	private ActionBarDrawerToggle drawer_toggle;
	private Menu optionsMenu;

	private String mTitle;
	private static float density;
	private int width;

	private static int download_finished;
	private ViewPager viewPager;
	private static Resources res;

	public static String[] current_groups;
	private static final int CONTENT_VIEW_ID = 10101010;
	private String[] nav_items, nav_final;

	private Fragment man, pref, feed;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("Feeds");
		FrameLayout frame = new FrameLayout(this);
		frame.setId(CONTENT_VIEW_ID);
		setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		File dump = new File(get_filepath("dump.txt"));
		dump.delete();

		File images_folder = new File(get_filepath("images"));
		File thumbnail_folder = new File(get_filepath("thumbnails"));

		if(!images_folder.exists())
			images_folder.mkdir();
		if(!thumbnail_folder.exists())
			thumbnail_folder.mkdir();

		current_groups = read_file_to_array("group_list.txt");
		if(current_groups.length == 0)
			append_string_to_file("group_list.txt", "All\n");

		getActionBar().setIcon(R.drawable.rss_icon);
		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction()
						.add(CONTENT_VIEW_ID, new top_fragment())
						.commit();
			Fragment feed = new the_feed_fragment();
			Fragment pref = new PrefsFragment();
			Fragment man = new manager_fragment();
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
		log("before");
		super.onPostCreate(savedInstanceState);
		log("after");

		String[] nav_items = new String[]{"Feeds", "Manage", "Settings"};
		nav_final = new String[current_groups.length + nav_items.length];
		System.arraycopy(nav_items, 0, nav_final, 0, nav_items.length);
		System.arraycopy(current_groups, 0, nav_final, nav_items.length, current_groups.length);

		navigation_list = (ListView) findViewById(R.id.left_drawer);
		ArrayAdapter<String> nav_adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, nav_final);
		navigation_list.setAdapter(nav_adapter);
		navigation_list.setOnItemClickListener(new DrawerItemClickListener());

		viewpager_adapter page_adapter = new viewpager_adapter(getFragmentManager());

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(page_adapter);
		viewPager.setOffscreenPageLimit(128);
		viewPager.setOnPageChangeListener(new page_listener());

		update_groups();
		new refresh_feeds().execute(true, 0);

		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

		mTitle = "Feeds";
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
		drawer_toggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			public void onDrawerClosed(View view){
				getActionBar().setTitle(mTitle);
			}

			public void onDrawerOpened(View drawerView){
				getActionBar().setTitle("Navigation");
			}
		};

		mDrawerLayout.setDrawerListener(drawer_toggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		drawer_toggle.syncState();
		density = getResources().getDisplayMetrics().density;
		res = getResources();
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
		mDrawerLayout.closeDrawer(navigation_list);
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

	public class manager_fragment extends Fragment
	{
		private ListView manage_list;
		public group_adapter manage_adapter;

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
						File in = new File(get_filepath("URLcheck.txt"));
						download_finished = 0;
						new adownload_file().execute(URL_check, "URLcheck.txt");
						while(download_finished == 0)
						{
							try{
								Thread.sleep(20);
							}
							catch(Exception e){}
						}
						if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
						{
							try
							{
								BufferedReader reader = new BufferedReader(new FileReader(in));
								try
								{
									for(int i=0; i<3; i++)
									{
										String line = reader.readLine();
										if(line.contains("rss")){
											rss = true;
											break;
										}
										else if((line.contains("Atom"))||(line.contains("atom"))){
											rss = true;
											break;
										}
									}
								}
								catch(Exception e)
								{
									rss = false;
								}
							}
							catch(Exception e)
							{
								rss = false;
							}
						}
						if(rss!= null && !rss)
							toast_message("Invalid RSS URL", 0);
						else
						{
							if((!found)&&(new_group_mode))
								add_group(new_group);
							/// Put duplication name checking in here.
							if(feed_name.equals(""))
							{
								try{
									String file_path = get_filepath("URLcheck.txt");
									File temp = new File(file_path + ".content.txt");
									new parsered(file_path);
									String line = (new BufferedReader(new FileReader(temp))).readLine();
									int content_start = line.indexOf("title|") + 6;
									feed_name = line.substring(content_start, line.indexOf('|', content_start));
									temp.delete();
									(new File(file_path)).delete();
								}
								catch(Exception e){
									toast_message("Failed to get title.", 1);
								}
							}
							add_feed(feed_name, URL_check, new_group);
							alertDialog.dismiss();
						}
						in.delete();
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
						fout = new FileOutputStream(get_filepath(file_name));

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

	private class adownload_file extends AsyncTask<String, Void, Long>
	{
		protected Long doInBackground(String... ton)
		{
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			{
				try{
					BufferedInputStream in = null;
					FileOutputStream fout = null;
					try
					{
						in = new BufferedInputStream(new URL(ton[0]).openStream());
						fout = new FileOutputStream(get_filepath(ton[1]));

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
			download_finished = 1;
			return 1L;
		}

		protected void onPostExecute(Long result) {
		}
	}

	private String get_filepath(String filename){
		return this.getExternalFilesDir(null).getAbsolutePath() + "/" + filename;
	}

	private void append_string_to_file(String file_name, String string)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(get_filepath(file_name), true));
				out.write(string);
				out.close();
			}
			catch (Exception e)
			{
			}
		}
		else
			toast_message("External storage is not mounted", 1);
	}

	private void remove_string_from_file(String file_name, String string)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				String line;
				File in = new File(get_filepath(file_name));
				File out = new File(get_filepath(file_name) + ".temp");

				BufferedReader reader = new BufferedReader(new FileReader(in));
				BufferedWriter writer = new BufferedWriter(new FileWriter(out));

				while((line = reader.readLine()) != null)
				{
					if(!(line.trim().equals(string)))
						writer.write(line + "\n");
				}

				out.renameTo(in);
				reader.close();
				writer.close();
			}
			catch (Exception e)
			{
			}
		}
		else
			toast_message("External storage is not mounted", 1);
	}

	private void check_for_no_groups()
	{
		List<String> groups = Arrays.asList(read_file_to_array("group_list.txt"));
		if(!groups.contains("All"))
			add_group("All");
	}

	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		append_string_to_file(feed_group + ".txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
		append_string_to_file("All.txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
	}

	private void update_groups()
	{
		current_groups = read_file_to_array("group_list.txt");
		if((current_groups.length+3)>nav_final.length)
		{
			String[] nav_finaler = new String[nav_final.length + 1];
			System.arraycopy(nav_final, 0, nav_finaler, 0, nav_final.length);
			nav_finaler[nav_finaler.length - 1] = current_groups[current_groups.length - 1];
			nav_final = nav_finaler;
			ArrayAdapter<String> nav_adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, nav_final);
			navigation_list.setAdapter(nav_adapter);
			viewPager.getAdapter().notifyDataSetChanged();
		}
	}

	private void add_group(String group_name)
	{
		append_string_to_file("group_list.txt", group_name + "\n");
		update_groups();
	}

	private String[] read_file_to_array(String file_name)
	{
		String[] line_values;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				String line;
				int number_of_lines = 0, i = 0;
				File in = new File(get_filepath(file_name));

				BufferedReader reader = new BufferedReader(new FileReader(in));

				while(reader.readLine() != null)
					number_of_lines++;

				reader.close();
				reader = new BufferedReader(new FileReader(in));
				
				line_values = new String[number_of_lines];
				
				while((line = reader.readLine()) != null)
				{
					line_values[i] = line;
					i++;
				}
			}
			catch (Exception e)
			{
				line_values = new String[0];
			}
		}
		else{
			line_values = new String[0];
		}
		
		return line_values;
	}

	private String return_first_line_containing(String file_name, String content)
	{
		String line = "";

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				File in = new File(get_filepath(file_name));
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

	private String[] read_file_to_array_skip(String file_name)
	{
		String[] line_values;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			try
			{
				String line;
				int number_of_lines = 0, i = 0;
				File in = new File(get_filepath(file_name));

				BufferedReader reader = new BufferedReader(new FileReader(in));

				while(reader.readLine() != null)
					number_of_lines++;

				reader.close();
				reader = new BufferedReader(new FileReader(in));
				
				line_values = new String[number_of_lines];
				reader.readLine();
				
				while((line = reader.readLine()) != null)
				{
					line_values[i] = line;
					i++;
				}
			}
			catch (Exception e){
				line_values = new String[0];
			}
		}
		else
			line_values = new String[0];
		
		return line_values;
	}

	private String[] read_feeds_to_array(String file_path)
	{
		String[] content_values;
		try
		{
			String line;
			int number_of_lines = 0, i = 0;
			File in = new File(file_path);

			BufferedReader reader = new BufferedReader(new FileReader(in));

			while(reader.readLine() != null)
				number_of_lines++;

			reader.close();
			reader = new BufferedReader(new FileReader(in));

			content_values = new String[number_of_lines];

			while((line = reader.readLine()) != null)
			{
				content_values[i] = line.substring(0, line.indexOf('|', 0));
				i++;
			}
		}
		catch (Exception e)
		{
			content_values = new String[0];
		}

		return content_values;
	}

	private String[] read_csv_to_array(String content_type, String feed_path)
	{
		ArrayList<String> content_values = new ArrayList<String>();
		try{
			content_type = content_type + "|";
			String line;
			File in = new File(feed_path);
			BufferedReader reader = new BufferedReader(new FileReader(in));
			reader.readLine();

			while((line = reader.readLine()) != null)
			{
				if((!line.contains(content_type))||(line.contains(content_type + '|')))
					content_values.add("");
				else
				{
					int content_start = line.indexOf(content_type) + content_type.length();
					line = line.substring(content_start, line.indexOf('|', content_start));
					content_values.add(line);
				}
			}
		}
		catch(Exception e){
		}
		return content_values.toArray(new String[content_values.size()]);
	}

	private class refresh_feeds extends AsyncTask<Object, Object, Long> {

		@Override
		protected void onPreExecute(){
			set_refresh(true);
		}

		@Override
		protected Long doInBackground(Object... ton)
		{
			String[] feeds_array = read_feeds_to_array(get_filepath(current_groups[((Integer) ton[1])] + ".txt"));
			String[] titles, descriptions, links, images;
			boolean success = true, exists = true;

			for(String feed : feeds_array)
			{
				if((!((Boolean) ton[0]))&&(((Integer) ton[1]) != 0))
					success = update_feed(feed);
				if(!((new File(get_filepath(feed + ".store.txt.content.txt"))).exists()))
					exists = false;
			}

			if(exists)
			{
				File test = new File(get_filepath(current_groups[((Integer) ton[1])] + ".txt.content.txt"));

				/// Only sort the new ones into the group chaches
				if((!test.exists())||(!((Boolean) ton[0])))
					sort_groups_by_time(current_groups[((Integer) ton[1])]);

				if(success)
				{
					String content_path = get_filepath(current_groups[((Integer) ton[1])] + ".txt.content.txt");

					titles = 				read_csv_to_array("title", content_path);
					if(titles.length>0)
					{
						images = 			read_csv_to_array("image", content_path);
						descriptions = 		read_csv_to_array("description", content_path);
						links = 			read_csv_to_array("link", content_path);

						int image_width = 0, image_height = 0;
						String partial_image_path = get_filepath("images/");
						String partial_thumbnail_path = get_filepath("thumbnails/");
						String image_name = "", thumbnail_path = "";
						File image, thumbnail;

						for(int m=0; m<titles.length; m++)
						{
							if(!images[m].equals(""))
							{
								image_name = images[m].substring(images[m].lastIndexOf("/") + 1, images[m].length());
								image = new File(partial_image_path + image_name);
								thumbnail = new File(partial_thumbnail_path + image_name);

								if(!image.exists())
								{
									download_file(images[m], "images/" + image_name);
									compress_file(partial_image_path + image_name, partial_thumbnail_path + image_name, image_name);
								}
								else if(!thumbnail.exists())
									compress_file(partial_image_path + image_name, partial_thumbnail_path + image_name, image_name);

								Integer[] dim = get_dim(image_name);
								image_height = dim[1];
								image_width = dim[0];
								thumbnail_path = partial_thumbnail_path + image_name;
							}

							List<String> ith_list = get_card_adapter(((Integer) ton[1])).return_links();
							if((!ith_list.contains(links[m]))||(ith_list.size() == 0))
								publishProgress(((Integer) ton[1]), titles[m], descriptions[m], links[m], thumbnail_path, image_height, image_width);
						}
					}
				}
			}
			return 1L;
		}

		@Override
		protected void onProgressUpdate(Object... progress){
			card_adapter ith = get_card_adapter((Integer) progress[0]);
			ith.add_list((String) progress[1], (String) progress[2], (String) progress[3], (String) progress[4], (Integer) progress[5], (Integer) progress[6]); 
			ith.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Long tun)
		{
			if(viewPager.getOffscreenPageLimit() > 1){
				viewPager.setOffscreenPageLimit(1);
			}
			set_refresh(false);
		}
	}

	private card_adapter get_card_adapter(int page_index)
	{
		return ((card_adapter)((card_fragment) getFragmentManager()
						.findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	private void sort_groups_by_time(String group)
	{
		String[] links, pubDates, content;
		Date time;

		String[] feeds_array = read_feeds_to_array(get_filepath(group + ".txt"));
		List<Date> dates = new ArrayList<Date>();
		List<String> links_ordered = new ArrayList<String>();
		List<String> content_all = new ArrayList<String>();
		
		for(String feed : feeds_array)
		{
			String content_path = get_filepath(feed + ".store.txt") + ".content.txt";
			links = 			read_csv_to_array("link", content_path);
			content = read_file_to_array_skip(feed + ".store.txt.content.txt");
			pubDates = 			read_csv_to_array("pubDate", content_path);
			if(pubDates[0].length()<8)
				pubDates = 		read_csv_to_array("published", content_path);
			if(pubDates[0].length()<8)
				pubDates = 		read_csv_to_array("updated", content_path);

			for(int i=0; i<pubDates.length; i++)
			{
				content_all.add(content[i]);
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

		String group_content_path = group + ".txt.content.txt";
		File group_content = new File(get_filepath(group_content_path));
		group_content.delete();

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


	private boolean update_feed(String feed_name)
	{
		boolean found = false;
		try{
			String line = "";
			BufferedReader read = (new BufferedReader(new FileReader(new File(get_filepath("All.txt")))));
			while(!(line.contains(feed_name))){
				line = read.readLine();
				found = true;
			}
			if(found)
			{
				int content_start = line.indexOf(feed_name + "|") + feed_name.length() + 1;
				String feed_url = line.substring(content_start, line.indexOf('|', content_start));
				String content_name = feed_name + ".store.txt.content.txt";
				String store_name = feed_name + ".store.txt";
				String store_path = get_filepath(store_name);
				
				download_file(feed_url, store_name);
				new parsered(store_path);
				(new File(store_path)).delete();

				remove_duplicates(content_name);
				return true;
			}
			else
				return false;
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
		String content_path = get_filepath(content_name);
		File temp = new File(content_path);

		String[] feeds = read_file_to_array(content_name);
		Set<String> set = new LinkedHashSet<String>(Arrays.asList(feeds));
		temp.delete();
		feeds = set.toArray(new String[set.size()]);
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
		{
			insample =  Math.round((float) width_tmp / (float) width);
		}
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

	private Integer[] get_dim(final String image_name)
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
