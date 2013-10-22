package com.poloure.simplerss;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

class OnFinishServiceHandler extends Handler
{
   private final ActionBarActivity m_activity;
   private final Menu              m_optionsMenu;

   OnFinishServiceHandler(ActionBarActivity activity, Menu optionsMenu)
   {
      m_activity = activity;
      m_optionsMenu = optionsMenu;

   }

   /* The stuff we would like to run when the service completes. */
   @Override
   public
   void handleMessage(Message msg)
   {
      FeedsActivity.setRefreshingIcon(false, m_optionsMenu);

      Bundle bundle = msg.getData();
      if(null != bundle)
      {
         int page = bundle.getInt("page_number");
         FragmentManager fragmentManager = m_activity.getSupportFragmentManager();

         Update.asyncCompatRefreshPage(0, fragmentManager, m_activity);

         int tagsCount = PagerAdapterFeeds.getSize();
         if(0 == page)
         {
            for(int i = 1; i < tagsCount; i++)
            {
               Update.asyncCompatRefreshPage(i, fragmentManager, m_activity);
            }
         }
         else
         {
            Update.asyncCompatRefreshPage(page, fragmentManager, m_activity);
         }
      }
   }
}
