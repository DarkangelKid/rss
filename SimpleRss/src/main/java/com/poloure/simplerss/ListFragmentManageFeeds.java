package com.poloure.simplerss;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/* Must be public for rotation. */
public
class ListFragmentManageFeeds extends ListFragment
{
   private static final int MODE_ADD = -1;

   static
   ListFragment newInstance()
   {
      return new ListFragmentManageFeeds();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview, container, false);
   }

   /* Edit the feed. */
   @Override
   public
   void onListItemClick(ListView l, View v, int position, long id)
   {
      super.onListItemClick(l, v, position, id);
      Activity activity = getActivity();
      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      showEditDialog(activity, position, applicationFolder);
   }

   static
   void showEditDialog(Activity activity, int position, String applicationFolder)
   {
      boolean modeEdit = MODE_ADD != position;

      Dialog editDialog = DialogEditFeed.newInstance(activity, position, applicationFolder);
      editDialog.show();

      /* Get the text resources. */
      int titleResource = !modeEdit ? R.string.add_dialog_title : R.string.edit_dialog_title;
      String titleText = activity.getString(titleResource);
      editDialog.setTitle(titleText);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      Activity activity = getActivity();
      ListView listView = getListView();
      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      ListAdapter listAdapter = new AdapterManageFragments(activity);

      /* Make an alertDialog for the long click of a list item. */
      AlertDialog.Builder build = new AlertDialog.Builder(activity);

      /* Get the items that the onClick listener needs to refresh when deleting/clearing a feed. */
      ViewPager feedPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      PagerAdapterFeeds pagerAdapterFeeds = (PagerAdapterFeeds) feedPager.getAdapter();

      ListView navigationDrawer = (ListView) activity.findViewById(R.id.navigation_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationDrawer.getAdapter();

      setListAdapter(listAdapter);
      listView.setOnItemLongClickListener(
            new OnLongClickManageFeedItem(listView, pagerAdapterFeeds, navigationAdapter, build,
                  applicationFolder));
      AsyncManageFeedsRefresh.newInstance(listView, applicationFolder);
   }
}
