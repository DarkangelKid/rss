package yay.poloure.simplerss;

import android.preference.PreferenceFragment;

import android.content.Context;
import android.content.DialogInterface;

import android.app.AlertDialog;
import java.util.List;
import java.util.ArrayList;
import java.nio.channels.*;
import java.util.Arrays;

import android.os.Bundle;

import android.app.Activity;
import org.apache.commons.io.FileUtils;

import android.app.ListFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.widget.DrawerLayout;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.content.res.Configuration;

import android.widget.Button;
import android.widget.EditText;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import android.os.Environment;
import java.io.File;
import java.net.URL;

import android.os.AsyncTask;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.lang.Thread;


public class main_view extends Activity
{
	private DrawerLayout mDrawerLayout;
	private View.OnClickListener refreshListener;
	
	private ListView navigation_list;
	private ActionBarDrawerToggle drawer_toggle;
	private Menu optionsMenu;

	private Button btnClosePopup;

	private CharSequence mTitle;
	private CharSequence MainTitle;

	private static int download_finished;
	private MyFragmentPagerAdapter page_adapter;
	private ViewPager viewPager;

	private static String[] current_groups;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager);

		File dump = new File(get_filepath("dump.txt"));
		dump.delete();

		current_groups = read_file_to_array("group_list.txt");
		if(current_groups.length == 0)
			append_string_to_file("group_list.txt", "All\n");

		getActionBar().setIcon(R.drawable.rss_icon);
		page_adapter = new MyFragmentPagerAdapter(getFragmentManager());

		String[] nav_items = new String[]{"Feeds", "Manage", "Settings"};
		String[] nav_final = new String[current_groups.length + nav_items.length];
		System.arraycopy(nav_items, 0, nav_final, 0, nav_items.length);
		System.arraycopy(current_groups, 0, nav_final, nav_items.length, current_groups.length);

		navigation_list = (ListView) findViewById(R.id.left_drawer);
		navigation_list.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, nav_final));
		navigation_list.setOnItemClickListener(new DrawerItemClickListener());

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(page_adapter);
		viewPager.setOffscreenPageLimit(128);

		update_groups();
		
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

		mTitle = MainTitle = getTitle();
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
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawer_toggle.syncState();
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

	/** Swaps fragments in the main content view */
	private boolean selectItem(int position)
	{
		if(position < 3)
		{
			String[] titles = new String[] {"Feeds", "Manage", "Settings"};
			Fragment fragment;
			if(position == 2)
				fragment = new PrefsFragment();
			else if(position == 1)
				fragment = new manage_fragment();
			else if(position == 0)
			{
				if((mTitle.equals(MainTitle)))
				{
					mDrawerLayout.closeDrawer(navigation_list);
					((ViewPager)findViewById(R.id.pager)).setCurrentItem(0);
					return false;
				}
				else
				{
					getFragmentManager()
							.beginTransaction()
							.detach(getFragmentManager().findFragmentByTag(mTitle.toString()))
							.commit();
					setTitle(MainTitle);
					mDrawerLayout.closeDrawer(navigation_list);
					return true;
				}
			}
			else
				return false;
			Bundle args = new Bundle();
			args.putInt("Position", position);
			fragment.setArguments(args);
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
						//.setCustomAnimations(17432576, 17432577)
						.replace(R.id.content_frame, fragment, titles[position])
						.commit();
			navigation_list.setItemChecked(position, true);
			setTitle(titles[position]);
			mDrawerLayout.closeDrawer(navigation_list);
			return true;
		}
		else
		{
			if(!(mTitle.equals(MainTitle)))
			{
				getFragmentManager()
						.beginTransaction()
						.detach(getFragmentManager().findFragmentByTag(mTitle.toString()))
						.commit();
				setTitle(MainTitle);
			}
			mDrawerLayout.closeDrawer(navigation_list);
			int page = position - 3;
			((ViewPager)findViewById(R.id.pager)).setCurrentItem(page);
			return true;
		}
	}

	@Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

	public static class PrefsFragment extends PreferenceFragment
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

	public class manage_fragment extends Fragment
	{

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.manage_fragment, container, false);
		}

		@Override
		public void onResume()
		{
			super.onResume();
		}

		@Override
		public void onPause()
		{
			super.onPause();
		}

	}

	public static class MyFragmentPagerAdapter extends FragmentPagerAdapter
	{
		public MyFragmentPagerAdapter(FragmentManager fm){
			super(fm);
		}
 
		@Override
		public int getCount(){
			return current_groups.length;
		}

 		@Override
		public Fragment getItem(int position){
			return ArrayListFragment.newInstance(position);
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

	public static class ArrayListFragment extends ListFragment
	{
		private static int mNum;
		
		static ArrayListFragment newInstance(int num)
		{
			ArrayListFragment f = new ArrayListFragment();
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);
			return f;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
		}

		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			/*View ve = new View(getActivity());
			getListView().addFooterView(ve);
			getListView().addHeaderView(ve);
			setListAdapter(new card_adapter(getActivity()));*/
			mNum = getArguments() != null ? getArguments().getInt("num") : 1;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState){
			View view = inflater.inflate(R.layout.fragment_main_dummy, container, false);
			return view;
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
			new refresh_feeds().execute();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	public void set_refresh(final boolean refreshing)
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
							for(int i = 0; i < current_groups.length; i++)
							{
								if((current_groups[i].toLowerCase()).equals(new_group))
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
				URL website = new URL(urler);
				
				/*ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(get_filepath(file_name));
				
				fos.getChannel().transferFrom(rbc, 0, 1 << 24);*/
				FileUtils.copyURLToFile(website, new File(get_filepath(file_name)));
			}
			catch (Exception e)
			{
				append_string_to_file("dump.txt", "error\n");
			}
		}
	}

	private class adownload_file extends AsyncTask<String, Void, Long>
	{
		protected Long doInBackground(String... ton)
		{
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			{
				try
				{
					URL url = new URL(ton[0]);
					url.openConnection().connect();

					InputStream input = new BufferedInputStream(url.openStream());
					OutputStream output = new FileOutputStream(get_filepath(ton[1]));

					byte data[] = new byte[1024];
					int count;
					while ((count = input.read(data)) != -1)
						output.write(data, 0, count);

					output.close();
					input.close();
				}
				catch (Exception e)
				{
				}
			}
			download_finished = 1;
			long lo = 1;
			return lo;
		}

		protected void onPostExecute(Long result) {
		}
	}

	private String get_filepath(String filename)
	{
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
		String[] groups = read_file_to_array("group_list.txt");
		boolean all_exists = false;
		for(int i=0; i<groups.length; i++)
		{
			if(groups[i].equals("All"))
			{
				all_exists = true;
				break;
			}
		}
		if(!all_exists)
			add_group("All");
	}

	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		/// Check to see if the last character is a \n, if not, make it.
		append_string_to_file(feed_group + ".txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
		append_string_to_file("all_feeds.txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
	}

	private void delete_feed(String feed_name, String feed_url, String feed_group)
	{
		remove_string_from_file(feed_group + ".txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
		remove_string_from_file("all_feeds.txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
	}

	private void update_groups()
	{
		current_groups = read_file_to_array("group_list.txt");
		viewPager.getAdapter().notifyDataSetChanged();
		
	}

	private void add_group(String group_name)
	{
		/// Check to see if the last character is a \n, if not, make it.
		append_string_to_file("group_list.txt", group_name + "\n");
		update_groups();
	}

	private void delete_group(String group_name)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			remove_string_from_file("group_list.txt", group_name);
			File file = new File(get_filepath(group_name + ".txt"));
			file.delete();
			update_groups();
		}
		else
			toast_message("External storage is not mounted", 1);
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
			toast_message("External storage is not mounted", 1);
			line_values = new String[0];
		}
		
		return line_values;
	}

	private String[] read_feeds_to_array(int index, String file_path)
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
				if(index == 0)
				{
					content_values[i] = line.substring(0, line.indexOf('|', 0));
					i++;
				}
				else if(index == 1)
				{
					int bar_index = line.indexOf('|', 0);
					line = line.substring(bar_index + 1, line.indexOf('|', bar_index + 1));
					content_values[i] = line;
					i++;
				}
				else if(index == 2)
				{
					int bar_index = line.indexOf('|', line.indexOf('|') + 1);
					line = line.substring(bar_index + 1, line.length());
					content_values[i] = line;
					i++;
				}
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
		String[] content_values;
		content_type = content_type + "|";
		try
		{
			String line;
			int number_of_lines = 0, i = 0;
			File in = new File(feed_path);

			BufferedReader reader = new BufferedReader(new FileReader(in));

			while(reader.readLine() != null)
				number_of_lines++;

			reader.close();
			reader = new BufferedReader(new FileReader(in));
			reader.readLine();

			content_values = new String[number_of_lines - 1];

			while((line = reader.readLine()) != null)
			{
				int content_start = line.indexOf(content_type) + content_type.length();
				line = line.substring(content_start, line.indexOf('|', content_start));
				content_values[i] = line;
				i++;
			}
		}
		catch (Exception e)
		{
			content_values = new String[0];
		}

		return content_values;
	}

	private class refresh_feeds extends AsyncTask<Void, Integer, Long> {

		protected void onPreExecute(){
			set_refresh(true);
		}
		protected Long doInBackground(Void... ton)
		{
			get_card_adapter(0).clear_static_list();
					
			for(int i=0; i<current_groups.length; i++)
			{
				String[] feeds_array = read_feeds_to_array(0, get_filepath(current_groups[i] + ".txt"));
				String[] url_array = read_feeds_to_array(1, get_filepath(current_groups[i] + ".txt"));
				File wait;
				String feed_path;

				List<String> titles = new ArrayList();
				List<String> descriptions = new ArrayList();
				List<String> links = new ArrayList();
				List<Drawable> icons = new ArrayList();

				for(int k=0; k<feeds_array.length; k++)
				{
					update_feed(feeds_array[k]);

					/// Order by time here.
					feed_path = get_filepath(feeds_array[k] + ".store.txt");
					String[] new_titles = read_csv_to_array("title", feed_path + ".content.txt");
					titles.addAll(Arrays.asList(new_titles));
					descriptions.addAll(Arrays.asList(read_csv_to_array("description", feed_path + ".content.txt")));
					String[] link = read_csv_to_array("link", feed_path + ".content.txt");
					if(link[0].length()<10)
						link = read_csv_to_array("id", feed_path + ".content.txt");
					links.addAll(Arrays.asList(link));

					/// Get feed icon
					/*try{
						String line = (new BufferedReader(new FileReader(new File(feed_path + ".content.txt")))).readLine();
						int content_start = line.indexOf("icon|") + 5;
						String icon_url = line.substring(content_start, line.indexOf('|', content_start));
						content_start = line.indexOf("title|") + 6;
						String icon_name = line.substring(content_start, line.indexOf('|', content_start)) + ".png";					

						download_file(icon_url, icon_name);

						Drawable feed_icon = Drawable.createFromPath(get_filepath(icon_name));
						for(int l=0; l<new_titles.length; l++)
							icons.add(feed_icon);
					}
					catch(Exception e)
					{
					}*/
				}
				if(titles.size()>0)
				{
					get_card_adapter(i).add_list(titles, descriptions, links);
					get_card_adapter(0).add_static_list(titles, descriptions, links);
					publishProgress(i);
				}
			}
			long lo = 1;
			return lo;
		}

		protected void onProgressUpdate(Integer... progress)
		{
			get_card_adapter(progress[0]).notifyDataSetChanged();
			get_card_adapter(0).notifyDataSetChanged();
		}

		protected void onPostExecute(Long tun)
		{
			if(viewPager.getOffscreenPageLimit() > 1)
				viewPager.setOffscreenPageLimit(1);
			set_refresh(false);
		}
	}

	private card_adapter get_card_adapter(int page_index)
	{
		return ((card_adapter)((ArrayListFragment) getFragmentManager()
						.findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	private void update_feed(String feed_name)
	{
		String line = "";
		boolean found = false;
		try{
			BufferedReader read = (new BufferedReader(new FileReader(new File(get_filepath("all_feeds.txt")))));
			while(!(line.contains(feed_name))){
				line = read.readLine();
				found = true;
			}
			if(found)
			{
				int content_start = line.indexOf(feed_name + "|") + feed_name.length() + 1;
				String feed_url = line.substring(content_start, line.indexOf('|', content_start));

				int length_before = read_file_to_array(feed_name + ".store.txt.content.txt").length;
				
				download_file(feed_url, feed_name + ".store.txt");
				String feed_path = get_filepath(feed_name + ".store.txt");
				new parsered(feed_path);
				(new File(feed_path)).delete();
				
				if(length_before > 1)
					remove_duplicates(feed_name + ".store.txt.content.txt", length_before);
			}
		}
		catch(Exception e){}
	}

	private void remove_duplicates(String file_name, int length_before)
	{
		/// Remove the duplicate title
		String file_path = get_filepath(file_name);
		File temp = new File(file_path);

		String[] title_remove = read_file_to_array(file_name);
		temp.delete();
		for(int i=0; i<title_remove.length ; i++)
		{
			if(i != length_before)
				append_string_to_file(file_name, title_remove[i] + "\n");
		}

		String[] titles = read_csv_to_array("title", file_path);
		int[] is_duplicate = new int[titles.length];
		boolean found = false;
		int index = 0;
		for(int k=0; k<titles.length - 1; k++)
		{
			if(is_duplicate[k] == 0)
			{
				for(int i=k + 1; i<titles.length; i++)
				{
					if(titles[k].equals(titles[i]))
					{
						found = true;
						index = i;
						break;
					}
				}
			}
			if(found)
				is_duplicate[index] = 1;
			found = false;
			index = 0;
		}
		String[] feeds_old = read_file_to_array(file_name);
		append_string_to_file("dump.txt", "Size of feeds_old = " + feeds_old.length + "\n");
		temp.delete();
		append_string_to_file(file_name, feeds_old[0] + "\n");
		for(int i=0; i<feeds_old.length - 1; i++)
		{
			append_string_to_file("dump.txt", is_duplicate[i] + "\n");
			if(is_duplicate[i] == 0)
				append_string_to_file(file_name, feeds_old[i+1] + "\n");
		}
	}

	private void log(String text)
	{
		append_string_to_file(get_filepath("dump.txt", text + "\n");
	}
}
