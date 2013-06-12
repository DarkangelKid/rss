package yay.poloure.simplerss;

import yay.poloure.simplerss.card_adapter;
import yay.poloure.simplerss.parsered;
import android.content.Context;
import android.content.DialogInterface;


import android.app.AlertDialog;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.GravityCompat;
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
import android.widget.TextView; //not permanent
import android.widget.Toast;
import android.content.res.Configuration;

import android.widget.PopupWindow;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.Gravity;


import android.os.Environment;
import java.io.File;
import java.net.URL;

import android.os.AsyncTask;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.lang.Thread;


public class main_view extends FragmentActivity
{
	private DrawerLayout mDrawerLayout;
	/// Actionbar Toggle to open navigation drawer.
	private ActionBarDrawerToggle drawer_toggle;
	private View.OnClickListener refreshListener;
	private String[] mPlanetTitles;
	private ListView mDrawerList;

	private Button btnClosePopup;
	
	SectionsPagerAdapter page_adapter;
	ViewPager view_pager;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		getActionBar().setIcon(R.drawable.rss_icon);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager);

		mPlanetTitles = getResources().getStringArray(R.array.planets_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));

		page_adapter = new SectionsPagerAdapter(getSupportFragmentManager());
		view_pager = (ViewPager) findViewById(R.id.pager);
		view_pager.setAdapter(page_adapter);
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(Color.argb(0, 51, 181, 229));

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawer_toggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle("Simple RSS");
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle("Navigation");
			}
		};

		mDrawerLayout.setDrawerListener(drawer_toggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		//download_file("http://www.gamingonlinux.com/article_rss.php", "poop.xml");

		//download_file("http://www.textfiles.com/hacking/CABLE/cablefrq.txt", "archie.man");
		//String[] archie_lines = read_file_to_array("archie.man");
		//getActionBar().setTitle(archie_lines[30]);
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

	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{

		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount()
		{
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			Locale l = Locale.getDefault();
			switch (position)
			{
				case 0:
					return "ALL";
				case 1:
					return "TECHNOLOGY";
				case 2:
					return "ANDROID";
			}
			return null;
		}

	}


	public class DummySectionFragment extends ListFragment
	{
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment()
		{
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState)
		{
			super.onActivityCreated(savedInstanceState);
			String[] values;
			if(getArguments().getInt(ARG_SECTION_NUMBER) == 1)
			{
			values = new String[] { "Android", "iPhone", "WindowsMobile",
					"Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
					"Linux", "OS/2" };
			}
			else{
			values = new String[] {	"enen", "Blneackberry", "WenebOS", "Ubuhthntu", "Windnsthows7", "Max SHIT X",
					"Linthneux", "OS/2sith"};
			}

			setListAdapter(new card_adapter(get_context(), values));
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
		{
			View view = inflater.inflate(R.layout.fragment_main_dummy, container, false);
			return view;
		}
	}

	public Context get_context()
	{
			return this;
	}	

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
		{
			return true;
		}
		else if(item.getTitle().equals("add"))
		{
			show_add_dialog();
			return true;
		}
		else if(item.getTitle().equals("refresh"))
		{
			parsered papa = new parsered(get_filepath("poop.xml"));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void show_add_dialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View add_rss_dialog = inflater.inflate(R.layout.add_rss_dialog, null);

		String[] array_spinner = new String[] {"Mercury", "Venus", "Earth", "Mars", "Asteroid Belt", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto", "Kuiper Belt", "Oort CLoud"};
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
						download_file(URL_check, "URLcheck.txt");
						File wait = new File(get_filepath("URLcheck.txt"));
						int j = 0;
						while((wait.exists() == false)&&(j<120))
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
									if((reader.readLine().contains("rss")) == true)
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
						if(rss == false)
						{
							toast_message("Invalid RSS URL", 0);
						}
						else
						{
							///A function will add this rss title and url to								
							alertDialog.dismiss();
						}
					}
				});
			}
		});
		alertDialog.show();
	}

	public void toast_message(String message, int zero_or_one)
	{
		Context context = getApplicationContext();
		Toast message_toast = Toast.makeText(context, message, zero_or_one);
		message_toast.show();
	}

	private void download_file(String url, String file_name)
	{
		DownloadFile downloadFile = new DownloadFile();
		downloadFile.execute(url, file_name);
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

					byte data[] = new byte[1024];
					int count;
					while ((count = input.read(data)) != -1)
						output.write(data, 0, count);

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

				out.renameTo(in);
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
		append_string_to_file(feed_group + ".txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
		append_string_to_file("all_feeds.txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
	}

	private void delete_feed(String feed_name, String feed_url, String feed_group)
	{
		remove_string_from_file(feed_group + ".txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
		remove_string_from_file("all_feeds.txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
	}

	private void add__group(String group_name)
	{
		append_string_to_file("group_list.txt", "\n" + group_name);
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
}

