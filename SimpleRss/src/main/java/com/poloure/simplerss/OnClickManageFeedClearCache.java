package com.poloure.simplerss;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import java.io.File;

class OnClickManageFeedClearCache implements DialogInterface.OnClickListener
{
   private final Adapter     m_adapter;
   private final BaseAdapter m_navigationAdapter;
   private final Context     m_context;
   private final int m_position;

   OnClickManageFeedClearCache(Adapter adapter, BaseAdapter navigationAdapter, int position, Context context)
   {
      m_adapter = adapter;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
      m_position = position;
   }

   /// Delete the cache.
   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      String feedName = (String) m_adapter.getItem(m_position);
      String path = feedName + File.separator;

      Util.deleteDirectory(new File(path));

      /* Refresh pages and navigation counts. */
      Update.navigation(m_navigationAdapter, null, 0, m_context);
      // TODO Update.manageFeeds();
      // TODO Update.manageTags();
   }
}
