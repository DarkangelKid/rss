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
         new fragment_manage_feed.refresh().execute();
      else
         new fragment_manage_feed.refresh()
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
   }

   public static void manage_groups()
   {
      if(!main.HONEYCOMB)
         new fragment_manage_group.refresh().execute();
      else
         new fragment_manage_group.refresh()
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

   public static void check_feed(android.app.AlertDialog dlg, String ngroup, String fname, String mode, String ctitle, String cgroup, String sgroup, int pos, String URL_check)
   {
      if(main.HONEYCOMB)
         new add_edit_dialog.check_feed_exists(dlg, ngroup, fname, mode, ctitle, cgroup, sgroup, pos).execute(URL_check);
      else
         new add_edit_dialog.check_feed_exists(dlg, ngroup, fname, mode, ctitle, cgroup, sgroup, pos).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL_check);
   }
}
