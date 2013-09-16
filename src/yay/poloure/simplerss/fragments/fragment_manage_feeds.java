package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.widget.ListAdapter;

import java.io.File;

class fragment_manage_feeds extends ListFragment
{
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
      setRetainInstance(false);
   }

   @Override
   public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
   {
      return inf.inflate(R.layout.listview_cards, cont, false);
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView feed_list = getListView();
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

      final adapter_manage_feeds adpt = new adapter_manage_feeds();
      setListAdapter(adpt);

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
                  getString(R.string.delete_dialog),
                  new DialogInterface.OnClickListener()
                  {
                     /* Delete the feed. */
                     @Override
                     public void onClick(DialogInterface dialog, int id)
                     {
                        util.delete_feed(adpt.get_info(pos), adpt.getItem(pos), pos);
                     }
                  }
               )
               .setPositiveButton
               (
                  getString(R.string.clear_dialog),
                  new DialogInterface.OnClickListener()
                  {
                     /// Delete the cache.
                     @Override
                     public void onClick(DialogInterface dialog, int id)
                     {
                        /* Parse for the group name from the info string. */
                        String group    = adpt.get_info(pos);
                        int start       = group.indexOf('\n') + 1;
                        int end         = group.indexOf(' ');
                        group           = group.substring(start, end);

                        String name      = adpt.getItem(pos);
                        String feed_path = util.get_path(group, name, "");
                        String all_content = util.get_path(main.ALL, main.CONTENT);

                        util.rmdir(new File(feed_path));
                        /* make the image and thumnail folders. */
                        util.mkdir(feed_path + main.IMAGE_DIR);
                        util.mkdir(feed_path + main.THUMBNAIL_DIR);

                        /* Delete the all content files. */
                        util.rm(all_content);

                        /* Refresh pages and update groups and stuff. */
                        util.update_groups();
                        update.manage_feeds();
                        update.manage_groups();
                     }
                  }
               ).show();
               return true;
            }
         }
      );
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(navigation_drawer.drawer_toggle.onOptionsItemSelected(item))
         return true;
      else if(item.getTitle().equals("add"))
      {
         add_edit_dialog.show_add_dialog(main.cgroups);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   static class refresh extends AsyncTask<Void, String[], Void>
   {
      Animation fade_in = AnimationUtils.loadAnimation(main.con, android.R.anim.fade_in);
      ListView listview   = pageradapter_manage.fragments[1].getListView();
      adapter_manage_feeds adapter = (adapter_manage_feeds) pageradapter_manage.fragments[1].getListAdapter();

      public refresh()
      {
         if(adapter.getCount() == 0)
            listview.setVisibility(View.INVISIBLE);
      }

      @Override
      protected Void doInBackground(Void... hey)
      {
         if(adapter != null)
         {
            String path    = util.get_path(main.ALL, main.TXT);

            /* Read the all group file for names, urls, and groups. */
            String[][] content  = read.csv(path, 'n', 'u', 'g');
            int size            = content[0].length;
            String[] info_array = new String[size];

            for(int i = 0; i < size; i++)
            {
               /* Form the path to the feed_content file. */
               path = util.get_path(content[2][i], content[0][i], main.CONTENT);

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
         adapter.set_items(progress[0], progress[1]);
      }

      @Override
      protected void onPostExecute(Void tun)
      {
         listview.setAnimation(fade_in);
         listview.setVisibility(View.VISIBLE);
      }
   }
}
