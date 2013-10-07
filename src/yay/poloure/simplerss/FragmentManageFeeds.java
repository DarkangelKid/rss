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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import java.io.File;
import java.util.Arrays;

class FragmentManageFeeds extends ListFragment
{
   private final BaseAdapter m_navigationAdapter;

   FragmentManageFeeds(BaseAdapter navigationAdapter)
   {
      m_navigationAdapter = navigationAdapter;
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      return inflater.inflate(R.layout.listview_cards, container, false);
   }

   /* Edit the feed. */
   @Override
   public
   void onListItemClick(ListView l, View v, int position, long id)
   {
      super.onListItemClick(l, v, position, id);
      Context con = Util.getContext();
      LayoutInflater inf = LayoutInflater.from(con);
      View editRssLayout = inf.inflate(R.layout.add_rss_dialog, null);
      String[][] content = Read.csv();
      String title = content[0][position];
      String url = content[1][position];
      String tag = content[2][position];

      AdapterView<SpinnerAdapter> spinnerTag
            = (AdapterView<SpinnerAdapter>) editRssLayout.findViewById(R.id.tag_spinner);

      String[] currentTags = Read.file(Constants.TAG_LIST);
      String[] spinnerTags = Arrays.copyOfRange(currentTags, 1, currentTags.length);

      SpinnerAdapter adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
            spinnerTags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      spinnerTag.setAdapter(adapter);
      Util.setText(url, editRssLayout, R.id.URL_edit);
      Util.setText(title, editRssLayout, R.id.name_edit);

      spinnerTag.setSelection(Util.index(spinnerTags, tag));

      AlertDialog.Builder build = new AlertDialog.Builder(con);
      build.setTitle(con.getString(R.string.edit_dialog_title))
            .setView(editRssLayout)
            .setCancelable(true)
            .setNegativeButton(con.getString(R.string.cancel_dialog), new OnDialogClickCancel());

      AlertDialog editFeedDialog = build.create();
      editFeedDialog.setButton(DialogInterface.BUTTON_POSITIVE,
            con.getString(R.string.accept_dialog),
            new OnDialogClickEdit(editRssLayout, spinnerTag, title));

      editFeedDialog.show();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      setListAdapter(new AdapterManageFeeds());
      getListView().setOnItemLongClickListener(new FeedItemLongClick(getListAdapter()));
      manageFeeds(getListView(), getListAdapter());
   }

   private static
   void manageFeeds(ListView listView, ListAdapter listAdapter)
   {
      if(Constants.HONEYCOMB)
      {
         new AsyncManageFeedsRefresh(listView, listAdapter).executeOnExecutor(
               AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         new AsyncManageFeedsRefresh(listView, listAdapter).execute();
      }
   }

   /* Add a new feed. */
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
         FeedDialog.showAddDialog();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   private
   class FeedItemLongClick implements OnItemLongClickListener
   {
      private final AlertDialog.Builder m_build;

      private
      FeedItemLongClick(Adapter p_adapter)
      {
         m_build = new AlertDialog.Builder(Util.getContext());

         m_build.setCancelable(true)
               .setNegativeButton(Util.getString(R.string.delete_dialog),
                     new FeedDeleteClick(p_adapter))
               .setPositiveButton(Util.getString(R.string.clear_dialog),
                     new FeedClearCacheClick(p_adapter));
      }

      @Override
      public
      boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
      {
         m_build.show();
         return true;
      }
   }

   private
   class FeedClearCacheClick implements DialogInterface.OnClickListener
   {
      private final Adapter m_adapter;

      FeedClearCacheClick(Adapter p_adapter)
      {
         m_adapter = p_adapter;
      }

      /// Delete the cache.
      @Override
      public
      void onClick(DialogInterface dialog, int position)
      {
         String feedName = (String) m_adapter.getItem(position);
         String path = Util.getPath(feedName, "");

         Util.rmdir(new File(path));

         /* Refresh pages and navigation counts. */
         Update.navigation(m_navigationAdapter);
         // TODO Update.manageFeeds();
         // TODO Update.manageTags();
      }
   }

   private
   class FeedDeleteClick implements DialogInterface.OnClickListener
   {
      private final Adapter m_adapter;

      FeedDeleteClick(Adapter p_adapter)
      {
         m_adapter = p_adapter;
      }

      /* Delete the feed. */
      @Override
      public
      void onClick(DialogInterface dialog, int position)
      {
         String feed = (String) m_adapter.getItem(position);
         /* Delete the feed's folder. */
         Util.rmdir(new File(Util.getStorage() + Util.getPath(feed, "")));

         /* Remove the feed from the index file. */
         Write.removeLine(Constants.INDEX, feed, true);

         Util.updateTags(m_navigationAdapter);
         ((AdapterManageFeeds) m_adapter).removeItem(position);
         ((BaseAdapter) m_adapter).notifyDataSetChanged();

         /*Update.manageTags();*/
      }
   }
}
