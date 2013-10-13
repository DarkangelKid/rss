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
   private final Context m_context;

   OnClickManageFeedClearCache(Adapter adapter, BaseAdapter navigationAdapter, Context context)
   {
      m_adapter = adapter;
      m_navigationAdapter = navigationAdapter;
      m_context = context;
   }

   /// Delete the cache.
   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      String feedName = (String) m_adapter.getItem(position);
      String path = Util.getPath(feedName, "");

      Util.rmdir(new File(path));

      /* Refresh pages and navigation counts. */
      Update.navigation(m_navigationAdapter, m_context);
      // TODO Update.manageFeeds();
      // TODO Update.manageTags();
   }
}
