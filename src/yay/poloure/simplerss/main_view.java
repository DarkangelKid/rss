package yay.poloure.simplerss;

import android.preference.PreferenceFragment;

import android.content.Context;
import android.content.DialogInterface;

import android.app.AlertDialog;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;

import android.app.Activity;

import android.app.ListFragment;
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

	private Button btnClosePopup;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager);

		getActionBar().setIcon(R.drawable.rss_icon);
		MyFragmentPagerAdapter page_adapter = new MyFragmentPagerAdapter(getFragmentManager());

		String[] nav_items = new String[]{"Manage", "Settings"};
		String[] feeds_array = read_file_to_array("group_list.txt");		
		String[] nav_final = new String[feeds_array.length + nav_items.length];
		for(int i=0; i<nav_items.length; i++){
			nav_final[i] = nav_items[i];
		}
		for(int i=nav_items.length; i<nav_final.length; i++){
			nav_final[i] = feeds_array[i - nav_items.length];
			page_adapter.add_page(feeds_array[i - nav_items.length]);
		}
		
		navigation_list = (ListView) findViewById(R.id.left_drawer);
		navigation_list.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, nav_final));
		navigation_list.setOnItemClickListener(new DrawerItemClickListener());

		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(page_adapter);
		
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, 8388611);
		drawer_toggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			public void onDrawerClosed(View view) {
				getActionBar().setTitle("Simple RSS");
			}

			public void onDrawerOpened(View drawerView) {
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
		public void onItemClick(AdapterView parent, View view, int position, long id)
		{
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		// Create a new fragment and specify the planet to show based on position
		Fragment fragment = new PrefsFragment();
		Bundle args = new Bundle();
		args.putInt("ARG_PLANET_NUMBER", position);
		fragment.setArguments(args);

		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment)
					.commit();

		// Highlight the selected item, update the title, and close the drawer
		navigation_list.setItemChecked(position, true);
		setTitle("Preferences");
		mDrawerLayout.closeDrawer(navigation_list);
	}

	@Override
	public void setTitle(CharSequence title) {
		getActionBar().setTitle(title);
	}

	public static class PrefsFragment extends PreferenceFragment
	{

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.preferences);
		}
	}

	public static class MyFragmentPagerAdapter extends FragmentPagerAdapter
	{
		private static List<String> group_list = new ArrayList();

		public MyFragmentPagerAdapter(FragmentManager fm)
		{
			///add function for reading groups from file
			super(fm);
			group_list = new ArrayList();
		}

		private static void add_page(String title){
			group_list.add(title);
		}
 
		@Override
		public int getCount(){
			return group_list.size();
		}

 		@Override
		public Fragment getItem(int position){
			return ArrayListFragment.newInstance(position);
		}

		@Override
		public String getPageTitle(int position){
			for(int i = 0; i < group_list.size(); i++)
			{
				if(position == i)
					return group_list.get(position);
			}
			return "";
		}
	}

	public static class ArrayListFragment extends ListFragment
	{
		int mNum;
		
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
			if(mNum == 0)
				setListAdapter(new card_adapter(getActivity()));
		}

		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
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
			String file_path = get_filepath("all_feeds.txt");
			String[] feeds_array = read_feeds_to_array(0, file_path);
			String[] url_array = read_feeds_to_array(1, file_path);

			File wait;
			String feed_path;
			List<String> one = new ArrayList();
			List<String> two = new ArrayList();
			List<String> three = new ArrayList();
			boolean result;

			for(int k=0; k<feeds_array.length; k++)
			{
				feed_path = get_filepath(feeds_array[k] + ".store");
				download_file(url_array[k], feeds_array[k] + ".store", "nocheck");

				wait = new File(feed_path);
				int j = 0;
				while((!wait.exists())&&(j<100))
				{
					try
					{
						Thread.sleep(50);
					}
					catch(Exception e)
					{
					}
					j++;
				}
				
				parsered papa = new parsered(feed_path);

				result = wait.delete();
				
				String[] titles = read_csv_to_array("title", feed_path + ".content.txt");
				String[] links = read_csv_to_array("link", feed_path + ".content.txt");
				String[] descriptions = read_csv_to_array("description", feed_path + ".content.txt");

				for(int i=0; i<titles.length; i++)
				{
					one.add(titles[i]);
					try{
						two.add(descriptions[i]);
					}
					catch(Exception e){}
					three.add(links[i]);
				}
			}

			card_adapter.add_list(one, two, three);
			((BaseAdapter) ((ListView) findViewById(android.R.id.list)).getAdapter()).notifyDataSetChanged();

			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void show_add_dialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View add_rss_dialog = inflater.inflate(R.layout.add_rss_dialog, null);

		
		String[] array_spinner = read_file_to_array("group_list.txt");
		boolean all_exists = false;
		for(int i=0; i<array_spinner.length; i++)
		{
			if(array_spinner[i].equals("All"))
			{
				all_exists = true;
				break;
			}
		}
		if(!all_exists)
			add_group("All");

		array_spinner = read_file_to_array("group_list.txt");

		Spinner group_spinner = (Spinner) add_rss_dialog.findViewById(R.id.group_spinner);
		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.group_spinner_text, array_spinner);
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
						Boolean rss = false;
						EditText URL_edit = (EditText) add_rss_dialog.findViewById(R.id.URL_edit);
						String URL_check = URL_edit.getText().toString();
						File in = new File(get_filepath("URLcheck.txt"));
						in.delete();
						download_file(URL_check, "URLcheck.txt", "check_mode");
						File wait = new File(get_filepath("URLcheck.txt"));
						int j = 0;
						while((!wait.exists())&&(j<120))
						{
							try
							{
								Thread.sleep(10);
							}
							catch(Exception e)
							{
							}
							j++;
						}
						if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
						{
							try
							{
								BufferedReader reader = new BufferedReader(new FileReader(in));
								try{
									reader.readLine();
									if(reader.readLine().contains("rss"))
										rss = true;
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
							in.delete();
						}
						if(rss!= null && !rss)
						{
							toast_message("Invalid RSS URL", 0);
						}
						else
						{
							add_feed(((EditText) add_rss_dialog.findViewById(R.id.name_edit)).getText().toString(), URL_check, "Add");
							///A function will add this rss title and url to								
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

	private void download_file(String url, String file_name, String check)
	{
		DownloadFile downloadFile = new DownloadFile();
		downloadFile.execute(url, file_name, check);
	}

	private class DownloadFile extends AsyncTask<String, Integer, String>
	{
		@Override
		protected String doInBackground(String... sUrl)
		{
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			{
				try
				{
					URL url = new URL(sUrl[0]);
					URLConnection connection = url.openConnection();
					connection.connect();

					InputStream input = new BufferedInputStream(url.openStream());
					OutputStream output = new FileOutputStream(get_filepath(sUrl[1]));

					if(sUrl[2].equals("check_mode"))
					{
						byte data[] = new byte[256];
						int count;
						count = input.read(data);
						output.write(data, 0, count);
					}
					else
					{
						byte data[] = new byte[1024];
						int count;
						while ((count = input.read(data)) != -1)
							output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();
				}
				catch (Exception e)
				{
				}
			}
			return null;
		}
	}

	private String get_filepath(String filename)
	{
		return this.getExternalFilesDir(null).getAbsolutePath() + "/" + filename;
	}

	public void append_string_to_file(String file_name, String string)
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

				Boolean rte = out.renameTo(in);
				reader.close();
				writer.close();
			}
			catch (Exception e)
			{
			}
		}
	}

	private void add_feed(String feed_name, String feed_url, String feed_group)
	{
		append_string_to_file(feed_group + ".txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
		append_string_to_file("all_feeds.txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
	}

	private void delete_feed(String feed_name, String feed_url, String feed_group)
	{
		remove_string_from_file(feed_group + ".txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
		remove_string_from_file("all_feeds.txt", feed_name + "|" + feed_url + "|" + feed_group + "\n");
	}

	private void add_group(String group_name)
	{
		append_string_to_file("group_list.txt", group_name + "\n");
	}

	private void delete_group(String group_name)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			remove_string_from_file("group_list.txt", group_name);
			File file = new File(get_filepath(group_name + ".txt"));
			file.delete();
		}
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

				while((line = reader.readLine()) != null)
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
		else
			line_values = new String[0];
		
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

			while((line = reader.readLine()) != null)
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
				if(index == 1)
				{
					int bar_index = line.indexOf('|', 0);
					line = line.substring(bar_index + 1, line.indexOf('|', bar_index + 1));
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
		try
		{
			String line;
			int number_of_lines = 0, i = 0;
			File in = new File(feed_path);

			BufferedReader reader = new BufferedReader(new FileReader(in));

			while((line = reader.readLine()) != null)
				number_of_lines++;

			reader.close();
			reader = new BufferedReader(new FileReader(in));

			content_values = new String[number_of_lines];

			while((line = reader.readLine()) != null)
			{
				int content_start = line.indexOf(content_type) + content_type.length() + 1;
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
}
