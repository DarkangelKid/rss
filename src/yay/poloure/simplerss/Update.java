package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.os.AsyncTask;

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
   void manageFeeds()
   {
      if(Constants.HONEYCOMB)
      {
         new FragmentManageFeeds.ManageRefresh().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         new FragmentManageFeeds.ManageRefresh().execute();
      }
   }

   public static
   void manageTags()
   {
      if(Constants.HONEYCOMB)
      {
         new FragmentManageTags.RefreshTags().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         new FragmentManageTags.RefreshTags().execute();
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
         String stag, int pos, String url)
   {
      if(Constants.HONEYCOMB)
      {
         new FeedDialog.CheckFeed(dlg, ntag, fname, mode, ctitle, stag, pos).execute(url);
      }
      else
      {
         new FeedDialog.CheckFeed(dlg, ntag, fname, mode, ctitle, stag, pos).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR, url);
      }
   }
}
