package yay.poloure.simplerss;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

public
class ServiceUpdate extends IntentService
{
   public
   ServiceUpdate()
   {
      super("Service");
   }

   @Override
   protected
   void onHandleIntent(Intent intent)
   {
      Util.setContext(this);

      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SIMPLERSS");
      wakeLock.acquire();

      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

      int page = intent.getIntExtra("GROUP_NUMBER", 0);

      String tag = Read.file(Constants.TAG_LIST)[page];

      String[][] content = Read.csv();
      String[] names = content[0];
      String[] urls = content[1];
      String[] tags = content[2];

      /* Download and parse each feed in the m_imageViewTag. */
      int namesLength = names.length;
      for(int i = 0; i < namesLength; i++)
      {
         if(tags[i].equals(tag) || tag.equals(Constants.ALL_TAG))
         {
            boolean success = Write.download(urls[i], names[i] + Constants.STORE);
            if(success)
            {
               FeedParser.parseFeed(names[i]);
            }
            else
            {
               Util.post("Download of " + urls[i] + " failed.");
            }
         }
      }

      int[] unreadCounts = Util.getUnreadCounts();

      /* If activity is running. */
      if(null != FeedsActivity.s_serviceHandler)
      {
         Message message = new Message();
         Bundle bundle = new Bundle();
         bundle.putInt("page_number", page);
         message.setData(bundle);
         FeedsActivity.s_serviceHandler.sendMessage(message);
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

         String titleSingle = Util.getString(R.string.notification_title_singular);
         String titlePlural = Util.getString(R.string.notification_title_plural);
         String contentSingleItem = Util.getString(R.string.notification_content_tag_item);
         String contentSingleTag = Util.getString(R.string.notification_content_tag_items);
         String contentPluralTag = Util.getString(R.string.notification_content_tags);

         String notificationTitle = 1 == unreadCounts[0]
               ? String.format(titleSingle, 1)
               : String.format(titlePlural, unreadCounts[0]);

         String notificationContent;
         if(1 == unreadCounts[0] && 1 == tagItems - 1)
         {
            notificationContent = contentSingleItem;
         }
         else
         {
            notificationContent = 1 < unreadCounts[0] && 1 == tagItems - 1
                  ? contentSingleTag
                  : String.format(contentPluralTag, tagItems - 1);
         }

         Builder notificationBuilder = new Builder(this);
         notificationBuilder.setSmallIcon(R.drawable.rss_icon)
               .setContentTitle(notificationTitle)
               .setContentText(notificationContent)
               .setAutoCancel(true);

         Intent notificationIntent = new Intent(this, FeedsActivity.class);

         TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

         stackBuilder.addParentStack(FeedsActivity.class);
         stackBuilder.addNextIntent(notificationIntent);
         PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
               PendingIntent.FLAG_UPDATE_CURRENT);
         notificationBuilder.setContentIntent(pendingIntent);
         NotificationManager notificationManager = (NotificationManager) getSystemService(
               Context.NOTIFICATION_SERVICE);
         notificationManager.notify(1, notificationBuilder.build());
      }

      wakeLock.release();
      stopSelf();
   }
}
