package yay.poloure.simplerss;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

class fragment_manage_group extends Fragment
{
	private static ListView manage_list;
	private static boolean multi_mode = false;
	private static ActionMode actionmode;
	private static ActionMode.Callback actionmode_callback;
	public  static adapter_manage_groups group_list_adapter;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.manage_listviews, container, false);
		manage_list = (ListView) view.findViewById(R.id.manage_listview);

		group_list_adapter = new adapter_manage_groups(getActivity());
		manage_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		manage_list.setItemsCanFocus(false);
		manage_list.setAdapter(group_list_adapter);

		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new refresh_manage_groups().execute();
		else
			new refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			registerForContextMenu(manage_list);
		else
		{

			actionmode_callback = new ActionMode.Callback()
			{
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu)
				{
					getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
					return true;
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu)
				{
					/// false only if nothing.
					return false;
				}

				// Called when the user selects a contextual menu item
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item)
				{
					switch (item.getItemId())
					{
						default:
							return false;
					}
				}

				@Override
				public void onDestroyActionMode(ActionMode mode)
				{
					for(int i = 0; i < manage_list.getAdapter().getCount(); i++)
					{
						manage_list.setItemChecked(i, false);
						try{
							manage_list.getChildAt(i).setBackgroundColor(Color.WHITE);
						}
						catch(Exception e){
						}
					}

					multi_mode = false;
					actionmode = null;
				}
			};
		}

		manage_list.setOnItemLongClickListener
		(
			new OnItemLongClickListener()
			{
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id)
				{
					if(pos == 0)
					{
						manage_list.setItemChecked(pos, false);
						return false;
					}
					view = manage_list.getChildAt(pos);

					if(!manage_list.isItemChecked(pos))
						manage_list.setItemChecked(pos, true);

					if(!multi_mode)
					{
						multi_mode = true;
						if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB){
						}
						else
							actionmode = main.activity.startActionMode(actionmode_callback);
					}
					view.setBackgroundColor(Color.parseColor("#8033b5e5"));
					return true;
				}
			}
		);

		manage_list.setOnItemClickListener
		(
			new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
				{
					if(position == 0)
					{
						manage_list.setItemChecked(position, false);
						return;
					}
					view = manage_list.getChildAt(position);

					if(multi_mode)
					{
						if(!manage_list.isItemChecked(position))
						{
							view.setBackgroundColor(Color.parseColor("#ffffffff"));

							if(manage_list.getCheckedItemPositions().indexOfValue(true) < 0)
							{
								actionmode.finish();
								multi_mode = false;
							}
						}
						else
							view.setBackgroundColor(Color.parseColor("#8033b5e5"));
					}
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

	public static class refresh_manage_groups extends AsyncTask<Void, String[], Void>
	{
		final Animation animFadeIn = AnimationUtils.loadAnimation(main.activity_context, android.R.anim.fade_in);
		private final ListView listview;

		public refresh_manage_groups()
		{
			listview = fragment_manage_group.manage_list;
			if(group_list_adapter.getCount() == 0)
				listview.setVisibility(View.INVISIBLE);

		}

		@Override
		protected Void doInBackground(Void... nothing)
		{
			String[][] content		= utilities.create_info_arrays(main.current_groups, main.current_groups.length, main.storage);
			publishProgress(content[1], content[0]);
			return null;
		}

		@Override
		protected void onProgressUpdate(String[][] progress)
		{
			if(group_list_adapter != null)
			{
				group_list_adapter.set_items(progress[0], progress[1]);
				group_list_adapter.notifyDataSetChanged();
			}
		}

		@Override
		protected void onPostExecute(Void nothing)
		{
			listview.setAnimation(animFadeIn);
			listview.setVisibility(View.VISIBLE);
		}
	}
}
