package com.poloure.simplerss;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

class AsyncRefreshNavigationAdapter extends AsyncTask<int[], Void, int[]>
{
   private final BaseAdapter m_navigationAdapter;
   private final Context     m_context;

   AsyncRefreshNavigationAdapter(BaseAdapter navigationAdapter, Context context)
   {
      m_navigationAdapter = navigationAdapter;
      m_context = context;
   }

   @Override
   protected
   int[] doInBackground(int[]... counts)
   {
      /* If null was passed into the task, count the unread items. */
      return 0 == counts[0].length ? Util.getUnreadCounts(m_context) : counts[0];
   }

   @Override
   protected
   void onPostExecute(int[] result)
   {
      /* Set the titles & counts arrays in this file and notify the adapter. */
      ((AdapterNavDrawer) m_navigationAdapter).m_tagArray = Read.file(Constants.TAG_LIST,
            m_context);
      ((AdapterNavDrawer) m_navigationAdapter).m_unreadArray = result;
      m_navigationAdapter.notifyDataSetChanged();
   }
}
