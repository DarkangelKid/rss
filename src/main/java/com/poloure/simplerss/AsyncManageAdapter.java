/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.AsyncTask;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class AsyncManageAdapter extends AsyncTask<String, String[][], Void>
{
   private final ListFragment m_listFragment;
   private final FeedsActivity m_activity;

   private
   AsyncManageAdapter(FeedsActivity activity, ListFragment listFragment)
   {
      m_activity = activity;
      m_listFragment = listFragment;
   }

   static
   void run(FeedsActivity activity)
   {
      FragmentManager manager = activity.getFragmentManager();
      ListFragment fragment = (ListFragment) manager.findFragmentById(R.id.fragment_manage);

      if(null != fragment && fragment.isVisible())
      {
         AsyncManageAdapter task = new AsyncManageAdapter(activity, fragment);
         task.executeOnExecutor(THREAD_POOL_EXECUTOR);
      }
   }

   @Override
   protected
   Void doInBackground(String... applicationFolder)
   {
      /* ObjectIO the index file for names, urls, and tags. */
      NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
      List<IndexItem> indexItems = m_activity.m_index;
      String[][] strings = new String[indexItems.size()][3];

      for(int i = 0; i < indexItems.size(); ++i)
      {
         /* Append the url to the next line. */
         IndexItem item = indexItems.get(i);

         ObjectIO reader = new ObjectIO(m_activity, item.m_uid + ServiceUpdate.ITEM_LIST);
         int itemCount = reader.getElementCount();

         strings[i][0] = format.format(itemCount);
         strings[i][1] = item.m_url;
         strings[i][2] = Utilities.formatTags(item.m_tags);
      }
      publishProgress(strings);
      return null;
   }

   @Override
   protected
   void onProgressUpdate(String[][]... values)
   {
      AdapterManage adapterManage = new AdapterManage(m_activity);
      m_listFragment.setListAdapter(adapterManage);

      adapterManage.addAll(Arrays.asList(values[0]));
   }
}
