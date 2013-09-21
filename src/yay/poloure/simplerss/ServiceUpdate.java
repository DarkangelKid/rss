package yay.poloure.simplerss;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;

public class ServiceUpdate extends IntentService
{
   static Context s_serviceContext;

   public ServiceUpdate()
   {
      super("Service");
   }

   @Override
   protected void onHandleIntent(Intent intent)
   {
      s_serviceContext = this;

      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SIMPLERSS");
      wakelock.acquire();

      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

      int page = intent.getIntExtra("GROUP_NUMBER", 0);

      String[] allTags = Read.file(FeedsActivity.GROUP_LIST);
      String tag = allTags[page];

      String[][] content = Read.csv();
      String[] names = content[0];
      String[] urls = content[1];
      String[] tags = content[2];

      /* Download and parse each feed in the m_imageViewTag. */
      for(int i = 0; i < names.length; i++)
      {
         if(tags[i].equals(tag) || tag.equals(FeedsActivity.all))
         {
            boolean success = Write.download(urls[i], names[i] + FeedsActivity.STORE);
            if(success)
            {
               new FeedParser(names[i]);
            }
            else
            {
               Util.post("Download of " + urls[i] + " failed.");
            }
         }
      }

      int[] unreadCounts = Util.getUnreadCounts(allTags);

      /* If activity is running. */
      if(null != FeedsActivity.service_handler)
      {
         Message m = new Message();
         Bundle b = new Bundle();
         b.putInt("page_number", page);
         m.setData(b);
         FeedsActivity.service_handler.sendMessage(m);
      }
      else if(0 != unreadCounts[0] && intent.getBooleanExtra("NOTIFICATIONS", false))
      {
         /* Calculate the number of tags with new items. */
         int tagItems = 1;
         int sizes = unreadCounts.length;

         for(int i = 1; i < sizes; i++)
         {
            int count = unreadCounts[i];
            if(0 != count)
            {
               tagItems++;
            }
         }

         /* TODO replace with R.string values for UNREAD etc. */
         /*String not_title = (unreadCounts[0] == 1) ? String.format(UNREAD_ITEM, 1)
                                                    : String.format(UNREAD_ITEMS, unreadCounts[0]);

         String not_content;
         if((1 == unreadCounts[0]) && (1 == (tagItems - 1)))
         {
            not_content = String.format(GROUP_UNREAD, 1);
         }
         else
         {
            not_content = unreadCounts[0] > 1 && (tagItems - 1) == 1 ? String
                  .format(GROUP_UNREADS, 1) : String.format(GROUPS_UNREADS,

                                                            tagItems - 1);
         }*/

         /*NotificationCompat.Builder not_builder = new NotificationCompat.Builder(this)
               .setSmallIcon(R.drawable.rss_icon).setContentTitle(not_title)
               .setContentText(not_content).setAutoCancel(true);

         Intent result_intent = new Intent(this, FeedsActivity.class);

         TaskStackBuilder stack_builder = TaskStackBuilder.create(this);

         stack_builder.addParentStack(FeedsActivity.class);
         stack_builder.addNextIntent(result_intent);
         PendingIntent result_pending_intent = stack_builder
               .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
         not_builder.setContentIntent(result_pending_intent);
         NotificationManager notification_manager = (NotificationManager) getSystemService(
               Context.NOTIFICATION_SERVICE);
         notification_manager.notify(1, not_builder.build());*/
      }

      wakelock.release();
      stopSelf();
   }

   public static boolean isServiceRunning(Activity activity)
   {
      ActivityManager manager = (ActivityManager) activity.getSystemService(
            Context.ACTIVITY_SERVICE);
      for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
      {
         if(ServiceUpdate.class.getName().equals(service.service.getClassName()))
         {
            return true;
         }
      }
      return false;
   }
}
