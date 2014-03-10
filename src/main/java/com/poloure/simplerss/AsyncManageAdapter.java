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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

class AsyncManageAdapter extends AsyncTask<String, String[][], Void>
{
   private final AdapterManage m_adapterManage;
   private final Context m_context;

   private
   AsyncManageAdapter(Context context, AdapterManage adapterManage)
   {
      m_context = context;
      m_adapterManage = adapterManage;
   }

   static
   void update(Activity activity)
   {
      String manageTag = FeedsActivity.FRAGMENT_TAGS[1];
      FragmentManager manager = activity.getFragmentManager();
      ListFragment fragment = (ListFragment) manager.findFragmentByTag(manageTag);

      if(null != fragment && fragment.isVisible())
      {
         AdapterManage adapter = (AdapterManage) fragment.getListAdapter();

         new AsyncManageAdapter(activity, adapter).executeOnExecutor(THREAD_POOL_EXECUTOR);
      }
   }

   private static
   int count(Context context, String fileName)
   {
      int count = 0;
      try
      {
         BufferedReader read = new BufferedReader(new InputStreamReader(context.openFileInput(fileName)));
         try
         {
            while(null != read.readLine())
            {
               count++;
            }
         }
         finally
         {
            if(null != read)
            {
               read.close();
            }
         }
      }
      catch(IOException ignored)
      {
      }
      return count;
   }

   @Override
   protected
   Void doInBackground(String... applicationFolder)
   {
      /* Read the index file for names, urls, and tags. */
      String[][] feedsIndex = Read.csvFile(m_context, Read.INDEX, 'i', 'u', 't');
      int size = feedsIndex[0].length;

      String[][] strings = new String[size][3];

      for(int i = 0; i < size; i++)
      {
         /* Append the url to the next line. */
         strings[i][0] = Utilities.getLocaleInt(count(m_context, feedsIndex[0][i] + ServiceUpdate.CONTENT_FILE));
         strings[i][1] = feedsIndex[1][i];
         strings[i][2] = feedsIndex[2][i];
      }
      publishProgress(strings);
      return null;
   }

   @Override
   protected
   void onProgressUpdate(String[][]... values)
   {
      m_adapterManage.m_manageItems.clear();
      m_adapterManage.m_manageItems.addAll(Arrays.asList(values[0]));
      m_adapterManage.notifyDataSetChanged();
   }
}
