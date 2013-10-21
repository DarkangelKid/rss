package com.poloure.simplerss;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

class FragmentManageFeeds extends ListFragment
{
   static final int MODE_ADD = -1;

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

      ListView navigationList = (ListView) ((Activity) context).findViewById(R.id.left_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      showEditDialog(navigationAdapter, context, position);
   }

   static
   void showEditDialog(BaseAdapter navigationAdapter, Context context, int position)
   {
      LayoutInflater inf = LayoutInflater.from(context);
      View dialogLayout = inf.inflate(R.layout.add_rss_dialog, null);
      DialogInterface.OnClickListener onAddEdit;

      String titleText;
      String negativeButtonText;
      String positiveButtonText;

      /* If the mode is edit. */
      if(MODE_ADD == position)
      {
         titleText = context.getString(R.string.add_dialog_title);
         negativeButtonText = context.getString(R.string.cancel_dialog);
         positiveButtonText = context.getString(R.string.add_dialog);

         onAddEdit = new OnDialogClickAdd(dialogLayout, navigationAdapter, context);
      }
      else
      {
         String[][] content = Read.indexFile(context);
         String title = content[0][position];
         String url = content[1][position];
         String tag = content[2][position];

         ((TextView) dialogLayout.findViewById(R.id.feed_url_edit)).setText(url);
         ((TextView) dialogLayout.findViewById(R.id.name_edit)).setText(title);
         ((TextView) dialogLayout.findViewById(R.id.tag_edit)).setText(tag);

         titleText = context.getString(R.string.edit_dialog_title);
         negativeButtonText = context.getString(R.string.cancel_dialog);
         positiveButtonText = context.getString(R.string.accept_dialog);

         onAddEdit = new OnDialogClickEdit(dialogLayout, title, navigationAdapter, context);
      }

      /* Create the button click listeners. */
      DialogInterface.OnClickListener onCancel = new OnDialogClickCancel();

      /* Build the AlertDialog. */
      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(titleText);
      build.setView(dialogLayout);
      build.setCancelable(true);
      build.setNegativeButton(negativeButtonText, onCancel);
      build.setPositiveButton(positiveButtonText, onAddEdit);
      build.show();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      Context context = getActivity();
      ListView listView = getListView();

      ListView navigationList = (ListView) ((Activity) context).findViewById(R.id.left_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationList.getAdapter();

      ListAdapter listAdapter = new AdapterManageFeeds(context);
      AdapterView.OnItemLongClickListener onItemLongClick = new OnLongClickManageFeedItem(this,
            (BaseAdapter) listAdapter, navigationAdapter);

      setListAdapter(listAdapter);
      listView.setOnItemLongClickListener(onItemLongClick);
      manageFeeds(listView, listAdapter);

      ActionBarActivity activity = (ActionBarActivity) getActivity();

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

   private
   void manageFeeds(ListView listView, ListAdapter listAdapter)
   {
      Context context = getActivity();
      AsyncTask<Void, String[], Void> task = new AsyncManageFeedsRefresh(listView,
            (BaseAdapter) listAdapter, context);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
   }

}
