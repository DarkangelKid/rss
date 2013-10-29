package com.poloure.simplerss;

import android.app.AlertDialog;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

class OnLongClickManageFeedItem implements AdapterView.OnItemLongClickListener
{
   private final AlertDialog.Builder  m_builder;
   private final String               m_applicationFolder;
   private final String               m_allTag;
   private final FragmentPagerAdapter m_pagerAdapterFeeds;
   private final BaseAdapter          m_navigationAdapter;

   OnLongClickManageFeedItem(FragmentPagerAdapter pagerAdapterFeeds, BaseAdapter navigationAdapter,
         AlertDialog.Builder builder, String applicationFolder, String allTag)
   {
      m_pagerAdapterFeeds = pagerAdapterFeeds;
      m_navigationAdapter = navigationAdapter;
      m_allTag = allTag;
      m_builder = builder;
      m_applicationFolder = applicationFolder;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
   {
      Adapter adapter = parent.getAdapter();
      String feedName = (String) adapter.getItem(pos);
      m_builder.setItems(R.array.long_click_manage_feeds,
            new OnClickManageFeedDialogItem(m_pagerAdapterFeeds, m_navigationAdapter, feedName,
                  m_applicationFolder, m_allTag));
      m_builder.show();
      return true;
   }
}
