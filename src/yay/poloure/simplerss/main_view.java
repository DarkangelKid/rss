package yay.poloure.simplerss;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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

public class main_view extends FragmentActivity
{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private View.OnClickListener refreshListener;
    SectionsPagerAdapter page_adapter;
    ViewPager view_pager;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager);
        
        page_adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        view_pager = (ViewPager) findViewById(R.id.pager);
        view_pager.setAdapter(page_adapter);
       
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
        {
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        //getActionBar().setTitle(Integer.toString(result));
        
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

    public class DummySectionFragment extends Fragment
    {
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            DownloadFile downloadFile = new DownloadFile();
			downloadFile.execute("http://textfiles.com/internet/archie.man", "poo.txt");
            return rootView;
        }
    }


    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menu_inflater = getMenuInflater();
        menu_inflater.inflate(R.menu.main_overflow, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item)
    {
		getActionBar().setTitle("hi");
		return true;
	}
	
	private class DownloadFile extends AsyncTask<String, Integer, String>
	{
		@Override
		protected String doInBackground(String... sUrl) {
			try {
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
			catch (Exception e) {
			}
			return null;
		}
	}
	
	public String get_filepath(String filename)
	{
		return this.getExternalFilesDir(null).getAbsolutePath() + "/" + filename;
	}
	
	private void add_new_feed(String feed_url, String feed_group){
	}
}

