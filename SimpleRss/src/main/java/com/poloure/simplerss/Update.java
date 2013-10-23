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
   static
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

   static
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

   static
   void executeFeedCheck(AlertDialog dialog, int mode, String oldFeedTitle, Context context)
   {
      AsyncTask<Void, Void, String[]> task = new AsyncCheckFeed(dialog, mode, oldFeedTitle,
            context);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
   }
}
