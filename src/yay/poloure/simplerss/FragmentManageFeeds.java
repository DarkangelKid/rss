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

import java.io.File;

class FragmentManageFeeds extends ListFragment
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
      feed_list.setOnItemClickListener(new FeedClick());

      final AdapterManageFeeds adpt = new AdapterManageFeeds();
      setListAdapter(adpt);

      Update.manageFeeds();

      feed_list.setOnItemLongClickListener(new OnItemLongClickListener()
      {
         @Override
         public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id)
         {
            new AlertDialog.Builder(FeedsActivity.con).setCancelable(true).setNegativeButton(
                  getString(R.string.delete_dialog), new DialogInterface.OnClickListener()
            {
               /* Delete the feed. */
               @Override
               public void onClick(DialogInterface dialog, int id_no)
               {
                  Util.deleteFeed(adpt.getItem(pos), pos);
               }
            }).setPositiveButton(getString(R.string.clear_dialog),
                                 new DialogInterface.OnClickListener()
                                 {
                                    /// Delete the cache.
                                    @Override
                                    public void onClick(DialogInterface dialog, int id_no)
                                    {
                                       String name = adpt.getItem(pos);
                                       String feed_path = Util.getPath(name, "");

                                       Util.rmdir(new File(feed_path));
                        /* make the Image and thumnail folders. */
                                       Util.mkdir(feed_path + FeedsActivity.IMAGE_DIR);
                                       Util.mkdir(feed_path + FeedsActivity.THUMBNAIL_DIR);

                        /* Refresh pages and Update tags and stuff. */
                                       Util.updateTags();
                                       Update.manageFeeds();
                                       Update.manageTags();
                                    }
                                 }).show();
            return true;
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.drawer_toggle.onOptionsItemSelected(item))
      {
         return true;
      }
      if(item.getTitle().equals("add"))
      {
         FeedDialog.showAddDialog(FeedsActivity.ctags);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   static class ManageRefresh extends AsyncTask<Void, String[], Void>
   {
      final Animation          fade_in  = AnimationUtils
            .loadAnimation(FeedsActivity.con, android.R.anim.fade_in);
      final ListView           listview = PagerAdapterManage.MANAGE_FRAGMENTS[1].getListView();
      final AdapterManageFeeds adapter
                                        = (AdapterManageFeeds) PagerAdapterManage
            .MANAGE_FRAGMENTS[1]
            .getListAdapter();

      public ManageRefresh()
      {
         if(0 == adapter.getCount())
         {
            listview.setVisibility(View.INVISIBLE);
         }
      }

      @Override
      protected Void doInBackground(Void... hey)
      {
         if(null != adapter)
         {
            /* Read the all tag file for names, urls, and tags. */
            String[][] content = Read.csv();
            int size = content[0].length;
            String[] info_array = new String[size];

            for(int i = 0; i < size; i++)
            {
               /* Form the path to the feed_content file. */
               String path = Util.getPath(content[0][i], FeedsActivity.CONTENT);

               /* Build the info string. */
               info_array[i] = content[1][i] + FeedsActivity.NL + content[2][i] + " • " +
                               Read.count(path) +
                               " items";
            }
            publishProgress(content[0], info_array);
         }
         return null;
      }

      @Override
      protected void onProgressUpdate(String[][] progress)
      {
         adapter.setArrays(progress[0], progress[1]);
      }

      @Override
      protected void onPostExecute(Void tun)
      {
         listview.setAnimation(fade_in);
         listview.setVisibility(View.VISIBLE);
      }
   }

   private static class FeedClick implements OnItemClickListener
   {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         FeedDialog.showEditDialog(FeedsActivity.ctags, FeedsActivity.con, position);
      }
   }
}
