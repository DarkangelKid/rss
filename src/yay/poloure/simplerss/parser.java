package yay.poloure.simplerss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

class parser
{
	private static final SimpleDateFormat rss_date		= new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	private static final SimpleDateFormat rfc3339		= new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
	private static final Pattern regex_tags				= Pattern.compile("(&lt;).*?(&gt;)");
	private static final Pattern regex_cdata_tags		= Pattern.compile("\\<.*?\\>");
	private static final Pattern space_tags				= Pattern.compile("[\\t\\n\\x0B\\f\\r\\|]");
	private String[] start								= new String[]{"<link>", "<published>", "<pubDate>", "<description>", "<title", "<content"};
	private String[] end								= new String[]{"/link", "/publ", "/pubD", "/desc", "/titl", "/cont"};
	private String[] of_types							= new String[]{"<link>", "<published>", "<pubDate>", "<description>", "<title", "<content",
				"</link>", "</published>", "</pubDate>", "</description>", "</title", "</content", "<entry", "<item", "</entry", "</item"};
	private String dump_path, url_path;
	private final int width;

	public parser(String storage, String group, String feed, int widther)
	{
		width = widther;
		try
		{
			parse_local_xml(storage, group, feed);
		}
		catch(IOException e)
		{
		}
	}

	private void parse_local_xml(String storage, String group, String feed) throws IOException
	{
		dump_path						= storage + "content.dump" + main.TXT;
		url_path							= storage + "content.url"  + main.TXT;
		final String store_file		= storage + feed + main.STORE_APPENDIX;
		final String feed_folder	= storage + main.GROUPS_DIRECTORY + group + main.SEPAR + feed + main.SEPAR;
		final String content_file	= feed_folder + feed + main.CONTENT_APPENDIX;
		final String image_dir		= feed_folder + main.IMAGE_DIRECTORY;
		final String thumbnail_dir	= feed_folder + main.THUMBNAIL_DIRECTORY;
		final File in					= new File(store_file);
		final File out					= new File(content_file);
		final String[] filters		= utilities.read_file_to_array(storage + main.FILTER_LIST);

		Set<String> set				= new LinkedHashSet<String>();
		Boolean write_mode			= false;
		Boolean c_mode					= false;
		Time time						= new Time();
		BufferedReader reader		= new BufferedReader(new FileReader(in));
		StringBuilder line			= new StringBuilder();
		String current_tag, temp_line, cont, image, image_name;
		int tem, tem2, tem3, description_length, take, cont_length, i;

		/// Read the file's lines to a list and make a set from that.
		if(out.exists())
		{
			String liner;
			final BufferedReader stream = new BufferedReader(new FileReader(out));
			while((liner = stream.readLine()) != null)
				set.add(liner);
			stream.close();
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		reader.mark(2);
		while(reader.read() != -1)
		{
			reader.reset();
			try
			{
				current_tag = get_next_tag(reader, of_types);
			}
			catch(Exception e)
			{
				current_tag = "";
			}
			if((current_tag.contains("<entry"))||(current_tag.contains("<item")))
			{
				/// Add line to set and reset the line.
				if((line.length() > 1)&&(write_mode))
				{
					temp_line = line.toString();
					if(!temp_line.contains("published|")&&(!temp_line.contains("pubDate|"))&&(!set.contains(temp_line)))
						temp_line = temp_line.concat(("pubDate|").concat(rfc3339.format(new Date()).concat("|")));
					set.add(temp_line);
				}
				line.setLength(0);
				write_mode = true;
			}
			else if((current_tag.contains("</entry"))||(current_tag.contains("</item")))
			{
				image = check_for_image();
				if(!image.equals(""))
				{
					line.append("image|").append(image).append('|'); /// ends with a |
					image_name = image.substring(image.lastIndexOf(main.SEPAR) + 1, image.length());

					if(!utilities.exists(image_dir + image_name))
						utilities.download_file(image, image_dir + image_name);
					if(!utilities.exists(thumbnail_dir + image_name))
						compress_file(image_dir, thumbnail_dir, image_name);

					/// TODO: If it does exist, skip this next step somehow. (turn write mode to false)

					BitmapFactory.decodeFile(thumbnail_dir + image_name, options);
					line.append("width|").append(options.outWidth).append('|')
						.append("height|").append(options.outHeight).append('|');
				}
				line.append(check_for_url());
			}
			else
			{
				for(i = 0; i < start.length; i++)
				{
					if(current_tag.contains(start[i]))
					{
						description_length = 0;

						if(current_tag.contains("<title"))
							current_tag = "<title>";
						else if(current_tag.contains("<description"))
						{
							/// remove content
							if(start.length == 6)
							{
								start		= utilities.remove_element(start, 5);
								end		= utilities.remove_element(end, 5);
								of_types	= utilities.remove_element(of_types, 5);
								of_types	= utilities.remove_element(of_types, 10);
							}
							description_length = -2048;
						}
						else if(current_tag.contains("<content"))
							current_tag = "<description>";

						/// Write description| to the line buffer.
						line.append(current_tag.substring(1, current_tag.length() - 1)).append("|");

						cont = get_content_to_end_tag(reader, end[i]).trim();
						cont_length = cont.length();

						/// remove <![CDATA[ if it exists.
						if((cont_length > 10)&&(cont.substring(0, 9).equals("<![CDATA[")))
						{
								cont = cont.substring(9, cont_length - 3);
								c_mode = true;
						}

						cont = cont.replace("&amp;", "&").replace("&quot;", "\"");

						/// Save the image url from cont.
						if(current_tag.contains("<description"))
						{
							tem = cont.indexOf("img src=");
							if(tem != -1)
							{
								tem2 = cont.indexOf("\"", tem + 10);
								if(tem2 == -1)
									tem2 = cont.indexOf("\'", tem + 10);
								else
								{
									tem3 = cont.indexOf("\'", tem + 10);
									if((tem3 != -1)&&(tem3 < tem2))
											tem2 = tem3;
								}
								to_file(dump_path, cont.substring(tem + 9, tem2) + main.NL);
							}
						}
						/// If it follows the rss 2.0 specification for rfc882
						else if(current_tag.equals("<pubDate>"))
						{
							try
							{
								cont = rfc3339.format(rss_date.parse(cont));
							}
							catch(Exception e)
							{
								utilities.append_string_to_file(storage + "time_bug.txt", "BUG : Meant to be atom-3339 but looks like: " + cont + main.NL);
								cont = rfc3339.format(new Date());
							}
							line.append(cont).append("|");
							break;
						}
						/// If it is an atom feed it will be one of four rfc3339 formats.
						else if(current_tag.equals("<published>"))
						{
							try
							{
								time.parse3339(cont);
								cont = time.format3339(false);
							}
							catch(Exception e)
							{
								utilities.append_string_to_file(storage + "time_bug.txt", "BUG : Meant to be atom-3339 but looks like: " + cont + main.NL);
								cont = rfc3339.format(new Date());
							}
							line.append(cont).append("|");
							break;
						}

						/// Replace all <x> with nothing.
						if(c_mode)
						{
							cont = regex_cdata_tags.matcher(cont).replaceAll("");
							c_mode = false;
						}
						else
						{
							cont = regex_tags.matcher(cont).replaceAll("");
						}
						cont = space_tags.matcher(cont).replaceAll(" ");

						if(current_tag.contains("<title>"))
						{
							String cont2 = cont.toLowerCase();
							for(String filter : filters)
							{
								if(cont2.contains(filter.toLowerCase()))
								{
									write_mode = false;
									break;
								}
							}
						}

						take = description_length;
						description_length += cont.length();

						if((description_length > 512)&&(take < 512))
							line.append(cont.substring(0, 512 - take));
						else if(description_length < 512)
							line.append(cont);

						line.append("|");
						break;
					}
				}
			}
			reader.mark(2);
		}

		/// Add the last line that has no <entry / <item after it.
		if(write_mode)
		{
			temp_line = line.toString();
			if(!temp_line.contains("published|")&&(!temp_line.contains("pubDate|"))&&(!set.contains(temp_line)))
				temp_line = temp_line.concat(("pubDate|").concat(rfc3339.format(new Date()).concat("|")));
			set.add(temp_line);
		}
		/// Write the new content to the file.
		in.delete();
		out.delete();

		final String[] feeds = set.toArray(new String[set.size()]);

		/// TODO: May already be the out File.
		final BufferedWriter write = new BufferedWriter(new FileWriter(content_file, true));
		for(String fed : feeds)
			write.write(fed + main.NL);
		write.close();
	}

	private String get_content_to_end_tag(BufferedReader reader, String tag)
	{
		/* </link> */
		StringBuilder cont = new StringBuilder();
		char[] buffer = new char[5];
		try
		{
			while(!(new String(buffer)).equals(tag))
			{
				cont.append(read_string_to_next_char(reader, '<'));
				/* hello my name is a penguin< */
				reader.mark(6);
				reader.read(buffer, 0, 5);
				reader.reset();
			}
			/* hello my name is a penguin<link>blash stha */
			cont.deleteCharAt(cont.length() - 1);
		}
		catch(Exception e){
			return "";
		}
		return cont.toString();
	}

	private String read_string_to_next_char(BufferedReader reader, char next)
	{
		char   current;
		char[] build	= new char[4096];
		int    i			= 0;
		try
		{
			while((current = ((char) reader.read())) != next)
			{
				build[i] = current;
				i++;
			}
			build[i] = next;

			return new String(build, 0, i + 1);
		}
		catch(Exception e)
		{
			return "";
		}
	}

	private String check_for_image()
	{
		final File im = new File(dump_path);
		String popo = "";
		try
		{
			final BufferedReader image = new BufferedReader(new FileReader(im));
			final String image_url = image.readLine();
			if(image_url.length() > 6)
				popo = image_url;
		}
		catch(Exception e){
		}
		im.delete();
		return popo;
	}

	private String check_for_url()
	{
		final File iu = new File(url_path);
		String momo = "";
		try
		{
			final BufferedReader u = new BufferedReader(new FileReader(iu));
			final String url = u.readLine();
			if(url.length() > 6)
				momo = "link|" + url + "|";
		}
		catch(Exception e){
		}
		iu.delete();
		return momo;
	}

	private String get_next_tag(BufferedReader reader, String... types) throws IOException
	{
		boolean found = false;
		int tem, tem2, tem3;
		String tag = "";
		int eof;
		while(!found)
		{
			char current = '\0';
			while(current != '<')
			{
				eof = reader.read();
				if(eof == -1)
					return "eof";
				else
					current = (char) eof;
			}

			tag = "<" + read_string_to_next_char(reader, '>');

			tem = tag.indexOf("img src=");
			if(tem != -1)
			{
				tem2 = tag.indexOf("\"", tem + 10);
				if(tem2 == -1)
					tem2 = tag.indexOf("\'", tem + 10);
				else
				{
					tem3 = tag.indexOf("\'", tem + 10);
					if((tem3 != -1)&&(tem3 < tem2))
							tem2 = tem3;
				}
				to_file(dump_path, tag.substring(tem + 9, tem2) + main.NL);
			}

			if((tag.contains("type=\"text/html"))||(tag.contains("type=\'text/html")))
			{
				tem = tag.indexOf("href=");
				if(tem != -1)
				{
					tem2 = tag.indexOf("\"", tem + 7);
					if(tem2 == -1)
						tem2 = tag.indexOf("\'", tem + 7);
					else
					{
						tem3 = tag.indexOf("\'", tem + 7);
						if((tem3 != -1)&&(tem3 < tem2))
								tem2 = tem3;
					}
					to_file(url_path, tag.substring(tem + 6, tem2) + main.NL);
				}
			}
			else if((tag.contains("type=\"image/jpeg"))||(tag.contains("type=\'image/jpeg")))
			{
				tem = tag.indexOf("href=");
				if(tem != -1)
				{
					tem2 = tag.indexOf("\"", tem + 7);
					if(tem2 == -1)
						tem2 = tag.indexOf("\'", tem + 7);
					else
					{
						tem3 = tag.indexOf("\'", tem + 7);
						if((tem3 != -1)&&(tem3 < tem2))
								tem2 = tem3;
					}
					utilities.delete(dump_path);
					to_file(dump_path, tag.substring(tem + 6, tem2) + main.NL);
				}
			}

			for(String type : types)
				if(tag.contains(type))
					found = true;
		}
		return tag;
	}

	private void compress_file(String image_dir, String thumbnail_dir, String image_name)
	{
		int insample;

		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image_dir + image_name, o);

		int width_tmp = o.outWidth;

		insample = (width_tmp > width) ? (Math.round((float) width_tmp / (float) width)) : 1;

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = insample;
		Bitmap bitmap = BitmapFactory.decodeFile(image_dir + image_name, o2);

		try
		{
			FileOutputStream out = new FileOutputStream(thumbnail_dir + image_name);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		}
		catch (Exception e)
		{
		}
	}

	private void to_file(String file_namer, String string)
	{
		try
		{
			final BufferedWriter out = new BufferedWriter(new FileWriter(file_namer));
			out.write(string);
			out.close();
		}
		catch(Exception e){}
	}
}
