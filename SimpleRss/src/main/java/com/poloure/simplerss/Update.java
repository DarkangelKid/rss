package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

class Update
{
   public static
   void navigation(BaseAdapter navigationAdapter, Context context)
   {
      AsyncRefreshNavigationAdapter task = new AsyncRefreshNavigationAdapter(navigationAdapter,
            context);
      if(Constants.HONEYCOMB)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Util.EMPTY_INT_ARRAY);
      }
      else
      {
         task.execute(Util.EMPTY_INT_ARRAY);
      }
   }

   public static
   void AsyncCompatManageTagsRefresh(ListView listView, ListAdapter listAdapter, Context context)
   {
      AsyncManageTagsRefresh task = new AsyncManageTagsRefresh(listView, listAdapter, context);
      if(Constants.HONEYCOMB)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
   }

   public static
   void page(BaseAdapter navigationAdapter, int pageNumber, FragmentManager fragmentManager,
         Context context)
   {
      RefreshPage task = new RefreshPage(navigationAdapter, fragmentManager, context);
      if(Constants.HONEYCOMB)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pageNumber);
      }
      else
      {
         task.execute(pageNumber);
      }
   }

   public static
   void executeFeedCheck(AlertDialog dlg, String ntag, String fname, String mode, String ctitle,
         String url, BaseAdapter navigationAdapter, Context context)
   {
      AsyncCheckFeed task = new AsyncCheckFeed(dlg, ntag, fname, mode, ctitle, navigationAdapter,
            context);
      if(Constants.HONEYCOMB)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
      }
      else
      {
         task.execute(url);
      }
   }
}
