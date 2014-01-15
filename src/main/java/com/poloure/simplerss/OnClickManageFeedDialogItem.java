/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

class OnClickManageFeedDialogItem implements DialogInterface.OnClickListener
{
   private final Activity m_activity;
   private final ListView m_listView;
   private final String m_feedName;
   private final String m_applicationFolder;
   private final PagerAdapterFeeds m_pagerAdapterFeeds;

   OnClickManageFeedDialogItem(Activity activity, ListView listView,
         PagerAdapterFeeds pagerAdapterFeeds, String feedName, String applicationFolder)
   {
      m_activity = activity;
      m_listView = listView;
      m_pagerAdapterFeeds = pagerAdapterFeeds;
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
         AsyncNavigationAdapter.newInstance(m_activity, m_applicationFolder, -1);

         /* Refresh pages and navigation counts. */
         AsyncManage.newInstance((ArrayAdapter<Editable>) m_listView.getAdapter(),
               m_applicationFolder);
      }
   }
}
