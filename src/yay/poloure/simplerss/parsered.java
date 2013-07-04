package yay.poloure.simplerss;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

class parsered
{
	public parsered(String file_path){
		parse_local_xml(file_path);
	}
	
	private void parse_local_xml(String file_name)
	{
		try
		{
			final String[] start = new String[]{"<name>", "<link>", "<published>", "<updated>", "<pubDate>", "<description>", "<title", "<content"};
			final String[] end = new String[]{"</name>", "</link>", "</published>", "</updated>", "</pubDate>", "</description>", "</title", "</content"};
			String[] of_types = new String[]{"<name>", "<link>", "<published>", "<updated>", "<pubDate>", "<description>", "<title", "<content",
				"</name>", "</link>", "</published>", "</updated>", "</pubDate>", "</description>", "</title", "</content", "<entry", "<item", "</entry", "</item"}; 
				
			File in = new File(file_name);
			BufferedReader reader = new BufferedReader(new FileReader(in));
			int description_length = 0;
			Boolean tag_content;
			String end_tag = "", cont = "";
			reader.mark(2);

			while(reader.read() != -1)
			{
				reader.reset();
				String current_tag = get_next_tag(reader, of_types, file_name);
				if((current_tag.contains("<entry"))||(current_tag.contains("<item")))
					to_file(file_name + ".content.txt", "\n", true);
				else if((current_tag.contains("</entry"))||(current_tag.contains("</item")))
				{
					check_for_image(file_name);
					check_for_url(file_name);
				}
				else
				{
					for(int i=0; i<start.length; i++)
					{
						if(current_tag.contains(start[i]))
						{
							if(current_tag.contains("<content"))
								current_tag = "<description>";
							else if(current_tag.contains("<title"))
								current_tag = "<title>";
							if(!current_tag.contains("description"))
								description_length = -2048;
							to_file(file_name + ".content.txt", current_tag.substring(1, current_tag.length() - 1) + "|", true);
							while(!(end_tag.contains(end[i])))
							{
								cont = read_string_to_next_char(reader, '<', false)
									.replaceAll("\n", "")
									.replace("|", "")
									.replace("&amp;", "&")
									.replace("&nbsp;", " ")
									.replaceAll("\r", " ")
									.replace("&lt;", "<")
									.replace("&gt;", ">")
									.replace("&quot;", "\"")
									.replace("&mdash;", "—")
									.replace("&hellip;", "…")
									.replace("&#8217;", "’")
									.replace("&#8216;", "‘")
									.replaceAll("\t", "&t&")
									.replace("</p>", "&n&")
									.replace("&rsquo;", "'");
									
								if(cont.contains("img src="))
									to_file(file_name + ".content.dump.txt", cont.substring(cont.indexOf("src=\"") + 5, cont.indexOf("\"", cont.indexOf("src=\"") + 6)) + "\n", false);

								cont = cont.replaceAll("\\<.*?\\>", "").trim();

								int take = description_length;
								description_length = description_length + cont.length();
								
								if((description_length > 512)&&(take < 512))
									to_file(file_name + ".content.txt", cont.substring(0, 512 - take), true);
								else if(description_length < 512)
									to_file(file_name + ".content.txt", cont, true);

								end_tag = "<" + read_string_to_next_char(reader, '>', true);

								if(!(end_tag.contains(end[i])))
								{
									if(end_tag.contains("<![CDATA["))
									{
										end_tag = end_tag
											.replaceAll("\r", " ")
											.replaceAll("\n", "")
											.replace("|", "")
											.replace("<![CDATA[", "")
											.replace("]]>", "")
											.replace("&amp;", "&")
											.replace("&lt;", "<")
											.replace("&gt;", ">")
											.replaceAll("\\<.*?\\>", "")
											.replace("&quot;", "\"")
											.replace("&mdash;", "—")
											.replace("&hellip;", "…")
											.replace("&#8217;", "’")
											.replace("&#8216;", "‘")
											.replaceAll("\t", "&t&")
											.replace("</p>", "&n&")
											.replace("&rsquo;", "'");

										take = description_length;

										description_length = description_length + end_tag.length();
										if((description_length > 512)&&(take < 512))
											to_file(file_name + ".content.txt", end_tag.substring(0, 512 - take), true);
										else if(description_length < 512)
											to_file(file_name + ".content.txt", end_tag, true);
									}
								}
							}
							to_file(file_name + ".content.txt", "|", true);
							description_length = 0;
							break;
						}
					}
				}
				reader.mark(2);
			}
		}
		catch(Exception e){
		}
	}

	private String read_string_to_next_char(BufferedReader reader, char next, Boolean keep_last_char)
	{
		char[] current = new char[1];
		char[] buf = new char[65536];
		int count = 0;
		try{
			while(current[0] != next)
			{
				buf[count] = current[0];
				try{
					reader.read(current, 0, 1);
				}
				catch(Exception e){
				}
				count++;
			}
			if(keep_last_char)
				buf[count] = current[0];
			return (new String(buf)).trim();
		}
		catch(Exception e){
			return "Content exceeds 65536 chars";
		}
	}

	private void check_for_image(String file_name)
	{
		File im = new File(file_name + ".content.dump.txt");
		try
		{
			BufferedReader image = new BufferedReader(new FileReader(im));
			String image_url = image.readLine();
			if(image_url.length()>6)
				to_file(file_name + ".content.txt" , "image|" + image_url + "|", true);
		}
		catch(Exception e){
		}
		im.delete();
	}

	private void check_for_url(String file_name)
	{
		File iu = new File(file_name + ".content.url.txt");
		try
		{
			BufferedReader u = new BufferedReader(new FileReader(iu));
			String url = u.readLine();
			if(url.length()>6)
				to_file(file_name + ".content.txt" , "link|" + url + "|", true);
		}
		catch(Exception e){
		}
		iu.delete();
	}

	private String get_next_tag(BufferedReader reader, String[] types, String file_name) throws Exception
	{
		boolean found = false;
		String tag = "";
		int eof = 0;
		while((!found)&&(eof != -1))
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
			
			tag = "<" + read_string_to_next_char(reader, '>', true);

			if(tag.contains("img src="))
				to_file(file_name + ".content.dump.txt", tag.substring(tag.indexOf("src=\"") + 5, tag.indexOf("\"", tag.indexOf("src=\"") + 6)) + "\n", false);

			if((tag.contains("type=\"text/html\""))&&(tag.contains("href=\"")))
				to_file(file_name + ".content.url.txt", tag.substring(tag.indexOf("href=\"") + 6, tag.indexOf("\"", tag.indexOf("href=\"") + 7)) + "\n", false);
			else if((tag.contains("type=\'text/html\'"))&&(tag.contains("href=\'")))
				to_file(file_name + ".content.url.txt", tag.substring(tag.indexOf("href=\'") + 6, tag.indexOf("\'", tag.indexOf("href=\'") + 7)) + "\n", false);

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
