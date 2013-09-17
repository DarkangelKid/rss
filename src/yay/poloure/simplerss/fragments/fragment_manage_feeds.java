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
               add_edit_dialog.show_edit_dialog(main.ctags, main.con, position);
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
                        String name        = adpt.getItem(pos);
                        String feed_path   = util.get_path(name, "");

                        util.rmdir(new File(feed_path));
                        /* make the image and thumnail folders. */
                        util.mkdir(feed_path + main.IMAGE_DIR);
                        util.mkdir(feed_path + main.THUMBNAIL_DIR);

                        /* Refresh pages and update tags and stuff. */
                        util.update_tags();
                        update.manage_feeds();
                        update.manage_tags();
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
         add_edit_dialog.show_add_dialog(main.ctags);
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
            /* Read the all tag file for names, urls, and tags. */
            String[][] content  = read.csv(main.INDEX, 'n', 'u', 'g');
            int size            = content[0].length;
            String[] info_array = new String[size];

            for(int i = 0; i < size; i++)
            {
               /* Form the path to the feed_content file. */
               String path = util.get_path(content[0][i], main.CONTENT);

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
