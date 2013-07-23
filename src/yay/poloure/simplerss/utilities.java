package yay.poloure.simplerss;

import android.content.Context;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

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
import android.util.Log;

public class utilities
{
	public static void save_positions(FragmentManager fragment_manager, ViewPager viewpager, String storage)
	{
		card_adapter adapter;
		BufferedWriter out;
		String url, group;
		List<String> feeds, lines;
		Boolean found_url = false;
		List<String> current_groups = read_file_to_list(storage + "groups/group_list.txt");
		final int size = current_groups.size();

		for(int i = 1; i < size; i++)
		{
			try
			{
				group = current_groups.get(i);
				adapter = get_card_adapter(fragment_manager, viewpager, i);
				if(adapter.getCount() > 0)
				{
					/// Read each of the content files from the group and find the line with the url.
					feeds = read_csv_to_list(storage + "groups/" + group + ".txt", new String[]{"name|"}, false).get(0);
					found_url = false;
					url = adapter.return_latest_url();
					if(!url.isEmpty())
					{
						for(String feed: feeds)
						{
							lines = read_file_to_list(storage + "content/" + feed + ".store.txt.content.txt");
							delete(storage + "content/" + feed + ".store.txt.content.txt");

							out = new BufferedWriter(new FileWriter(storage + "content/" + feed + ".store.txt.content.txt", true));
							for(String line : lines)
							{
								if(!found_url)
								{
									if(!line.contains(url))
										out.write(line + "\n");
									else if(!line.substring(0, 9).equals("marker|1|"))
									{
										out.write("marker|1|" + line + "\n");
										found_url = true;
									}
									else
										out.write(line + "\n");
								}
								else
									out.write(line + "\n");
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

	public static void toast_message(Context activity_context, String message, final Boolean short_long)
	{
		Toast message_toast;
		if(short_long)
			message_toast = Toast.makeText(activity_context, message, Toast.LENGTH_SHORT);
		else
			message_toast = Toast.makeText(activity_context, message, Toast.LENGTH_LONG);
		message_toast.show();
	}

	public static byte[] concat_byte_arrays(byte[] a, byte[] b)
	{
		final int a_length = a.length;
		final int b_length = b.length;
		byte[] c = new byte[a_length + b_length];
		System.arraycopy(a, 0, c, 0, a_length);
		System.arraycopy(b, 0, c, a_length, b_length);
		return c;
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

	public static void dump(String storage, Exception e)
	{
		try
		{
			FileWriter fstream = new FileWriter(storage + "Exception -" + (new Time()).format3339(false) + ".txt", false);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(e.toString());
			out.close();
		}
		catch(Exception ee)
		{
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
		final List<String> list = read_file_to_list(file_path);
		delete(file_path);
		try{
			final BufferedWriter out = new BufferedWriter(new FileWriter(file_path, true));
			for(String item : list)
			{
				if(contains)
				{
					if(!item.contains(string))
						out.write(item + "\n");
				}
				else
				{
					if(!item.equals(string))
						out.write(item + "\n");
				}
			}
			out.close();
		}
		catch(Exception e){
		}
	}

	public static String[][] read_csv_to_array(String file_path, char[] type, Boolean dimensions)
	{
		int next, offset, line_length, k, j;
		String line, one;
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
		final int total_size = lines.size();

		final int type_size = (dimensions) ? type.length + 2 : type.length;
		String[][] types = new String[type_size][total_size];

		for(j = 0; j < total_size; j++)
		{
			next = 0;
			offset = 0;
			line = lines.get(j);
			line_length = line.length();
			while((next = line.indexOf('|', offset)) != -1)
			{
				//one = line.substring(offset, next);
				if(offset == line_length)
					break;

				ch = line.charAt(offset);
				offset = next + 1;
				switch(ch)
				{
					case 'm':
						next = line.indexOf('|', offset);
						types[0][j] = "1";
						break;
					case 'w':
						next = line.indexOf('|', offset);
						types[type.length][j]		= line.substring(offset, next);
						break;
					case 'h':
						next = line.indexOf('|', offset);
						types[type.length + 1][j]	= line.substring(offset, next);
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
		int next, offset, line_length, k, j;
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
		final int total_size = lines.size();

		String[][] types = new String[7][total_size];

		for(j = 0; j < total_size; j++)
		{
			next = 0;
			offset = 0;
			line = lines.get(j);
			line_length = line.length();
			while((next = line.indexOf('|', offset)) != -1)
			{
				if(offset == line_length)
					break;

				ch = line.charAt(offset);
				offset = next + 1;
				switch(ch)
				{
					case 'm':
						next = line.indexOf('|', offset);
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
				}
				offset = line.indexOf('|', offset) + 1;
			}
		}

		return types;
	}

	public static List< List<String> > read_csv_to_list(String file_path, String[] type, Boolean dimensions)
	{
		int content_start, content_index, i, bar_index;
		String line;
		final Boolean marker = type[0].equals("marker|");

		List< List<String> > types = new ArrayList< List<String> >();
		int[] lengths = new int[type.length];

		for(i = 0; i < type.length; i++)
		{
			types.add(new ArrayList<String>());
			lengths[i] = type[i].length();
		}
		if(dimensions)
		{
			types.add(new ArrayList<String>());
			types.add(new ArrayList<String>());
		}

		/// Index the pattern of tag occurence.
		try
		{
			BufferedReader stream = new BufferedReader(new FileReader(file_path));
			while((line = stream.readLine()) != null)
			{
				for(i = 0; i < type.length; i++)
				{
					if((i == 0)&&(marker))
					{
						if(line.charAt(0) != 'm')
							types.get(0).add("");
						else
							types.get(0).add("1");
					}
					else
					{
						content_index = line.indexOf(type[i]);
						if(content_index == -1)
							types.get(i).add("");
						else
						{
							content_start = content_index + lengths[i];
							bar_index = line.indexOf('|', content_start);
							if(content_start == bar_index)
								types.get(i).add("");
							else
								types.get(i).add(line.substring(content_start, bar_index));
						}
					}
				}
				if(dimensions)
				{
					content_index = line.lastIndexOf("width|");
					if(content_index == -1)
					{
						types.get(type.length).add("");
						types.get(type.length + 1).add("");
					}
					else
					{
						content_index += 6;
						bar_index = line.lastIndexOf("|height|");
						types.get(type.length).add(line.substring(content_index, bar_index));
						types.get(type.length + 1).add(line.substring(bar_index + 8, line.lastIndexOf('|')));
					}
				}
			}
			stream.close();
		}
		catch(Exception e){
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

	public static List<Integer> get_unread_counts(FragmentManager fragment_manager, ViewPager viewpager, String storage)
	{
		List<String> current_groups = read_file_to_list(storage + "groups/group_list.txt");
		List<Integer> unread_list = new ArrayList<Integer>();
		List<String> count_list;
		int sized, i, total = 0;
		card_adapter ith = null;
		main_view.fragment_card fc;
		final int size = current_groups.size();

		for(int j = 1; j < size; j++)
		{
			try
			{
				fc = (main_view.fragment_card) (fragment_manager.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(j)));
				ith = (card_adapter) fc.getListAdapter();
			}
			catch(Exception e){
			}
			/// TODO: Or if no items are in the list.
			if(ith == null)
				unread_list.add(0);
			else
			{
				int most = count_lines(storage + "groups/" + current_groups.get(j) + ".txt.content.txt");
				unread_list.add(most - ith.return_unread_item_count() - 1);
			}
		}

		for(Integer un : unread_list)
			total += un;
		unread_list.add(0, total);

		return unread_list;
	}

	public static card_adapter get_card_adapter(FragmentManager fragment_manager, ViewPager viewpager, int page_index)
	{
		return ((card_adapter)((main_view.fragment_card) fragment_manager
						.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + Integer.toString(page_index)))
						.getListAdapter());
	}

	public static void sort_group_content_by_time(String storage, String group)
	{
		final String group_path = storage + "groups/" + group + ".txt.content.txt";
		String content_path;
		Time time = new Time();
		List<String> pubDates, content;
		Map<Long, String> map = new TreeMap<Long, String>();
		int size, i;

		final List<String> feeds_array	= read_csv_to_list(storage + "groups/" + group + ".txt", new String[]{"name|"}, false).get(0);

		for(String feed : feeds_array)
		{
			content_path = storage + "content/" + feed + ".store.txt.content.txt";
			if(exists(content_path))
			{
				content 		= read_file_to_list(content_path);
				pubDates		= read_csv_to_list(content_path, new String[]{"published|"}, false).get(0);

				if(pubDates.get(0).length() < 8)
					pubDates 	= read_csv_to_list(content_path, new String[]{"pubDate|"}, false).get(0);

				size = pubDates.size();
				for(i = 0; i < size; i++)
				{
					try{
						time.parse3339(pubDates.get(i));
					}
					catch(Exception e){
						log(storage, "BUG : Meant to be 3339 but looks like: " + pubDates.get(i));
						return;
					}

					map.put(time.toMillis(false), content.get(i));
				}
			}
		}

		delete(group_path);
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(group_path, true));
			for(Map.Entry<Long, String> entry : map.entrySet())
				out.write(entry.getValue() + "\n");
			out.close();
		}
		catch(Exception e){
		}
	}

	public static void log(String storage, String text)
	{
		append_string_to_file(storage + "dump.txt", text + "\n");
	}

	public static void delete(String file_path)
	{
		(new File(file_path)).delete();
	}

	public static Boolean exists(String file_path)
	{
		return (new File(file_path)).exists();
	}
}
