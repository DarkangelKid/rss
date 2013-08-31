package yay.poloure.simplerss;

import android.content.Context;
import android.support.v4.view.PagerTabStrip;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import android.widget.ListView;

public class utilities
{
		private final static String[] media_error_messages = new String[]
		{
			"Media not mounted.",
			"Unable to mount media.",
			"Media is shared via USB mass storage.",
			"Media does not exist.",
			"Media contains unsupported filesystem.",
			"Media mounted as read-only.",
			"Media is being disk checked.",
			"Media was removed before unmounted."
		};

		private final static String[] media_errors = new String[]
		{
			Environment.MEDIA_UNMOUNTED,
			Environment.MEDIA_UNMOUNTABLE,
			Environment.MEDIA_SHARED,
			Environment.MEDIA_REMOVED,
			Environment.MEDIA_NOFS,
			Environment.MEDIA_MOUNTED_READ_ONLY,
			Environment.MEDIA_CHECKING,
			Environment.MEDIA_BAD_REMOVAL
		};

	public static void delete_group(String storage, String group)
	{
		/// Move all feeds to an unsorted group.
		//rm(storage + main.GROUPS_DIRECTORY + group + main.TXT);
		//rm(storage + main.GROUPS_DIRECTORY + group + main.GROUP_CONTENT_APPENDIX);
	}

	public static <T> T[] concat(T[] first, T[] second)
	{
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static byte[] concat(byte[] first, byte[] second)
	{
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static <T> int index(T[] array, T value)
	{
		for(int i = 0; i < array.length; i++)
		{
			if(array[i].equals(value))
				return i;
		}
		/* Throws an ArrayOutOfBoundsException if not handled. */
		return -1;
	}

	public static int index(int[] array, int value)
	{
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == value)
				return i;
		}
		/* Throws an ArrayOutOfBoundsException if not handled. */
		return -1;
	}

	public static String[] remove_element(String[] a, int index)
	{
		String[] b = new String[a.length - 1];
		System.arraycopy(a, 0, b, 0, index);
		if(a.length != index)
			System.arraycopy(a, index + 1, b, index, a.length - index - 1);
		return b;
	}

	public static void write_collection_to_file(String path, Iterable<?> content)
	{
		rm(path);
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(path, true));
			for(Object item : content)
				out.write(item.toString() + main.NL);
			out.close();
		}
		catch(Exception e)
		{
		}
	}

	public static String[][] create_info_arrays(String[] current_groups, int size, String storage)
	{
		String info;
		int number, i, j, total;
		String[] content;
		String[] group_array		= new String[size];
		String[] info_array		= new String[size];

		final String content_path = storage + main.GROUPS_DIRECTORY + current_groups[0] + main.SEPAR + current_groups[0] + main.CONTENT_APPENDIX;
		final String count_path = content_path + main.COUNT_APPENDIX;

		String[] count = read_file(count_path);
		if(count.length != 0)
			total = Integer.parseInt(count[0]);
		else
			total = count_lines(content_path);

		for(i = 0; i < size; i++)
		{
			group_array[i] = current_groups[i];
			content = read_csv_to_array(storage + main.GROUPS_DIRECTORY + group_array[i] + main.SEPAR + group_array[i] + main.TXT, 'n')[0];
			if(i == 0)
				info = (size == 1) ? "1 group" :  size + " groups";
			else
			{
				info = "";
				number = (content.length < 3) ? content.length : 3;

				for(j = 0; j < number - 1; j++)
					info += content[j].concat(", ");

				if(content.length > 3)
					info += "...";
				else if(number > 0)
					info += content[number - 1];
			}
			info_array[i] = Integer.toString(content.length) + " feeds • " + info;
		}
		info_array[0] =  total + " items • " + info_array[0];
		return (new String[][]{info_array, group_array});
	}

	/* Function should be safe, returns false if fails. */
	public static boolean download_file(String urler, String file_path)
	{
		/* Check to see if we can write to the media. */
		if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			return false;

		try
		{
			BufferedInputStream in = null;
			FileOutputStream fout = null;
			try
			{
				in = new BufferedInputStream(new URL(urler).openStream());
				fout = new FileOutputStream(file_path);

				byte data[] = new byte[1024];
				int count;
				while ((count = in.read(data, 0, 1024)) != -1)
					fout.write(data, 0, count);
			}
			finally
			{
				if (in != null)
					in.close();
				if (fout != null)
					fout.close();
			}
		}
		catch(Exception e)
		{
			rm(file_path);
			return false;
		}
		File file = new File(file_path);
		return (file.exists() && file.length() != 0);
	}

	/* Function should be safe, returns false if fails. */
	public static boolean append_string_to_file(String file_path, String string)
	{
		/* Check to see if we can write to the media. */
		if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			return false;

		BufferedWriter out = null;
		try
		{
			try
			{
				out = new BufferedWriter(new FileWriter(file_path, true));
				out.write(string);
			}
			finally
			{
				if(out != null)
					out.close();
			}
		}
		catch (Exception e)
		{
			rm(file_path);
			return false;
		}
		return true;
	}

	/* This function should be safe, returns false if it failed (and consequently has changed nothing). */
	public static boolean remove_string_from_file(String file_path, String string, Boolean contains)
	{
		/* Check to see if we can write to the media. */
		if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			return false;

		final String temp_path	= file_path + main.TEMP;
		final String[] lines;
		BufferedWriter out		= null;
		try
		{
			try
			{
				/* Read the file to a String array, if the file does not exist, return false. */
				lines = read_file(file_path);
				if(lines.length == 0)
					return false;

				out = new BufferedWriter(new FileWriter(temp_path, true));

				for(String item : lines)
				{
					if(contains && !item.contains(string))
						out.write(item + main.NL);
					else if(!contains && !item.equals(string))
						out.write(item + main.NL);
				}
			}
			finally
			{
				if(out != null)
					out.close();
			}
		}
		/* If fail while writing to the temp file, delete the temp file and return false. */
		catch(Exception e)
		{
			rm(temp_path);
			return false;
		}

		/* If the rename failed, delete the file and write the original back to the file. */
		boolean success = mv(temp_path, file_path);
		if(!success)
		{
			rm(file_path);
			write_collection_to_file(file_path, java.util.Arrays.asList(lines));
		}
		return success;
	}

	public static String[][] read_csv_to_array(String file_path, char... type)
	{
		int next, offset, i, j;
		String line;
		String[][] types;
		String[] lines;
		char ch;

		lines = read_file(file_path);
		if(lines.length == 0)
			return new String[0][0];

		types = new String[type.length][lines.length];

		for(j = 0; j < lines.length; j++)
		{
			offset = 0;
			line = lines[j];
			while((next = line.indexOf('|', offset)) != -1)
			{
				if(offset == line.length())
					break;

				ch = line.charAt(offset);
				offset = next + 1;
				for(i = 0; i < type.length; i++)
				{
					if(ch == type[i])
					{
						next = line.indexOf('|', offset);
						types[i][j] = line.substring(offset, next);
						break;
					}
				}
				offset = line.indexOf('|', offset) + 1;
			}
		}
		return types;
	}

	/* This function is now safe. It will return a zero length array on error. */
	public static String[] read_file(String file_path)
	{
		final String count_path = file_path + main.COUNT_APPENDIX;
		final int count;
		String line;

		/* If the file_path is not a count file, get the number of lines. */
		if(!file_path.contains(main.COUNT_APPENDIX))
		{
			String[] temp	= read_file(count_path);
			count				= (temp.length == 0) ? count_lines(file_path) : Integer.parseInt(temp[0]);
		}
		else
			count = count_lines(count_path);

		/* If the file is empty, return a zero length array. */
		if(count == 0)
			return new String[0];

		/* Use the count to allocate memory for the array. */
		String[] lines = new String[count];

		/* Begin reading the file to the String array. */
		BufferedReader in = null;
		try
		{
			try
			{
				in = new BufferedReader(new FileReader(file_path));
				for(int i = 0; i < lines.length; i++)
					lines[i] = in.readLine();
				in.close();
			}
			finally
			{
				if(in != null)
					in.close();
			}
		}
		catch(IOException e)
		{
			return new String[0];
		}
		return lines;
	}

	public static Set<String> read_file_to_set(String file_path)
	{
		Set set = new HashSet<String>();
		java.util.Collections.addAll(set, read_file(file_path));
		return set;
	}

	public static int count_lines(String file_path)
	{
		BufferedReader in = null;
		int i = 0;
		try
		{
			try
			{
				in = new BufferedReader(new FileReader(file_path));
				while(in.readLine() != null)
					i++;
			}
			finally
			{
				if(in != null)
					in.close();
			}
		}
		catch(IOException e)
		{
		}
		return i;
	}

	public static adapter_feeds_cards get_adapter_feeds_cards(FragmentManager fragment_manager, ViewPager viewpager, int page_number)
	{
		ListView list = get_listview(fragment_manager, viewpager, page_number);
		if(list == null)
			return null;
		return (adapter_feeds_cards) get_listview(fragment_manager, viewpager, page_number).getAdapter();
	}

	public static ListView get_listview(FragmentManager fragment_manager, ViewPager viewpager, int page_number)
	{
		try
		{
			return ((ListFragment) fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + page_number)).getListView();
		}
		catch(Exception e)
		{
			return null;
		}
	}

	/* This function will return null if it fails. You must check for null each time. */
	public static String get_storage()
	{
		final Context context;

		/* Try to get the context from the activity, if it is not running, ask the service. */
		if(main.activity_context != null)
			context = main.activity_context;
		else if(service_update.service_context != null)
			context = service_update.service_context;
		else /* This case should never happen because either must be running. */
			return null;

		/* Check the media state for any undesirable states. */
		final String state = Environment.getExternalStorageState();
		for(int i = 0; i < media_errors.length; i++)
		{
			if(state.equals(media_errors[i]))
			{
				post(media_error_messages[i]);
				return null;
			}
		}

		/* If it has reached here the state is MEDIA_MOUNTED and we can continue.
			Build the storage string depending on android version. */
		String storage;

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
			storage					= context.getExternalFilesDir(null).getAbsolutePath() + main.SEPAR;
		else
		{
			String packageName	= context.getPackageName();
			File externalPath		= Environment.getExternalStorageDirectory();
			storage					= externalPath.getAbsolutePath() + main.SEPAR + "Android" + main.SEPAR + "data" + main.SEPAR + packageName + main.SEPAR + "files" + main.SEPAR;
			File storage_file		= new File(storage);
			if(!storage_file.exists())
				storage_file.mkdirs();
		}
		return storage;
	}

	public static int[] get_unread_counts(String storage, String[] current_groups)
	{
		int total = 0, unread, num;
		final int size = current_groups.length;
		int[] unread_counts	= new int[size];
		adapter_feeds_cards temp;

		/* read_items == null when being called from the service for notifications. */
		if(adapter_feeds_cards.read_items == null)
			adapter_feeds_cards.read_items = read_file_to_set(storage + main.READ_ITEMS);

		for(int i = 1; i < size; i++)
		{
			unread = 0;
			String[] urls = read_file(storage + main.GROUPS_DIRECTORY + current_groups[i] + main.SEPAR + current_groups[i] + main.CONTENT_APPENDIX + main.URL_APPENDIX);
			for(String url : urls)
			{
				if(!adapter_feeds_cards.read_items.contains(url))
						unread++;
			}

			total += unread;
			unread_counts[i] = unread;
		}

		unread_counts[0] = total;
		return unread_counts;
	}

	public static void set_pagertabstrip_colour(String storage, PagerTabStrip strip)
	{
		final String colour_path	= storage + main.SETTINGS + main.PAGERTABSTRIPCOLOUR;
		String colour					= "blue";

		/* Read the colour from the settings/colour file, if blank, save as blue. */
		String[] colour_array = read_file(colour_path);
		if(colour_array.length == 0)
			append_string_to_file(colour_path, colour);
		else
			colour = colour_array[0];

		/* Find the colour stored in adapter_stettings_interface that we want. */
		int position = utilities.index(adapter_settings_interface.colours, colour);
		if(position != -1)
			strip.setTabIndicatorColor(adapter_settings_interface.colour_ints[position]);
	}

	public static void sort_group_content_by_time(String storage, String group, String all_group)
	{
		final String sep						= main.SEPAR;
		final String group_dir				= storage + main.GROUPS_DIRECTORY + group + sep;
		final String group_content_path	= group_dir + group + main.CONTENT_APPENDIX;
		final String group_count_file		= group_content_path + main.COUNT_APPENDIX;

		String[][] temp;

		final String[][] contents	= read_csv_to_array(group_dir + group + main.TXT, 'n', 'g');
		final String[] names			= contents[0];
		final String[] groups		= contents[1];

		String[] urls					= new String[0];

		String content_path;
		Time time = new Time();
		String[] pubDates;
		String[] content;
		Map<Long, String> map = new TreeMap<Long, String>();

		for(int k = 0; k < names.length; k++)
		{
			/// "/storage/groups/Tumblr/mariam/mariam.content.txt"
			content_path = storage + main.GROUPS_DIRECTORY + groups[k] + sep + names[k] + sep + names[k] + main.CONTENT_APPENDIX;
			if(exists(content_path))
			{
				temp		= read_csv_to_array(content_path, 'p', 'l');
				content 	= read_file(content_path);
				pubDates	= temp[0];
				urls		= concat(urls, temp[1]);

				for(int i = 0; i < pubDates.length; i++)
				{
					try
					{
						time.parse3339(pubDates[i]);
					}
					catch(Exception e)
					{
						break;
					}
					map.put(time.toMillis(false) - i, content[i] + "group|" + groups[k] + "|feed|" + names[k] + "|");
				}
			}
		}

		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(group_content_path, false));
			for(Map.Entry<Long, String> entry : map.entrySet())
					out.write(entry.getValue().concat(main.NL));
			out.close();

			utilities.rm(group_count_file);
			utilities.append_string_to_file(group_count_file, Integer.toString(map.size()));

			BufferedWriter out3 = new BufferedWriter(new FileWriter(group_content_path + main.URL_APPENDIX, false));
			for(String url : urls)
				out3.write(url.concat(main.NL));
			out3.close();

			utilities.rm(group_content_path + main.URL_APPENDIX + main.COUNT_APPENDIX);
			utilities.append_string_to_file(group_content_path + main.URL_APPENDIX + main.COUNT_APPENDIX, Integer.toString(urls.length));

		}
		catch(Exception e)
		{
			log(storage, "Failed to write the group content file.");
		}

		/* Sorts the all_group every time another group is updated. */
		if(!group.equals(all_group))
			sort_group_content_by_time(storage, all_group, all_group);
	}

	public static void update_navigation_adapter_compat(int[] counts)
	{
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new navigation_drawer.update_navigation_adapter().execute(counts);
		else
			new navigation_drawer.update_navigation_adapter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, counts);
	}

	public static void refresh_manage_feeds_compat()
	{
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new fragment_manage_feed.refresh_manage_feeds().execute();
		else
			new fragment_manage_feed.refresh_manage_feeds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public static void refresh_manage_groups_compat()
	{
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new fragment_manage_group.refresh_manage_groups().execute();
		else
			new fragment_manage_group.refresh_manage_groups().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public static void refresh_page_compat(int page_number)
	{
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new refresh_page().execute(page_number);
		else
			new refresh_page().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page_number);
	}

	public static void check_feed_exists_compat(android.app.AlertDialog dlg, String ngroup, String fname, String mode, String ctitle, String cgroup, String sgroup, int pos, String all_str, String URL_check)
	{
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			new add_edit_dialog.check_feed_exists(dlg, ngroup, fname, mode, ctitle, cgroup, sgroup, pos, all_str).execute(URL_check);
		else
			new add_edit_dialog.check_feed_exists(dlg, ngroup, fname, mode, ctitle, cgroup, sgroup, pos, all_str).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
	}

	public static boolean load_checkbox(android.widget.CheckBox checkbox, String path)
	{
		String[] check = read_file(path);
		boolean value = (check.length == 0) ? false : Boolean.parseBoolean(check[0]);

		if(check.length == 0)
			append_string_to_file(path, Boolean.toString(value));

		checkbox.setChecked(value);
		return value;
	}

	public static int load_seekbar(android.widget.SeekBar seekbar, String path)
	{
		String[] check = read_file(path);
		int value = (check.length == 0) ? 3 : Integer.parseInt(check[0]);

		if(check.length == 0)
			append_string_to_file(path, Integer.toString(adapter_settings_function.times[3]));

		seekbar.setProgress(index(adapter_settings_function.times, value));
		return value;
	}

	public static void log(String storage, String text)
	{
		append_string_to_file(storage + main.DUMP_FILE, text + main.NL);
	}

	public static void post(String message)
	{
		/* If the activity is running, make a toast notification. Log the event regardless. */
		if(main.service_handler != null)
			Toast.makeText(main.activity_context, (CharSequence) message, Toast.LENGTH_LONG).show();
		/* This function could be called from a state where it is impossible to get storage so this is optional. */
		if(main.storage != null)
			log(main.storage, message);
		else if(service_update.storage != null)
			log(service_update.storage, message);
	}

	public static boolean rm(String file_path)
	{
		return (new File(file_path)).delete();
	}

	public static void rm_empty(String file_path)
	{
		File file = new File(file_path);
		if(file.exists() && file.length() == 0)
			file.delete();
	}

	public static boolean rmdir(File directory)
	{
		if(directory.isDirectory())
		{
			for(String child : directory.list())
			{
				boolean success = rmdir(new File(directory, child));
				if(!success)
					return false;
			}
		}
		return directory.delete();
	}

	public static boolean mv(String a, String b)
	{
		return (new File(a)).renameTo(new File(b));
	}

	public static boolean exists(String file_path)
	{
		return (new File(file_path)).exists();
	}
}
