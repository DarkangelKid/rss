package yay.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerTabStrip;
import android.os.AsyncTask;
import android.os.Environment;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

public class util
{
   static final String[] media_error_messages = new String[]
   {
      "Media not mounted.",
      "Unable to mount media.",
      "Media is shared via USB mass storage.",
      "Media does not exist.",
      "Media contains unsupported filesystem.",
      "Media mounted as read-only.",
      "Media is being disk checked.",
      "Media was removed before unmounted."
   };

   static final String[] media_errors = new String[]
   {
      Environment.MEDIA_UNMOUNTED,
      Environment.MEDIA_UNMOUNTABLE,
      Environment.MEDIA_SHARED,
      Environment.MEDIA_REMOVED,
      Environment.MEDIA_NOFS,
      Environment.MEDIA_MOUNTED_READ_ONLY,
      Environment.MEDIA_CHECKING,
      Environment.MEDIA_BAD_REMOVAL
   };

   /* TODO UPDATE FOR INTERNAL STORAGE. */
   static void delete_feed(String group, String feed, int pos)
   {
      /* Get strings to make things clearer. */
      String storage = get_storage();
      String sep     = main.SEPAR;
      String txt     = main.TXT;
      String all     = main.ALL;
      String content = main.CONTENT;
      String count   = main.COUNT;
      String g_list  = main.GROUP_LIST;
      String g_dir   = main.GROUPS_DIR;

      /* Parse the group name from the info string. */
      int start    = group.indexOf('\n') + 1;
      int end      = group.indexOf(' ');
      group        = group.substring(start, end);

      String group_dir  = storage + g_dir + group;
      String all_file   = storage + g_dir + all + sep + all;
      String group_file = group_dir + sep + group;

      rmdir(new File(group_dir + sep + feed));
      write.remove_string(group_file + txt, feed, true);
      write.remove_string(all_file + txt, feed, true);

      rm_empty(group_file + txt);
      if(!(new File(group_file + txt).exists()))
      {
         rmdir(new File(group_dir));
         write.remove_string(storage + g_list, group, false);
      }
      else
      {
         write.sort_content(group, all);
         rm_empty(group_file + content);
         rm_empty(group_file + count);
      }

      String[] all_groups = read.file(storage + g_list);
      if(all_groups.length == 1)
         rmdir(new File(storage + g_dir + all));

      else if(all_groups.length != 0)
      {
         /* This line may be broken. */
         write.sort_content(all, all);
         rm_empty(all_file + content);
         rm_empty(all_file + count);
      }

      main.update_groups();
      fragment_manage_feed.feed_list_adapter.remove_item(pos);
      fragment_manage_feed.feed_list_adapter.notifyDataSetChanged();

      update.manage_groups();
   }

   static boolean check_media_mounted()
   {
      /* Check to see if we can write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      if(!mounted.equals(Environment.getExternalStorageState()))
      {
         util.post(write.MEDIA_UNMOUNTED);
         return false;
      }
      return true;
   }

   static <T> T[] concat(T[] first, T[] second)
   {
      T[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   static byte[] concat(byte[] first, byte[] second)
   {
      byte[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   /* index throws an ArrayOutOfBoundsException if not handled. */
   static <T> int index(T[] array, T value)
   {
      for(int i = 0; i < array.length; i++)
      {
         if(array[i].equals(value))
            return i;
      }
      return -1;
   }

   static int index(int[] array, int value)
   {
      for(int i = 0; i < array.length; i++)
      {
         if(array[i] == value)
            return i;
      }
      return -1;
   }

   static String[] remove_element(String[] a, int index)
   {
      String[] b = new String[a.length - 1];
      System.arraycopy(a, 0, b, 0, index);
      if(a.length != index)
      {
         System.arraycopy(a, index + 1, b, index, a.length - index - 1);
      }
      return b;
   }

   static String[][] create_info_arrays(String[] cgroups, int size)
   {
      String info, group_path, sep = main.SEPAR;
      String storage      = util.get_storage();
      int number, i, j, total;
      String[] content;
      String[] group_array = new String[size];
      String[] info_array  = new String[size];

      String content_path = storage + main.GROUPS_DIR + cgroups[0]
                            + sep + cgroups[0] + main.CONTENT;

      String count_path = content_path + main.COUNT;

      String[] count = read.file(count_path);
      if(count.length != 0)
         total = stoi(count[0]);
      else
         total = read.count(content_path);

      for(i = 0; i < size; i++)
      {
         group_array[i] = cgroups[i];
         group_path     = storage + main.GROUPS_DIR + group_array[i]
                          + sep + group_array[i] + main.TXT;

         content = read.csv(group_path, 'n')[0];
         if(i == 0)
            info = (size == 1) ? "1 group" :  size + " groups";
         else
         {
            info = "";
            number = (content.length < 3) ? content.length : 3;

            for(j = 0; j < number - 1; j++)
               info += content[j].concat(", ");

            if(content.length > 3)
               info += "...";
            else if(number > 0)
               info += content[number - 1];
         }
         info_array[i] = content.length + " feeds • " + info;
      }
      info_array[0] =  total + " items • " + info_array[0];
      return (new String[][]{info_array, group_array});
   }

   static boolean check_unmounted()
   {
      String storage  = get_storage();
      String internal = get_internal();
      if(storage == null || internal.equals(storage));
      {
         if(!util.check_media_mounted())
            return true;
      }
      return false;
   }

   static String create_internal_name(String path)
   {
      String name = path;
      name = path.substring(path.indexOf("/files/") + 7);
      return name.replaceAll("/", "-");
   }

   static adapter_feeds_cards get_card_adapter(FragmentManager fman, ViewPager viewpager, int page_number)
   {
      ListView list = get_listview(fman, viewpager, page_number);
      if(list == null)
         return null;
      return (adapter_feeds_cards) get_listview(fman, viewpager, page_number).getAdapter();
   }

   static ListView get_listview(FragmentManager fman, ViewPager viewpager, int page_number)
   {
      try
      {
         return ((ListFragment) fman.findFragmentByTag("android:switcher:" + viewpager.getId() + ":" + page_number)).getListView();
      }
      catch(Exception e)
      {
         return null;
      }
   }

   static Context get_context()
   {
      /* If running get the context from the activity, else ask the service. */
      if(main.con != null)
         return main.con;

      else if(service_update.service_context != null)
         return service_update.service_context;

      else /* This case should never happen because either must be running. */
         return null;
   }

   static String get_internal()
   {
      /* First check to see if storage all ready exists in the service
       * or activity. */
      if(main.internal != null)
         return main.internal;

      if(service_update.internal != null)
         return service_update.internal;

      Context context = get_context();
      String settings = main.SETTINGS;
      String internal = util.get_context().getFilesDir().getAbsolutePath() + main.SEPAR;

      /* If setting says force external for all, use external for internal. */
      /*String use = read.setting(internal + settings + main.INT_STORAGE);
      if(use.equals("false"))
          util.get_internal()get_storage();*/

      return internal;
   }

   /* This function will return null if it fails. Check for null each time.
    * It should be pretty safe and efficient to call all the time. */
   static String get_storage()
   {
      /* First check to see if storage all ready exists in the service
       * or activity. */
      if(main.storage != null)
         return main.storage;

      if(service_update.storage != null)
         return service_update.storage;

      Context context = get_context();

      /* Check the media state for any undesirable states. */
      String state = Environment.getExternalStorageState();
      for(int i = 0; i < media_errors.length; i++)
      {
         if(state.equals(media_errors[i]))
         {
            post(media_error_messages[i]);
            return null;
         }
      }

      /* If it has reached here the state is MEDIA_MOUNTED and we can continue.
         Build the storage string depending on android version. */
      String storage;
      String sep = main.SEPAR;

      if(main.FROYO)
         storage = context.getExternalFilesDir(null).getAbsolutePath() + sep;
      else
      {
         String name = context.getPackageName();
         File ext    = Environment.getExternalStorageDirectory();
         storage     = ext.getAbsolutePath() + sep + "Android" + sep + "data"
                       + sep + name + sep + "files" + sep;

         /* If the folder does not exist, create it. */
         File storage_file  = new File(storage);
         if(!storage_file.exists())
            storage_file.mkdirs();
      }
      return storage;
   }

   static int[] get_unread_counts(String[] cgroups)
   {
      String storage       = util.get_storage();
      int total            = 0, unread, num;
      final int size       = cgroups.length;
      int[] unread_counts  = new int[size];
      adapter_feeds_cards temp;

      /* read_items == null when called from the service for notifications. */
      if( adapter_feeds_cards.read_items == null )
      {
         adapter_feeds_cards.read_items = read.set(storage + main.READ_ITEMS);
      }

      for(int i = 1; i < size; i++)
      {
         unread = 0;
         String[] urls = read.file(storage + main.GROUPS_DIR + cgroups[i]
                                   + main.SEPAR + cgroups[i] +
                                   main.CONTENT + main.URL);
         for(String url : urls)
         {
            if(!adapter_feeds_cards.read_items.contains(url))
                  unread++;
         }

         total += unread;
         unread_counts[i] = unread;
      }

      unread_counts[0] = total;
      return unread_counts;
   }

   static void set_strip_colour(PagerTabStrip strip)
   {
      String storage     = util.get_storage();
      String colour_path = storage + main.SETTINGS + main.STRIP_COLOR;

      /* Read the colour from the settings/colour file, if blank, use blue. */
      String[] check  = read.file(colour_path);
      String   colour = (check.length == 0) ? "blue" : check[0];

      /* Find the colour stored in adapter_stettings_interface that we want. */
      int pos = index(adapter_settings_interface.colours, colour);
      if(pos != -1)
      {
         strip.setTabIndicatorColor(adapter_settings_interface.colour_ints[pos]);
      }
   }

   static Intent make_intent(Context context, int page)
   {
      /* Load notification boolean. */
      String path    = util.get_storage() + main.SETTINGS
                     + adapter_settings_function.file_names[3] + main.TXT;
      String[] check = read.file(path);

      boolean notif = (check.length != 0) ? strbol(check[0]) : false;
      Intent intent = new Intent(context, service_update.class);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notif);
      return intent;
   }

   static void set_service(Context con, int page, String state)
   {
      String   alarm = Activity.ALARM_SERVICE;
      int      time  = adapter_settings_function.times[3];
      String[] names = adapter_settings_function.file_names;
      String   pre   = util.get_storage() + main.SETTINGS;
      String   txt   = main.TXT;

      /* Load the refresh boolean value from settings. */
      String[] check  = read.file(pre + names[1] + txt);
      Boolean refresh = (check.length != 0) ? strbol(check[0]) : false;

      if(!refresh && state.equals("start"))
         return;

      /* Load the refresh time from settings. */
      check = read.file(pre + names[2] + txt);
      if(check.length != 0)
         time = stoi(check[0]);

      /* Create intent, turn into pending intent, and get the alarmmanager. */
      Intent        intent  = make_intent(con, 0);
      PendingIntent pintent = PendingIntent.getService(con, 0, intent, 0);
      AlarmManager  am      = (AlarmManager) con.getSystemService(alarm);

      /* Depending on the state string, start or stop the service. */
      if(state.equals("start"))
      {
         long interval = (long) time*60000;
         long next     =  System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pintent);
      }
      else if(state.equals("stop"))
      {
         am.cancel(pintent);
      }
   }

   /* Use this after content has been updated and you need to refresh */
   static void refresh_pages(int page)
   {
      update.page(0);
      if(page != 0)
         update.page(page);
      else
      {
         for(int i = 1; i < main.cgroups.length; i++)
            update.page(i);
      }
   }

   static void post(CharSequence message)
   {
      /* If the activity is running, make a toast notification.
       * Log the event regardless. */
      if(main.service_handler != null)
         Toast.makeText(main.con, message, Toast.LENGTH_LONG).show();

      /* This function can be called when no media, so this is optional. */
      String mounted = Environment.MEDIA_MOUNTED;
      if(mounted.equals(Environment.getExternalStorageState()))
         write.log((String) message);
   }

   static boolean rm(String file_path)
   {
      get_context().deleteFile(create_internal_name(file_path));
      return (new File(file_path)).delete();
   }

   static void rm_empty(String file_path)
   {
      File file = new File(file_path);
      if(file.exists() && file.length() == 0)
         file.delete();
   }

   static boolean rmdir(File directory)
   {
      if(directory.isDirectory())
      {
         for(String child : directory.list())
         {
            boolean success = rmdir(new File(directory, child));
            if(!success)
               return false;
         }
      }
      return directory.delete();
   }

   static void mkdir(String path)
   {
      File folder = new File(get_storage() + path);
      if(!folder.exists())
         folder.mkdirs();
   }


   /* Wrappers for neatness. */

   static boolean mv(String a, String b)
   {
      return (new File(a)).renameTo(new File(b));
   }

   static boolean strbol(String str)
   {
      return Boolean.parseBoolean(str);
   }

   static int stoi(String str)
   {
      return Integer.parseInt(str);
   }

   static boolean exists(String file_path)
   {
      return (new File(file_path)).exists();
   }

   static String getstr(TextView t)
   {
      return t.getText().toString().trim();
   }

   /* Returns a zero-length array if the resource is not found and logs the
    * event in log. */
   static String[] get_array(Context con, int resource)
   {
      String[] array  = new String[0];
      try
      {
         array = con.getResources().getStringArray(resource);
      }
      catch(android.content.res.Resources.NotFoundException e)
      {
         write.log(resource + " does not exist.");
      }
      return array;
   }
}
