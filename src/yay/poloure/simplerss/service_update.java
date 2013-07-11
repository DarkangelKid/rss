package yay.poloure.simplerss;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.view.Display;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.net.URL;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Point;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.app.NotificationManager;
import android.content.Intent;
import android.app.PendingIntent;

public class service_update extends IntentService
{
	int group;
	private static String storage;
	final private int width = Integer.parseInt(main_view.read_file_to_list("width.txt", 0).get(0));
	
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

		List<String> all_groups = read_file_to_list("groups/group_list.txt", 0);
		String grouper = all_groups.get(group);
		String group_file_path 		= storage + "groups/" + grouper + ".txt";
		String partial_image_path 		= storage + "images/";
		String partial_thumbnail_path 	= storage + "thumbnails/";

		List< List<String> > content 		= read_csv_to_list(new String[]{group_file_path, "0", "name", "url"});
		List<String> group_feeds_names 		= content.get(0);
		List<String> group_feeds_urls 		= content.get(1);

		String image_name = "", thumbnail_path = "", feed_path = "";

		final int size = group_feeds_names.size();
		for(int i=0; i<size; i++)
		{
			feed_path = storage + "content/" + group_feeds_names.get(i); /// mariam_feed.txt
			download_file(group_feeds_urls.get(i), "content/" + group_feeds_names.get(i) + ".store.txt"); /// Downloads file as mariam_feed.store.txt
			new parsered(feed_path + ".store.txt"); /// Parses the file and makes other files like mariam_feed.store.txt.content.txt
		}

		/// Sort group order
		if(!grouper.equals("All"))
			sort_group_content_by_time("All");
		else
		{
			for(String gro : all_groups)
			{
				if(!gro.equals("All"))
					sort_group_content_by_time(gro);
			}
		}

		/// If we should download and update the feeds inside that group.
		String group_content_path = storage + "groups/" + grouper + ".txt.content.txt";

		String[] passer = {group_content_path, "0", "image"};
		List< List<String> > contenter 	= read_csv_to_list(passer);
		List<String> images 			= contenter.get(0);

			/// For each line of the group_content_file
		final int sizer = images.size();
		for(int m = 0; m < sizer; m++)
		{
			if(!images.get(m).equals(""))
			{
				image_name = images.get(m).substring(images.get(m).lastIndexOf("/") + 1, images.get(m).length());

				/// If the image_name does not exist in images/ then download the file at url (images[m]) to images with name image_name
				if(!(new File(partial_image_path + image_name)).exists())
					download_file(images.get(m), "images/" + image_name);
				/// If the thumbnail does not exist in thumbnails/, compress the image in images/ to thumbnails with image_name.
				if(!(new File(partial_thumbnail_path + image_name)).exists())
					compress_file(image_name, grouper, false);
			}
		}

		/// Read all the group files and how many new items.
		List<Integer> unread_list = new ArrayList<Integer>();
		for(String gro : all_groups)
		{
			int count = 0;
			List<String> count_list = read_file_to_list("groups/" + gro + ".txt.content.txt", 0);
			int sized = count_list.size();
			int i;
			for(i = sized - 1; i >= 0; i--)
			{
				if(count_list.get(i).substring(0, 9).equals("marker|1|"))
					break;
			}
			unread_list.add(sized - i - 1);
		}
		
		int group_items = 0;
		for(int un : unread_list)
		{
			if(un > 0)
				group_items++;
		}

		if((unread_list.get(0) > 0)&&(!check_activity_running()))
		{
			NotificationCompat.Builder not_builder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.rss_icon)
					.setContentTitle(Integer.toString(unread_list.get(0)) + " Unread Item" + ((unread_list.get(0) == 1) ? "" : "s"))
					.setContentText(Integer.toString(group_items) + " groups have unread item" + ((group_items == 1) ? "." : "s."))
					.setAutoCancel(true);

			Intent result_intent = new Intent(this, main_view.class);

			TaskStackBuilder stack_builder = TaskStackBuilder.create(this);

			stack_builder.addParentStack(main_view.class);
			stack_builder.addNextIntent(result_intent);
			PendingIntent result_pending_intent = stack_builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			not_builder.setContentIntent(result_pending_intent);
			NotificationManager notification_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notification_manager.notify(1, not_builder.build());
		}

		///notification end
		wakelock.release();
		stopSelf();
	}

	public boolean check_activity_running()
	{
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

		for(RunningTaskInfo task : tasks)
		{
			if(getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) 
				return true;                                  
		}
		return false;
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

	private String compress_file(String image_name, String group, Boolean skip_save)
	{
		int insample;
		if(!skip_save)
		{
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(storage + "images/" + image_name, o);

			/// TODO: If the activity has not run deal with it. and not produce the compressed file.
			int width_tmp = o.outWidth;

			if(width_tmp > width)
				insample =  Math.round((float) width_tmp / (float) width);
			else
				insample = 1;
		}
		else
			insample = 1;

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = insample;
		Bitmap bitmap = BitmapFactory.decodeFile(storage + "images/" + image_name, o2);
		if(o2.outWidth > 9)
			main_view.append_string_to_file(group + ".image_size.cache.txt", image_name + "|" + o2.outWidth + "|" + o2.outHeight + "\n");

		if(!skip_save)
		{
			try
			{
				FileOutputStream out = new FileOutputStream(storage + "thumbnails/" + image_name);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			}
			catch (Exception e){
			}
		}

		return image_name + "|" + o2.outWidth + "|" + o2.outHeight;
	}

	private void sort_group_content_by_time(String group)
	{
		Date time;

		List<String> feeds_array	= read_csv_to_list(new String[]{storage + "groups/" + group + ".txt", "0", "name"}).get(0);
		List<Date> dates 			= new ArrayList<Date>();
		List<String> links_ordered 	= new ArrayList<String>();
		List<String> content_all 	= new ArrayList<String>();
		List<String> links, pubDates, content;

		for(String feed : feeds_array)
		{
			String content_path = storage + "content/" + feed + ".store.txt.content.txt";
			File test = new File(content_path);
			if(test.exists())
			{

				List< List<String> > contenter	= read_csv_to_list(new String[]{content_path, "0", "link", "pubDate"});
				links 							= contenter.get(0);
				pubDates						= contenter.get(1);
				content 						= read_file_to_list("content/" + feed + ".store.txt.content.txt", 0);

				if(pubDates.get(0).length()<8)
					pubDates 					= read_csv_to_list(new String[]{content_path, "0", "published"}).get(0);
				if(pubDates.get(0).length()<8)
					pubDates 					= read_csv_to_list(new String[]{content_path, "0", "updated"}).get(0);

				final int size = pubDates.size();
				for(int i=0; i<size; i++)
				{
					content_all.add(content.get(i));
					try{
						time 					= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH)).parse(pubDates.get(i));
					}
					catch(Exception e){
						try{
							time 				= (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)).parse(pubDates.get(i));
						}
						catch(Exception t){
							try{
								time 			= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)).parse(pubDates.get(i));
							}
							catch(Exception c){
								try{
									time 		= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH)).parse(pubDates.get(i));
								}
								catch(Exception n){
									try{
										time 	= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)).parse(pubDates.get(i));
									}
									catch(Exception o){
										try{
											time 	= (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)).parse(pubDates.get(i));
										}
										catch(Exception r){
											try{
												time = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)).parse(pubDates.get(i));
											}
											catch(Exception x){
												main_view.log("BUG : Format not found and date looks like: " + pubDates.get(i));
												time = new Date();
											}
										}
									}
								}
							}
						}
					}
					
					final int sizer = dates.size();
					for(int j=0; j<sizer; j++)
					{
						if(time.before(dates.get(j)))
						{
							dates.add(j, time);
							links_ordered.add(j, links.get(i));
							break;
						}
						else if((j == dates.size() - 1)&&(time.after(dates.get(j))))
						{
							dates.add(time);
							links_ordered.add(links.get(i));
							break;
						}
					}
					if(dates.size() == 0)
					{
						dates.add(time);
						links_ordered.add(links.get(i));
					}
				}
			}
		}

		final String group_content_path = "groups/" + group + ".txt.content.txt";
		String last_url = "";
		if(main_view.exists("groups/" + group + ".txt.content.txt"))
		{
			List< List<String> > bonne = read_csv_to_list(new String[]{storage + "groups/" + group + ".txt.content.txt", "0", "link", "marker"});
			List<String> urls = bonne.get(0);
			List<String> marks = bonne.get(1);
			int sized = marks.size();
			for(int i = sized - 1; i >= 0; i--)
			{
				if(marks.get(i).equals("1"))
				{
					last_url = urls.get(i);
					break;
				}
			}
			if((last_url.equals(""))&&(sized > 0))
				last_url = urls.get(0);
		}
		if(last_url.equals(""))
			last_url = links_ordered.get(0);

		main_view.delete(group_content_path);

		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(storage + group_content_path, true));

			if(links_ordered.size()>0)
			{
				for(String link : links_ordered)
				{
					for(String line : content_all)
					{
						if(line.contains(link))
						{
							if(link.equals(last_url))
								out.write(line + "marker|1|\n");
							else
								out.write(line + "\n");
							break;
						}
					}
				}
			}
			out.close();
		}
		catch(Exception e){
		}
	}
}
