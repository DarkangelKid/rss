package yay.poloure.simplerss;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.WindowManager;

import java.util.List;

public class service_update extends IntentService
{
	private Handler handler;

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

		final int group		= intent.getIntExtra("GROUP_NUMBER", 0);
		handler					= main.service_handler;

		final String storage;
		final String SEPAR = main.SEPAR;

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
			storage				= getExternalFilesDir(null).getAbsolutePath() + SEPAR;
		else
		{
			String packageName	= getPackageName();
			storage				= Environment.getExternalStorageDirectory().getAbsolutePath() + SEPAR + "Android" + SEPAR + "data" + SEPAR + packageName + SEPAR + "files" + SEPAR;
		}

		final String[] all_groups			= utilities.read_file_to_array(storage + main.GROUP_LIST);
		final String grouper					= all_groups[group];
		final String group_file_path		= storage + main.GROUPS_DIRECTORY + grouper + main.SEPAR + grouper + main.TXT;

		final String[][] content			= utilities.read_csv_to_array(group_file_path, 'n', 'u', 'g');
		final String[] names					= content[0];
		final String[] urls					= content[1];
		final String[] groups				= content[2];

		int i, width;

		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
			width = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		else
		{
			final Point screen_size = new Point();
			((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(screen_size);
			width = (int) (Math.round(((screen_size.x)*0.944)));
		}

		final int size = names.length;
		/// For each feed you must do this.
		for(i = 0; i < size; i++)
		{
			utilities.download_file(urls[i], storage + names[i] + main.STORE_APPENDIX);
			new parser(storage, groups[i], names[i], width);
		}

		/// Sort group order
		if(!grouper.equals(main.ALL))
			utilities.sort_group_content_by_time(storage, grouper);

		else
		{
			for(int j = 1; j < all_groups.length; j++)
				utilities.sort_group_content_by_time(storage, all_groups[j]);
		}

		final int[] unread_counts = main.get_unread_counts();

		int group_items = 1;
		int total = 0, count;
		final int sizes = unread_counts.length;

		for(i = 1 ; i < sizes; i++)
		{
			count = unread_counts[i];
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

			Intent result_intent = new Intent(this, main.class);

			TaskStackBuilder stack_builder = TaskStackBuilder.create(this);

			stack_builder.addParentStack(main.class);
			stack_builder.addNextIntent(result_intent);
			PendingIntent result_pending_intent = stack_builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			not_builder.setContentIntent(result_pending_intent);
			NotificationManager notification_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notification_manager.notify(1, not_builder.build());
		}

		/* If activity is running. */
		if(handler != null)
		{
			/* 120 is just an int. */
			Message msg = handler.obtainMessage(120, "false");
			handler.sendMessage(msg);
		}

		wakelock.release();
		stopSelf();
	}

	public static boolean check_service_running(Activity activity)
	{
		ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
			if(service_update.class.getName().equals(service.service.getClassName()))
				return true;
		return false;
	}
}
