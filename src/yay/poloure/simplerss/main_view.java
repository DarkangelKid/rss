package yay.poloure.simplerss;

import yay.poloure.simplerss.card_adapter;
import android.content.Context;

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
import android.content.res.Configuration;

import android.widget.PopupWindow;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.Gravity;

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


public class main_view extends FragmentActivity
{
	private DrawerLayout mDrawerLayout;
	/// Actionbar Toggle to open navigation drawer.
	private ActionBarDrawerToggle drawer_toggle;
	private View.OnClickListener refreshListener;
	private String[] mPlanetTitles;
	private ListView mDrawerList;

	//private Button btnClosePopup;
	
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawer_toggle.onOptionsItemSelected(item)) {
			return true;
		}

		//initiate_add_window();
		return super.onOptionsItemSelected(item);
	}

	/*private PopupWindow add_window;

	private void initiate_add_window()
	{
		try
		{
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.add_popup,(ViewGroup) findViewById(R.id.popup_element));
			add_window = new PopupWindow(layout, 350, 350, true);
			add_window.showAtLocation(layout, Gravity.CENTER, 0, 0);

			btnClosePopup = (Button) layout.findViewById(R.id.btn_close_popup);
			btnClosePopup.setOnClickListener(cancel_button_click_listener);

		}
		catch (Exception e)
		{
		}
	}

	private OnClickListener cancel_button_click_listener = new OnClickListener()
	{
		public void onClick(View v)
		{
			add_window.dismiss();
		}
	};*/

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

	private void parse_local_xml(String file_name)
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try
		{
			SAXParser sp = spf.newSAXParser();
			sp.parse(get_filepath(file_name), this);
		}
		catch (exception e)
		{
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		tempVal = "";
		append_string_to_file("startElement.txt", "START: " + uri + "; " + localName + "; " + qName + "\n")
	}


	public void characters(char[] ch, int start, int length) throws SAXException {
		tempVal = new String(ch,start,length);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		append_string_to_file("startElement.txt", "END: " + uri + "; " + localName + "; " + qName + "\n")
	}
}

