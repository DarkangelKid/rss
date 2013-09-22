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
   static Context s_serviceContext;

   public
   ServiceUpdate()
   {
      super("Service");
   }

   @Override
   protected
   void onHandleIntent(Intent intent)
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
               FeedParser.parseFeed(names[i]);
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

         String titleSingle = Util.getString(R.string.notification_title_singular);
         String titlePlural = Util.getString(R.string.notification_title_plural);
         String contentSingleItem = Util.getString(R.string.notification_content_tag_item);
         String contentSingleTag = Util.getString(R.string.notification_content_tag_items);
         String contentPluralTag = Util.getString(R.string.notification_content_tags);

         String not_title = 1 == unreadCounts[0] ? String.format(titleSingle, 1)
               : String.format(titlePlural, unreadCounts[0]);

         String not_content;
         if(1 == unreadCounts[0] && 1 == tagItems - 1)
         {
            not_content = contentSingleItem;
         }
         else
         {
            not_content = 1 < unreadCounts[0] && 1 == (tagItems - 1) ? contentSingleTag
                  : String.format(contentPluralTag, tagItems - 1);
         }

         Builder not_builder = new Builder(this);
         not_builder.setSmallIcon(R.drawable.rss_icon)
               .setContentTitle(not_title)
               .setContentText(not_content)
               .setAutoCancel(true);

         Intent result_intent = new Intent(this, FeedsActivity.class);

         TaskStackBuilder stack_builder = TaskStackBuilder.create(this);

         stack_builder.addParentStack(FeedsActivity.class);
         stack_builder.addNextIntent(result_intent);
         PendingIntent result_pending_intent = stack_builder.getPendingIntent(0,
               PendingIntent.FLAG_UPDATE_CURRENT);
         not_builder.setContentIntent(result_pending_intent);
         NotificationManager notification_manager = (NotificationManager) getSystemService(
               Context.NOTIFICATION_SERVICE);
         notification_manager.notify(1, not_builder.build());
      }

      wakelock.release();
      stopSelf();
   }
}
