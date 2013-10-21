package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.animation.Animation;
import android.widget.BaseAdapter;

class Update
{
   public static
   void navigation(BaseAdapter navigationAdapter, ViewPager.OnPageChangeListener pageChange,
         int pos, Context context)
   {
      AsyncTask<int[], Void, int[]> task = new AsyncRefreshNavigationAdapter(navigationAdapter,
            context, pageChange, pos);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Util.EMPTY_INT_ARRAY);
      }
      else
      {
         task.execute(Util.EMPTY_INT_ARRAY);
      }
   }

   public static
   void asyncCompatRefreshPage(BaseAdapter navigationAdapter, int pageNumber,
         FragmentManager fragmentManager, Context context)
   {
      AsyncTask<Integer, Object, Animation> task = new AsyncRefreshPage(navigationAdapter,
            fragmentManager, context);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
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
      AsyncTask<String, Void, String[]> task = new AsyncCheckFeed(dlg, ntag, fname, mode, ctitle,
            navigationAdapter, context);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
      }
      else
      {
         task.execute(url);
      }
   }
}
