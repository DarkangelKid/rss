package com.poloure.simplerss;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

class AsyncManageTagsRefresh extends AsyncTask<Void, String[], Void>
{
   private final Animation   m_fadeIn;
   private final ListView    m_listView;
   private final ListAdapter m_adapter;
   private final Context     m_context;

   AsyncManageTagsRefresh(ListView listView, ListAdapter adapter, Context context)
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
   Void doInBackground(Void... nothing)
   {
      String[][] content = getInfoArrays();
      if(0 != content.length)
      {
         publishProgress(content[1], content[0]);
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
   void onProgressUpdate(String[][] values)
   {
      if(null != m_adapter)
      {
         setArrays(values[0], values[1]);
         ((BaseAdapter) m_adapter).notifyDataSetChanged();
      }
   }

   private static
   void setArrays(String[] tags, String... infos)
   {
      AdapterManagerTags.s_tagArray = tags;
      AdapterManagerTags.s_infoArray = infos;
   }

   private
   String[][] getInfoArrays()
   {
      String[] currentTags = Read.file(Constants.TAG_LIST, m_context);
      int tagCount = currentTags.length;
      if(0 == tagCount)
      {
         return Util.EMPTY_STRING_STRING_ARRAY;
      }

      String[] tagArray = new String[tagCount];
      String[] infoArray = new String[tagCount];
      StringBuilder info = new StringBuilder(40);

      String[][] content = Read.csv(m_context);
      String[] feeds = content[0];
      String[] tags = content[2];

      for(int i = 0; i < tagCount; i++)
      {
         info.setLength(0);
         tagArray[i] = currentTags[i];

         if(0 == i)
         {
            info.append(tagCount).append(" tags");
         }
         else
         {
            int feedsCount = feeds.length;
            for(int j = 0; j < feedsCount; j++)
            {
               if(currentTags[i].equals(tags[j]))
               {
                  info.append(feeds[j]).append(", ");
               }
            }
         }
         infoArray[i] = info.toString();
      }
         /* 0 is meant to be total. */
      infoArray[0] = 0 + " items • " + infoArray[0];
      return new String[][]{infoArray, tagArray};
   }
}
