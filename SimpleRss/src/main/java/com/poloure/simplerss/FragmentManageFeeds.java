package com.poloure.simplerss;

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

      showEditDialog(context, position);
   }

   static
   void showEditDialog(Context context, int position)
   {
      LayoutInflater inf = LayoutInflater.from(context);
      View dialogLayout = inf.inflate(R.layout.add_rss_dialog, null);
      DialogInterface.OnClickListener onAddEdit;

      int titleResource = MODE_ADD == position
            ? R.string.add_dialog_title
            : R.string.edit_dialog_title;
      String titleText = context.getString(titleResource);

      String negativeButtonText = context.getString(R.string.cancel_dialog);
      String positiveButtonText = context.getString(R.string.add_dialog);

      /* If the mode is edit. */
      if(MODE_ADD == position)
      {
         onAddEdit = new OnDialogClickAdd(context);
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

         onAddEdit = new OnDialogClickEdit(title, context);
      }

      /* Create the button click listeners. */
      DialogInterface.OnClickListener onCancel = new OnDialogClickCancel();

      /* Build the AlertDialog. */
      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(titleText);
      build.setView(dialogLayout);
      build.setNegativeButton(negativeButtonText, onCancel);
      build.setPositiveButton(positiveButtonText, onAddEdit);
      build.show();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ActionBarActivity activity = (ActionBarActivity) getActivity();
      ListView listView = getListView();

      ListAdapter listAdapter = new AdapterManageFeeds(activity);
      AdapterView.OnItemLongClickListener onItemLongClick = new OnLongClickManageFeedItem(activity,
            (BaseAdapter) listAdapter);

      setListAdapter(listAdapter);
      listView.setOnItemLongClickListener(onItemLongClick);
      asyncCompatManageFeedsRefresh(listView, activity);

      Resources resources = activity.getResources();
      String[] manageTitles = resources.getStringArray(R.array.manage_titles);

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(manageTitles[0]);
   }

   static
   void asyncCompatManageFeedsRefresh(ListView listView, Context context)
   {
      AsyncTask<Void, String[], Void> task = new AsyncManageFeedsRefresh(listView, context);
      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
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
