package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

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

      String[][] content = Read.csv(context);
      String title = content[0][position];
      String url = content[1][position];
      String tag = content[2][position];

      LayoutInflater inflater = LayoutInflater.from(context);
      View editRssLayout = inflater.inflate(R.layout.add_rss_dialog, null);

      AdapterView<SpinnerAdapter> spinnerTag
            = (AdapterView<SpinnerAdapter>) editRssLayout.findViewById(R.id.tag_spinner);

      String[] currentTags = Read.file(Constants.TAG_LIST, context);
      String[] spinnerTags = Arrays.copyOfRange(currentTags, 1, currentTags.length);

      SpinnerAdapter adapter = new ArrayAdapter<String>(context, R.layout.group_spinner_text,
            spinnerTags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      spinnerTag.setAdapter(adapter);
      Util.setText(url, editRssLayout, R.id.URL_edit);
      Util.setText(title, editRssLayout, R.id.name_edit);

      int tagIndex = Util.index(spinnerTags, tag);
      spinnerTag.setSelection(tagIndex);

      String editTitle = context.getString(R.string.edit_dialog_title);
      String cancelText = context.getString(R.string.cancel_dialog);
      String acceptText = context.getString(R.string.accept_dialog);

      DialogInterface.OnClickListener onDialogClickCancel = new OnDialogClickCancel();
      DialogInterface.OnClickListener onDialogClickEdit = new OnDialogClickEdit(editRssLayout,
            spinnerTag, title, m_navigationAdapter, context);

      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(editTitle);
      build.setView(editRssLayout);
      build.setCancelable(true);
      build.setNegativeButton(cancelText, onDialogClickCancel);
      build.setPositiveButton(acceptText, onDialogClickEdit);
      build.show();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      Context context = getActivity();
      ListView listView = getListView();
      ListAdapter listAdapter = new AdapterManageFeeds(context);
      AdapterView.OnItemLongClickListener onItemLongClick = new OnLongClickManageFeedItem(this,
            (BaseAdapter) listAdapter, m_navigationAdapter);

      setListAdapter(listAdapter);
      listView.setOnItemLongClickListener(onItemLongClick);
      manageFeeds(listView, listAdapter);
   }

   private
   void manageFeeds(ListView listView, ListAdapter listAdapter)
   {
      Context context = getActivity();
      AsyncManageFeedsRefresh task = new AsyncManageFeedsRefresh(listView, (BaseAdapter) listAdapter, context);
      if(Constants.HONEYCOMB)
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
      if(activity.onOptionsItemSelected(item))
      {
         return true;
      }

      String addFeed = activity.getString(R.string.add_feed);
      CharSequence menuTitle = item.getTitle();

      if(addFeed.equals(menuTitle))
      {
         FeedDialog.showAddDialog(m_navigationAdapter, activity);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

}
