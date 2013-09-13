package yay.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.graphics.Point;

import java.io.File;
import java.util.Arrays;

public class util
{
   static final String[] media_error_messages = get_array(R.array.media_errors);

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
      if( all_groups.length == 1 )
      {
         rmdir(new File(storage + g_dir + all));
      }
      else if( all_groups.length != 0 )
      {
         /* This line may be broken. */
         write.sort_content(all, all);
         rm_empty(all_file + content);
         rm_empty(all_file + count);
      }

      update_groups();
      adapter_manage_feeds adapter = (adapter_manage_feeds) pageradapter_manage.fragments[1].getListAdapter();
      adapter.remove_item(pos);
      adapter.notifyDataSetChanged();

      update.manage_groups();
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

   static boolean update_groups()
   {
      /* Since this function is static, we can not rely on the fields being
       * non-null. */
      String storage = get_storage();

      /* Update cgroups. */
      main.cgroups = read.file(storage + main.GROUP_LIST);

      /* If no groups exists, add the ALL group. */
      if(main.cgroups.length == 0)
      {
         write.single(storage + main.GROUP_LIST, main.ALL + main.NL);
         main.cgroups = new String[]{main.ALL};
      }

      if(main.viewpager != null)
      {
         main.viewpager.getAdapter().notifyDataSetChanged();
         /* Does not run on first update. */
         update.navigation(null);
         return true;
      }
      return false;
   }

   static int jump_to_latest_unread(String[] links, boolean update, int page)
   {
      int i, pos;

      if(main.viewpager == null)
         return -1;

      if(update)
         page = main.viewpager.getCurrentItem();

      if(links == null)
      {
         adapter_card adp = get_card_adapter(page);
         if(adp == null)
            return -1;

         links = adp.links;
      }

      for(i = links.length - 1; i >= 0; i--)
      {
         pos = links.length - i - 1;
         if(!adapter_card.read_items.contains(links[pos]))
            break;
      }

      /* 0 is the top. links.length - 1 is the bottom.
       * May not be true anymore.*/
      if(update)
      {
         ListView lv = get_listview(page);
         if(lv == null)
            return -1;

         lv.setSelection(i);
      }
      return i;
   }

   static String[][] create_info_arrays(String[] cgroups)
   {
      String info, group_path;
      int number, i, j, total;
      String[] content;

      int size             = cgroups.length;
      String sep           = main.SEPAR;
      String all           = main.ALL;
      String txt           = main.TXT;
      String g_dir         = get_storage() + main.GROUPS_DIR;
      String[] group_array = new String[size];
      String[] info_array  = new String[size];

      String content_path = g_dir + all + sep + all + main.CONTENT;

      String count_path = content_path + main.COUNT;

      total = read.count(content_path);

      for(i = 0; i < size; i++)
      {
         group_array[i] = cgroups[i];

         content = read.csv(cgroups[i], 'n')[0];
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

      /* TODO If setting to force sd is true, return false. */
      /*if(!internal.equals(storage))
         return false;*/

      /* Check to see if we can write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      if(!mounted.equals(Environment.getExternalStorageState()))
      {
         post(write.MEDIA_UNMOUNTED);
         return true;
      }

      return false;
   }

   /* Replaces all '/'s with '-' to emulate a folder directory layout in
    * data/data. */
   static String create_internal_name(String path)
   {
      String name = path;
      name = path.substring(path.indexOf("/files/") + 7);
      return name.replaceAll("/", "-");
   }

   /* For these two functions, check for null. Should only really be
    * null if called from the service_update. */
   static adapter_card get_card_adapter(int page)
   {
      ListView list = get_listview(page);
      if(list == null)
         return null;

      return (adapter_card) list.getAdapter();
   }

   /* This is the second one. */
   static ListView get_listview(int page)
   {
      FragmentManager fman = main.fman;
      ViewPager  viewpager = main.viewpager;
      if(fman == null || viewpager == null)
         return null;

      String tag = "android:switcher:" + viewpager.getId() + ":" + page;
      return ((ListFragment) fman.findFragmentByTag(tag)).getListView();
   }

   /* For feed files. */
   static String get_path(String group, String feed, String append)
   {
      return get_storage() + main.GROUPS_DIR + group + main.SEPAR + feed
                           + main.SEPAR + feed + append;
   }

   /* For group files. */
   static String get_path(String group, String append)
   {
      return get_storage() + main.GROUPS_DIR + group + main.SEPAR + group
                           + append;
   }

   /* For image folders. */
   static String get_path(String group, String feed, String folder)
   {
      String prepend = get_storage() + main.GROUPS_DIR + group + main.SEPAR + feed
                                     + main.SEPAR + feed + main.SEPAR;
      if(folder.equals("images"))
         return prepend + main.IMAGE_DIR;
      if(folder.equals("thumbnails"))
         return prepend + main.THUMBNAIL_DIR;
   }

   /* This should never return null and so do not check. */
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

   static LayoutInflater get_inflater()
   {
      String inflate = Context.LAYOUT_INFLATER_SERVICE;
      Context con = get_context();
      return (LayoutInflater) con.getSystemService(inflate);
   }

   /* Safe to call at anytime. */
   static int get_screen_width()
   {
      return get_context().getResources().getDisplayMetrics().widthPixels;
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
      Context context = get_context();

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
      String storage       = get_storage();
      int total            = 0, unread, num;
      final int size       = cgroups.length;
      int[] unread_counts  = new int[size];
      adapter_card temp;

      /* read_items == null when called from the service for notifications. */
      if( adapter_card.read_items == null )
      {
         adapter_card.read_items = read.set(storage + main.READ_ITEMS);
      }

      for(int i = 1; i < size; i++)
      {
         unread = 0;
         String[] urls = read.file(util.get_path(cgroups[i], main.CONTENT + main.URL));
         for(String url : urls)
         {
            if(!adapter_card.read_items.contains(url))
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
      String storage     = get_storage();
      String colour_path = storage + main.SETTINGS + main.STRIP_COLOR;

      /* Read the colour from the settings/colour file, if blank, use blue. */
      String[] check  = read.file(colour_path);
      String   colour = (check.length == 0) ? "blue" : check[0];

      /* Find the colour stored in adapter_stettings_interface that we want. */
      int pos = index(adapter_settings_UI.colours, colour);
      if(pos != -1)
      {
         strip.setTabIndicatorColor(adapter_settings_UI.colour_ints[pos]);
      }
   }

   static void show_fragment(Fragment fragment)
   {
      FragmentTransaction tran = main.fman.beginTransaction();
      for(Fragment frag : main.main_fragments)
      {
         if(!frag.isHidden())
         {
            tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
            .hide(frag)
            .show(fragment)
            .commit();
            return;
         }
      }
      tran.show(fragment).commit();
   }

   static Intent make_intent(Context context, int page)
   {
      /* Load notification boolean. */
      String path    = get_storage() + main.SETTINGS
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
      String   pre   = get_storage() + main.SETTINGS;
      String   txt   = main.TXT;

      /* Load the refresh boolean value from settings. */
      String[] check  = read.file(pre + names[1] + txt);
      boolean refresh = (check.length != 0) ? strbol(check[0]) : false;

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

   static void set_alpha(TextView title, TextView url, ImageView image,
                                             TextView des, String link)
   {
      if(!main.HONEYCOMB)
         return;

      if(adapter_card.read_items.contains(link))
      {
         title .setTextColor(R.color.title_grey);
         url   .setTextColor(R.color.link_grey);

         if(des != null)
            des.setTextColor(R.color.des_grey);

         if(image != null)
            image.setAlpha(0.5f);
      }
      else
      {
         title .setTextColor(R.color.title_black);
         url   .setTextColor(R.color.link_black);

         if(des!= null)
            des.setTextColor(R.color.des_black);

         if(image != null)
            image.setAlpha(1.0f);
      }
   }

   /* Changes the refresh menu item to an animation if mode = true. */
   static void set_refresh(boolean mode)
   {
      if(main.optionsMenu == null)
         return;

      /* Find the menu item by ID caled refresh. */
      MenuItem item = main.optionsMenu.findItem(R.id.refresh);
      if(item == null)
         return;

      /* Change it depending on the mode. */
      if(mode)
         MenuItemCompat.setActionView(item, R.layout.progress_circle);
      else
         MenuItemCompat.setActionView(item, null);
   }

   /* Updates and refreshes the groups with any new content. */
   static void refresh_feeds()
   {
      set_refresh(true);

      /* Set the service handler in main so we can check and call it
       * from service_update. */

      main.service_handler = new Handler()
      {
         /* The stuff we would like to run when the service completes. */
         @Override
         public void handleMessage(Message msg)
         {
            set_refresh(false);
            int page = msg.getData().getInt("page_number");
            refresh_pages(page);
         }
      };
      Context context = get_context();
      int current_page = main.viewpager.getCurrentItem();
      Intent intent = make_intent(context, current_page);
      context.startService(intent);
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

   /* This will log and toast any message. */
   static void post(CharSequence message)
   {
      /* If this is called from off the UI thread, it will die. */
      try
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
      catch(RuntimeException e)
      {
      }
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
      File folder = new File(path);
      if(!folder.exists())
         folder.mkdirs();
   }


   /* Wrappers for neatness. */

   static boolean mv(String a, String b)
   {
      return (new File(a)).renameTo(new File(b));
   }

   static boolean use_sd()
   {
      /* Return true if force sd setting is true. */
      return true;
   }

   static boolean strbol(String str)
   {
      return Boolean.parseBoolean(str);
   }

   static int stoi(String str)
   {
      return Integer.parseInt(str);
   }

   static boolean exists(String path)
   {
      if( use_sd() || path.contains(main.IMAGE_DIR) ||
                      path.contains(main.THUMBNAIL_DIR))
         return (new File(path)).exists();
      else
      {
         String in_path = create_internal_name(path);
         return (get_context().getFileStreamPath(in_path)).exists();
      }
   }

   static void set_text(String str, android.view.View v, int id)
   {
      ((TextView) v.findViewById(R.id.url)).setText(str);
   }

   static String getstr(android.view.View v, int id)
   {
      return ((TextView) v.findViewById(R.id.url)).getText().toString().trim();
   }

   static String getstr(TextView v)
   {
      return v.getText().toString().trim();
   }

   /* Returns a zero-length array if the resource is not found and logs the
    * event in log. */
   static String[] get_array(int resource)
   {
      String[] array  = new String[0];
      Context  con    = get_context();
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

   static String get_string(int resource)
   {
      String  str = "";
      Context con = get_context();
      try
      {
         str = con.getString(resource);
      }
      catch(android.content.res.Resources.NotFoundException e)
      {
         write.log(resource + " does not exist.");
      }
      return str;
   }
}
