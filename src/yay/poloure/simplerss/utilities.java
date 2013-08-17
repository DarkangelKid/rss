package yay.poloure.simplerss;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.os.Environment;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collection;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;

import android.os.Debug;
import android.text.format.Time;
import android.widget.Toast;

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

	public static void save_positions(FragmentManager fragment_manager, ViewPager viewpager, String storage)
	{
		adapter_feeds_cards adapter;
		BufferedWriter out;
		String url, group, feed_content_file;
		String[] feeds;
		List<String> lines;
		Boolean found_url = false;
		List<String> current_groups = read_file_to_list(storage + main.GROUP_LIST);
		final int size = current_groups.size();

		for(int i = 1; i < size; i++)
		{
			try
			{
				group = current_groups.get(i);
				adapter = get_adapter_feeds_cards(fragment_manager, viewpager, i);
				if(adapter.getCount() > 0)
				{
					/// Read each of the content files from the group and find the line with the url.
					feeds = read_single_to_array(storage + main.GROUPS_DIRECTORY + group + main.SEPAR + group + main.TXT, "name|");
					found_url = false;
					url = adapter.return_latest_url();
					if(!url.equals(""))
					{
						for(String feed: feeds)
						{
							feed_content_file = storage + main.GROUPS_DIRECTORY + group + main.SEPAR + feed + main.SEPAR + feed + main.CONTENT_APPENDIX;
							lines = read_file_to_list(feed_content_file);
							delete(feed_content_file);

							out = new BufferedWriter(new FileWriter(feed_content_file, true));
							for(String line : lines)
							{
								if(!found_url)
								{
									if(!line.contains(url))
										out.write(line + main.NL);
									else if(!line.substring(0, 9).equals("marker|1|"))
									{
										out.write("marker|1|" + line + main.NL);
										found_url = true;
									}
									else
										out.write(line + main.NL);
								}
								else
									out.write(line + main.NL);
							}
							out.close();
							if(found_url)
								break;
						}
						sort_group_content_by_time(storage, group);
					}
				}
			}
			catch(Exception e){
			}
		}
		if(found_url)
			sort_group_content_by_time(storage, current_groups.get(0));
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

	public static void write_array_to_file(String path, String... content)
	{
		delete(path);
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(path, true));
			for(String item : content)
				out.write(item + main.NL);
			out.close();
		}
		catch(Exception e)
		{
		}
	}

	public static String[][] create_info_arrays(List<String> current_groups, int size, String storage)
	{
		String info;
		int number, i, j, total;
		String[] content;
		String[] group_array	= new String[size];
		String[] info_array		= new String[size];

		final String content_path = storage + main.GROUPS_DIRECTORY + current_groups.get(0) + main.SEPAR + current_groups.get(0) + main.CONTENT_APPENDIX;
		final String count_path = content_path + main.COUNT_APPENDIX;

		if(exists(count_path))
			total = Integer.parseInt(read_file_to_list(count_path).get(0));
		else
			total = count_lines(content_path);

		for(i = 0; i < size; i++)
		{
			group_array[i] = current_groups.get(i);
			content = read_single_to_array(storage + main.GROUPS_DIRECTORY + group_array[i] + main.SEPAR + group_array[i] + main.TXT, "name|");
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

	public static void remove_string_from_file(String file_path, CharSequence string, Boolean contains)
	{
		final List<String> list = read_file_to_list(file_path);
		delete(file_path);
		try{
			final BufferedWriter out = new BufferedWriter(new FileWriter(file_path, true));
			for(String item : list)
			{
				if(contains)
				{
					if(!item.contains(string))
						out.write(item + main.NL);
				}
				else
				{
					if(!item.equals(string))
						out.write(item + main.NL);
				}
			}
			out.close();
		}
		catch(Exception e){
		}
	}

	private static String[] read_single_to_array(String file_path, String type)
	{
		int next, offset, j;
		String line;
		char ch;
		List<String> lines = new ArrayList<String>();

		try
		{
			BufferedReader stream = new BufferedReader(new FileReader(file_path));
			while((line = stream.readLine()) != null)
				lines.add(line);
			stream.close();
		}
		catch(Exception e)
		{
		}

		String[] types = new String[lines.size()];

		for(j = 0; j < lines.size(); j++)
		{
			line = lines.get(j);
			if((next = line.indexOf(type, 0)) != -1)
			{
				ch = type.charAt(0);
				offset = next + 1;
				switch(ch)
				{
					case 'm':
						types[j] = "1";
						break;
					default:
						next = line.indexOf('|', offset);
						offset = next + 1;
						types[j] = line.substring(offset, line.indexOf('|', offset));
						break;
				}
			}
		}
		return types;
	}

	public static String[][] read_csv_to_array(String file_path, char... type)
	{
		int next, offset, k, j;
		String line;
		char ch;
		int start = (type[0] == 'm') ? 1 : 0;
		List<String> lines = new ArrayList<String>();

		try
		{
			BufferedReader stream = new BufferedReader(new FileReader(file_path));
			while((line = stream.readLine()) != null)
				lines.add(line);
			stream.close();
		}
		catch(Exception e)
		{
		}
		String[][] types = new String[type.length][lines.size()];

		for(j = 0; j < lines.size(); j++)
		{
			offset = 0;
			line = lines.get(j);
			while((next = line.indexOf('|', offset)) != -1)
			{
				if(offset == line.length())
					break;

				ch = line.charAt(offset);
				offset = next + 1;
				switch(ch)
				{
					case 'm':
						types[0][j] = "1";
						break;
					default:
						for(k = start; k < type.length; k++)
						{
							if(ch == type[k])
							{
								next = line.indexOf('|', offset);
								types[k][j] = line.substring(offset, next);
								break;
							}
						}
						break;
				}
				offset = line.indexOf('|', offset) + 1;
			}
		}
		return types;
	}

	public static String[][] load_csv_to_array(String file_path)
	{
		int next, offset, j;
		String line;
		char ch;

		List<String> lines = new ArrayList<String>();
		try
		{
			BufferedReader stream = new BufferedReader(new FileReader(file_path));
			while((line = stream.readLine()) != null)
				lines.add(line);
			stream.close();
		}
		catch(Exception e)
		{
		}

		String[][] types = new String[9][lines.size()];

		for(j = 0; j < lines.size(); j++)
		{
			offset = 0;
			line = lines.get(j);
			while((next = line.indexOf('|', offset)) != -1)
			{
				if(offset == line.length())
					break;

				ch = line.charAt(offset);
				offset = next + 1;
				switch(ch)
				{
					case 'm':
						types[0][j] = "1";
						break;
					case 't':
						next = line.indexOf('|', offset);
						types[1][j]		= line.substring(offset, next);
						break;
					case 'd':
						next = line.indexOf('|', offset);
						types[2][j]		= line.substring(offset, next);
						break;
					case 'l':
						next = line.indexOf('|', offset);
						types[3][j]		= line.substring(offset, next);
						break;
					case 'i':
						next = line.indexOf('|', offset);
						types[4][j]		= line.substring(offset, next);
						break;
					case 'w':
						next = line.indexOf('|', offset);
						types[5][j]		= line.substring(offset, next);
						break;
					case 'h':
						next = line.indexOf('|', offset);
						types[6][j]		= line.substring(offset, next);
						break;
					case 'g':
						next = line.indexOf('|', offset);
						types[7][j]		= line.substring(offset, next);
						break;
					case 'f':
						next = line.indexOf('|', offset);
						types[8][j]		= line.substring(offset, next);
						break;
				}
				offset = line.indexOf('|', offset) + 1;
			}
		}
		return types;
	}

	public static List<String> read_file_to_list(String file_path)
	{
		String line;
		BufferedReader stream;
		List<String> lines = new ArrayList<String>();
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

	public static adapter_feeds_cards get_adapter_feeds_cards(FragmentManager fragment_manager, ViewPager viewpager, int page_index)
	{
		return ((adapter_feeds_cards)((ListFragment) fragment_manager
						.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	public static void sort_group_content_by_time(String storage, String group)
	{
		/// "/storage/groups/Tumblr/Tumbler.content.txt"
		final String sep				= main.SEPAR;
		final String group_dir			= storage + main.GROUPS_DIRECTORY + group + sep;
		final String group_content_path	= group_dir + group + main.CONTENT_APPENDIX;
		final String group_count_file	= group_content_path + main.COUNT_APPENDIX;

		final String[][] contents	= utilities.read_csv_to_array(group_dir + group + main.TXT, 'n', 'g');
		final String[] names		= contents[0];
		final String[] groups		= contents[1];

		String content_path;
		Time time = new Time();
		String[] pubDates;
		List<String> content;
		Map<Long, String> map = new TreeMap<Long, String>();
		int i;

		for(int k = 0; k < names.length; k++)
		{
			/// "/storage/groups/Tumblr/mariam/mariam.content.txt"
			content_path = storage + main.GROUPS_DIRECTORY + groups[k] + sep + names[k] + sep + names[k] + main.CONTENT_APPENDIX;
			if(exists(content_path))
			{
				content 		= read_file_to_list(content_path);
				pubDates		= read_single_to_array(content_path, "pubDate|");

				if((pubDates[0] == null)||(pubDates[0].length() < 8))
					pubDates 	= read_single_to_array(content_path, "published|");

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
					map.put(time.toMillis(false) - i, content.get(i) + "group|" + groups[k] + "|feed|" + names[k] + "|");
				}
			}
		}

		delete(group_content_path);
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(group_content_path, true));
			for(Map.Entry<Long, String> entry : map.entrySet())
				out.write(entry.getValue() + main.NL);

			out.close();

			BufferedWriter out2 = new BufferedWriter(new FileWriter(group_count_file, false));
			out2.write(Integer.toString(map.size()));
			out2.close();
		}
		catch(Exception e)
		{
			log(storage, "Failed to write the group content file.");
		}
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
