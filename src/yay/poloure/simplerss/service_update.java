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
import android.os.Process;
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
	private String all_string;

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

		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		all_string = getString(R.string.all_group);

		group = Integer.parseInt(intent.getStringExtra("GROUP_NUMBER"));
		storage = this.getExternalFilesDir(null).getAbsolutePath() + "/";
		if(!storage.equals(""))
		{
			main_view.delete(storage + "storage_location.txt");
			main_view.append_string_to_file(storage + "storage_location.txt", storage);
		}

		List<String> all_groups = main_view.read_file_to_list(storage + "groups/group_list.txt");
		String grouper = all_groups.get(group);
		String group_file_path 		= storage + "groups/" + grouper + ".txt";
		String partial_image_path 		= storage + "images/";

		List< List<String> > content 		= main_view.read_csv_to_list(new String[]{group_file_path, "name", "url"});
		List<String> group_feeds_names 		= content.get(0);
		List<String> group_feeds_urls 		= content.get(1);

		String image_name = "", thumbnail_path = "", feed_path = "";

		final int size = group_feeds_names.size();
		for(int i=0; i<size; i++)
		{
			feed_path = storage + "content/" + group_feeds_names.get(i); /// mariam_feed.txt
			main_view.download_file(group_feeds_urls.get(i), feed_path + ".store.txt"); /// Downloads file as mariam_feed.store.txt
			new parsered(feed_path + ".store.txt"); /// Parses the file and makes other files like mariam_feed.store.txt.content.txt
		}

		/// Sort group order
		if(!grouper.equals(all_string))
		{
			main_view.sort_group_content_by_time(all_string);
			main_view.sort_group_content_by_time(grouper);
		}
		else
		{
			for(String gro : all_groups)
				main_view.sort_group_content_by_time(gro);
		}

		String group_content_path 		= storage + "groups/" + grouper + ".txt.content.txt";
		List< List<String> > contenter 	= main_view.read_csv_to_list(new String[]{group_content_path, "image"});
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
					main_view.download_file(images.get(m), storage + "images/" + image_name);
				/// If the thumbnail does not exist in thumbnails/, compress the image in images/ to thumbnails with image_name.
				if(!(new File(storage + "thumbnails/" + image_name)).exists())
					main_view.compress_file(storage, image_name, grouper, false);
			}
		}

		/// Read all the group files and how many new items.
		/// TODO: If new feed, count new set objects and return from parser to add to the total.
		List<Integer> unread_list = main_view.get_unread_counts();

		int group_items = 1;
		int total = 0, count = 0;
		final int sizes = unread_list.size();

		for(int i = 1 ; i < sizes; i++)
		{
			count = unread_list.get(i);
			if(count > 0)
			{
				total += count;
				group_items++;
			}
		}

		if(total > 0)
		{
			NotificationCompat.Builder not_builder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.rss_icon)
					.setContentTitle(Integer.toString(total) + " Unread Item" + ((total == 1) ? "" : "s"))
					.setContentText(
					Integer.toString(group_items - 1) +
					((group_items - 1 == 1) ? " group has" : " groups have") +
					((total == 1) ? " an unread item." : " unread items."))
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

	private boolean check_activity_running()
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
}
