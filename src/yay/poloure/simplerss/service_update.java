package yay.poloure.simplerss;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.net.URL;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class service_update extends IntentService
{
	int group;
	private static String storage;
	
	public service_update()
	{
		super("service_update");
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakelock = pm.newWakeLock(pm.PARTIAL_WAKE_LOCK, "SIMPLERSS");
		wakelock.acquire();
		
		group = Integer.parseInt(intent.getStringExtra("GROUP_NUMBER"));
		storage = this.getExternalFilesDir(null).getAbsolutePath() + "/";

		String grouper = read_file_to_list(storage + "groups/group_list.txt", 0).get(group);
		String group_file_path 		= storage + "groups/" + grouper + ".txt";

		List< List<String> > content 		= read_csv_to_list(new String[]{group_file_path, "0", "name", "url"});
		List<String> group_feeds_names 		= content.get(0);
		List<String> group_feeds_urls 		= content.get(1);

		String feed_path = "";

		/// If we should download and update the feeds inside that group.
		for(int i=0; i<group_feeds_names.size(); i++)
		{
			feed_path = storage + "content/" + group_feeds_names.get(i); /// mariam_feed.txt
			download_file(group_feeds_urls.get(i), "content/" + group_feeds_names.get(i) + ".store.txt"); /// Downloads file as mariam_feed.store.txt
			new parsered(feed_path + ".store.txt"); /// Parses the file and makes other files like mariam_feed.store.txt.content.txt
		}
		wakelock.release();
	}
	
	private List<String> read_file_to_list(String file_name, int lines_to_skip)
	{
		String line = null;
		BufferedReader stream = null;
		List<String> lines = new ArrayList<String>();
		try
		{
			stream = new BufferedReader(new FileReader(storage + file_name));
			for(int i=0; i<lines_to_skip; i++)
				stream.readLine();
			while((line = stream.readLine()) != null)
				lines.add(line);
			if (stream != null)
				stream.close();
		}
		catch(Exception e){
		}
		return lines;
	}
	
	private List< List<String> > read_csv_to_list(String[] type)
	{
		String feed_path = type[0];
		int lines_to_skip = Integer.parseInt(type[1]);
		int number_of_items = type.length - 2;

		String line = null;
		BufferedReader stream = null;
		List< List<String> > types = new ArrayList< List<String> >();
		for(int i = 0; i < number_of_items; i++)
			types.add(new ArrayList< String >());

		String content = "";

		try
		{
			stream = new BufferedReader(new FileReader(feed_path));

			/// Skip lines.
			for(int i=0; i<lines_to_skip; i++)
				stream.readLine();

			while((line = stream.readLine()) != null)
			{
				for(int i = 0; i < number_of_items; i++)
				{
					content = type[2 + i] + "|";
					if((!line.contains(content))||(line.contains(content + '|')))
						types.get(i).add("");
					else
					{
						int content_start = line.indexOf(content) + content.length();
						types.get(i).add(line.substring(content_start, line.indexOf('|', content_start)));
					}
				}
			}
		}
		catch(Exception e){
		}
		return types;
	}
	
	private void download_file(String urler, String file_name)
	{
			try
			{
				BufferedInputStream in = null;
				FileOutputStream fout = null;
				try
				{
					in = new BufferedInputStream(new URL(urler).openStream());
					fout = new FileOutputStream(storage + file_name);

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
