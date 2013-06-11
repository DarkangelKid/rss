package yay.poloure.simplerss;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

public class parsered extends DefaultHandler
{
	public String file;
	public String mode;
	public boolean check_existing = false;
	public boolean skip = false;
	
	String[] content_titles;
	
	public parsered(String file_path)
	{
		this.file = file_path;
		content_titles = read_csv_to_array("title", "content");
		parse_local_xml(file_path);
	}
	
	private void parse_local_xml(String file_name)
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			SAXParser parserer = factory.newSAXParser();
			File uri = new File(file_name);
			parserer.parse(uri, this);
		}
		catch(Exception e)
		{
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try{
			if(qName.equals("item"))
				mode = "item_mode";
			else if(qName.equals("channel"))
				mode = "channel_mode";

			if(check_existing == false){
				if((qName == "title")&&(mode.equals("item_mode")))
					check_existing = true;
				else
				{
					if((mode.equals("item_mode"))&&(!(qName.equals("item")))&&(!qName.equals("title")))
						to_file(this.file + ".content.txt", qName + "|");
					else if((mode.equals("channel_mode"))&&(!(qName.equals("channel")))&&(!(qName.equals("description")))&&(!(qName.equals("atom:link"))))
						to_file(this.file + ".title.txt", qName + "|");
				}
			}
		}
		catch(Exception e){}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try
		{
			if(qName.equals("item"))
			{
				mode = "empty_mode";
				if(check_existing == true)
				{
					check_existing = false;
					skip = false;
				}
				else
				{
					content_titles = read_csv_to_array("title", "content");
					to_file(this.file + ".content.txt", "\n");
				}
			}
			else if(qName.equals("lastBuildDate"))
			{
				mode = "empty_mode";
				to_file(this.file + ".title.txt", "\n");
			}
		}
		catch(Exception e){}
	}

	@Override
	public void characters(char[] ac, int i, int j) throws SAXException {
		String content_string = new String(ac, i, j);
		boolean empty = true;
		if(content_string.length()>0)
		{
			for(int k=0; i<content_string.length()-1; i++)
			{
				if((content_string.charAt(k) != ' ')&&(content_string.charAt(k) != '\t')){
					empty = false;
					break;
				}
			}
		}
		if(empty == false)
		{
			try
			{
				if((check_existing == true)&&(skip == false))
				{
					boolean match = false;
					for(int l = 0; l < content_titles.length; l++)
					{
						if(content_titles[l].equals(content_string))
						{
							match = true;
							break;
						}
					}
					if(match == false)
					{
						check_existing = false;
						to_file(this.file + ".content.txt", "title|");
					}
					skip = true;
				}
				if(check_existing == false)
				{
					if(mode.equals("item_mode"))
						to_file(this.file + ".content.txt", content_string + "|");
					if(mode.equals("channel_mode"))
						to_file(this.file + ".title.txt", content_string + "|");
				}
			}
			catch(Exception e){
			}
		}
	}

	private void to_file(String file_namer, String string) throws Exception
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(file_namer, true));
		out.write(string);
		out.close();
	}

	private String[] read_csv_to_array(String content_type, String title_or_content)
	{
		String[] content_values;
		try
		{
			String line;
			int number_of_lines = 0, i = 0;
			File in = new File(this.file + "." + title_or_content + ".txt");

			BufferedReader reader = new BufferedReader(new FileReader(in));

			while((line = reader.readLine()) != null)
				number_of_lines++;

			reader.close();
			reader = new BufferedReader(new FileReader(in));
				
			content_values = new String[number_of_lines];

			/// Get the stuff
			while((line = reader.readLine()) != null)
			{
				int content_start = line.indexOf(content_type) + content_type.length() + 1;
				line = line.substring(content_start, line.indexOf('|', content_start));
				content_values[i] = line;
				i++;
			}
		}
		catch (Exception e)
		{
			content_values = new String[0];
		}

		return content_values;
	}
}

