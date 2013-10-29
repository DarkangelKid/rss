package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.io.File;

class OnClickManageFeedDialogItem implements DialogInterface.OnClickListener
{
   private final String               m_feedName;
   private final String               m_applicationFolder;
   private final String               m_allTag;
   private final FragmentPagerAdapter m_pagerAdapterFeeds;
   private final BaseAdapter          m_navigationAdapter;

   OnClickManageFeedDialogItem(FragmentPagerAdapter pagerAdapterFeeds,
         BaseAdapter navigationAdapter, String feedName, String applicationFolder, String allTag)
   {
      m_pagerAdapterFeeds = pagerAdapterFeeds;
      m_navigationAdapter = navigationAdapter;
      m_allTag = allTag;
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

      /* This if statement must come first since the AsyncRefreshNavigationAdapter relies on the
      tagList in the PagerAdapterFeeds. */
      if(isDelete)
      {
         /* Delete the feed. */
         Write.removeLine(Read.INDEX, m_feedName, true, m_applicationFolder);

         /* Show deleted feed toast notification. */
         Context context = ((Dialog) dialog).getContext();
         String deletedText = context.getString(R.string.deleted_feed) + ' ' + m_feedName;
         Toast.makeText(context, deletedText, Toast.LENGTH_SHORT).show();

         /* Update the PagerAdapter for the tag fragments. */
         ((PagerAdapterFeeds) m_pagerAdapterFeeds).getTagsFromDisk(m_applicationFolder, m_allTag);
         m_pagerAdapterFeeds.notifyDataSetChanged();
      }
      if(isDelete || isClearContent)
      {
         deleteDirectory(new File(feedFolderPath));

         /* Update the navigationDrawer without the ActionBar. */
         AsyncRefreshNavigationAdapter.newInstance(m_navigationAdapter, m_applicationFolder);
      }

      /* Refresh pages and navigation counts. */
      // TODO Update.manageFeeds();
      // TODO Update.manageTags();
   }
}
