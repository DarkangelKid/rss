package com.poloure.simplerss;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;

class AsyncManageFeedsRefresh extends AsyncTask<String, Editable[], Animation>
{
   private static final int FADE_IN_DURATION = 330;
   private static final AbsoluteSizeSpan TITLE_SIZE = new AbsoluteSizeSpan(14, true);
   private static final StyleSpan SPAN_BOLD = new StyleSpan(Typeface.BOLD);
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
      AsyncTask<String, Editable[], Animation> task = new AsyncManageFeedsRefresh(listView);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR, applicationFolder);
   }

   @Override
   protected
   Animation doInBackground(String... applicationFolder)
   {
      String appFolder = applicationFolder[0];

      /* Read the index file for names, urls, and tags. */
      String[][] feedsIndex = Read.csvFile(Read.INDEX, appFolder, 'f', 'u', 't');
      String[] feedNames = feedsIndex[0];
      String[] feedUrls = feedsIndex[1];
      String[] feedTags = feedsIndex[2];

      int size = feedNames.length;
      Editable[] editables = new SpannableStringBuilder[size];

      for(int i = 0; i < size; i++)
      {
         /* New object here because we make it a reference in the array. */
         Editable editable = new SpannableStringBuilder();

         /* Append the feed name. */
         editable.append(feedNames[i]);
         editable.append("\n");

         /* Make the feed name size 16dip. */
         int titleLength = feedNames[i].length();
         editable.setSpan(TITLE_SIZE, 0, titleLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

         /* Form the path to the feed_content file. */
         String feedContentFileName = feedNames[i] + File.separatorChar + ServiceUpdate.CONTENT;
         int feedContentSize = Read.count(feedContentFileName, appFolder);
         String contentSize = Integer.toString(feedContentSize);

         /* Append the url to the next line. */
         editable.append(feedUrls[i]);
         editable.append("\n");

         /* Append an bold "Items :" text. */
         int thirdLinePosition = editable.length();
         editable.append("Items: ");
         editable.setSpan(SPAN_BOLD, thirdLinePosition, editable.length(),
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         editable.append(contentSize);

         editable.append(" · ");

         /* Append the tags in bold. */
         int currentPosition = editable.length();
         editable.append(feedTags[i]);
         editable.setSpan(SPAN_BOLD, currentPosition, editable.length(),
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         editables[i] = editable;
      }
      publishProgress(editables);

      Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
      fadeIn.setDuration((long) FADE_IN_DURATION);
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
   void onProgressUpdate(Editable[]... values)
   {
      BaseAdapter adapter = (BaseAdapter) m_listView.getAdapter();
      ((AdapterManageFragments) adapter).setEditable(values[0]);
      adapter.notifyDataSetChanged();
   }
}
