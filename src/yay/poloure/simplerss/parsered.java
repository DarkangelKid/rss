package yay.poloure.simplerss;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class parsered extends DefaultHandler
{
	public String file;
	public parsered(String file_path)
	{
		this.file = file_path;
		parse_local_xml(file_path);
	}
	
	private void parse_local_xml(String file_name)
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			SAXParser parserer = factory.newSAXParser();
			to_file(this.file + ".parsed.txt", "DEBUG: Starting parse of:" + file_name + "\n");
			parserer.parse(file_name, this);
		}
		catch(Exception e)
		{
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try{
			to_file(this.file + ".parsed.txt", "START: " + uri + "; " + localName + "; " + qName + "\n");
		}
		catch(Exception e){}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		try{
			to_file(this.file + ".parsed.txt", "END: " + uri + "; " + localName + "; " + qName + "\n");
		}
		catch(Exception e){}
	}

	private void to_file(String file_namer, String string) throws Exception
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(file_namer, true));
		out.write(string);
		out.close();
	}
}

