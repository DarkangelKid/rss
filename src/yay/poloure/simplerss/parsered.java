package yay.poloure.simplerss;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
//import android.os.Debug;
import android.text.format.Time;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

class parsered
{

	private static final SimpleDateFormat rss_date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	private static final SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
	/*private static final SimpleDateFormat atom_date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH);
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH),
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH),
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
	};*/

	public parsered(String file_path){
		parse_local_xml(file_path);
	}

	private void parse_local_xml(String file_name)
	{
		try
		{
			final Pattern regex_tags = Pattern.compile("(&lt;).*?(&gt;)");
			final Pattern regex_cdata_tags = Pattern.compile("\\<.*?\\>");
			final String[] start = new String[]{"<name>", "<link>", "<published>", "<pubDate>", "<description>", "<title", "<content"};
			final String[] end = new String[]{"</name>", "</link>", "</published>", "</pubDate>", "</description>", "</title", "</content"};
			String[] of_types = new String[]{"<name>", "<link>", "<published>", "<pubDate>", "<description>", "<title", "<content",
				"</name>", "</link>", "</published>", "</pubDate>", "</description>", "</title", "</content", "<entry", "<item", "</entry", "</item"};

			File in = new File(file_name), out = new File(file_name + ".content.txt");
			Set<String> set = new LinkedHashSet<String>();
			Boolean write_mode = false, c_mode = false;;
			int pos, tem, tem2, tem3, description_length, take, cont_length;
			Time time = new Time();
			final int start_size = start.length;

			/// Read the file's lines to a list and make a set from that.
			if(out.exists())
			{
				String liner;
				BufferedReader stream = new BufferedReader(new FileReader(out));
				while((liner = stream.readLine()) != null)
					set.add(liner);
				stream.close();
			}

			BufferedReader reader = new BufferedReader(new FileReader(in));
			StringBuilder line = new StringBuilder();
			String current_tag, temp_line, cont;

			reader.mark(2);
			while(reader.read() != -1)
			{
				reader.reset();
				current_tag = get_next_tag(reader, of_types, file_name);
				if((current_tag.contains("<entry"))||(current_tag.contains("<item")))
				{
					/// Add line to set and reset the line.
					if((line.length() > 1)&&(write_mode))
					{
						temp_line = line.toString();
						if(!set.contains("marker|1|" + temp_line))
							set.add(temp_line);
					}
					line.setLength(0);
					write_mode = true;
				}
				else if((current_tag.contains("</entry"))||(current_tag.contains("</item")))
				{
					line.append(check_for_image(file_name));
					line.append(check_for_url(file_name));
				}
				else
				{
					for(int i=0; i < start_size; i++)
					{
						if(current_tag.contains(start[i]))
						{
							description_length = 0;

							if(current_tag.contains("<content"))
								current_tag = "<description>";
							else if(current_tag.contains("<title"))
								current_tag = "<title>";
							if(!current_tag.contains("description"))
								description_length = -2048;

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
									to_file(file_name + ".content.dump.txt", cont.substring(tem + 9, tem2) + "\n", false);
								}
							}
							/// If it follows the rss 2.0 specification for rfc882
							else if(current_tag.equals("<pubDate>"))
							{
								try{
									cont = rfc3339.format(rss_date.parse(cont));
								}
								catch(Exception e){
									main_view.log("BUG : Meant to be rss-882 but looks like: " + cont);
									cont = rfc3339.format(new Date());
								}
								line.append(cont).append("|");
								break;
							}
							/// If it is an atom feed it will be one of four rfc3339 formats.
							else if(current_tag.equals("<published>"))
							{
								try{
									time.parse3339(cont);
									cont = time.format3339(false);
								}
								catch(Exception e){
									main_view.log("BUG : Meant to be atom-3339 but looks like: " + cont);
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
								cont = regex_tags.matcher(cont).replaceAll("");

							/// Format the final string.
							cont = cont	.replaceAll("\n", " ")
										.replace("|", "")
										.replace("&amp;", "&")
										.replace("&nbsp;", " ")
										.replaceAll("\r", " ");

							take = description_length;
							description_length = description_length + cont.length();

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
				if(!set.contains("marker|1|" + temp_line))
					set.add(temp_line);
			}
			/// Write the new content to the file.
			in.delete();
			out.delete();

			String[] feeds = set.toArray(new String[set.size()]);

			BufferedWriter write = new BufferedWriter(new FileWriter(file_name + ".content.txt", true));
			for(String feed : feeds)
				write.write(feed + "\n");
			write.close();
		}
		catch(Exception e){
		}
	}

	private String get_content_to_end_tag(BufferedReader reader, String end_tag)
	{
		StringBuilder cont = new StringBuilder();
		Boolean found = false;
		if(end_tag.length() > 8)
			end_tag = end_tag.substring(0, 8);
		char[] test_tag = new char[8];
		try
		{
			while(!found)
			{
				cont.append(read_string_to_next_char(reader, '<'));
				reader.mark(8);
				reader.read(test_tag, 0, 8);
				if(("<" + new String(test_tag)).contains(end_tag))
					found = true;
				reader.reset();
			}
			cont.deleteCharAt(cont.length() - 1);
		}
		catch(Exception e){
			return "";
		}
		return cont.toString();
	}

	private String read_string_to_next_char(BufferedReader reader, char next)
	{
		StringBuilder build = new StringBuilder();
		char current;
		try{
			while((current = ((char) reader.read())) != next)
				build.append(current);
			build.append(next);

			return build.toString();
		}
		catch(Exception e){
			return "";
		}
	}

	private String check_for_image(String file_name)
	{
		final File im = new File(file_name + ".content.dump.txt");
		String popo = "";
		try
		{
			BufferedReader image = new BufferedReader(new FileReader(im));
			String image_url = image.readLine();
			if(image_url.length() > 6)
				popo = "image|" + image_url + "|";
		}
		catch(Exception e){
		}
		im.delete();
		return popo;
	}

	private String check_for_url(String file_name)
	{
		final File iu = new File(file_name + ".content.url.txt");
		String momo = "";
		try
		{
			BufferedReader u = new BufferedReader(new FileReader(iu));
			String url = u.readLine();
			if(url.length() > 6)
				momo = "link|" + url + "|";
		}
		catch(Exception e){
		}
		iu.delete();
		return momo;
	}

	private String get_next_tag(BufferedReader reader, String[] types, String file_name) throws Exception
	{
		boolean found = false;
		int tem, tem2, tem3;
		String tag = "";
		int eof = 0;
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
				to_file(file_name + ".content.dump.txt", tag.substring(tem + 9, tem2) + "\n", false);
			}

			if(tag.contains("type=\"text/html\""))
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
					to_file(file_name + ".content.url.txt", tag.substring(tem + 6, tem2) + "\n", false);
				}
			}

			for(String type : types)
			{
				if(tag.contains(type))
					found = true;
			}
		}
		return tag;
	}

	private void to_file(String file_namer, String string, boolean append)
	{
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(file_namer, append));
			out.write(string);
			out.close();
		}
		catch(Exception e){}
	}
}
