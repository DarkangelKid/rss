package com.poloure.simplerss;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.view.animation.Animation;

class Update
{
   public static
   void navigation(Activity activity)
   {
      AsyncTask<int[], Void, int[]> task = new AsyncRefreshNavigationAdapter(activity);
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
   void asyncCompatRefreshPage(int pageNumber, FragmentManager fragmentManager, Context context)
   {
      AsyncTask<Integer, Object, Animation> task = new AsyncRefreshPage(fragmentManager, context);
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
         String url, Context context)
   {
      AsyncTask<String, Void, String[]> task = new AsyncCheckFeed(dlg, ntag, fname, mode, ctitle,
            context);
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
