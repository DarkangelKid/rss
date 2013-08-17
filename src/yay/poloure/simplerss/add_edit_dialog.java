package yay.poloure.simplerss;

import java.net.URL;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;

public class add_edit_dialog
{
	private static class check_feed_exists extends AsyncTask<String, Void, String[]>
	{
		private Boolean existing_group = false, real = false;
		private final AlertDialog dialog;
		private String group, name;
		private final String mode, all_string, spinner_group, current_group, current_title;
		private final int pos;
		private final Pattern illegal_file_chars = Pattern.compile("[/\\?%*|<>:]");

		public check_feed_exists(AlertDialog edit_dialog, String new_group, String feed_name, String moder, String current_tit, String current_grop, String spin_group, int position, String all_str)
		{
			dialog			= edit_dialog;
			group			= new_group;
			name			= feed_name;
			mode			= moder;
			spinner_group	= spin_group;
			current_group	= current_grop;
			current_title	= current_tit;
			pos				= position;
			all_string		= all_str;
			Button button	= dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			if(button != null)
				button.setEnabled(false);
		}

		@Override
		protected String[] doInBackground(String... passed_url)
		{
			/// If the group entry has text, check to see if it is an old group or if it is new.
			String url = "", feed_title = "";
			if(group.length()>0)
			{
				final List<String> current_groups = utilities.read_file_to_list(main.storage + main.GROUP_LIST);
				for(String gro : current_groups)
				{
					if((gro.toLowerCase(Locale.getDefault())).equals(group.toLowerCase(Locale.getDefault())))
					{
						group = gro;
						existing_group = true;
					}
				}
				if(!existing_group)
				{
					String[] words = group.split(" ");
					group = "";

					for(String word: words)
						group += (word.substring(0, 1).toUpperCase(Locale.getDefault())).concat(word.substring(1).toLowerCase(Locale.getDefault())) + " ";
					group = group.substring(0, group.length() - 1);
				}

			}
			else
			{
				group = spinner_group;
				existing_group = true;
			}

			String[] check_list;
			if(!passed_url[0].contains("http"))
				check_list = new String[]{"http://" + passed_url[0], "https://" + passed_url[0]};
			else
				check_list = new String[]{passed_url[0]};

			try
			{
				for(String check : check_list)
				{
					final BufferedInputStream in = new BufferedInputStream((new URL(check)).openStream());
					byte data[] = new byte[512], data2[];
					in.read(data, 0, 512);

					String line = new String(data);
					if((line.contains("rss"))||((line.contains("Atom"))||(line.contains("atom"))))
					{
						while((!line.contains("<title"))&&(!line.contains("</title>")))
						{
							data2 = new byte[512];
							in.read(data2, 0, 512);
							data = utilities.concat_byte_arrays(data, data2);
							line = new String(data);
						}
						final int ind = line.indexOf(">", line.indexOf("<title")) + 1;
						feed_title = line.substring(ind, line.indexOf("</", ind));
						real = true;
						url = check;
						break;
					}
				}
			}
			catch(Exception e)
			{
			}
			return new String[]{url, feed_title};
		}

		@Override
		protected void onPostExecute(String[] ton)
		{
			if(!real)
			{
				utilities.toast_message(main.activity_context, main.activity_context.getString(R.string.feed_invalid), false);
				Button button	= dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				if(button != null)
					button.setEnabled(true);
			}
			else
			{
				String storage = main.storage;
				if(!existing_group)
					add_group(storage, group);
				if(name.equals(""))
					name = ton[1];

				name = illegal_file_chars.matcher(name).replaceAll("");

				if(mode.equals("edit"))
					edit_feed(storage, current_title, name, ton[0], current_group, group, pos, all_string);
				else
					add_feed(storage, name, ton[0], group, all_string);

				dialog.dismiss();
			}
		}
	}

	public static void show_add_filter_dialog(Context activity_context, final String storage)
	{
		final LayoutInflater inflater		= LayoutInflater.from(activity_context);
		final View add_filter_layout		= inflater.inflate(R.layout.add_filter_dialog, null);

		final AlertDialog add_filter_dialog = new AlertDialog.Builder(activity_context)
				.setTitle("Add Filter")
				.setView(add_filter_layout)
				.setCancelable(true)
				.setNegativeButton
				(activity_context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.create();

				add_filter_dialog.getWindow()
						.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

				add_filter_dialog.setButton(AlertDialog.BUTTON_POSITIVE, (activity_context.getString(R.string.add_dialog)),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						final String feed_name	= ((TextView) add_filter_layout).getText().toString().trim();
						String filter_path		= storage.concat(main.FILTER_LIST);
						List<String> filters	= utilities.read_file_to_list(filter_path);
						if(!filters.contains(feed_name))
							utilities.append_string_to_file(filter_path, feed_name + main.NL);
						main.filter_list_adapter.set_items(utilities.read_file_to_list(filter_path));
						main.filter_list_adapter.notifyDataSetChanged();
						add_filter_dialog.hide();
					}
				});
		add_filter_dialog.show();
	}

	public static void show_add_feed_dialog(final List<String> current_groups, Context activity_context)
	{
		final LayoutInflater inflater		= LayoutInflater.from(activity_context);
		final View add_rss_dialog			= inflater.inflate(R.layout.add_rss_dialog, null);
		final List<String> spinner_groups	= current_groups.subList(1, current_groups.size());
		final TextView group_edit			= (TextView) add_rss_dialog.findViewById(R.id.group_edit);
		final TextView URL_edit				= (TextView) add_rss_dialog.findViewById(R.id.URL_edit);
		final TextView name_edit			= (TextView) add_rss_dialog.findViewById(R.id.name_edit);
		final AdapterView<SpinnerAdapter> group_spinner	= (AdapterView<SpinnerAdapter>) add_rss_dialog.findViewById(R.id.group_spinner);

		final ArrayAdapter<String> adapter	= new ArrayAdapter<String>(activity_context, R.layout.group_spinner_text, spinner_groups);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner.setAdapter(adapter);

		final AlertDialog add_feed_dialog = new AlertDialog.Builder(activity_context).create();
		add_feed_dialog.setTitle("Add Feed");
		add_feed_dialog.setView(add_rss_dialog);
		add_feed_dialog.setCancelable(true);
		add_feed_dialog.setButton(AlertDialog.BUTTON_NEGATIVE, activity_context.getString(R.string.cancel_dialog),
		new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog,int id)
			{
			}
		});

		add_feed_dialog.setButton(AlertDialog.BUTTON_POSITIVE, activity_context.getString(R.string.add_dialog),
		new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				final String new_group		= group_edit	.getText().toString().trim().toLowerCase(Locale.getDefault());
				final String URL_check		= URL_edit		.getText().toString().trim();
				final String feed_name		= name_edit		.getText().toString().trim();
				String spinner_group;
				try
				{
					spinner_group	= group_spinner	.getSelectedItem().toString();
				}
				catch(Exception e)
				{
					spinner_group = "unsorted";
				}
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
					new check_feed_exists(add_feed_dialog, new_group, feed_name, "add", "", "", spinner_group, 0, current_groups.get(0)).execute(URL_check);
				else
					new check_feed_exists(add_feed_dialog, new_group, feed_name, "add", "", "", spinner_group, 0, current_groups.get(0)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
			}
		});
		add_feed_dialog.show();
	}

	public static void show_edit_feed_dialog(final List<String> current_groups, Context activity_context, String storage, final int position)
	{
		final LayoutInflater inflater		= LayoutInflater.from(activity_context);
		final View edit_rss_dialog			= inflater.inflate(R.layout.add_rss_dialog, null);
		final String[][] content			= utilities.read_csv_to_array(storage + main.GROUPS_DIRECTORY+ current_groups.get(0) + main.SEPAR + current_groups.get(0) + main.TXT, 'n', 'u', 'g');
		final String current_title			= content[0][position];
		final String current_url			= content[1][position];
		final String current_group			= content[2][position];

		final TextView group_edit			= (TextView) edit_rss_dialog.findViewById(R.id.group_edit);
		final TextView URL_edit				= (TextView) edit_rss_dialog.findViewById(R.id.URL_edit);
		final TextView name_edit			= (TextView) edit_rss_dialog.findViewById(R.id.name_edit);
		final AdapterView<SpinnerAdapter> group_spinner	= (AdapterView<SpinnerAdapter>) edit_rss_dialog.findViewById(R.id.group_spinner);

		final List<String> spinner_groups	= current_groups.subList(1, current_groups.size());

		final ArrayAdapter<String> adapter	= new ArrayAdapter<String>(activity_context, R.layout.group_spinner_text, spinner_groups);
		adapter			.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		group_spinner	.setAdapter(adapter);
		URL_edit		.setText(current_url);
		name_edit		.setText(current_title);
		group_spinner	.setSelection(spinner_groups.indexOf(current_group));

		final AlertDialog edit_feed_dialog = new AlertDialog.Builder(activity_context)
				.setTitle(activity_context.getString(R.string.edit_dialog_title))
				.setView(edit_rss_dialog)
				.setCancelable(true)
				.setNegativeButton
				(activity_context.getString(R.string.cancel_dialog),new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int id)
						{
						}
					}
				)
				.create();

				edit_feed_dialog.setButton(AlertDialog.BUTTON_POSITIVE, (activity_context.getString(R.string.accept_dialog)),
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
							String new_group 		= group_edit	.getText().toString().trim().toLowerCase(Locale.getDefault());
							String URL_check 		= URL_edit		.getText().toString().trim();
							String feed_name 		= name_edit		.getText().toString().trim();
							String spinner_group 	= group_spinner	.getSelectedItem().toString();
							if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
								new check_feed_exists(edit_feed_dialog, new_group, feed_name, "edit", current_title, current_group, spinner_group, position, current_groups.get(0)).execute(URL_check);
							else
								new check_feed_exists(edit_feed_dialog, new_group, feed_name, "edit", current_title, current_group, spinner_group, position, current_groups.get(0)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
					}
				});

				edit_feed_dialog.show();
	}

	private static void add_feed(String storage, String feed_name, String feed_url, String feed_group, String all_string)
	{
		String feed_path	= storage + main.GROUPS_DIRECTORY + feed_group + main.SEPAR + feed_name;
		File folder		= new File(feed_path);
		if(!folder.exists())
		{
			folder.mkdir();
			(new File(feed_path + main.SEPAR + "images")).mkdir();
			(new File(feed_path + main.SEPAR + "thumbnails")).mkdir();
		}
		feed_path	= storage + main.GROUPS_DIRECTORY + all_string;
		folder		= new File(feed_path);
		if(!folder.exists())
			folder.mkdir();

		String feed_info = "name|" +  feed_name + "|url|" + feed_url + "|group|" + feed_group + "|" + main.NL;
		utilities.append_string_to_file(storage + main.GROUPS_DIRECTORY + feed_group + main.SEPAR + feed_group + main.TXT, feed_info);
		utilities.append_string_to_file(storage + main.ALL_FILE, feed_info);

		if(main.feed_list_adapter != null)
		{
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				new main.refresh_manage_feeds().execute();
			else
				new main.refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		if(main.group_list_adapter != null)
		{
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				new main.refresh_manage_groups().execute();
			else
				new main.refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private static void edit_feed(String storage, String old_name, String new_name, String new_url, String old_group, String new_group, int position, String all_string)
	{
		final String sep					= main.SEPAR;
		final String txt					= main.TXT;
		final String count					= main.COUNT_APPENDIX;
		final String con					= main.CONTENT_APPENDIX;
		final String all_group_file			= storage + main.ALL_FILE;
		final String old_group_folder		= storage + main.GROUPS_DIRECTORY + old_group;
		final String new_group_folder		= storage + main.GROUPS_DIRECTORY + new_group;
		final String old_group_file			= old_group_folder + sep + old_group + txt;
		final String new_group_file			= new_group_folder + sep + new_group + txt;
		final String old_feed_folder		= old_group_folder + sep + old_name;
		final String new_feed_folder		= new_group_folder + sep + new_name;
		final String old_feed_folder_post	= new_group_folder + sep + old_name;
		final String new_feed_folder_post	= new_group_folder + sep + new_name;

		/// TEST 1: Did not copy the folder, did add the new name, left the old name and did not delete the folder.
		if(!old_name.equals(new_name))
		{
			(new File(old_feed_folder + sep + old_name + txt)).renameTo(new File(old_feed_folder + sep + new_name + txt));
			(new File(old_feed_folder + sep + old_name + con)).renameTo(new File(old_feed_folder + sep + new_name + con));
		}
		if(!old_group.equals(new_group))
		{
			/// Java 1.7 nio
			///	Files.move(old_feed_folder, new_feed_folder, Files.REPLACE_EXISTING);

			(new File(old_feed_folder)).renameTo(new File(new_feed_folder));

			utilities.remove_string_from_file(old_group_file, old_name, true);
			utilities.append_string_to_file(new_group_file, "name|" +  new_name + "|url|" + new_url + "|group|" + new_group + "|" + main.NL);

			utilities.delete_if_empty(old_group_file);
			if(!(new File(old_group_file).exists()))
			{
				utilities.delete_directory(new File(old_group_folder));
				utilities.remove_string_from_file(storage + main.GROUP_LIST, old_group, false);
			}
		}
		if(!old_name.equals(new_name))
			(new File(old_feed_folder_post)).renameTo(new File(new_feed_folder_post));

		/// Replace the new_group file with the new data.
		List<String> list = utilities.read_file_to_list(new_group_file);
		(new File(new_group_file)).delete();
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(new_group_file, true));
			for(String item : list)
			{
				if(item.contains(old_name))
					out.write("name|" +  new_name + "|url|" + new_url + "|group|" + new_group + "|" + main.NL);
				else
					out.write(item + main.NL);
			}
			out.close();
		}
		catch(Exception e)
		{
		}
		/// Replace the all_group file with the new group and data.
		list = utilities.read_file_to_list(all_group_file);
		(new File(all_group_file)).delete();
		try
		{
			BufferedWriter out2 = new BufferedWriter(new FileWriter(all_group_file, true));
			for(String item : list)
			{
				if(item.contains(old_name))
					out2.write("name|" +  new_name + "|url|" + new_url + "|group|" + new_group + "|" + main.NL);
				else
					out2.write(item + main.NL);
			}
			out2.close();
		}
		catch(Exception e)
		{
		}
		/// Delete the group count file and delete the group_content_file
		final String all_content_file = storage + main.GROUPS_DIRECTORY + all_string + sep + all_string + con;
		(new File(new_group_folder + sep + new_group + con))		.delete();
		(new File(new_group_folder + sep + new_group + con + count)).delete();
		(new File(old_group_folder + sep + old_group + con))		.delete();
		(new File(old_group_folder + sep + old_group + con + count)).delete();
		(new File(all_content_file))								.delete();
		(new File(all_content_file + count))						.delete();
		/// This is because the group file contains the feed name and feed group (for location of images).
		if((new File(old_group_file)).exists())
			utilities.sort_group_content_by_time(storage, old_group);
		if(!old_group.equals(new_group))
			utilities.sort_group_content_by_time(storage, new_group);
		utilities.sort_group_content_by_time(storage, all_string);

		main.feed_list_adapter.set_position(position, new_name, new_url + main.NL + new_group + " • " + Integer.toString(utilities.count_lines(storage + main.GROUPS_DIRECTORY + new_group + main.SEPAR + new_name + main.SEPAR + new_name + main.CONTENT_APPENDIX) - 1) + " items");
		main.feed_list_adapter.notifyDataSetChanged();

		/// To refresh the counts and the order of the groups.
		main.update_groups();

		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new main.refresh_manage_groups().execute();
		else
			new main.refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		/// Force a refresh of the page.
		int index;
		if((new File(old_group_file)).exists())
		{
			index = main.current_groups.indexOf(old_group);
			main.new_items.set(index, true);
		}
		if(!old_group.equals(new_group))
		{
			index = main.current_groups.indexOf(new_group);
			main.new_items.set(index, true);
		}
		main.new_items.set(0, true);
	}

	private static void add_group(String storage, String group_name)
	{
		utilities.append_string_to_file(storage + main.GROUP_LIST, group_name + main.NL);
		final File folder = new File(storage + main.GROUPS_DIRECTORY + group_name);
		if(!folder.exists())
			folder.mkdir();

		main.update_groups();
	}
}
