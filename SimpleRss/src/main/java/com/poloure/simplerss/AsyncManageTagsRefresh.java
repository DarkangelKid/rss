package com.poloure.simplerss;

import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

class AsyncManageTagsRefresh extends AsyncTask<String, String[], Animation>
{
   private static final byte INFO_INITIAL_CAPACITY = (byte) 40;
   private static final short FADE_IN_DURATION = (short) 330;
   private final ListView m_listView;

   private
   AsyncManageTagsRefresh(ListView listView)
   {
      m_listView = listView;

      /* If the adapter has no items, make the listView not shown. */
      Adapter adapter = listView.getAdapter();
      if(0 == adapter.getCount())
      {
         m_listView.setVisibility(View.INVISIBLE);
      }
   }

   static
   void newInstance(ListView listView, String applicationFolder, String allTag)
   {
      AsyncTask<String, String[], Animation> task = new AsyncManageTagsRefresh(listView);

      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(THREAD_POOL_EXECUTOR, allTag, applicationFolder);
      }
      else
      {
         task.execute(allTag, applicationFolder);
      }
   }

   @Override
   protected
   Animation doInBackground(String... allTagAndFolder)
   {
      /* Create the info strings for each tag. */
      String[] feedTags = PagerAdapterFeeds.getTagsArray();
      int tagCount = feedTags.length;

      if(0 == tagCount)
      {
         return null;
      }

      String allTag = allTagAndFolder[0];
      String applicationFolder = allTagAndFolder[1];

      /* Get the feed names from the index File. */
      String[][] feedsIndex = Read.csvFile(Read.INDEX, applicationFolder, 'f', 't');
      String[] indexNames = feedsIndex[0];
      String[] indexTags = feedsIndex[1];

      String[] feedInfoArray = new String[tagCount];
      StringBuilder info = new StringBuilder((int) INFO_INITIAL_CAPACITY);

      for(int i = 0; i < tagCount; i++)
      {
         info.setLength(0);
         int feedCount = 0;

         if(feedTags[i].equals(allTag))
         {
            info.append(tagCount);
            info.append(" tags");
         }
         else
         {
            int feedsCount = indexNames.length;
            for(int j = 0; j < feedsCount; j++)
            {
               if(indexTags[j].contains(feedTags[i]))
               {
                  info.append(indexNames[j]);
                  info.append(", ");
                  feedCount++;
               }
            }
         }
         if(0 != feedCount)
         {
            int infoSize = info.length();
            info.delete(infoSize - 2, infoSize);
         }
         feedInfoArray[i] = "<b>Feeds: </b>" + feedCount + " · " + info;
      }
      /* 0 is meant to be total. */
      feedInfoArray[0] = 0 + " items • " + feedInfoArray[0];

      publishProgress(feedTags, feedInfoArray);

      Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
      fadeIn.setDuration((long) FADE_IN_DURATION);
      return fadeIn;
   }

   @Override
   protected
   void onPostExecute(Animation result)
   {
      if(null != result && !m_listView.isShown())
      {
         m_listView.setAnimation(result);
         m_listView.setVisibility(View.VISIBLE);
      }
   }

   @Override
   protected
   void onProgressUpdate(String[][] values)
   {
      BaseAdapter adapter = (BaseAdapter) m_listView.getAdapter();
      if(null != adapter)
      {
         ((AdapterManageFragments) adapter).setArrays(values[0], values[1]);
         adapter.notifyDataSetChanged();
      }
   }
}
