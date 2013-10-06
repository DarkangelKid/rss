package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.widget.ListAdapter;
import android.widget.ListView;

class Update
{
   public static
   void navigation()
   {
      if(Constants.HONEYCOMB)
      {
         new NavDrawer.RefreshNavAdapter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
               Util.EMPTY_INT_ARRAY);
      }
      else
      {
         new NavDrawer.RefreshNavAdapter().execute(Util.EMPTY_INT_ARRAY);
      }
   }

   public static
   void manageFeeds(ListView listView, ListAdapter listAdapter)
   {
      if(Constants.HONEYCOMB)
      {
         new AsyncManageFeedsRefresh(listView, listAdapter).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         new AsyncManageFeedsRefresh(listView, listAdapter).execute();
      }
   }

   public static
   void manageTags(ListView listView, ListAdapter listAdapter)
   {
      if(Constants.HONEYCOMB)
      {
         new AsyncManageTagsRefresh(listView, listAdapter).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         new AsyncManageTagsRefresh(listView, listAdapter).execute();
      }
   }

   public static
   void page(int pageNumber)
   {
      if(Constants.HONEYCOMB)
      {
         new RefreshPage().execute(pageNumber);
      }
      else
      {
         new RefreshPage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pageNumber);
      }
   }

   public static
   void executeFeedCheck(AlertDialog dlg, String ntag, String fname, String mode, String ctitle,
         String url)
   {
      if(Constants.HONEYCOMB)
      {
         new FeedDialog.CheckFeed(dlg, ntag, fname, mode, ctitle).execute(url);
      }
      else
      {
         new FeedDialog.CheckFeed(dlg, ntag, fname, mode, ctitle).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR, url);
      }
   }
}
