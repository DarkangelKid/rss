package yay.poloure.simplerss;

import android.support.v4.app.ListFragment;
import android.os.AsyncTask;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import android.widget.ListView;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

class refresh_page extends AsyncTask<Void, Object, Animation>
{
	final int page_number;
	Boolean waited = true;
	Boolean flash  = false;
	ListFragment l;
	adapter_feeds_cards ith;
	ListView lv;
	List<Integer> counts;
	int number_of_items = 0;
	int oldest_unread = 0;

	public refresh_page(int page)
	{
		page_number = page;
	}

	@Override
	protected Animation doInBackground(Void[] hey)
	{
		if(main.new_items.get(page_number))
		{
			while(service_update.check_service_running(main.activity))
			{
				try{
					Thread.sleep(100);
				}
				catch(Exception e){
				}
			}
		}

		String group							= main.current_groups.get(page_number);
		final String group_path				= main.storage + main.GROUPS_DIRECTORY + group + main.SEPAR;
		final String group_file_path		= group_path + group + main.TXT;
		final String group_content_path	= group_path + group + main.CONTENT_APPENDIX;
		String thumbnail_path;

		if((!utilities.exists(group_file_path))||(!utilities.exists(group_content_path)))
			return null;

		String[][] contenter			= utilities.load_csv_to_array(group_content_path);
		String[] titles				= contenter[0];
		String[] descriptions		= contenter[1];
		String[] links					= contenter[2];
		String[] images				= contenter[3];
		String[] widths				= contenter[4];
		String[] heights				= contenter[5];
		String[] groups				= contenter[6];
		String[] sources				= contenter[7];

		if(links[0] == null || links.length == 0)
			return null;

		Set<String> existing_items = new HashSet<String>();
		try
		{
			existing_items = new HashSet<String>(utilities.get_adapter_feeds_cards(main.fragment_manager, main.viewpager, page_number).content_links);
		}
		catch(Exception e)
		{
		}

		Animation animFadeIn = AnimationUtils.loadAnimation(main.activity, android.R.anim.fade_in);
		final int size = titles.length;
		final List<String> new_titles			= new ArrayList<String>();
		final List<String> new_descriptions = new ArrayList<String>();
		final List<String> new_links			= new ArrayList<String>();
		final List<String> new_images			= new ArrayList<String>();
		final List<Integer> new_widths		= new ArrayList<Integer>();
		final List<Integer> new_heights		= new ArrayList<Integer>();

		int width, height;

		for(int m = 0; m < size; m++)
		{
			if(existing_items.add(links[m]))
			{
				thumbnail_path = "";
				width = 0;
				height = 0;

				if(images[m] != null)
				{
					width = Integer.parseInt(widths[m]);
					if(width > 32)
					{
						height = Integer.parseInt(heights[m]);
						thumbnail_path = main.storage + main.GROUPS_DIRECTORY + groups[m] + main.SEPAR + sources[m] + main.SEPAR + main.THUMBNAIL_DIRECTORY + images[m].substring(images[m].lastIndexOf(main.SEPAR) + 1, images[m].length());
					}
					else
						width = 0;
				}

				if(descriptions[m] == null || descriptions[m].length() < 8)
					descriptions[m] = "";
				if(titles[m] == null)
					titles[m] = "";

				new_titles			.add(titles[m]);
				new_links			.add(links[m]);
				new_descriptions	.add(descriptions[m]);
				new_images			.add(thumbnail_path);
				new_heights			.add(height);
				new_widths			.add(width);
				number_of_items++;
			}
			/* If this item has not been read, save its position. */
			if(!adapter_feeds_cards.read_items.contains(links[m]) && oldest_unread == 0)
				oldest_unread = m;
		}
		main.new_items.set(page_number, false);
		if(oldest_unread == 0)
			oldest_unread = number_of_items;

		while(lv == null)
		{
			try{
				Thread.sleep(5);
			}
			catch(Exception e){
			}
			if((main.viewpager != null)&&(l == null))
				l = (ListFragment) main.fragment_manager.findFragmentByTag("android:switcher:" + main.viewpager.getId() + ":" + Integer.toString(page_number));
			if((l != null)&&(ith == null))
				ith = ((adapter_feeds_cards) l.getListAdapter());
			if((l != null)&&(lv == null))
			{
				try
				{
					lv = l.getListView();
				}
				catch(IllegalStateException e)
				{
					lv = null;
				}
			}
		}
		if(new_titles.size() > 0)
			publishProgress(new_titles, new_descriptions, new_links, new_images, new_heights, new_widths);

		counts = main.get_unread_counts();

		return animFadeIn;
	}

	@Override
	protected void onProgressUpdate(Object[] progress)
	{
		if(ith.getCount() == 0)
		{
			lv.setVisibility(View.INVISIBLE);
			waited = false;
			flash  = true;
		}

		ith.add_list((List<String>) progress[0], (List<String>) progress[1], (List<String>) progress[2], (List<String>) progress[3], (List<Integer>) progress[4], (List<Integer>) progress[5]);
		ith.notifyDataSetChanged();
	}

	@Override
	protected void onPostExecute(Animation tun)
	{
		if(lv == null)
			return;

		main.set_refresh(service_update.check_service_running(main.activity));

		main.update_navigation_data(counts, false);

		if(flash)
		{
			lv.setSelection(number_of_items - oldest_unread);
			lv.setAnimation(tun);
			lv.setVisibility(View.VISIBLE);
		}
		main.update_navigation_data(null, false);
	}
}
