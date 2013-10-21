package com.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import java.io.File;

class OnClickManageFeedDelete implements DialogInterface.OnClickListener
{
   private final Adapter     m_adapter;
   private final BaseAdapter m_navigationAdapter;
   private final Context     m_context;
   private final int         m_position;

   OnClickManageFeedDelete(Adapter adapter, BaseAdapter navigationAdapter, int position,
         Context context)
   {
      m_adapter = adapter;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
      m_position = position;
   }

   /* Delete the cache.*/
   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      String feedName = (String) m_adapter.getItem(m_position);
      String path = feedName + File.separator;

      Write.removeLine(Constants.INDEX, feedName, true, m_context);
      Util.deleteDirectory(new File(path));

      /* Refresh pages and navigation counts. */
      Util.updateTags(m_navigationAdapter, (Activity) m_context);
      // TODO Update.manageFeeds();
      // TODO Update.manageTags();
   }
}
