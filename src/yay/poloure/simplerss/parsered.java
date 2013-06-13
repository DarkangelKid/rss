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
	
	String[] content_titles;
	boolean inside_item = false;
	boolean run_duplicate_test = false;
	boolean duplicate = false;
	boolean set_title_mode = false;
	
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
		if((qName.equals("title"))&&(inside_item == false))
			set_title_mode = true;
		else if(qName.equals("item"))
			inside_item = true;
		else if((qName.equals("title"))&&(inside_item == true))
			run_duplicate_test = true;
		else if((inside_item == true)&&(duplicate == false))
			to_file(this.file + ".content.txt", qName + "|");
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals("item"))
		{
			if(duplicate == false)
				to_file(this.file + ".content.txt", "\n");
			inside_item = false;
			duplicate = false;
		}
	}

	@Override
	public void characters(char[] ac, int i, int j) throws SAXException {
		String content_string = new String(ac, i, j);
		boolean empty = true;
		if(set_title_mode == true)
		{
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(this.file + ".title.txt", false));
				out.write(content_string);
				out.close();
			}
			catch(Exception e){}
			set_title_mode = false;
		}
		if(content_string.length()>0)
		{
			for(int k=0; k<content_string.length()-1; k++)
			{
				if((content_string.charAt(k) != ' ')&&(content_string.charAt(k) != '\t')){
					empty = false;
				}
			}
		}
		if(empty == false)
		{
			if(run_duplicate_test == true)
			{
				for(int l=0; l<content_titles.length; l++)
				{
					if(content_titles[l].equals(content_string))
					{
						duplicate = true;
						break;
					}
				}
				if(duplicate == false)
					to_file(this.file + ".content.txt", "title|");
				run_duplicate_test = false;
			}
			if((inside_item == true)&&(duplicate == false))
				to_file(this.file + ".content.txt", content_string + "|");
		}
	}

	private void to_file(String file_namer, String string)
	{
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(file_namer, true));
			out.write(string);
			out.close();
		}
		catch(Exception e){}
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

