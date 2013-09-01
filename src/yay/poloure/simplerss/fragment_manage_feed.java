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
   static ListView feed_list;
   static adapter_manage_feeds feed_list_adapter;

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
               add_edit_dialog.show_edit_dialog(main.cgroups, main.con, position);
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
                     /* Delete the feed. */
                     @Override
                     public void onClick(DialogInterface dialog, int id)
                     {
                        util.delete_feed(feed_list_adapter.get_info(pos),
                                         feed_list_adapter.getItem(pos),
                                         pos                             );
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
                        String all      = main.ALL;
                        String storage  = util.get_storage();
                        String sep      = main.SEPAR;
                        String g_dir    = main.GROUPS_DIR;

                        /* Parse for the group name from the info string. */
                        String group    = feed_list_adapter.get_info(pos);
                        int start       = group.indexOf('\n') + 1;
                        int end         = group.indexOf(' ');
                        group           = group.substring(start, end);

                        String name     = feed_list_adapter.getItem(pos);
                        String ex_path  = storage + g_dir + group + sep + name;
                        String all_path = storage + g_dir + all + sep + all;

                        util.rmdir(new File(ex_path));
                        /* make the image and thumnail folders. */
                        (new File(ex_path + sep + main.IMAGE_DIR)).mkdir();
                        (new File(ex_path + sep + main.THUMBNAIL_DIR)).mkdir();

                        /* Delete the all content files. */
                        util.rm(all_path + main.CONTENT);
                        util.rm(all_path + main.COUNT);

                        /* Refresh pages and update groups and stuff. */
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
         add_edit_dialog.show_add_dialog(main.cgroups, main.con);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   static class refresh extends AsyncTask<Void, String[], Void>
   {
      Animation fade_in = AnimationUtils.loadAnimation(main.con, android.R.anim.fade_in);
      ListView listview;

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
            String storage = util.get_storage();
            String sep     = main.SEPAR;
            String g_dir   = main.GROUPS_DIR;
            String all     = main.cgroups[0];
            String path = storage + g_dir + all + sep + all + main.TXT;

            /* Read the all group file for names, urls, and groups. */
            String[][] content  = read.csv(path, 'n', 'u', 'g');
            int size            = content[0].length;
            String[] info_array = new String[size];

            for(int i = 0; i < size; i++)
            {
               /* Form the path to the feed_content file. */
               path = storage + g_dir + content[2][i] + sep + content[0][i]
                      + sep + content[0][i] + main.CONTENT;

               /* Build the info string. */
               info_array[i] = content[1][i] + main.NL + content[2][i] + " • "
                               + read.count(path) + " items";
            }
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
         listview.setAnimation(fade_in);
         listview.setVisibility(View.VISIBLE);
      }
   }
}
