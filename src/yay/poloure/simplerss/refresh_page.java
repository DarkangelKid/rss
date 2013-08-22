package yay.poloure.simplerss;

import android.support.v4.app.ListFragment;
import android.os.AsyncTask;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

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
			existing_items = new HashSet<String>();
			Collections.addAll(existing_items, utilities.get_adapter_feeds_cards(main.fragment_manager, main.viewpager, page_number).links);
		}
		catch(Exception e)
		{
		}

		Animation animFadeIn = AnimationUtils.loadAnimation(main.activity, android.R.anim.fade_in);

		int width, height, count = 0;

		for(int m = 0; m < titles.length; m++)
		{
			if(!existing_items.contains(links[m]))
				count++;
		}

		final String[] new_titles	= new String[count];
		final String[] new_des		= new String[count];
		final String[] new_images	= new String[count];
		final String[] new_links	= new String[count];
		final int[]    new_heights	= new int[count];
		final int[]    new_widths	= new int[count];

		count = -1;

		for(int m = 0; m < titles.length; m++)
		{
			if(existing_items.add(links[m]))
			{
				count++;
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

				new_titles	[count] = titles[m];
				new_links	[count] = links[m];
				new_des		[count] = descriptions[m];
				new_images	[count] = thumbnail_path;
				new_heights	[count] = height;
				new_widths	[count] = width;
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
		if(new_titles.length > 0)
			publishProgress(new_titles, new_des, new_links, new_images, new_heights, new_widths);

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

		ith.add_array((String[]) progress[0], (String[]) progress[1], (String[]) progress[2], (String[]) progress[3], (int[]) progress[4], (int[]) progress[5]);
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
