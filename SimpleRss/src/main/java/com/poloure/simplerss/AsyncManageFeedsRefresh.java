package com.poloure.simplerss;

import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;

class AsyncManageFeedsRefresh extends AsyncTask<String, CharSequence[], Animation>
{
   private static final int FADE_IN_DURATION = 330;
   private final ListView m_listView;

   private
   AsyncManageFeedsRefresh(ListView listView)
   {
      m_listView = listView;

      Adapter adapter = listView.getAdapter();
      if(0 == adapter.getCount())
      {
         m_listView.setVisibility(View.INVISIBLE);
      }
   }

   static
   void newInstance(ListView listView, String applicationFolder)
   {
      AsyncTask<String, CharSequence[], Animation> task = new AsyncManageFeedsRefresh(listView);

      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(THREAD_POOL_EXECUTOR, applicationFolder);
      }
      else
      {
         task.execute(applicationFolder);
      }
   }

   @Override
   protected
   Animation doInBackground(String... applicationFolder)
   {
      String newLine = System.getProperty("line.separator");
      String appFolder = applicationFolder[0];

      /* Read the ALL_TAG m_imageViewTag file for names, urls, and tags. */
      String[][] feedsIndex = Read.indexFile(appFolder);
      String[] feedNames = feedsIndex[0];
      String[] feedUrls = feedsIndex[1];
      String[] feedTags = feedsIndex[2];

      int size = feedNames.length;
      CharSequence[] feedInfoArray = new CharSequence[size];

      for(int i = 0; i < size; i++)
      {
         /* Form the path to the feed_content file. */
         String feedContentFileName = feedNames[i] + File.separatorChar + ServiceUpdate.CONTENT;
         int feedContentSize = Read.count(feedContentFileName, appFolder);

         /* Build the info string. */
         StringBuilder builder = new StringBuilder(80);
         builder.append(feedUrls[i]);
         builder.append("<br><b>Items: </b>");
         builder.append(Integer.toString(feedContentSize));
         builder.append(" · <b>");
         builder.append(feedTags[i]);
         builder.append("</b>");

         feedInfoArray[i] = builder.toString();
      }
      publishProgress(feedNames, feedInfoArray);

      Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
      fadeIn.setDuration(FADE_IN_DURATION);
      return fadeIn;
   }

   @Override
   protected
   void onPostExecute(Animation result)
   {
      if(!m_listView.isShown())
      {
         m_listView.setAnimation(result);
         m_listView.setVisibility(View.VISIBLE);
      }
   }

   @Override
   protected
   void onProgressUpdate(CharSequence[]... values)
   {
      BaseAdapter adapter = (BaseAdapter) m_listView.getAdapter();
      ((AdapterManageFragments) adapter).setArrays(values[0], values[1]);
      adapter.notifyDataSetChanged();
   }
}
