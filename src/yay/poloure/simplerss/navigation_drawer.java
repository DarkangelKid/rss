package yay.poloure.simplerss;

import android.app.Activity;
import android.widget.ListView;
import android.view.View;
import android.content.Context;
import android.support.v4.app.ActionBarDrawerToggle;
import android.widget.AdapterView;
import android.view.Gravity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentTransaction;

class navigation_drawer
{
	private static adapter_navigation_drawer	nav_adapter;
	public static DrawerLayout						drawer_layout;
	public static ActionBarDrawerToggle			drawer_toggle;

	public static final String[] NAVIGATION_TITLES = new String[3];

	private static String							current_title;

	private final ListView navigation_list;

	public navigation_drawer(Activity activity, Context context, DrawerLayout draw_layout, ListView nav_list)
	{
		NAVIGATION_TITLES[0]	= context.getString(R.string.feeds_title);
		NAVIGATION_TITLES[1]	= context.getString(R.string.manage_title);
		NAVIGATION_TITLES[2]	= context.getString(R.string.settings_title);

		current_title		= NAVIGATION_TITLES[0];

		nav_adapter			= new adapter_navigation_drawer(context);
		navigation_list	= nav_list;
		drawer_layout		= draw_layout;

		drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.END);
		drawer_toggle = new ActionBarDrawerToggle(activity, drawer_layout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			@Override
			public void onDrawerClosed(View view)
			{
				main.action_bar.setTitle(current_title);
			}

			@Override
			public void onDrawerOpened(View drawerView)
			{
				main.action_bar.setTitle(main.NAVIGATION);
			}
		};

		drawer_layout.setDrawerListener(drawer_toggle);
		drawer_toggle.syncState();

		navigation_list.setOnItemClickListener(new click_navigation_drawer());
		navigation_list.setAdapter(nav_adapter);
	}

	public static void update_navigation_data(int[] counts, Boolean update_names)
	{
		if(counts == null)
			counts = utilities.get_unread_counts(main.storage, main.current_groups);

		if(update_names)
			nav_adapter.set_titles(main.current_groups);

		nav_adapter.set_counts(counts);
		nav_adapter.notifyDataSetChanged();
	}

	private class click_navigation_drawer implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id)
		{
			switch(position)
			{
				case 0:
					switch_page(NAVIGATION_TITLES[0], 0);
					break;
				case 1:
					switch_page(NAVIGATION_TITLES[1], 1);
					break;
				case 2:
					switch_page(NAVIGATION_TITLES[2], 2);
					break;
				default:
					switch_page(NAVIGATION_TITLES[0], position);
					main.viewpager.setCurrentItem(position - 4);
			}
		}
	}

	private void switch_page(String page_title, int position)
	{
		drawer_layout.closeDrawer(navigation_list);
		if(!current_title.equals(page_title))
		{
			main.fragment_manager.beginTransaction()
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
						.hide(main.fragment_manager.findFragmentByTag(current_title))
						.show(main.fragment_manager.findFragmentByTag(page_title))
						.commit();

			navigation_list.setItemChecked(position, true);
			if(position < 3)
				set_title(page_title);
			else
				set_title(navigation_drawer.NAVIGATION_TITLES[0]);
		}
		current_title = page_title;
	}

	private static void set_title(String title)
	{
		current_title = title;
		main.action_bar.setTitle(title);
	}
}
