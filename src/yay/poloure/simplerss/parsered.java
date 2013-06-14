package yay.poloure.simplerss;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class parsered
{
	public String file;
	
	public parsered(String file_path)
	{
		this.file = file_path;
		parse_local_xml(file_path);
	}
	
	private void parse_local_xml(String file_name)
	{
		try
		{
			String line;
			File in = new File(file_name);
			BufferedReader reader = new BufferedReader(new FileReader(in));
			boolean title_found = false;

			while((line = reader.readLine()) != null)
			{
				while((!line.contains("<entry>"))&&(!line.contains("<item>")))
				{
					if(title_found){
					}
					else if(line.contains("<tit"))
					{
						int stop = line.indexOf('>') + 1;
						String title = line.substring(stop, line.indexOf('<', stop));
						to_file(file_name + ".title.txt", title, false);
						title_found = true;
					}
					line = reader.readLine();
				}
				line = reader.readLine();

				while((!line.contains("</entry>"))&&(!line.contains("</item>")))
				{
					while(!line.contains("<"))
						line = reader.readLine();
						if ((!line.contains("<content"))&&(!line.contains("<des")))
						{
							line = line.trim();
							if(line.length() > line.indexOf('>') + 1)
							{
								int space_index, end_index, stop;
								space_index = line.indexOf(' ');
								end_index = line.indexOf('>');
								if(space_index == -1)
									stop = end_index;
								else if(space_index < end_index)
									stop = space_index;
								else
									stop = end_index;
								String tag = line.substring(line.indexOf('<') + 1, stop);
								to_file(file_name + ".content.txt", tag + "|", true);
								String content = line.substring(stop + 1, line.indexOf('<', stop + 1));
								to_file(file_name + ".content.txt", content + "|", true);
							}
						}
						else if(line.contains("<des"))
						{
							to_file(file_name + ".content.txt", "description|", true);
							String content = line.substring(line.indexOf('>') + 1, line.indexOf("</des"));
							to_file(file_name + ".content.txt", content + "|", true);
						}
						else
						{
							line = reader.readLine();
							to_file(file_name + ".content.txt", "description|", true);
							while(!line.contains("</content"))
							{
								line = line.trim();
								if(line.length()>0)
									to_file(file_name + ".content.txt", line + " ", true);
								line = reader.readLine();
							}
						}
					line = reader.readLine();
				}
				to_file(file_name + ".content.txt", "\n", true);
			}
		}
		catch(Exception e)
		{
		}
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

