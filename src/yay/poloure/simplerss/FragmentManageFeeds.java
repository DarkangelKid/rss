package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
      getListView().setOnItemLongClickListener(
            new FeedItemLongClick((AdapterManageFeeds) getListAdapter()));
      Update.manageFeeds(getListView(), getListAdapter());
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

   private static
   class FeedDeleteClick implements DialogInterface.OnClickListener
   {
      private final AdapterManageFeeds m_adapter;

      FeedDeleteClick(AdapterManageFeeds adpt)
      {
         m_adapter = adpt;
      }

      /* Delete the feed. */
      @Override
      public
      void onClick(DialogInterface dialog, int position)
      {
         String feed = m_adapter.getItem(position);
         /* Delete the feed's folder. */
         Util.rmdir(new File(Util.getStorage() + Util.getPath(feed, "")));

         /* Remove the feed from the index file. */
         Write.removeLine(Constants.INDEX, feed, true);

         Util.updateTags();
         removeItem(position);
         m_adapter.notifyDataSetChanged();

         /*Update.manageTags();*/
      }

      void removeItem(int position)
      {
         m_adapter.m_titleArray = Util.arrayRemove(m_adapter.m_infoArray, position);
         m_adapter.m_infoArray = Util.arrayRemove(m_adapter.m_infoArray, position);
      }
   }

   static private
   class FeedClearCacheClick implements DialogInterface.OnClickListener
   {
      private final AdapterManageFeeds m_adapterManageFeeds;

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
/* make the image and thumbnail folders. */
         Util.mkdir(path + Constants.IMAGE_DIR);
         Util.mkdir(path + Constants.THUMBNAIL_DIR);

/* Refresh pages and Update tags and stuff. */
         Util.updateTags();
         // TODO Update.manageFeeds();
         // TODO Update.manageTags();
      }
   }

   static private
   class FeedItemLongClick implements OnItemLongClickListener
   {
      private final AlertDialog.Builder m_build;

      private
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
