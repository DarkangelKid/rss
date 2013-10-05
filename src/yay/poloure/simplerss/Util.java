package yay.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Set;

final
class Util
{
   private static String s_storage = "";
   private static Context s_context;
   static final int[]      EMPTY_INT_ARRAY           = new int[0];
   static final String[]   EMPTY_STRING_ARRAY        = new String[0];
   static final String[][] EMPTY_STRING_STRING_ARRAY = new String[0][0];

   private
   Util()
   {
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
   void updateTags()
   {
      /* Since this function is static, we can not rely on the fields being
       * non-null. */

      /* Update s_currentTags. */
      FeedsActivity.s_currentTags = Read.file(Constants.TAG_LIST);

      /* If no tags exists, add the ALL_TAG m_imageViewTag. */
      if(0 == FeedsActivity.s_currentTags.length)
      {
         Write.single(Constants.TAG_LIST, Constants.ALL_TAG + Constants.NL);
         FeedsActivity.s_currentTags = new String[]{Constants.ALL_TAG};
      }

      if(null != FeedsActivity.s_ViewPager)
      {
         FeedsActivity.s_ViewPager.getAdapter().notifyDataSetChanged();
      }
      Update.navigation();
   }

   static
   int gotoLatestUnread(FeedItem[] items, boolean update, int page)
   {

      if(null == FeedsActivity.s_ViewPager)
      {
         return -1;
      }

      if(update)
      {
         page = FeedsActivity.s_ViewPager.getCurrentItem();
      }

      if(null == items)
      {
         AdapterCard adp = getCardAdapter(page);
         if(null == adp)
         {
            return -1;
         }

         items = adp.m_items;
      }

      int i;
      for(i = items.length - 1; 0 <= i; i--)
      {
         int pos = items.length - i - 1;
         if(!SetHolder.READ_ITEMS.contains(items[pos].url))
         {
            break;
         }
      }

      /* 0 is the top. links.length - 1 is the bottom.
       * May not be true anymore.*/
      if(update)
      {
         ListView lv = getFeedListView(page);
         if(null == lv)
         {
            return -1;
         }

         lv.setSelection(i);
      }
      return i;
   }

   static
   boolean isUnmounted()
   {
      /* TODO If setting to force sd is true, return false. */
      /*if(!internal.equals(s_storage))
         return false;*/

      /* Check to see if we can Write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      if(!mounted.equals(Environment.getExternalStorageState()))
      {
         post(Write.MEDIA_UNMOUNTED);
         return true;
      }

      return false;
   }

   /* Replaces ALL_TAG '/'s with '-' to emulate a folder directory layout in
    * data/data. */
   static
   String getInternalPath(String externalPath)
   {
      String substring = externalPath.substring(
            externalPath.indexOf(Constants.SEPAR + "files" + Constants.SEPAR) + 7);
      return substring.replaceAll(Constants.SEPAR, "-");
   }

   /* For these two functions, check for null. Should only really be
    * null if called from the ServiceUpdate. */
   static
   AdapterCard getCardAdapter(int page)
   {
      ListView list = getFeedListView(page);
      if(null == list)
      {
         return null;
      }

      return (AdapterCard) list.getAdapter();
   }

   /* This is the second one. */
   private static
   ListView getFeedListView(int page)
   {
      FragmentManager fman = FeedsActivity.s_fragmentManager;
      ViewPager viewpager = FeedsActivity.s_ViewPager;
      if(null == fman || null == viewpager)
      {
         return null;
      }

      String tag = String.format(Constants.FRAGMENT_TAG, viewpager.getId(), page);
      return ((FragmentCard) fman.findFragmentByTag(tag)).getListView();
   }

   /* For feed files. */
   static
   String getPath(String feed, String append)
   {
      String feedFolder = feed + Constants.SEPAR;

      if(Constants.IMAGES.equals(append))
      {
         return feedFolder + Constants.IMAGE_DIR;
      }
      if(Constants.THUMBNAILS.equals(append))
      {
         return feedFolder + Constants.THUMBNAIL_DIR;
      }

      return feedFolder + append;
   }

   /* This should never return null and so do not check. */
   static
   Context getContext()
   {
      return null != s_context ? s_context : null;
   }

   static
   void setContext(Context context)
   {
      s_context = context;
   }

   static
   LayoutInflater getLayoutInflater()
   {
      String inflate = Context.LAYOUT_INFLATER_SERVICE;
      Context con = getContext();
      return (LayoutInflater) con.getSystemService(inflate);
   }

   /* Safe to call at anytime. */
   static
   int getScreenWidth()
   {
      return getContext().getResources().getDisplayMetrics().widthPixels;
   }

   /* This function will return null if it fails. Check for null each time.
    * It should be pretty safe and efficient to call ALL_TAG the time. */
   static
   String getStorage()
   {
      if(!s_storage.isEmpty())
      {
         return s_storage;
      }
      /* Check the media state for any undesirable states. */
      /* TODO Check to see if it is the desired first then skip these. */
      String state = Environment.getExternalStorageState();

      int errorsLength = Constants.MEDIA_ERRORS.length;
      for(int i = 0; i < errorsLength; i++)
      {
         if(state.equals(Constants.MEDIA_ERRORS[i]))
         {
            post(Constants.MEDIA_ERROR_MESSAGES[i]);
            return null;
         }
      }

      /* If it has reached here the state is MEDIA_MOUNTED and we can continue.
         Build the s_storage string depending on android version. */
      Context context = getContext();
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
   class SetHolder
   {
      static final Set<String> READ_ITEMS = Read.set(Constants.READ_ITEMS);
   }

   static
   int[] getUnreadCounts(String... ctags)
   {
      int size = ctags.length;
      int[] unreadCounts = new int[size];

      /* READ_ITEMS == null when called from the service for notifications. */

      int total = 0;
      for(int i = 1; i < size; i++)
      {
         int unread = 0;
         String[] urls = Read.file(getPath(ctags[i], Constants.URL));
         for(String url : urls)
         {
            if(!SetHolder.READ_ITEMS.contains(url))
            {
               unread++;
            }
         }

         total += unread;
         unreadCounts[i] = unread;
      }

      unreadCounts[0] = total;
      return unreadCounts;
   }

   static
   void setStripColor(PagerTabStrip strip)
   {
      String colorSettingsPath = Constants.SETTINGS_DIR + Constants.STRIP_COLOR;

      /* Read the colour from the settings/colour file, if blank, use blue. */
      String[] check = Read.file(colorSettingsPath);
      String color = 0 == check.length ? "blue" : check[0];

      /* Find the colour stored in adapter_stettings_interface that we want. */
      int pos = index(AdapterSettingsUi.HOLO_COLORS, color);
      if(-1 != pos)
      {
         strip.setTabIndicatorColor(AdapterSettingsUi.COLOR_INTS[pos]);
      }
   }

   static
   Intent getServiceIntent(int page)
   {
      /* Load notification boolean. */
      String path = Constants.SETTINGS_DIR + AdapterSettingsFunctions.FILE_NAMES[3] +
            Constants.TXT;
      String[] check = Read.file(path);
      Context con = getContext();

      boolean notif = 0 != check.length && strbol(check[0]);
      Intent intent = new Intent(con, ServiceUpdate.class);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notif);
      return intent;
   }

   static
   void setCardAlpha(TextView title, TextView url, ImageView image, TextView des, String link)
   {
      if(!Constants.HONEYCOMB)
      {
         return;
      }

      Resources res = getContext().getResources();

      if(SetHolder.READ_ITEMS.contains(link))
      {
         title.setTextColor(res.getColor(R.color.title_grey));
         url.setTextColor(res.getColor(R.color.link_grey));

         if(null != des)
         {
            des.setTextColor(res.getColor(R.color.des_grey));
         }

         if(null != image)
         {
            image.setAlpha(0.5f);
         }
      }
      else
      {
         title.setTextColor(res.getColor(R.color.title_black));
         url.setTextColor(res.getColor(R.color.link_black));

         if(null != des)
         {
            des.setTextColor(res.getColor(R.color.des_black));
         }

         if(null != image)
         {
            image.setAlpha(1.0f);
         }
      }
   }

   /* Changes the ManageFeedsRefresh menu item to an animation if m_mode = true. */
   static
   void setRefreshingIcon(boolean mode)
   {
      if(null == FeedsActivity.s_optionsMenu)
      {
         return;
      }

      /* Find the menu item by ID called ManageFeedsRefresh. */
      MenuItem item = FeedsActivity.s_optionsMenu.findItem(R.id.refresh);
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

   /* This will log and toast any message. */
   static
   void post(CharSequence message)
   {
      /* If this is called from off the UI thread, it will die. */
      try
      {
         /* If the activity is running, make a toast notification.
            * Log the event regardless. */
         if(null != FeedsActivity.s_serviceHandler)
         {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
         }

         /* This function can be called when no media, so this is optional. */
         String mounted = Environment.MEDIA_MOUNTED;
         if(mounted.equals(Environment.getExternalStorageState()))
         {
            Write.log((String) message);
         }
      }
      catch(RuntimeException e)
      {
         e.printStackTrace();
      }
   }

   static
   boolean remove(String path)
   {
      path = getStorage() + path;
      getContext().deleteFile(getInternalPath(path));
      return new File(path).delete();
   }

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
   void mkdir(String path)
   {
      path = getStorage() + path;
      File folder = new File(path);
      if(!folder.exists())
      {
         folder.mkdirs();
      }
   }

   /* Wrappers for neatness. */

   static
   boolean move(String originalFile, String resultingFile)
   {
      originalFile = getStorage() + originalFile;
      resultingFile = getStorage() + resultingFile;
      return new File(originalFile).renameTo(new File(resultingFile));
   }

   static
   boolean isUsingSd()
   {
      /* Return true if force sd setting is true. */
      return true;
   }

   static
   boolean strbol(String str)
   {
      return Boolean.parseBoolean(str);
   }

   static
   int stoi(String str)
   {
      return Integer.parseInt(str);
   }

   static
   boolean exists(String path)
   {
      path = getStorage() + path;
      if(isUsingSd() || path.contains(Constants.IMAGE_DIR) ||
            path.contains(Constants.THUMBNAIL_DIR))
      {
         return !new File(path).exists();
      }
      else
      {
         String internalPath = getInternalPath(path);
         return !getContext().getFileStreamPath(internalPath).exists();
      }
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
   String getText(TextView view)
   {
      return view.getText().toString().trim();
   }

   /* Returns a zero-length array if the resource is not found and logs the
    * event in log. */
   static
   String[] getArray(int resource)
   {
      String[] array = EMPTY_STRING_ARRAY;
      Context con = getContext();
      try
      {
         array = con.getResources().getStringArray(resource);
      }
      catch(Resources.NotFoundException e)
      {
         e.printStackTrace();
         Write.log(resource + " does not exist.");
      }
      return array;
   }

   static
   String getString(int resource)
   {
      String str = "";
      Context con = getContext();
      try
      {
         str = con.getString(resource);
      }
      catch(Resources.NotFoundException e)
      {
         e.printStackTrace();
         Write.log(resource + " does not exist.");
      }
      return str;
   }
}
