package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import java.io.File;
import java.util.Arrays;

class FragmentManageFeeds extends ListFragment
{
   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
      setRetainInstance(false);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView feed_list = getListView();
      feed_list.setOnItemClickListener(new FeedClick());

      AdapterManageFeeds adpt = new AdapterManageFeeds();
      setListAdapter(adpt);

      Update.manageFeeds();

      AlertDialog.Builder build = new AlertDialog.Builder(FeedsActivity.con);

      build.setCancelable(true)
            .setNegativeButton(getString(R.string.delete_dialog), new FeedDeleteClick(adpt))
            .setPositiveButton(getString(R.string.clear_dialog), new FeedClearCacheClick(adpt));

      feed_list.setOnItemLongClickListener(new FeedItemLongClick(build));
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.DRAWER_TOGGLE.onOptionsItemSelected(item))
      {
         return true;
      }
      if("add".equals(item.getTitle()))
      {
         FeedDialog.showAddDialog(FeedsActivity.ctags);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public
   View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
   {
      return inf.inflate(R.layout.listview_cards, cont, false);
   }

   static
   class ManageRefresh extends AsyncTask<Void, String[], Void>
   {
      final Animation          fade_in  = AnimationUtils.loadAnimation(FeedsActivity.con,
            android.R.anim.fade_in);
      final ListView           listview = PagerAdapterManage.MANAGE_FRAGMENTS[1].getListView();
      final AdapterManageFeeds adapter
                                        = (AdapterManageFeeds) PagerAdapterManage
            .MANAGE_FRAGMENTS[1]
            .getListAdapter();

      ManageRefresh()
      {
         if(0 == adapter.getCount())
         {
            listview.setVisibility(View.INVISIBLE);
         }
      }

      @Override
      protected
      Void doInBackground(Void... hey)
      {
         if(null != adapter)
         {
            /* Read the all m_imageViewTag file for names, urls, and tags. */
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
      protected
      void onPostExecute(Void tun)
      {
         listview.setAnimation(fade_in);
         listview.setVisibility(View.VISIBLE);
      }

      @Override
      protected
      void onProgressUpdate(String[][] progress)
      {
         adapter.setArrays(progress[0], progress[1]);
      }
   }

   static
   class FeedClick implements OnItemClickListener
   {
      static
      void showEditDialog(String[] ctags, Context con, final int position)
      {
         LayoutInflater inf = LayoutInflater.from(con);
         final View edit_rss_dialog = inf.inflate(R.layout.add_rss_dialog, null);
         String[][] content = Read.csv();
         final String current_title = content[0][position];
         String current_url = content[1][position];
         final String current_tag = content[2][position];

         final AdapterView<SpinnerAdapter> tag_spinner
               = (AdapterView<SpinnerAdapter>) edit_rss_dialog.findViewById(R.id.tag_spinner);

         String[] spinner_tags = Arrays.copyOfRange(ctags, 1, ctags.length);

         ArrayAdapter<String> adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
               spinner_tags);
         //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
         tag_spinner.setAdapter(adapter);
         Util.setText(current_url, edit_rss_dialog, R.id.URL_edit);
         Util.setText(current_title, edit_rss_dialog, R.id.URL_edit);

         tag_spinner.setSelection(Util.index(spinner_tags, current_tag));

         final AlertDialog.Builder build = new AlertDialog.Builder(con);
         build.setTitle(con.getString(R.string.edit_dialog_title))
               .setView(edit_rss_dialog)
               .setCancelable(true)
               .setNegativeButton(con.getString(R.string.cancel_dialog),
                     new DialogInterface.OnClickListener()
                     {
                        @Override
                        public
                        void onClick(DialogInterface dialog, int id)
                        {
                        }
                     });

         AlertDialog edit_feed_dialog = build.create();
         edit_feed_dialog.setButton(DialogInterface.BUTTON_POSITIVE,
               con.getString(R.string.accept_dialog),
               new FeedDialog.addFeedClick(edit_rss_dialog, tag_spinner, edit_feed_dialog,
                     current_title, current_tag, position));

         edit_feed_dialog.show();
      }

      @Override
      public
      void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         showEditDialog(FeedsActivity.ctags, Util.getContext(), position);
      }
   }

   private static
   class FeedDeleteClick implements DialogInterface.OnClickListener
   {
      private final AdapterManageFeeds adpt;

      public
      FeedDeleteClick(AdapterManageFeeds adpt)
      {
         this.adpt = adpt;
      }

      static
      void deleteFeed(String feed, int pos)
      {
         /* Delete the feed's folder. */
         Util.rmdir(new File(Util.getStorage() + Util.getPath(feed, "")));

         /* Remove the feed from the index file. */
         Write.removeLine(FeedsActivity.INDEX, feed, true);

         Util.updateTags();
         AdapterManageFeeds.removeItem(pos);

         Update.manageTags();
      }

      /* Delete the feed. */
      @Override
      public
      void onClick(DialogInterface dialog, int id_no)
      {
         deleteFeed(adpt.getItem(id_no), id_no);
      }
   }

   static
   class FeedClearCacheClick implements DialogInterface.OnClickListener
   {
      private final AdapterManageFeeds adpt;

      public
      FeedClearCacheClick(AdapterManageFeeds adpt)
      {
         this.adpt = adpt;
      }

      /// Delete the cache.
      @Override
      public
      void onClick(DialogInterface dialog, int id_no)
      {
         String name = adpt.getItem(id_no);
         String feed_path = Util.getPath(name, "");

         Util.rmdir(new File(feed_path));
/* make the AsyncLoadImage and thumnail folders. */
         Util.mkdir(feed_path + FeedsActivity.IMAGE_DIR);
         Util.mkdir(feed_path + FeedsActivity.THUMBNAIL_DIR);

/* Refresh pages and Update tags and stuff. */
         Util.updateTags();
         Update.manageFeeds();
         Update.manageTags();
      }
   }

   static
   class FeedItemLongClick implements OnItemLongClickListener
   {
      AlertDialog.Builder m_build;

      FeedItemLongClick(AlertDialog.Builder build)
      {
         m_build = build;
      }

      @Override
      public
      boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
      {
         m_build.show();
         return true;
      }
   }
}
