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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import android.widget.ListView;

public class utilities
{
	public static void delete_group(String storage, String group)
	{
		/// Move all feeds to an unsorted group.
		//delete(storage + main.GROUPS_DIRECTORY + group + main.TXT);
		//delete(storage + main.GROUPS_DIRECTORY + group + main.GROUP_CONTENT_APPENDIX);
	}

	public static boolean delete_directory(File directory)
	{
		if(directory.isDirectory())
		{
			String[] children = directory.list();
			for (String child : children)
			{
				boolean success = delete_directory(new File(directory, child));
				if(!success)
					return false;
			}
		}
		return directory.delete();
	}

	public static void toast_message(Context activity_context, CharSequence message, final Boolean short_long)
	{
		Toast message_toast;
		if(short_long)
			message_toast = Toast.makeText(activity_context, message, Toast.LENGTH_SHORT);
		else
			message_toast = Toast.makeText(activity_context, message, Toast.LENGTH_LONG);
		message_toast.show();
	}

	public static byte[] concat_byte_arrays(byte[] a, byte... b)
	{
		final int a_length = a.length;
		final int b_length = b.length;
		byte[] c = new byte[a_length + b_length];
		System.arraycopy(a, 0, c, 0, a_length);
		System.arraycopy(b, 0, c, a_length, b_length);
		return c;
	}

	public static String[] concat_string_arrays(String[] A, String[] B)
	{
		if(A.length == 0)
			return B;
		if(B.length == 0)
			return A;
		int aLen = A.length;
		int bLen = B.length;
		String[] C = new String[aLen+bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}

	public static int[] concat_int_arrays(int[] A, int[] B)
	{
		if(A.length == 0)
			return B;
		if(B.length == 0)
			return A;
		int aLen = A.length;
		int bLen = B.length;
		int[] C = new int[aLen+bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}

	public static int index_of(String[] array, String value)
	{
		for(int i = 0; i < array.length; i++)
		{
			if(array[i].equals(value))
				return i;
		}
		/* Throws an ArrayOutOfBoundsException if not handled. */
		return -1;
	}

	public static int index_of_int(int[] array, int value)
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
		delete(path);
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

		if(exists(count_path))
			total = Integer.parseInt(read_file_to_array(count_path)[0]);
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
				number = 3;
				if(content.length < 3)
					number = content.length;
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

	public static void download_file(String urler, String file_path)
	{
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
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
			catch(Exception e){
			}
		}
	}

	public static void append_string_to_file(String file_path, String string)
	{
		try
		{
			final BufferedWriter out = new BufferedWriter(new FileWriter(file_path, true));
			out.write(string);
			out.close();
		}
		catch (Exception e)
		{
		}
	}

	public static void remove_string_from_file(String file_path, String string, Boolean contains)
	{
		delete(file_path);
		try
		{
			final BufferedWriter out = new BufferedWriter(new FileWriter(file_path, true));
			for(String item : read_file_to_array(file_path))
			{
				if(contains && !item.contains(string))
						out.write(item + main.NL);
				else
				{
					if(!item.equals(string))
						out.write(item + main.NL);
				}
			}
			out.close();
		}
		catch(Exception e)
		{
		}
	}

	public static String[][] read_csv_to_array(String file_path, char... type)
	{
		int next, offset, i, j;
		String line;
		String[][] types;
		String[] lines;
		char ch;

		lines = read_file_to_array(file_path);
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

	public static String[] read_file_to_array(String file_path)
	{
		String line, line_temp = "";
		String[] lines;

		/* If the count file does not exist, count the lines of the file. */
		if(!exists(file_path + main.COUNT_APPENDIX))
			lines = new String[count_lines(file_path)];
		else
		{
			/* Read the count file. */
			BufferedReader stream2;
			try
			{
				stream2 = new BufferedReader(new FileReader(file_path + main.COUNT_APPENDIX));
				line_temp = stream2.readLine();
				stream2.close();
			}
			catch(IOException e)
			{
			}

			lines = new String[Integer.parseInt(line_temp)];
		}

		/* If the file is empty, return a zero length array so we can check. */
		if(lines.length == 0)
			return new String[0];

		BufferedReader stream;
		try
		{
			stream = new BufferedReader(new FileReader(file_path));
			for(int i = 0; i < lines.length; i++)
				lines[i] = stream.readLine();
			stream.close();
		}
		catch(IOException e)
		{
		}
		return lines;
	}

	public static Set<String> read_file_to_set(String file_path)
	{
		String line;
		BufferedReader stream;
		Set<String> lines = new HashSet<String>();
		try
		{
			stream = new BufferedReader(new FileReader(file_path));
			while((line = stream.readLine()) != null)
				lines.add(line);
			stream.close();
		}
		catch(IOException e){
		}
		return lines;
	}

	public static int count_lines(String file_path)
	{
		BufferedReader stream;
		int i = 0;
		try
		{
			stream = new BufferedReader(new FileReader(file_path));
			while(stream.readLine() != null)
				i++;
			stream.close();
		}
		catch(IOException e){
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
			String[] urls = read_file_to_array(storage + main.GROUPS_DIRECTORY + current_groups[i] + main.SEPAR + current_groups[i] + main.CONTENT_APPENDIX + main.URL_APPENDIX);
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
		String[] colour_array = read_file_to_array(colour_path);
		if(colour_array.length == 0)
			append_string_to_file(colour_path, colour);
		else
			colour = colour_array[0];

		/* Find the colour stored in adapter_stettings_interface that we want. */
		int position = utilities.index_of(adapter_settings_interface.colours, colour);
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
		int i;

		for(int k = 0; k < names.length; k++)
		{
			/// "/storage/groups/Tumblr/mariam/mariam.content.txt"
			content_path = storage + main.GROUPS_DIRECTORY + groups[k] + sep + names[k] + sep + names[k] + main.CONTENT_APPENDIX;
			if(exists(content_path))
			{
				temp		= read_csv_to_array(content_path, 'p', 'l');
				content 	= read_file_to_array(content_path);
				pubDates	= temp[0];
				urls		= concat_string_arrays(urls, temp[1]);

				for(i = 0; i < pubDates.length; i++)
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

			utilities.delete(group_count_file);
			utilities.append_string_to_file(group_count_file, Integer.toString(map.size()));

			BufferedWriter out3 = new BufferedWriter(new FileWriter(group_content_path + main.URL_APPENDIX, false));
			for(String url : urls)
				out3.write(url.concat(main.NL));
			out3.close();

			utilities.delete(group_content_path + main.URL_APPENDIX + main.COUNT_APPENDIX);
			utilities.append_string_to_file(group_content_path + main.URL_APPENDIX + main.COUNT_APPENDIX, Integer.toString(urls.length));

		}
		catch(Exception e)
		{
			log(storage, "Failed to write the group content file.");
		}

		/* Sorts the all_group every time another group is updated. */
		if(!group.equals(all_group))
			sort_group_content_by_time(main.storage, all_group, all_group);
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

	public static void log(String storage, String text)
	{
		append_string_to_file(storage + main.DUMP_FILE, text + main.NL);
	}

	public static void delete(String file_path)
	{
		(new File(file_path)).delete();
	}

	public static Boolean exists(String file_path)
	{
		return (new File(file_path)).exists();
	}

	public static void delete_if_empty(String file_path)
	{
		File file = new File(file_path);
		if(file.exists() && file.length() == 0)
			file.delete();
	}
}
