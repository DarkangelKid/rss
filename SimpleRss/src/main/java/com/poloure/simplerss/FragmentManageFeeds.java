package com.poloure.simplerss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/* Must be public for rotation. */
public
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

      showEditDialog(l, position, applicationFolder, context);
   }

   static
   void showEditDialog(ListView listView, int position, String applicationFolder, Context context)
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

      Dialog editDialog = EditDialog.newInstance(context, listView, oldFeedTitle, applicationFolder,
            allTag);
      editDialog.show();

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
      editDialog.setTitle(titleText);
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

      /* Get the items that the onClick listener needs to refresh when deleting/clearing a feed. */
      ViewPager feedPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      FragmentPagerAdapter pagerAdapterFeeds = (FragmentPagerAdapter) feedPager.getAdapter();

      ListView navigationDrawer = (ListView) activity.findViewById(R.id.navigation_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationDrawer.getAdapter();

      String allTag = activity.getString(R.string.all_tag);

      setListAdapter(listAdapter);
      listView.setOnItemLongClickListener(
            new OnLongClickManageFeedItem(listView, pagerAdapterFeeds, navigationAdapter, build,
                  applicationFolder, allTag));
      AsyncManageFeedsRefresh.newInstance(listView, applicationFolder);
   }
}
