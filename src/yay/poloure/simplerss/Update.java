package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.os.AsyncTask;

class Update
{
   public static
   void navigation()
   {
      if(FeedsActivity.HONEYCOMB)
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
      if(FeedsActivity.HONEYCOMB)
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
      if(FeedsActivity.HONEYCOMB)
      {
         new FragmentManageTags.RefreshTags().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         new FragmentManageTags.RefreshTags().execute();
      }
   }

   public static
   void page(int page_number)
   {
      if(FeedsActivity.HONEYCOMB)
      {
         new RefreshPage().execute(page_number);
      }
      else
      {
         new RefreshPage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page_number);
      }
   }

   public static
   void checkFeedExists(AlertDialog dlg, String ntag, String fname, String mode, String ctitle,
         String stag, int pos, String URL_check)
   {
      if(FeedsActivity.HONEYCOMB)
      {
         new FeedDialog.CheckFeed(dlg, ntag, fname, mode, ctitle, stag, pos).execute(URL_check);
      }
      else
      {
         new FeedDialog.CheckFeed(dlg, ntag, fname, mode, ctitle, stag, pos).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
      }
   }
}
