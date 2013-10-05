package yay.poloure.simplerss;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public
class AsyncManageTagsRefresh extends AsyncTask<Void, String[], Void>
{
   final Animation animFadeIn = AnimationUtils.loadAnimation(Util.getContext(),
         android.R.anim.fade_in);

   ListView    m_listView;
   ListAdapter m_adapter;

   AsyncManageTagsRefresh(ListView listView, ListAdapter adapter)
   {
      m_listView = listView;
      m_adapter = adapter;

      if(0 == m_adapter.getCount())
      {
         m_listView.setVisibility(View.INVISIBLE);
      }
   }

   static
   void setArrays(String[] tags, String... infos)
   {
      AdapterManagerTags.s_tagArray = tags;
      AdapterManagerTags.s_infoArray = infos;
   }

   static
   String[][] getInfoArrays(String... ctags)
   {
      int tagCount = ctags.length;
      String[] tagArray = new String[tagCount];
      String[] infoArray = new String[tagCount];
      StringBuilder info = new StringBuilder(40);

      String[][] content = Read.csv();
      String[] feeds = content[0];
      String[] tags = content[2];

      for(int i = 0; i < tagCount; i++)
      {
         info.setLength(0);
         tagArray[i] = ctags[i];

         if(0 == i)
         {
            info.append(tagCount).append(" tags");
         }
         else
         {
            int feedsCount = feeds.length;
            for(int j = 0; j < feedsCount; j++)
            {
               if(ctags[i].equals(tags[j]))
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

   @Override
   protected
   Void doInBackground(Void... nothing)
   {
      String[][] content = getInfoArrays(FeedsActivity.s_currentTags);
      publishProgress(content[1], content[0]);
      return null;
   }

   @Override
   protected
   void onPostExecute(Void result)
   {
      m_listView.setAnimation(animFadeIn);
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
}
