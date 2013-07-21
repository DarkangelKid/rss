package yay.poloure.simplerss;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.List;

import android.os.PowerManager;
import android.os.Process;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.view.Display;
import android.graphics.Point;
import android.view.WindowManager;

public class service_update extends IntentService
{

	public service_update()
	{
		super("service_update");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SIMPLERSS");
		wakelock.acquire();

		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		final int group 						= intent.getIntExtra("GROUP_NUMBER", 0);
		final String all_string 				= getString(R.string.all_group);
		final String storage 					= this.getExternalFilesDir(null).getAbsolutePath() + "/";

		final List<String> all_groups 			= utilities.read_file_to_list(storage + "groups/group_list.txt");
		final String grouper					= all_groups.get(group);
		final String group_file_path 			= storage + "groups/" + grouper + ".txt";
		final String group_content_path 		= group_file_path + ".content.txt";

		final List< List<String> > content 		= utilities.read_csv_to_list(group_file_path, new String[]{"name|", "url|"}, false);
		final List<String> group_feeds_names 	= content.get(0);
		final List<String> group_feeds_urls 	= content.get(1);

		String image_name = "", thumbnail_path = "", feed_path = "", image;
		int i;

		final Point screen_size = new Point();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(screen_size);
		final int width = (int) (Math.round(((screen_size.x)*0.944)));

		final int size = group_feeds_names.size();
		for(i = 0; i < size; i++)
		{
			feed_path = storage + "content/" + group_feeds_names.get(i); /// mariam_feed.txt
			utilities.download_file(group_feeds_urls.get(i), feed_path + ".store.txt"); /// Downloads file as mariam_feed.store.txt
			new parsered(feed_path + ".store.txt", storage, width); /// Parses the file and makes other files like mariam_feed.store.txt.content.txt
		}

		/// Sort group order
		if(!grouper.equals(all_string))
		{
			utilities.sort_group_content_by_time(storage, all_string);
			utilities.sort_group_content_by_time(storage, grouper);
		}
		else
		{
			for(String gro : all_groups)
				utilities.sort_group_content_by_time(storage, gro);
		}

		/// Read all the group files and how many new items.
		/// TODO: If new feed, count new set objects and return from parser to add to the total.
		final List<Integer> unread_list = utilities.get_unread_counts(null, null, storage);

		int group_items = 1;
		int total = 0, count = 0;
		final int sizes = unread_list.size();

		for(i = 1 ; i < sizes; i++)
		{
			count = unread_list.get(i);
			if(count > 0)
			{
				total += count;
				group_items++;
			}
		}

		if((total > 0) && intent.getBooleanExtra("NOTIFICATIONS", false))
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

		wakelock.release();
		stopSelf();
	}

	/*private boolean check_activity_running()
	{
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

		for(RunningTaskInfo task : tasks)
		{
			if(getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
				return true;
		}
		return false;
	}*/
}
