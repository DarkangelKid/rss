package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;

class OnDialogClickPositive implements DialogInterface.OnClickListener
{
   private final Context m_context;
   private final String m_oldFeedTitle;

   OnDialogClickPositive(Context context, String oldFeedTitle)
   {
      m_context = context;
      m_oldFeedTitle = oldFeedTitle;
   }

   private static
   void executeFeedCheck(AlertDialog dialog, String oldFeedTitle, Context context)
   {
      AsyncTask<Void, Void, String[]> task = new AsyncCheckFeed(dialog, oldFeedTitle, context);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      executeFeedCheck((AlertDialog) dialog, m_oldFeedTitle, m_context);
   }
}
