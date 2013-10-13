package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;

class FragmentManageFeeds extends ListFragment
{
   private final BaseAdapter m_navigationAdapter;
   static final int MODE_ADD = -1;
   private final FragmentManager m_fragmentManager;

   FragmentManageFeeds(BaseAdapter navigationAdapter, FragmentManager fragmentManager)
   {
      m_navigationAdapter = navigationAdapter;
      m_fragmentManager = fragmentManager;
   }

   static
   void showEditDialog(BaseAdapter navigationAdapter, FragmentManager fragmentManager,
         Context context, int position)
   {
      LayoutInflater inf = LayoutInflater.from(context);
      View dialogLayout = inf.inflate(R.layout.add_rss_dialog, null);

      String[] currentTags = Read.file(Constants.TAG_LIST, context);

      AdapterView<SpinnerAdapter> spinnerTag
            = (AdapterView<SpinnerAdapter>) dialogLayout.findViewById(R.id.tag_spinner);

      SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(context, R.layout.group_spinner_text,
            currentTags);
      spinnerTag.setAdapter(spinnerAdapter);

      String titleText;
      String negativeButtonText;
      String positiveButtonText;

      /* If the mode is edit. */
      if(MODE_ADD != position)
      {
         String[][] content = Read.csv(context);
         String title = content[0][position];
         String url = content[1][position];
         String tag = content[2][position];

         ((TextView) dialogLayout.findViewById(R.id.URL_edit)).setText(url);
         ((TextView) dialogLayout.findViewById(R.id.name_edit)).setText(title);

         int tagIndex = Util.index(currentTags, tag);
         spinnerTag.setSelection(tagIndex);

         titleText = context.getString(R.string.edit_dialog_title);
         negativeButtonText = context.getString(R.string.cancel_dialog);
         positiveButtonText = context.getString(R.string.accept_dialog);
      }
      else
      {
         titleText = context.getString(R.string.add_dialog_title);
         negativeButtonText = context.getString(R.string.cancel_dialog);
         positiveButtonText = context.getString(R.string.add_dialog);
      }

      /* Create the button click listeners. */
      DialogInterface.OnClickListener onCancel = new OnDialogClickCancel();
      DialogInterface.OnClickListener onAdd = new OnDialogClickAdd(dialogLayout, spinnerTag,
            navigationAdapter, fragmentManager, context);

      /* Build the AlertDialog. */
      AlertDialog.Builder build = new AlertDialog.Builder(context);
      build.setTitle(titleText);
      build.setView(dialogLayout);
      build.setCancelable(true);
      build.setNegativeButton(negativeButtonText, onCancel);
      build.setPositiveButton(positiveButtonText, onAdd);
      build.show();
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

      showEditDialog(m_navigationAdapter, m_fragmentManager, context, position);
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
      AsyncManageFeedsRefresh task = new AsyncManageFeedsRefresh(listView,
            (BaseAdapter) listAdapter, context);
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
      return super.onOptionsItemSelected(item);
   }

}
