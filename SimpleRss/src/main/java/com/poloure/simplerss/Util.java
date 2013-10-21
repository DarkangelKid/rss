package com.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;

class Util
{
   static final   int[]      EMPTY_INT_ARRAY           = new int[0];
   static final   String[]   EMPTY_STRING_ARRAY        = new String[0];
   static final   String[][] EMPTY_STRING_STRING_ARRAY = new String[0][0];
   private static String     s_storage                 = "";

   static
   void updateTags(BaseAdapter navigationAdapter, Activity activity)
   {
      ViewPager tagPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);

      PagerAdapter pagerAdapter = tagPager.getAdapter();
      ((PagerAdapterFeeds) pagerAdapter).getTagsFromDisk(activity);
      pagerAdapter.notifyDataSetChanged();

      Update.navigation(navigationAdapter, null, 0, activity);
   }

   static
   void gotoLatestUnread(FragmentManager fragmentManager, int page)
   {
      String tag = "android:switcher:" + FragmentFeeds.VIEW_PAGER_ID + ':' + page;

      ListFragment tagFragment = (ListFragment) fragmentManager.findFragmentByTag(tag);
      ListView listView = tagFragment.getListView();

      AdapterTags cardAdapter = (AdapterTags) listView.getAdapter();

      int itemCount = cardAdapter.getCount() - 1;
      for(int i = itemCount; 0 <= i; i--)
      {
         if(!AdapterTags.S_READ_ITEM_TIMES.contains(cardAdapter.m_items[i].m_itemTime))
         {
            listView.setSelection(i);
            break;
         }
      }
   }

   static
   boolean isUnmounted()
   {
      if(!isUsingSd())
      {
         return false;
      }

      /* Check to see if we can Write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      String externalStorageState = Environment.getExternalStorageState();
      return !mounted.equals(externalStorageState);
   }

   static
   boolean isUsingSd()
   {
      /* Return true if force sd setting is true. */
      return true;
   }

   /* Changes the ManageFeedsRefresh menu item to an animation if m_mode = true. */
   static
   void setRefreshingIcon(boolean mode, Menu menu)
   {
      /* Find the menu item by ID called ManageFeedsRefresh. */
      MenuItem item = menu.findItem(R.id.refresh);
      if(null == item)
      {
         return;
      }

      /* Change it depending on the m_mode. */
      if(mode)
      {
         MenuItemCompat.setActionView(item, R.layout.progress_circle);
      }
      else
      {
         MenuItemCompat.setActionView(item, null);
      }
   }

   static
   void remove(String path, Context context)
   {
      String path1 = getStorage(context) + path;
      String internalPath = getInternalPath(path1);
      context.deleteFile(internalPath);
      File file = new File(path1);
      file.delete();
   }

   static
   String getInternalPath(String externalPath)
   {
      int index = externalPath.indexOf(File.separator + "files" + File.separator);
      String substring = externalPath.substring(index + 7);
      return substring.replaceAll(File.separator, "-");
   }

   /* This function will return null if it fails. Check for null each time.
    * It should be pretty safe and efficient to call ALL_TAG the time. */
   static
   String getStorage(Context context)
   {
      if(0 != s_storage.length())
      {
         return s_storage;
      }
      /* Check the media state for any undesirable states. */
      /* TODO Check to see if it is the desired first then skip these. */
      String state = Environment.getExternalStorageState();

      String mounted = Environment.MEDIA_MOUNTED;
      if(!mounted.equals(state))
      {
         return null;
      }

      /* If it has reached here the state is MEDIA_MOUNTED and we can continue.
         Build the s_storage string depending on android version. */
      String sep = File.separator;

      if(Build.VERSION_CODES.FROYO <= Build.VERSION.SDK_INT)
      {
         File externalFilesDir = context.getExternalFilesDir(null);
         s_storage = externalFilesDir.getAbsolutePath() + sep;
      }
      else
      {
         String name = context.getPackageName();
         File ext = Environment.getExternalStorageDirectory();
         s_storage = ext.getAbsolutePath() + sep + "Android" + sep + "data" + sep + name + sep +
               "files" + sep;

         /* If the folder does not exist, create it. */
         File storageFile = new File(s_storage);
         storageFile.mkdirs();
      }
      return s_storage;
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

   static
   boolean moveFile(String original, String resulting, Context context)
   {
      String storage = getStorage(context);

      File originalFile = new File(storage + original);
      File resultingFile = new File(storage + resulting);

      return originalFile.renameTo(resultingFile);
   }

   /* index throws an ArrayOutOfBoundsException if not handled. */
   static
   <T> int index(T[] array, T value)
   {
      if(null == array)
      {
         return -1;
      }

      int arrayLength = array.length;
      for(int i = 0; i < arrayLength; i++)
      {
         if(array[i].equals(value))
         {
            return i;
         }
      }
      return -1;
   }
}
