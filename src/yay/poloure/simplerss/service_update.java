package yay.poloure.simplerss;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.List;

import android.os.PowerManager;
import android.os.Process;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.app.NotificationManager;
import android.app.PendingIntent;

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

		final int group						= intent.getIntExtra("GROUP_NUMBER", 0);

		final String stor;
		final String storage;
		if((stor = this.getExternalFilesDir(null).getAbsolutePath()) == null)
			return;
		else
			storage							= stor + main_view.SEPAR;

		final List<String> all_groups		= utilities.read_file_to_list(storage + main_view.GROUP_LIST);
		final String grouper				= all_groups.get(group);
		final String group_file_path		= storage + main_view.GROUPS_DIRECTORY + grouper + main_view.TXT;

		final String[][] content			= utilities.read_csv_to_array(group_file_path, 'n', 'u');
		final String[] group_feeds_names	= content[0];
		final String[] group_feeds_urls		= content[1];

		String feed_path;
		int i;

		final Point screen_size = new Point();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(screen_size);
		final int width = (int) (Math.round(((screen_size.x)*0.944)));

		final int size = group_feeds_names.length;
		for(i = 0; i < size; i++)
		{
			feed_path = storage + main_view.CONTENT_DIRECTORY + group_feeds_names[i]; /// mariam_feed.txt
			utilities.download_file(group_feeds_urls[i], feed_path + main_view.STORE_APPENDIX); /// Downloads file as mariam_feed.store.txt
			new parsered(feed_path + main_view.STORE_APPENDIX, storage, width); /// Parses the file and makes other files like mariam_feed.store.txt.content.txt
		}

		/// Sort group order
		if(!grouper.equals(main_view.ALL))
		{
			utilities.sort_group_content_by_time(storage, main_view.ALL);
			utilities.sort_group_content_by_time(storage, grouper);
		}
		else
		{
			for(String gro : all_groups)
				utilities.sort_group_content_by_time(storage, gro);
		}

		final List<Integer> unread_list = main_view.get_unread_counts();

		int group_items = 1;
		int total = 0, count;
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
