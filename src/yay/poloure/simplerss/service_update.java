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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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

		final String UNREAD_ITEM		= getString(R.string.notification_title_singular);
		final String UNREAD_ITEMS		= getString(R.string.notification_title_plural);
		final String GROUP_UNREAD		= getString(R.string.notification_content_group_item);
		final String GROUP_UNREADS		= getString(R.string.notification_content_group_items);
		final String GROUPS_UNREADS	= getString(R.string.notification_content_groups);

		final int group			= intent.getIntExtra("GROUP_NUMBER", 0);

		final String storage;
		final String SEPAR = main.SEPAR;

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
			storage					= getExternalFilesDir(null).getAbsolutePath() + SEPAR;
		else
		{
			String packageName	= getPackageName();
			storage					= Environment.getExternalStorageDirectory().getAbsolutePath() + SEPAR + "Android" + SEPAR + "data" + SEPAR + packageName + SEPAR + "files" + SEPAR;
		}

		final String[] all_groups			= utilities.read_file_to_array(storage + main.GROUP_LIST);
		final String grouper					= all_groups[group];
		final String group_file_path		= storage + main.GROUPS_DIRECTORY + grouper + SEPAR + grouper + main.TXT;

		final String[][] content			= utilities.read_csv_to_array(group_file_path, 'n', 'u', 'g');
		final String[] names					= content[0];
		final String[] urls					= content[1];
		final String[] groups				= content[2];

		int width;

		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
			width = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		else
		{
			final Point screen_size = new Point();
			((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(screen_size);
			width = (int) (Math.round(((screen_size.x)*0.944)));
		}

		/* Download and parse each feed in the group. */
		for(int i = 0; i < names.length; i++)
		{
			utilities.download_file(urls[i], storage + names[i] + main.STORE_APPENDIX);
			new parser(storage, groups[i], names[i], width);
		}

		/* Always sort all & sort others too. */
		utilities.sort_group_content_by_time(storage, all_groups[0], all_groups[0]);
		if(!grouper.equals(main.ALL))
			utilities.sort_group_content_by_time(storage, grouper, all_groups[0]);
		else
		{
			for(int i = 1; i < all_groups.length; i++)
				utilities.sort_group_content_by_time(storage, all_groups[i], all_groups[0]);
		}

		final int[] unread_counts = utilities.get_unread_counts(storage, all_groups);

		/* If activity is running. */
		if(main.service_handler != null)
		{
			Message m = new Message();
			Bundle b = new Bundle();
			b.putInt("page_number", group);
			m.setData(b);
			main.service_handler.sendMessage(m);
		}
		else if(unread_counts[0] != 0 && intent.getBooleanExtra("NOTIFICATIONS", false))
		{
			/* Calculate the number of groups with new items. */
			int group_items = 1, count;
			final int sizes = unread_counts.length;

			for(int i = 1 ; i < sizes; i++)
			{
				count = unread_counts[i];
				if(count != 0)
					group_items++;
			}

			String not_title;
			if(unread_counts[0] == 1)
				not_title = String.format("%d %s", 1, UNREAD_ITEM);
			else
				not_title = String.format("%d %s", unread_counts[0], UNREAD_ITEMS);

			String not_content;
			if(unread_counts[0] == 1 && (group_items - 1) == 1)
				not_content = String.format("%d %s", 1, GROUP_UNREAD);
			else if(unread_counts[0] > 1 && (group_items - 1) == 1)
				not_content = String.format("%d %s", 1, GROUP_UNREADS);
			else
				not_content = String.format("%d %s", (group_items - 1), GROUPS_UNREADS);

			NotificationCompat.Builder not_builder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.rss_icon)
					.setContentTitle(not_title)
					.setContentText(not_content)
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
