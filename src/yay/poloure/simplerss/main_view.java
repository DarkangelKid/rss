package yay.poloure.simplerss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.content.res.Configuration;

import android.os.Environment;
import android.content.Context;
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


public class main_view extends FragmentActivity
{
	private DrawerLayout mDrawerLayout;
	/// Actionbar Toggle to open navigation drawer.
	private ActionBarDrawerToggle drawer_toggle;
	private View.OnClickListener refreshListener;
	private String[] mPlanetTitles;
	private ListView mDrawerList;
	SectionsPagerAdapter page_adapter;
	ViewPager view_pager;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager);

		mPlanetTitles = getResources().getStringArray(R.array.planets_array);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));

		page_adapter = new SectionsPagerAdapter(getSupportFragmentManager());
		view_pager = (ViewPager) findViewById(R.id.pager);
		view_pager.setAdapter(page_adapter);

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
					return "PAGE 1";
				case 1:
					return "PAGE 2";
				case 2:
					return "PAGE 3";
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

			setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.card_layout, R.id.label, values));
		}
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menu_inflater = getMenuInflater();
		menu_inflater.inflate(R.menu.main_overflow, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawer_toggle.onOptionsItemSelected(item)) {
          return true;
        }
        return super.onOptionsItemSelected(item);
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

	public String get_filepath(String filename)
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

	private void add_feed(String feed_name, String feed_url, String feed_group){
		append_string_to_file(feed_group + ".txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
		append_string_to_file("all_feeds.txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
	}
	
	private void delete_feed(String feed_name, String feed_url, String feed_group){
		remove_string_from_file(feed_group + ".txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
		remove_string_from_file("all_feeds.txt", "\n" + feed_name + "; " + feed_url + "; " + feed_group);
	}

	private void add__group(String group_name){
		append_string_to_file("group_list.txt", "\n" + group_name);
	}

	private void delete_group(String group_name){
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			remove_string_from_file("group_list.txt", group_name);
			File file = new File(get_filepath(group_name + ".txt"));
			file.delete();
		}
	}
}

