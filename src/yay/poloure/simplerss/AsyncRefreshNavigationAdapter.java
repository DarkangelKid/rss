package yay.poloure.simplerss;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

class AsyncRefreshNavigationAdapter extends AsyncTask<int[], Void, int[]>
{
   private final BaseAdapter m_navigationAdapter;

   AsyncRefreshNavigationAdapter(BaseAdapter navigationAdapter)
   {
      m_navigationAdapter = navigationAdapter;
   }

   @Override
   protected
   int[] doInBackground(int[]... counts)
   {
      /* If null was passed into the task, count the unread items. */
      return 0 == counts[0].length ? Util.getUnreadCounts() : counts[0];
   }

   @Override
   protected
   void onPostExecute(int[] result)
   {
      /* Set the titles & counts arrays in this file and notify the adapter. */
      ((AdapterNavDrawer) m_navigationAdapter).m_tagArray = Read.file(Constants.TAG_LIST);
      ((AdapterNavDrawer) m_navigationAdapter).m_unreadArray = result;
      m_navigationAdapter.notifyDataSetChanged();
   }
}
