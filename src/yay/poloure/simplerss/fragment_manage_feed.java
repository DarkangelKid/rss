package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import java.io.File;

class fragment_manage_feed extends Fragment
{
   private static ListView feed_list;
   public  static adapter_manage_feeds feed_list_adapter;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
      setRetainInstance(false);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      final View view = inflater.inflate(R.layout.manage_listviews, container, false);
      feed_list = (ListView) view.findViewById(R.id.manage_listview);
      feed_list.setOnItemClickListener
      (
         new OnItemClickListener()
         {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
               add_edit_dialog.show_edit_dialog(main.current_groups, main.con, main.storage, position);
            }
         }
      );

      feed_list_adapter = new adapter_manage_feeds(getActivity());
      feed_list.setAdapter(feed_list_adapter);

      update.manage_feeds();

      feed_list.setOnItemLongClickListener
      (
         new OnItemLongClickListener()
         {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id)
            {
               new AlertDialog.Builder(main.con)
               .setCancelable(true)
               .setNegativeButton
               (
                  main.DELETE_DIALOG,
                  new DialogInterface.OnClickListener()
                  {
                     /// Delete the feed.
                     @Override
                     public void onClick(DialogInterface dialog, int id)
                     {
                        String group = feed_list_adapter.get_info(pos);
                        group = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
                        final String name = feed_list_adapter.getItem(pos);

                        final String group_file    = main.storage + main.GROUPS_DIR + group +    main.SEPAR + group + main.TXT;
                        final String group_prepend = main.storage + main.GROUPS_DIR + group +    main.SEPAR + group;
                        final String all_file      = main.storage + main.GROUPS_DIR + main.ALL + main.SEPAR + main.ALL;

                        util.rmdir(new File(main.storage + main.GROUPS_DIR + group + main.SEPAR + name));
                        write.remove_string(group_file, name, true);
                        write.remove_string(all_file + main.TXT, name, true);

                        util.rm_empty(group_file);
                        if(!(new File(group_file).exists()))
                        {
                           util.rmdir(new File(main.storage + main.GROUPS_DIR + group));
                           write.remove_string(main.storage + main.GROUP_LIST, group, false);
                        }
                        else
                        {
                           write.sort_content(main.storage, group, main.ALL);
                           util.rm_empty(group_prepend + main.CONTENT);
                           util.rm_empty(group_prepend + main.COUNT);
                        }

                        String[] all_groups = read.file(main.storage + main.GROUP_LIST);
                        if(all_groups.length == 1)
                           util.rmdir(new File(main.storage + main.GROUPS_DIR + main.ALL));

                        else if(all_groups.length != 0)
                        {
                           /* This line may be broken. */
                           write.sort_content(main.storage, main.ALL, main.ALL);
                           util.rm_empty(all_file + main.CONTENT);
                           util.rm_empty(all_file + main.COUNT);
                        }

                        main.update_groups();
                        feed_list_adapter.remove_item(pos);
                        feed_list_adapter.notifyDataSetChanged();

                        update.manage_groups();
                     }
                  }
               )
               .setPositiveButton
               (
                  main.CLEAR_DIALOG,
                  new DialogInterface.OnClickListener()
                  {
                     /// Delete the cache.
                     @Override
                     public void onClick(DialogInterface dialog, int id)
                     {
                        String group            = feed_list_adapter.get_info(pos);
                        group                   = group.substring(group.indexOf('\n') + 1, group.indexOf(' '));
                        final String name       = feed_list_adapter.getItem(pos);
                        final String path       = main.storage + main.GROUPS_DIR + group + main.SEPAR + name;
                        final File feed_folder  = new File(path);
                        util.rmdir(feed_folder);
                        /// make the image and thumnail folders.
                        (new File(path + main.SEPAR + main.IMAGE_DIR))     .mkdir();
                        (new File(path + main.SEPAR + main.THUMBNAIL_DIR)) .mkdir();

                        /// Delete the all content files.
                        (new File(main.storage + main.GROUPS_DIR + main.ALL + main.SEPAR + main.ALL + main.CONTENT)).delete();
                        (new File(main.storage + main.GROUPS_DIR + main.ALL + main.SEPAR + main.ALL + main.COUNT)).delete();

                        /// Refresh pages and update groups and stuff
                        main.update_groups();
                        update.manage_feeds();
                        update.manage_groups();
                     }
                  }
               ).show();
               return true;
            }
         }
      );
      return view;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(navigation_drawer.drawer_toggle.onOptionsItemSelected(item))
         return true;
      else if(item.getTitle().equals("add"))
      {
         add_edit_dialog.show_add_dialog(main.current_groups, main.con);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   public static class refresh extends AsyncTask<Void, String[], Void>
   {
      private final Animation animFadeIn = AnimationUtils.loadAnimation(main.con, android.R.anim.fade_in);
      private final ListView listview;

      public refresh()
      {
         listview = fragment_manage_feed.feed_list;
         if(feed_list_adapter.getCount() == 0)
            listview.setVisibility(View.INVISIBLE);
      }

      @Override
      protected Void doInBackground(Void... hey)
      {
         if(feed_list_adapter != null)
         {
            final String[][] content   = read.csv(main.storage + main.GROUPS_DIR + main.current_groups[0] + main.SEPAR + main.current_groups[0] + main.TXT, 'n', 'u', 'g');
            final int size             = content[0].length;
            String[] info_array        = new String[size];
            for(int i = 0; i < size; i++)
               info_array[i] = content[1][i] + main.NL + content[2][i] + " • " + Integer.toString(read.count(main.storage + main.GROUPS_DIR + content[2][i] + main.SEPAR + content[0][i] + main.SEPAR + content[0][i] + main.CONTENT)) + " items";
            publishProgress(content[0], info_array);
         }
         return null;
      }

      @Override
      protected void onProgressUpdate(String[][] progress)
      {
         feed_list_adapter.set_items(progress[0], progress[1]);
         feed_list_adapter.notifyDataSetChanged();
      }

      @Override
      protected void onPostExecute(Void tun)
      {
         listview.setAnimation(animFadeIn);
         listview.setVisibility(View.VISIBLE);
      }
   }
}
