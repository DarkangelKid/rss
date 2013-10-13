package com.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
   int gotoLatestUnread(FeedItem[] p_items, boolean update, int page, Fragment fragment)
   {
      FeedItem[] items = p_items;
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
         AdapterTag cardAdapter = getCardAdapter(page1, fragment);
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
         if(!AdapterTag.s_readLinks.contains(items[i].url))
         {
            break;
         }
      }

      /* 0 is the top. links.length - 1 is the bottom.
       * May not be true anymore.*/
      if(update)
      {
         ListView lv = getFeedListView(page1, fragment);
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
   ListView getFeedListView(int page, Fragment fragment)
   {
      FragmentManager fragmentManager = fragment.getFragmentManager();

      ViewPager viewpager = FragmentFeeds.s_viewPager;

      int viewpagerId = viewpager.getId();
      String tag = String.format(Constants.FRAGMENT_TAG, viewpagerId, page);
      return ((ListFragment) fragmentManager.findFragmentByTag(tag)).getListView();
   }

   /* For these two functions, check for null. Should only really be
    * null if called from the ServiceUpdate. */
   static
   AdapterTag getCardAdapter(int page, Fragment fragment)
   {
      return (AdapterTag) getFeedListView(page, fragment).getAdapter();
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
      return !mounted.equals(Environment.getExternalStorageState());
   }

   static
   boolean isUsingSd()
   {
      /* Return true if force sd setting is true. */
      return true;
   }

   /* Safe to call at anytime. */
   static
   int getScreenWidth(Context context)
   {
      return context.getResources().getDisplayMetrics().widthPixels;
   }

   static
   int[] getUnreadCounts(Context context)
   {
      Collection<String> readLinks = new HashSet<String>(AdapterTag.s_readLinks.size());
      readLinks.addAll(AdapterTag.s_readLinks);

      String[] currentTags = Read.file(Constants.TAG_LIST, context);

      String[][] content = Read.csv(context);
      String[] indexNames = content[0];
      String[] indexUrls = content[1];
      String[] indexTags = content[2];

      int total = 0;
      int currentTagsCount = currentTags.length;
      int[] unreadCounts = new int[currentTagsCount];
      int[] totalCounts = new int[currentTagsCount];

      for(int i = 1; i < currentTagsCount; i++)
      {
         int read = 0;
         int indexTagsCount = indexTags.length;
         for(int j = 0; j < indexTagsCount; j++)
         {
            if(indexTags[j].equals(currentTags[i]))
            {
               totalCounts[i] += Read.count(getPath(indexNames[j], Constants.CONTENT), context);
               for(String readUrl : readLinks)
               {
                  int pos = readUrl.indexOf('/') + 1;
                  readUrl = readUrl.substring(pos, readUrl.indexOf('/', pos + 1));
                  if(indexUrls[j].contains(readUrl))
                  {
                     read++;
                  }
               }
            }
         }
         unreadCounts[i] = totalCounts[i] - read;
         total += unreadCounts[i];
      }

      unreadCounts[0] = total;
      return unreadCounts;
   }

   /* For feed files. */
   static
   String getPath(String feed, String append)
   {
      String feedFolder = feed + Constants.SEPAR;

      if(Constants.THUMBNAILS.equals(append))
      {
         return feedFolder + Constants.THUMBNAIL_DIR;
      }

      return feedFolder + append;
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

      boolean notificationsEnabled = 0 != check.length && strbol(check[0]);
      Intent intent = new Intent(context, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notificationsEnabled);
      return intent;
   }

   static
   boolean strbol(String str)
   {
      return Boolean.parseBoolean(str);
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

   static
   String getInternalPath(String externalPath)
   {
      String substring = externalPath.substring(
            externalPath.indexOf(Constants.SEPAR + "files" + Constants.SEPAR) + 7);
      return substring.replaceAll(Constants.SEPAR, "-");
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
   }   /* Replaces ALL_TAG '/'s with '-' to emulate a folder directory layout in
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
   void mkdir(String path, Context context)
   {
      String path1 = getStorage(context) + path;
      File folder = new File(path1);
      if(!folder.exists())
      {
         folder.mkdirs();
      }
   }

   static
   boolean move(String p_originalFile, String p_resultingFile, Context context)
   {
      String originalFile = getStorage(context) + p_originalFile;
      String resultingFile = getStorage(context) + p_resultingFile;
      return new File(originalFile).renameTo(new File(resultingFile));
   }

   /* Wrappers for neatness. */

   static
   int stoi(String str)
   {

      return null == str || 0 == str.length() ? 0 : Integer.parseInt(str);
   }

   static
   void setText(CharSequence charSequence, View view, int id)
   {
      ((TextView) view.findViewById(id)).setText(charSequence);
   }

   static
   String getText(View view, int id)
   {
      return ((TextView) view.findViewById(id)).getText().toString().trim();
   }

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
         strip.setTabIndicatorColor(Constants.COLOR_INTS[pos]);
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
