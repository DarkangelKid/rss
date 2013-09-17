package yay.poloure.simplerss;

import android.os.AsyncTask;

public class update
{
   public static void navigation(int[] counts)
   {
      if(!main.HONEYCOMB)
         new navigation_drawer.update_navigation_adapter().execute(counts);
      else
         new navigation_drawer.update_navigation_adapter()
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, counts);
   }

   public static void manage_feeds()
   {
      if(!main.HONEYCOMB)
         new fragment_manage_feeds.refresh().execute();
      else
         new fragment_manage_feeds.refresh()
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
   }

   public static void manage_tags()
   {
      if(!main.HONEYCOMB)
         new fragment_manage_tags.refresh().execute();
      else
         new fragment_manage_tags.refresh()
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
   }

   public static void page(int page_number)
   {
      if(main.HONEYCOMB)
         new refresh_page().execute(page_number);
      else
         new refresh_page()
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page_number);
   }

   public static void check_feed(android.app.AlertDialog dlg, String ntag, String fname, String mode, String ctitle, String ctag, String stag, int pos, String URL_check)
   {
      if(main.HONEYCOMB)
         new add_edit_dialog.check_feed_exists(dlg, ntag, fname, mode, ctitle, ctag, stag, pos).execute(URL_check);
      else
         new add_edit_dialog.check_feed_exists(dlg, ntag, fname, mode, ctitle, ctag, stag, pos).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
   }
}
