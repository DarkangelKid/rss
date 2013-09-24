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
      setListAdapter(new AdapterManageFeeds());
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.s_drawerToggle.onOptionsItemSelected(item))
      {
         return true;
      }
      if(Util.getString(R.string.add_feed).equals(item.getTitle()))
      {
         FeedDialog.showAddDialog(FeedsActivity.s_currentTags);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   /* onListItemClick */

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      /*feedList.setOnItemClickListener(new FeedClick());
      feedList.setOnItemLongClickListener(new FeedItemLongClick(adapterManageFeeds));*/

      // Update.manageFeeds();
      return inflater.inflate(R.layout.listview_cards, container, false);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      new ManageRefresh().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
   }

   class ManageRefresh extends AsyncTask<Void, String[], Void>
   {
      final Animation          fade_in  = AnimationUtils.loadAnimation(Util.getContext(),
            android.R.anim.fade_in);

      ManageRefresh()
      {
         if(0 == getListAdapter().getCount())
         {
           /* feedList.setVisibility(View.INVISIBLE);*/
         }
      }

      @Override
      protected
      Void doInBackground(Void... hey)
      {
         if(null != getListAdapter())
         {
            /* Read the ALL_TAG m_imageViewTag file for names, urls, and tags. */
            String[][] content = Read.csv();
            int size = content[0].length;
            String[] infoArray = new String[size];

            for(int i = 0; i < size; i++)
            {
               /* Form the path to the feed_content file. */
               String path = Util.getPath(content[0][i], Constants.CONTENT);

               /* Build the info string. */
               infoArray[i] = content[1][i] + Constants.NL + content[2][i] + " • " +
                     Read.count(path) +
                     " items";
            }
            publishProgress(content[0], infoArray);
         }
         return null;
      }

      @Override
      protected
      void onPostExecute(Void result)
      {
         Write.log("Setting visibility.");
         getListView().setAnimation(fade_in);
         getListView().setVisibility(View.VISIBLE);
      }

      @Override
      protected
      void onProgressUpdate(String[]... values)
      {
         ((AdapterManageFeeds) getListAdapter()).setArrays(values[0], values[1]);
         ((AdapterManageFeeds) getListAdapter()).notifyDataSetChanged();
      }
   }

   static
   class FeedClick implements OnItemClickListener
   {
      static
      void showEditDialog(String[] ctags, Context con, int position)
      {
         LayoutInflater inf = LayoutInflater.from(con);
         View editRssLayout = inf.inflate(R.layout.add_rss_dialog, null);
         String[][] content = Read.csv();
         String title = content[0][position];
         String url = content[1][position];
         String tag = content[2][position];

         AdapterView<SpinnerAdapter> spinnerTag
               = (AdapterView<SpinnerAdapter>) editRssLayout.findViewById(R.id.tag_spinner);

         String[] spinnerTags = Arrays.copyOfRange(ctags, 1, ctags.length);

         SpinnerAdapter adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
               spinnerTags);
         //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
         spinnerTag.setAdapter(adapter);
         Util.setText(url, editRssLayout, R.id.URL_edit);
         Util.setText(title, editRssLayout, R.id.URL_edit);

         spinnerTag.setSelection(Util.index(spinnerTags, tag));

         AlertDialog.Builder build = new AlertDialog.Builder(con);
         build.setTitle(con.getString(R.string.edit_dialog_title))
               .setView(editRssLayout)
               .setCancelable(true)
               .setNegativeButton(con.getString(R.string.cancel_dialog), new OnDialogClickCancel());

         AlertDialog editFeedDialog = build.create();
         editFeedDialog.setButton(DialogInterface.BUTTON_POSITIVE,
               con.getString(R.string.accept_dialog),
               new OnDialogClickEdit(editRssLayout, spinnerTag, editFeedDialog, title, position));

         editFeedDialog.show();
      }

      @Override
      public
      void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         showEditDialog(FeedsActivity.s_currentTags, Util.getContext(), position);
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
         Write.removeLine(Constants.INDEX, feed, true);

         Util.updateTags();
         removeItem(pos);

         Update.manageTags();
      }

      static
      void removeItem(int position)
      {
        /* AdapterManageFeeds.s_titleArray = Util.arrayRemove(AdapterManageFeeds.s_titleArray,
               position);
         AdapterManageFeeds.s_infoArray = Util.arrayRemove(AdapterManageFeeds.s_infoArray,
               position);*/
      }

      /* Delete the feed. */
      @Override
      public
      void onClick(DialogInterface dialog, int position)
      {
         deleteFeed(adpt.getItem(position), position);
      }
   }

   static
   class FeedClearCacheClick implements DialogInterface.OnClickListener
   {
      private final AdapterManageFeeds m_adapterManageFeeds;

      public
      FeedClearCacheClick(AdapterManageFeeds adapterManageFeeds)
      {
         m_adapterManageFeeds = adapterManageFeeds;
      }

      /// Delete the cache.
      @Override
      public
      void onClick(DialogInterface dialog, int position)
      {
         String feedName = m_adapterManageFeeds.getItem(position);
         String path = Util.getPath(feedName, "");

         Util.rmdir(new File(path));
/* make the AsyncLoadImage and thumnail folders. */
         Util.mkdir(path + Constants.IMAGE_DIR);
         Util.mkdir(path + Constants.THUMBNAIL_DIR);

/* Refresh pages and Update tags and stuff. */
         Util.updateTags();
         Update.manageFeeds();
         Update.manageTags();
      }
   }

   static
   class FeedItemLongClick implements OnItemLongClickListener
   {
      private AlertDialog.Builder m_build;

      FeedItemLongClick(AdapterManageFeeds adapterManageFeeds)
      {
         m_build = new AlertDialog.Builder(Util.getContext());

         m_build.setCancelable(true)
               .setNegativeButton(Util.getString(R.string.delete_dialog),
                     new FeedDeleteClick(adapterManageFeeds))
               .setPositiveButton(Util.getString(R.string.clear_dialog),
                     new FeedClearCacheClick(adapterManageFeeds));
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
