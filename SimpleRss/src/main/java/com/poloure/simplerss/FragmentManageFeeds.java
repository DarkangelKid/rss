package com.poloure.simplerss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

class FragmentManageFeeds extends ListFragment
{
   private static final int MODE_ADD = -1;

   static
   ListFragment newInstance()
   {
      return new FragmentManageFeeds();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_cards, container, false);
   }

   /* Edit the feed. */
   @Override
   public
   void onListItemClick(ListView l, View v, int position, long id)
   {
      super.onListItemClick(l, v, position, id);
      Context context = getActivity();
      String applicationFolder = FeedsActivity.getApplicationFolder(context);

      showEditDialog(position, applicationFolder, context);
   }

   static
   void showEditDialog(int position, String applicationFolder, Context context)
   {
      String allTag = context.getString(R.string.all_tag);
      String oldFeedTitle = "";
      String url = "";
      String tag = "";

      if(MODE_ADD != position)
      {
         String[][] content = Read.indexFile(applicationFolder);
         oldFeedTitle = content[0][position];
         url = content[1][position];
         tag = content[2][position];
      }

      Dialog editDialog = EditDialog.newInstance(context, oldFeedTitle, applicationFolder, allTag);

      /* If the mode is edit. */
      if(MODE_ADD != position)
      {
         ((TextView) editDialog.findViewById(R.id.feed_url_edit)).setText(url);
         ((TextView) editDialog.findViewById(R.id.name_edit)).setText(oldFeedTitle);
         ((TextView) editDialog.findViewById(R.id.tag_edit)).setText(tag);
      }

      /* Get the text resources. */
      int titleResource = MODE_ADD == position
            ? R.string.add_dialog_title
            : R.string.edit_dialog_title;

      String titleText = context.getString(titleResource);
      //TODO ((TextView) editDialog.findViewById(R.id.title_text)).setText(titleText);

      editDialog.show();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ActionBarActivity activity = (ActionBarActivity) getActivity();
      ListView listView = getListView();
      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      ListAdapter listAdapter = new AdapterManageFeeds(activity);

      /* Make an alertDialog for the long click of a list item. */
      AlertDialog.Builder build = new AlertDialog.Builder(activity);

      setListAdapter(listAdapter);
      listView.setOnItemLongClickListener(new OnLongClickManageFeedItem(build, applicationFolder));
      AsyncManageFeedsRefresh.newInstance(listView, applicationFolder);

      Resources resources = activity.getResources();
      String[] manageTitles = resources.getStringArray(R.array.manage_titles);

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(manageTitles[0]);
   }

   /* Add a new feed. */
   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      FragmentActivity activity = getActivity();
      return activity.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
   }
}
