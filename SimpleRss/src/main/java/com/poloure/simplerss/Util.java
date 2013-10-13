package com.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

class Util
{
   static final   int[]      EMPTY_INT_ARRAY           = new int[0];
   static final   String[]   EMPTY_STRING_ARRAY        = new String[0];
   static final   String[][] EMPTY_STRING_STRING_ARRAY = new String[0][0];
   private static String     s_storage                 = "";

   static
   String[] arrayRemove(String[] first, int index)
   {
      if(null == first || 0 == first.length)
      {
         return EMPTY_STRING_ARRAY;
      }

      String[] resultArray = new String[first.length - 1];
      System.arraycopy(first, 0, resultArray, 0, index);
      if(first.length != index)
      {
         System.arraycopy(first, index + 1, resultArray, index, first.length - index - 1);
      }
      return resultArray;
   }

   static
   void updateTags(BaseAdapter navigationAdapter, Context context)
   {
      /* If no tags exists, add the ALL_TAG. */
      if(0 == Read.count(Constants.TAG_LIST, context))
      {
         Write.single(Constants.TAG_LIST, context.getString(R.string.all_tag) + Constants.NL,
               context);
      }

      if(null != FragmentFeeds.s_viewPager)
      {
         PagerAdapter pagerAdapter = FragmentFeeds.s_viewPager.getAdapter();
         pagerAdapter.notifyDataSetChanged();
      }

      Update.navigation(navigationAdapter, context);
   }

   static
   int gotoLatestUnread(FeedItem[] feedItems, boolean update, int page,
         FragmentManager fragmentManager)
   {
      FeedItem[] items = feedItems;
      int page1 = page;

      if(null == FragmentFeeds.s_viewPager)
      {
         return -1;
      }

      if(update)
      {
         page1 = FragmentFeeds.s_viewPager.getCurrentItem();
      }

      if(null == items)
      {
         AdapterTags cardAdapter = (AdapterTags) getFeedListView(page1,
               fragmentManager).getAdapter();
         if(null == cardAdapter)
         {
            return -1;
         }

         items = cardAdapter.m_items;
      }

      int i;
      int itemCount = items.length - 1;
      for(i = itemCount; 0 <= i; i--)
      {
         if(!AdapterTags.s_readItemTimes.contains(items[i].time))
         {
            break;
         }
      }

      /* 0 is the top. links.length - 1 is the bottom.
       * May not be true anymore.*/
      if(update)
      {
         ListView lv = getFeedListView(page1, fragmentManager);
         if(null == lv)
         {
            return -1;
         }

         lv.setSelection(i);
      }
      return i;
   }

   /* This is the second one. */
   static
   ListView getFeedListView(int page, FragmentManager fragmentManager)
   {
      ViewPager viewpager = FragmentFeeds.s_viewPager;

      int viewpagerId = viewpager.getId();
      String tag = String.format(Constants.FRAGMENT_TAG, viewpagerId, page);
      return ((ListFragment) fragmentManager.findFragmentByTag(tag)).getListView();
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

   static
   int[] getUnreadCounts(Context context)
   {
      int readCount = AdapterTags.s_readItemTimes.size();
      Collection<String> readItemTimes = new HashSet<String>(readCount);
      readItemTimes.addAll(AdapterTags.s_readItemTimes);

      String[] currentTags = Read.file(Constants.TAG_LIST, context);

      String[][] content = Read.csv(Constants.INDEX, context, 'f', 't');
      String[] indexNames = content[0];
      String[] indexTags = content[1];

      int total = 0;
      int tagCount = currentTags.length;

      int[] unreadCounts = new int[tagCount];

      /* For each tag. */
      for(int i = 1; i < tagCount; i++)
      {
         int read = 0;
         int indexTagsCount = indexTags.length;
         int totalTagItems = 0;

         /* For each index entry. */
         for(int j = 0; j < indexTagsCount; j++)
         {
            if(indexTags[j].equals(currentTags[i]))
            {
               String contentPath = indexNames[j] + Constants.SEPAR + Constants.CONTENT;
               String[] times = Read.csv(contentPath, context, 'p')[0];

               totalTagItems += times.length;

               for(String time : times)
               {
                  if(readItemTimes.contains(time))
                  {
                     read++;
                  }
               }
            }
         }
         unreadCounts[i] = totalTagItems - read;
         total += unreadCounts[i];
      }

      unreadCounts[0] = total;
      return unreadCounts;
   }

   static
   Intent getServiceIntent(int page, String notificationName, Context context)
   {
      if(null == notificationName)
      {
         Resources resources = context.getResources();
         notificationName = resources.getStringArray(R.array.settings_names)[3];
      }
      /* Load notification boolean. */
      String path = Constants.SETTINGS_DIR + notificationName +
            Constants.TXT;
      String[] check = Read.file(path, context);

      boolean notificationsEnabled = 0 != check.length && Boolean.parseBoolean(check[0]);
      Intent intent = new Intent(context, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notificationsEnabled);
      return intent;
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
      context.deleteFile(getInternalPath(path1));
      new File(path1).delete();
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
      String sep = Constants.SEPAR;

      if(Constants.FROYO)
      {
         s_storage = context.getExternalFilesDir(null).getAbsolutePath() + sep;
      }
      else
      {
         String name = context.getPackageName();
         File ext = Environment.getExternalStorageDirectory();
         s_storage = ext.getAbsolutePath() + sep + "Android" + sep + "data" + sep + name + sep +
               "files" + sep;

         /* If the folder does not exist, create it. */
         File storageFile = new File(s_storage);
         if(!storageFile.exists())
         {
            storageFile.mkdirs();
         }
      }
      return s_storage;
   }

   static
   String getInternalPath(String externalPath)
   {
      String substring = externalPath.substring(
            externalPath.indexOf(Constants.SEPAR + "files" + Constants.SEPAR) + 7);
      return substring.replaceAll(Constants.SEPAR, "-");
   }

   /* Replaces ALL_TAG '/'s with '-' to emulate a folder directory layout in
    * data/data. */
   static
   boolean rmdir(File directory)
   {
      if(directory.isDirectory())
      {
         for(String child : directory.list())
         {
            boolean success = !rmdir(new File(directory, child));
            if(success)
            {
               return false;
            }
         }
      }
      return directory.delete();
   }

   static
   boolean move(String original, String resulting, Context context)
   {
      String originalFile = getStorage(context) + original;
      String resultingFile = getStorage(context) + resulting;
      return new File(originalFile).renameTo(new File(resultingFile));
   }

   /* Wrappers for neatness. */
   static
   PagerTabStrip newPagerTabStrip(Context context)
   {
      PagerTabStrip pagerTabStrip = new PagerTabStrip(context);
      int fourDp = Math.round(4.0F * context.getResources().getDisplayMetrics().density);

      pagerTabStrip.setDrawFullUnderline(true);
      pagerTabStrip.setGravity(Gravity.START);
      pagerTabStrip.setPadding(0, fourDp, 0, fourDp);
      pagerTabStrip.setTextColor(Color.WHITE);
      pagerTabStrip.setBackgroundColor(Color.parseColor("#404040"));
      setStripColor(pagerTabStrip, context);

      return pagerTabStrip;
   }

   static
   void setStripColor(PagerTabStrip strip, Context context)
   {
      String colorSettingsPath = Constants.SETTINGS_DIR + Constants.STRIP_COLOR;

      /* Read the colour from the settings/colour file, if blank, use blue. */
      String[] check = Read.file(colorSettingsPath, context);
      String color = 0 == check.length ? "blue" : check[0];

      /* Find the colour stored in adapter_settings_interface that we want. */
      Resources resources = context.getResources();
      String[] colors = resources.getStringArray(R.array.settings_colours);

      int pos = index(colors, color);
      if(-1 != pos)
      {
         int[] colorInts = {
               resources.getColor(R.color.blue_light),
               resources.getColor(R.color.purple_light),
               resources.getColor(R.color.green_light),
               resources.getColor(R.color.yellow_light),
               resources.getColor(R.color.red_light),
         };
         strip.setTabIndicatorColor(colorInts[pos]);
      }
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
