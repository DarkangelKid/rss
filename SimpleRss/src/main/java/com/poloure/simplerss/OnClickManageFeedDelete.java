package com.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Adapter;

import java.io.File;

class OnClickManageFeedDelete implements DialogInterface.OnClickListener
{
   private final Adapter m_adapter;
   private final Context m_context;
   private final int     m_position;

   OnClickManageFeedDelete(Adapter adapter, int position, Context context)
   {
      m_adapter = adapter;
      m_context = context;
      m_position = position;
   }

   /* Replaces ALL_TAG '/'s with '-' to emulate a folder directory layout in
       * data/data. */
   static
   boolean deleteDirectory(File directory)
   {
      if(directory.isDirectory())
      {
         for(String child : directory.list())
         {
            boolean success = !deleteDirectory(new File(directory, child));
            if(success)
            {
               return false;
            }
         }
      }
      return directory.delete();
   }

   /* Delete the cache.*/
   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      String feedName = (String) m_adapter.getItem(m_position);
      String path = feedName + File.separatorChar;

      Write.removeLine(Read.INDEX, feedName, true, m_context);
      deleteDirectory(new File(path));

      /* Refresh pages and navigation counts. */
      AsyncCheckFeed.updateTags((Activity) m_context);
      // TODO Update.manageFeeds();
      // TODO Update.manageTags();
   }
}
