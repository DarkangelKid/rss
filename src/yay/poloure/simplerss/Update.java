package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

class Update
{
   public static
   void navigation(BaseAdapter navigationAdapter)
   {
      if(Constants.HONEYCOMB)
      {
         new AsyncRefreshNavigationAdapter(navigationAdapter).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR, Util.EMPTY_INT_ARRAY);
      }
      else
      {
         new AsyncRefreshNavigationAdapter(navigationAdapter).execute(Util.EMPTY_INT_ARRAY);
      }
   }

   public static
   void AsyncCompatManageTagsRefresh(ListView listView, ListAdapter listAdapter)
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
   void page(BaseAdapter navigationAdapter, int pageNumber)
   {
      if(Constants.HONEYCOMB)
      {
         new RefreshPage(navigationAdapter).execute(pageNumber);
      }
      else
      {
         new RefreshPage(navigationAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
               pageNumber);
      }
   }

   public static
   void executeFeedCheck(AlertDialog dlg, String ntag, String fname, String mode, String ctitle,
         String url)
   {
      if(Constants.HONEYCOMB)
      {
         new AsyncCheckFeed(dlg, ntag, fname, mode, ctitle).execute(url);
      }
      else
      {
         new AsyncCheckFeed(dlg, ntag, fname, mode, ctitle).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR, url);
      }
   }
}
