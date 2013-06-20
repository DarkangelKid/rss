package yay.poloure.simplerss;

import android.preference.PreferenceFragment;

import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.ClipData;

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
import android.graphics.drawable.BitmapDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.View.OnDragListener;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import java.util.Iterator;
import java.util.Set;
import java.io.StringWriter;
import java.io.PrintWriter;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class main_view extends Activity
{
	private DrawerLayout mDrawerLayout;
	private View.OnClickListener refreshListener;
	
	private ListView navigation_list;
	private ActionBarDrawerToggle drawer_toggle;
	private Menu optionsMenu;

	private Button btnClosePopup;

	private String mTitle;
	private CharSequence MainTitle;
	public static float density;
	public int width;

	private static int download_finished;
	private viewpager_adapter page_adapter;
	private ViewPager viewPager;
	public static Resources res;

	private static String[] current_groups;
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

		current_groups = read_file_to_array("group_list.txt");
		if(current_groups.length == 0)
			append_string_to_file("group_list.txt", "All\n");

		getActionBar().setIcon(R.drawable.rss_icon);
		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction()
						.add(CONTENT_VIEW_ID, new top_fragment())
						.commit();
			feed = new the_feed_fragment();
			pref = new PrefsFragment();
			man = new manager_fragment();
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

		nav_items = new String[]{"Feeds", "Manage", "Settings"};
		nav_final = new String[current_groups.length + nav_items.length];
		System.arraycopy(nav_items, 0, nav_final, 0, nav_items.length);
		System.arraycopy(current_groups, 0, nav_final, nav_items.length, current_groups.length);

		navigation_list = (ListView) findViewById(R.id.left_drawer);
		ArrayAdapter<String> nav_adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, nav_final);
		navigation_list.setAdapter(nav_adapter);
		navigation_list.setOnItemClickListener(new DrawerItemClickListener());

		page_adapter = new viewpager_adapter(getFragmentManager());

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(page_adapter);
		viewPager.setOffscreenPageLimit(128);

		update_groups();

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

	private boolean selectItem(int position)
	{
		if(position < 3)
		{
			Fragment fragment;
			if(position == 2)
				switch_page("Settings", position);
			else if(position == 1)
				switch_page("Manage", position);
			else
				switch_page("Feeds", position);
		}
		else
		{
			switch_page("Feeds", position);
			int page = position - 3;
			viewPager.setCurrentItem(page);
		}
		return true;
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

	public class top_fragment extends Fragment
	{
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.pager, container, false);
		}
	}

	public class the_feed_fragment extends Fragment
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

	public class manager_fragment extends Fragment
	{
		private ListView manage_list;
		//private ArrayAdapter<String> manage_adapter;
		private group_adapter manage_adapter;

		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.manage_fragment, container, false);
			manage_list = (ListView) view.findViewById(R.id.group_listview);
			manage_adapter = new group_adapter(getActivity());
			for(int i = 0; i < current_groups.length; i++)
				manage_adapter.add_list(current_groups[i]);
			manage_list.setAdapter(manage_adapter);
			manage_adapter.notifyDataSetChanged();
			/*manage_adapter = new ArrayAdapter<String>(getActivity(), R.layout.manage_list_item, R.id.group_item, current_groups);
			manage_list.setAdapter(manage_adapter);
			manage_adapter.notifyDataSetChanged();*/
			return view;
		}

	}
	
	//shit start
	//findViewById(R.id.bottomright).setOnDragListener(new MyDragListener());
	 
	/*class MyDragListener implements OnDragListener
	{
		Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
		Drawable normalShape = getResources().getDrawable(R.drawable.shape);
		
		@Override
		public boolean onDrag(View v, DragEvent event)
		{
			int action = event.getAction();
			switch (event.getAction())
			{
				case DragEvent.ACTION_DRAG_STARTED:
					break;
					
				case DragEvent.ACTION_DRAG_ENTERED:
					v.setBackgroundDrawable(enterShape);
					break;
					
				case DragEvent.ACTION_DRAG_EXITED:        
					v.setBackgroundDrawable(normalShape);
					break;
					
				case DragEvent.ACTION_DROP:
					  View view = (View) event.getLocalState();
					  ViewGroup owner = (ViewGroup) view.getParent();
					  owner.removeView(view);
					  LinearLayout container = (LinearLayout) v;
					  container.addView(view);
					  view.setVisibility(View.VISIBLE);
					  break;
					  
				case DragEvent.ACTION_DRAG_ENDED:
					  v.setBackgroundDrawable(normalShape);
					  default:
					  break;
			}
			return true;
		}
	}*/
	//shit end

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
		private static int mNum;
		
		static card_fragment newInstance(int num)
		{
			card_fragment f = new card_fragment();
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);
			return f;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState){
			super.onActivityCreated(savedInstanceState);
		}

		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setListAdapter(new card_adapter(getActivity()));
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
			long lo = 1;
			return lo;
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
		if((current_groups.length+3)>nav_final.length)
		{
			String[] nav_finaler = new String[nav_final.length + 1];
			for(int i=0; i<nav_final.length; i++)
				nav_finaler[i] = nav_final[i];
			nav_finaler[nav_finaler.length - 1] = current_groups[current_groups.length - 1];
			nav_final = nav_finaler;
			ArrayAdapter<String> nav_adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, nav_final);
			navigation_list.setAdapter(nav_adapter);
			viewPager.getAdapter().notifyDataSetChanged();
		}
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

	private String[] read_images_to_array(String feed_path)
	{
		ArrayList<String> content_values = new ArrayList<String>();
		try
		{
			String line;
			int i = 0;
			File in = new File(feed_path);
			BufferedReader reader = new BufferedReader(new FileReader(in));
			reader.readLine();

			while((line = reader.readLine()) != null)
			{
				int content_start = line.indexOf("image|") + 6;
				line = line.substring(content_start, line.indexOf('|', content_start));
				if(line.length()>5)
					content_values.add(line);
				else
					content_values.add("null");
				i++;
			}
		}
		catch (Exception e)
		{
		}

		return content_values.toArray(new String[0]);
	}

	private class refresh_feeds extends AsyncTask<Void, Integer, Long> {

		protected void onPreExecute(){
			set_refresh(true);
		}
		protected Long doInBackground(Void... ton)
		{			
			for(int i=0; i<current_groups.length; i++)
			{
				String[] feeds_array = read_feeds_to_array(0, get_filepath(current_groups[i] + ".txt"));
				String[] titles, descriptions, links, images;

				for(int k=0; k<feeds_array.length; k++)
				{
					if(update_feed(feeds_array[k]))
					{
						String content_path = get_filepath(feeds_array[k] + ".store.txt") + ".content.txt";
						titles = 			read_csv_to_array("title", content_path);
						images = 			read_images_to_array(content_path);
						descriptions = 		read_csv_to_array("description", content_path);
						links = 			read_csv_to_array("link", content_path);
						if(links[0].length()<10)
							links = 		read_csv_to_array("id", content_path);
						ArrayList<String> image_set = new ArrayList<String>();
						ArrayList<Integer> image_heights = new ArrayList<Integer>();
						ArrayList<Integer> image_widths = new ArrayList<Integer>();

						for(int m=0; m<titles.length; m++)
						{
							if(!images[m].equals("null"))
							{
								String icon_name = images[m].substring(images[m].lastIndexOf("/") + 1, images[m].length());
								String image_path = get_filepath(icon_name);
								if(!((new File(image_path)).exists()))
								{
									download_file(images[m], icon_name);
									compress_file(image_path);
								}
								image_set.add(image_path);
								Integer[] dim = get_dim(image_path);
								image_heights.add(dim[1]);
								image_widths.add(dim[0]);
							}
							else
							{
								image_set.add("");
								image_heights.add(0);
								image_widths.add(0);
							}
						}

						//smoothScrollToPosition(int position)
						if(titles.length>0)
						{
							card_adapter ith = get_card_adapter(i);
							List<String> ith_list = ith.return_titles();

							for(int j=0; j<titles.length; j++)
							{
								if(ith_list.size()>0)
								{
									if(!ith_list.contains(titles[j]))
									{
										ith					.add_list(titles[j], descriptions[j], links[j], image_set.get(j), image_heights.get(j), image_widths.get(j));
										get_card_adapter(0)	.add_list(titles[j], descriptions[j], links[j], image_set.get(j), image_heights.get(j), image_widths.get(j));
										publishProgress(i);
									}
								}
								else
								{
									ith					.add_list(titles[j], descriptions[j], links[j], image_set.get(j), image_heights.get(j), image_widths.get(j));
									get_card_adapter(0)	.add_list(titles[j], descriptions[j], links[j], image_set.get(j), image_heights.get(j), image_widths.get(j));
									publishProgress(i);
								}
							}
						}
					}
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
		return ((card_adapter)((card_fragment) getFragmentManager()
						.findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	private boolean update_feed(String feed_name)
	{
		boolean found = false;
		try{
			String line = "";
			BufferedReader read = (new BufferedReader(new FileReader(new File(get_filepath("all_feeds.txt")))));
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
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			append_string_to_file("updater.dump.txt", sw.toString());	
			return false;
		}
	}

	private void remove_duplicates(String content_name)
	{
		String content_path = get_filepath(content_name);
		File temp = new File(content_path);

		String[] feeds = read_file_to_array(content_name);
		Set<String> set = new LinkedHashSet<String>(Arrays.asList(feeds));
		temp.delete();
		feeds = set.toArray(new String[0]);
		for(int i=0; i<feeds.length; i++)
			append_string_to_file(content_name, feeds[i] + "\n");
	}

	private void log(String text)
	{
		append_string_to_file("dump.txt", text + "\n");
	}

	public void compress_file(String file_path) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file_path, o);

		if(width < 10)
		{
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			width = (int) Math.round(((float)size.x)*0.90);
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
		Bitmap bitmap = BitmapFactory.decodeFile(file_path, o2);

		try
		{
			FileOutputStream out = new FileOutputStream(file_path + ".small.png");
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		}
		catch (Exception e){
		}
	}

	private Integer[] get_dim(final String image_path)
	{
		Integer[] size = new Integer[2];
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inSampleSize = 1;
		Bitmap bitmap = BitmapFactory.decodeFile(image_path, o);
		size[0] = o.outWidth;
		size[1] = o.outHeight;
		return size;
	}
}
