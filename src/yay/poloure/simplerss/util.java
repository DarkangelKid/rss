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
      private final static String[] media_error_messages = new String[]
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

      private final static String[] media_errors = new String[]
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

   public static void delete_group(String storage, String group)
   {
      /// Move all feeds to an unsorted group.
      //rm(storage + main.GROUPS_DIR + group + main.TXT);
      //rm(storage + main.GROUPS_DIR + group + main.GROUP_CONTENT);
   }

   public static <T> T[] concat(T[] first, T[] second)
   {
      T[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   public static byte[] concat(byte[] first, byte[] second)
   {
      byte[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
   }

   /* index throws an ArrayOutOfBoundsException if not handled. */
   public static <T> int index(T[] array, T value)
   {
      for(int i = 0; i < array.length; i++)
      {
         if(array[i].equals(value))
            return i;
      }
      return -1;
   }

   public static int index(int[] array, int value)
   {
      for(int i = 0; i < array.length; i++)
      {
         if(array[i] == value)
            return i;
      }
      return -1;
   }

   public static String[] remove_element(String[] a, int index)
   {
      String[] b = new String[a.length - 1];
      System.arraycopy(a, 0, b, 0, index);
      if(a.length != index)
      {
         System.arraycopy(a, index + 1, b, index, a.length - index - 1);
      }
      return b;
   }

   public static String[][] create_info_arrays(String[] current_groups, int size, String storage)
   {
      String info, group_path, sep = main.SEPAR;
      int number, i, j, total;
      String[] content;
      String[] group_array = new String[size];
      String[] info_array  = new String[size];

      String content_path = storage + main.GROUPS_DIR + current_groups[0]
                            + sep + current_groups[0] + main.CONTENT;

      String count_path = content_path + main.COUNT;

      String[] count = read.file(count_path);
      if(count.length != 0)
         total = stoi(count[0]);
      else
         total = read.count(content_path);

      for(i = 0; i < size; i++)
      {
         group_array[i] = current_groups[i];
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
         info_array[i] = Integer.toString(content.length) + " feeds • " + info;
      }
      info_array[0] =  total + " items • " + info_array[0];
      return (new String[][]{info_array, group_array});
   }

   public static adapter_feeds_cards get_card_adapter(FragmentManager fman, ViewPager viewpager, int page_number)
   {
      ListView list = get_listview(fman, viewpager, page_number);
      if(list == null)
         return null;
      return (adapter_feeds_cards) get_listview(fman, viewpager, page_number).getAdapter();
   }

   public static ListView get_listview(FragmentManager fman, ViewPager viewpager, int page_number)
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

   /* This function will return null if it fails. Check for null each time. */
   public static String get_storage()
   {
      final Context context;

      /* If running get the context from the activity, else ask the service. */
      if(main.con != null)
      {
         context = main.con;
      }
      else if(service_update.service_context != null)
      {
         context = service_update.service_context;
      }
      else /* This case should never happen because either must be running. */
         return null;

      /* Check the media state for any undesirable states. */
      final String state = Environment.getExternalStorageState();
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
         String ext  = Environment.getExternalStorageDirectory().getAbsolutePath();
         storage     = ext + sep + "Android" + sep + "data" + sep + name + sep + "files" + sep;

         /* If the folder does not exist, create it. */
         File storage_file  = new File(storage);
         if(!storage_file.exists())
            storage_file.mkdirs();
      }
      return storage;
   }

   public static int[] get_unread_counts(String storage, String[] current_groups)
   {
      int total = 0, unread, num;
      final int size = current_groups.length;
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
         String[] urls = read.file(storage + main.GROUPS_DIR + current_groups[i]
                                   + main.SEPAR + current_groups[i] +
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

   public static void set_pagertabstrip_colour(String storage, PagerTabStrip strip)
   {
      final String colour_path = storage + main.SETTINGS + main.STRIP_COLOR;

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

   public static Intent make_intent(Context context, String storage, int page)
   {
      /* Load notification boolean. */
      String path    = storage + main.SETTINGS
                     + adapter_settings_function.file_names[3] + main.TXT;
      String[] check = read.file(path);

      boolean notif = (check.length != 0) ? util.strbol(check[0]) : false;
      Intent intent = new Intent(context, service_update.class);
      intent.putExtra("GROUP_NUMBER", page);
      intent.putExtra("NOTIFICATIONS", notif);
      return intent;
   }

   public static void set_service(Context con, String storage, int page, String state)
   {
      int      time  = adapter_settings_function.times[3];
      String[] names = adapter_settings_function.file_names;
      String   pre   = storage + main.SETTINGS;
      String   txt   = main.TXT;

      /* Load the refresh boolean value from settings. */
      String[] check  = read.file(pre + names[1] + txt);
      Boolean refresh = (check.length != 0) ? util.strbol(check[0]) : false;

      if(!refresh && state.equals("start"))
         return;

      /* Load the refresh time from settings. */
      check = read.file(pre + names[2] + txt);
      if(check.length != 0)
         time = stoi(check[0]);

      Intent intent             = make_intent(con, storage, 0);
      PendingIntent pend_intent = PendingIntent.getService(con, 0, intent, 0);

      AlarmManager am = (AlarmManager) con.getSystemService(Activity.ALARM_SERVICE);
      if(state.equals("start"))
      {
         long interval = (long) time*60000;
         long next     =  System.currentTimeMillis() + interval;
         am.setRepeating(AlarmManager.RTC_WAKEUP, next, interval, pend_intent);
      }
      else if(state.equals("stop"))
      {
         am.cancel(pend_intent);
      }
   }

   /* Use this after content has been updated and you need to refresh */
   public static void refresh_pages(int page)
   {
      update.page(0);
      if(page != 0)
         update.page(page);
      else
      {
         for(int i = 1; i < main.current_groups.length; i++)
            update.page(i);
      }
   }

   public static void post(String message)
   {
      /* If the activity is running, make a toast notification.
       * Log the event regardless. */
      if(main.service_handler != null)
      {
         Toast.makeText(main.con, (CharSequence) message, Toast.LENGTH_LONG).show();
      }

      /* This function can be called when no media, so this is optional. */
      if(main.storage != null)
      {
         write.log(main.storage, message);
      }
      else if(service_update.storage != null)
      {
         write.log(service_update.storage, message);
      }
   }

   public static boolean rm(String file_path)
   {
      return (new File(file_path)).delete();
   }

   public static void rm_empty(String file_path)
   {
      File file = new File(file_path);
      if(file.exists() && file.length() == 0)
         file.delete();
   }

   public static boolean rmdir(File directory)
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


   /* Wrappers for neatness. */

   public static boolean mv(String a, String b)
   {
      return (new File(a)).renameTo(new File(b));
   }

   public static boolean strbol(String str)
   {
      return Boolean.parseBoolean(str);
   }

   public static int stoi(String str)
   {
      return Integer.parseInt(str);
   }

   public static boolean exists(String file_path)
   {
      return (new File(file_path)).exists();
   }

   public static String getstr(TextView t)
   {
      return t.getText().toString().trim();
   }
}
