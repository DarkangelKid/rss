package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.io.File;

class fragment_manage_feed extends Fragment
{
	private static ListView feed_list;
	public  static adapter_manage_feeds	feed_list_adapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.manage_listviews, container, false);
		feed_list = (ListView) view.findViewById(R.id.manage_listview);
		feed_list.setOnItemClickListener
		(
			new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					add_edit_dialog.show_edit_feed_dialog(main.current_groups, main.activity_context, main.storage, position);
				}
			}
		);

		feed_list_adapter = new adapter_manage_feeds(getActivity());
		feed_list.setAdapter(feed_list_adapter);

		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new refresh_manage_feeds().execute();
		else
			new refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		feed_list.setOnItemLongClickListener
		(
			new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id)
				{
					new AlertDialog.Builder(main.activity_context)
					.setCancelable(true)
					.setNegativeButton
					(
						main.DELETE_DIALOG,
						new DialogInterface.OnClickListener()
						{
							/// Delete the feed.
							@Override
							public void onClick(DialogInterface dialog, int id)
							{
								String group = feed_list_adapter.get_info(pos);
								group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
								final String name = feed_list_adapter.getItem(pos);

								final String group_file		= main.storage + main.GROUPS_DIRECTORY + group +    main.SEPAR + group + main.TXT;
								final String group_prepend	= main.storage + main.GROUPS_DIRECTORY + group +    main.SEPAR + group;
								final String all_file		= main.storage + main.GROUPS_DIRECTORY + main.ALL + main.SEPAR + main.ALL;

								utilities.delete_directory(new File(main.storage + main.GROUPS_DIRECTORY + group + main.SEPAR + name));
								utilities.remove_string_from_file(group_file, name, true);
								utilities.remove_string_from_file(all_file + main.TXT, name, true);

								utilities.delete_if_empty(group_file);
								if(!(new File(group_file).exists()))
								{
									utilities.delete_directory(new File(main.storage + main.GROUPS_DIRECTORY + group));
									utilities.remove_string_from_file(main.storage + main.GROUP_LIST, group, false);
								}
								else
								{
									utilities.sort_group_content_by_time(main.storage, group, main.ALL);
									utilities.delete_if_empty(group_prepend + main.CONTENT_APPENDIX);
									utilities.delete_if_empty(group_prepend + main.COUNT_APPENDIX);
								}

								String[] all_groups = utilities.read_file_to_array(main.storage + main.GROUP_LIST);
								if(all_groups.length == 1)
									utilities.delete_directory(new File(main.storage + main.GROUPS_DIRECTORY + main.ALL));

								else if(all_groups.length != 0)
								{
									/* This line may be broken. */
									utilities.sort_group_content_by_time(main.storage, main.ALL, main.ALL);
									utilities.delete_if_empty(all_file + main.CONTENT_APPENDIX);
									utilities.delete_if_empty(all_file + main.COUNT_APPENDIX);
								}

								main.update_groups();
								feed_list_adapter.remove_item(pos);
								feed_list_adapter.notifyDataSetChanged();

								if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
									new fragment_manage_group.refresh_manage_groups().execute();
								else
									new fragment_manage_group.refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							}
						}
					)
					.setPositiveButton
					(
						main.CLEAR_DIALOG,
						new DialogInterface.OnClickListener()
						{
							/// Delete the cache.
							@Override
							public void onClick(DialogInterface dialog, int id)
							{
								String group				= feed_list_adapter.get_info(pos);
								group							= group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
								final String name			= feed_list_adapter.getItem(pos);
								final String path			= main.storage + main.GROUPS_DIRECTORY + group + main.SEPAR + name;
								final File feed_folder	= new File(path);
								utilities.delete_directory(feed_folder);
								/// make the image and thumnail folders.
								(new File(path + main.SEPAR + main.IMAGE_DIRECTORY))		.mkdir();
								(new File(path + main.SEPAR + main.THUMBNAIL_DIRECTORY))	.mkdir();

								/// Delete the all content files.
								(new File(main.storage + main.GROUPS_DIRECTORY + main.ALL + main.SEPAR + main.ALL + main.CONTENT_APPENDIX)).delete();
								(new File(main.storage + main.GROUPS_DIRECTORY + main.ALL + main.SEPAR + main.ALL + main.COUNT_APPENDIX)).delete();

								//feed_list_adapter.notifyDataSetChanged();
								/// Refresh pages and update groups and stuff
								main.update_groups();
								if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
								{
									new refresh_manage_feeds()									.execute();
									new fragment_manage_group.refresh_manage_groups()	.execute();
								}
								else
								{
									new refresh_manage_feeds()									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
									new fragment_manage_group.refresh_manage_groups()	.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
								}
							}
						}
					).show();
					return true;
				}
			}
		);
		return view;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(navigation_drawer.drawer_toggle.onOptionsItemSelected(item))
			return true;
		else if(item.getTitle().equals("add"))
		{
			add_edit_dialog.show_add_feed_dialog(main.current_groups, main.activity_context);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class refresh_manage_feeds extends AsyncTask<Void, String[], Void>
	{
		private final Animation animFadeIn = AnimationUtils.loadAnimation(main.activity_context, android.R.anim.fade_in);
		private final ListView listview;

		public refresh_manage_feeds()
		{
			listview = fragment_manage_feed.feed_list;
			if(feed_list_adapter.getCount() == 0)
				listview.setVisibility(View.INVISIBLE);
		}

		@Override
		protected Void doInBackground(Void... hey)
		{
			if(feed_list_adapter != null)
			{
				final String[][] content	= utilities.read_csv_to_array(main.storage + main.GROUPS_DIRECTORY + main.current_groups[0] + main.SEPAR + main.current_groups[0] + main.TXT, 'n', 'u', 'g');
				final int size					= content[0].length;
				String[] info_array			= new String[size];
				for(int i = 0; i < size; i++)
					info_array[i] = content[1][i] + main.NL + content[2][i] + " • " + Integer.toString(utilities.count_lines(main.storage + main.GROUPS_DIRECTORY + content[2][i] + main.SEPAR + content[0][i] + main.SEPAR + content[0][i] + main.CONTENT_APPENDIX)) + " items";
				publishProgress(content[0], info_array);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String[][] progress)
		{
			feed_list_adapter.set_items(progress[0], progress[1]);
			feed_list_adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPostExecute(Void tun)
		{
			listview.setAnimation(animFadeIn);
			listview.setVisibility(View.VISIBLE);
		}
	}
}
