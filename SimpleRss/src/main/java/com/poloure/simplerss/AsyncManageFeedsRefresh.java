package com.poloure.simplerss;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;

class AsyncManageFeedsRefresh extends AsyncTask<Void, String[], Void>
{
   private final Animation   m_fadeIn;
   private final ListView    m_listView;
   private final BaseAdapter m_adapter;
   private final Context     m_context;

   AsyncManageFeedsRefresh(ListView listView, BaseAdapter adapter, Context context)
   {
      m_listView = listView;
      m_adapter = adapter;
      m_fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

      if(0 == m_adapter.getCount())
      {
         m_listView.setVisibility(View.INVISIBLE);
      }
      m_context = context;
   }

   @Override
   protected
   Void doInBackground(Void... hey)
   {
      if(null != m_adapter)
      {
            /* Read the ALL_TAG m_imageViewTag file for names, urls, and tags. */
         String[][] content = Read.csv(m_context);
         int size = content[0].length;
         String[] infoArray = new String[size];

         for(int i = 0; i < size; i++)
         {
               /* Form the path to the feed_content file. */
            String path = Util.getPath(content[0][i], Constants.CONTENT);

               /* Build the info string. */
            infoArray[i] = content[1][i] + Constants.NL + content[2][i] + " • " +
                  Read.count(path, m_context) +
                  " items";
         }
         publishProgress(content[0], infoArray);
      }
      return null;
   }

   @Override
   protected
   void onPostExecute(Void result)
   {
      m_listView.setAnimation(m_fadeIn);
      m_listView.setVisibility(View.VISIBLE);
   }

   @Override
   protected
   void onProgressUpdate(String[]... values)
   {
      ((AdapterManageFeeds) m_adapter).setArrays(values[0], values[1]);
      m_adapter.notifyDataSetChanged();
   }
}
