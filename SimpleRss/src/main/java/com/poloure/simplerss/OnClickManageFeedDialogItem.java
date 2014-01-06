package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

class OnClickManageFeedDialogItem implements DialogInterface.OnClickListener
{
   private final ListView m_listView;
   private final String m_feedName;
   private final String m_applicationFolder;
   private final PagerAdapterFeeds m_pagerAdapterFeeds;
   private final BaseAdapter m_navigationAdapter;

   OnClickManageFeedDialogItem(ListView listView, PagerAdapterFeeds pagerAdapterFeeds,
         BaseAdapter navigationAdapter, String feedName, String applicationFolder)
   {
      m_listView = listView;
      m_pagerAdapterFeeds = pagerAdapterFeeds;
      m_navigationAdapter = navigationAdapter;
      m_feedName = feedName;
      m_applicationFolder = applicationFolder;
   }

   private static
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
   void onClick(DialogInterface dialog, int which)
   {
      String feedFolderPath = m_applicationFolder + m_feedName + File.separatorChar;

      boolean isDelete = 0 == which;
      boolean isClearContent = 1 == which;

      /* This if statement must come first since the AsyncNavigationAdapter relies on the
      tagList in the PagerAdapterFeeds. */
      if(isDelete)
      {
         /* Delete the feed. */
         Write.editIndexLineContaining(m_feedName, m_applicationFolder, Write.MODE_REMOVE, "");

         /* Show deleted feed toast notification. */
         Context context = ((Dialog) dialog).getContext();
         String deletedText = context.getString(R.string.deleted_feed) + ' ' + m_feedName;
         Toast toast = Toast.makeText(context, deletedText, Toast.LENGTH_SHORT);
         toast.show();

         /* Update the PagerAdapter for the tag fragments. */
         m_pagerAdapterFeeds.updateTags(m_applicationFolder, context);
      }
      if(isDelete || isClearContent)
      {
         deleteDirectory(new File(feedFolderPath));

         /* Update the navigationDrawer without the ActionBar. */
         AsyncNavigationAdapter.newInstance(m_navigationAdapter, m_applicationFolder);

         /* Refresh pages and navigation counts. */
         AsyncManage.newInstance((BaseAdapter) m_listView.getAdapter(), m_applicationFolder);
      }
   }
}
