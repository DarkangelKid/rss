package com.poloure.simplerss;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

/* Must be public for rotation. */
public
class FragmentManage extends Fragment
{
   static final int LIST_VIEW_MANAGE = 5634126;

   static
   Fragment newInstance()
   {
      return new FragmentManage();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      final Activity activity = (Activity) container.getContext();

      final String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      BaseAdapter baseAdapter = new AdapterManageFragments(activity);

      ListView listView = new ListView(activity);
      listView.setAdapter(baseAdapter);
      listView.setId(LIST_VIEW_MANAGE);

      /* Set the onItemClickListener that makes the EditDialog show. */
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
      {
         @Override
         public
         void onItemClick(AdapterView<?> parent, View view, int position, long id)
         {
            Dialog dialog = DialogEditFeed.newInstance(activity, position, applicationFolder);
            dialog.show();
         }
      });

      /* Make an alertDialog for the long click of a list item. */
      AlertDialog.Builder build = new AlertDialog.Builder(activity);

      /* Get the items that the onClick listener needs to refresh when deleting/clearing a feed. */
      ViewPager feedPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      PagerAdapterFeeds pagerAdapterFeeds = (PagerAdapterFeeds) feedPager.getAdapter();

      ListView navigationDrawer = (ListView) activity.findViewById(R.id.navigation_drawer);
      BaseAdapter navigationAdapter = (BaseAdapter) navigationDrawer.getAdapter();

      listView.setOnItemLongClickListener(
            new OnLongClickManageFeedItem(listView, pagerAdapterFeeds, navigationAdapter, build,
                  applicationFolder));

      /* Set the background to white.*/
      listView.setBackgroundColor(Color.WHITE);

      /* Create a slight grey divider. */
      listView.setDivider(new ColorDrawable(Color.argb(255, 237, 237, 237)));
      listView.setDividerHeight(2);

      AsyncManage.newInstance(baseAdapter, applicationFolder);

      return listView;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      MenuItem refreshMenu = menu.findItem(R.id.refresh);
      MenuItem unreadMenu = menu.findItem(R.id.unread);
      MenuItem addFeedMenu = menu.findItem(R.id.add_feed);

      if(null != refreshMenu && null != unreadMenu && null != addFeedMenu)
      {
         refreshMenu.setVisible(false);
         unreadMenu.setVisible(false);
         addFeedMenu.setVisible(true);
      }
   }
}
