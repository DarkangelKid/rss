package com.poloure.simplerss;

import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.Map;
import java.util.TreeMap;

class RefreshPage extends AsyncTask<Integer, FeedItem, Animation>
{
   private final BaseAdapter  m_navigationAdapter;
   private       int          m_pageNumber;
   private       boolean      m_flash;
   private       ListFragment m_listFragment;
   private       AdapterTag   m_adapterCard;
   private       ListView     m_listView;
   private int position = -3;

   RefreshPage(BaseAdapter navigationAdapter)
   {
      m_navigationAdapter = navigationAdapter;
   }

   @Override
   protected
   Animation doInBackground(Integer... page)
   {
      m_pageNumber = page[0];
      String tag = Read.file(Constants.TAG_LIST)[m_pageNumber];

      String[][] contents = Read.csv();
      if(0 == contents.length)
      {
         return null;
      }
      String[] feeds = contents[0];
      String[] tags = contents[2];

      Animation animFadeIn = AnimationUtils.loadAnimation(Util.getContext(),
            android.R.anim.fade_in);

      while(null == m_listView)
      {
         /* Anti-pattern. */
         try
         {
            Thread.sleep(5L);
         }
         catch(InterruptedException e)
         {
            e.printStackTrace();
         }

         if(null != FragmentFeeds.VIEW_PAGER && null == m_listFragment)
         {
            String fragmentTag = String.format(Constants.FRAGMENT_TAG,
                  FragmentFeeds.VIEW_PAGER.getId(), m_pageNumber);
            m_listFragment = (ListFragment) FeedsActivity.getActivity()
                  .getSupportFragmentManager()
                  .findFragmentByTag(fragmentTag);
         }
         if(null != m_listFragment && null == m_adapterCard)
         {
            m_adapterCard = (AdapterTag) m_listFragment.getListAdapter();
         }
         if(null != m_listFragment && null == m_listView)
         {
            try
            {
               m_listView = m_listFragment.getListView();
            }
            catch(IllegalStateException e)
            {
               /* Do not throw. */
               m_listView = null;
            }
         }
      }

      Map<Long, FeedItem> map = new TreeMap<Long, FeedItem>();

      int feedsLength = feeds.length;
      for(int j = 0; j < feedsLength; j++)
      {
         if(tags[j].contains(tag) || tag.equals(Constants.ALL_TAG))
         {
            String[][] content = csv(feeds[j], 't', 'd', 'l', 'i', 'w', 'h', 'p');
            if(0 == content.length)
            {
               return null;
            }
            String[] titles = content[0];
            String[] descriptions = content[1];
            String[] links = content[2];
            String[] images = content[3];
            String[] widths = content[4];
            String[] heights = content[5];
            String[] times = content[6];

            int timesLength = times.length;
            for(int i = 0; i < timesLength; i++)
            {
               /* TODO Do not allow duplicates in the adapter. */
               /*if(-1 == Util.index(Util.getCardAdapter(m_pageNumber).m_links, links[i]))*/
               {
                  /* Edit the data. */
                  if(null != images[i])
                  {
                     if(32 < Util.stoi(widths[i]))
                     {
                        images[i] = Util.getPath(feeds[j], Constants.THUMBNAILS) +
                              images[i].substring(images[i].lastIndexOf(Constants.SEPAR) + 1);
                     }
                     else
                     {
                        images[i] = "";
                        widths[i] = "";
                        heights[i] = "";
                     }
                  }

                  if(null == descriptions[i] || 8 > descriptions[i].length())
                  {
                     descriptions[i] = "";
                  }
                  else if(360 <= descriptions[i].length())
                  {
                     descriptions[i] = descriptions[i].substring(0, 360);
                  }
                  if(null == titles[i])
                  {
                     titles[i] = "";
                  }

                  FeedItem data = new FeedItem();
                  data.title = titles[i];
                  data.url = links[i];
                  data.description = descriptions[i];
                  data.image = images[i];
                  data.width = Util.stoi(widths[i]);
                  data.height = Util.stoi(heights[i]);

                  map.put(Long.parseLong(times[i]) - i, data);
               }
            }
         }
      }

      /* Do not count items as Read while we are updating the list. */
      m_adapterCard.m_touchedScreen = false;

      FeedItem[] items = map.values().toArray(new FeedItem[map.size()]);

      if(0 < items.length)
      {
         publishProgress(items);
      }

      return animFadeIn;
   }

   /* This is for reading an rss file. */
   private static
   String[][] csv(String feed, char... type)
   {
      return Read.csv(Util.getPath(feed, Constants.CONTENT), type);
   }

   @Override
   protected
   void onPostExecute(Animation result)
   {
      if(null == result)
      {
         return;
      }

      /* Update the unread counts in the navigation drawer. */
      Update.navigation(m_navigationAdapter);

      if(null == m_listView)
      {
         return;
      }

      /* If there were no items to start with (the m_listview is invisible).*/
      if(m_flash)
      {
         m_listView.setSelection(position);
         m_listView.setAnimation(result);
         m_listView.setVisibility(View.VISIBLE);
      }
      /* Resume Read item checking. */
      m_adapterCard.m_touchedScreen = true;
   }

   @Override
   protected
   void onProgressUpdate(FeedItem[] values)
   {
      /* If these are the first items to be added to the list. */
      if(0 == m_listView.getCount())
      {
         m_listView.setVisibility(View.INVISIBLE);
         m_flash = true;
      }

      int index = 0;
      int top = 0;
      /* Find the exact position in the list. */
      if(!m_flash)
      {
         index = m_listView.getFirstVisiblePosition() + 1;
         View v = m_listView.getChildAt(0);
         top = null == v ? 0 : v.getTop();
         if(0 == top)
         {
            index++;
         }
         else if(0 > top && null != m_listView.getChildAt(1))
         {
            index++;
            View childAt = m_listView.getChildAt(1);
            top = childAt.getTop();
         }
      }

      m_adapterCard.prependArray(values);
      m_adapterCard.notifyDataSetChanged();

      if(m_flash)
      {
         position = Util.gotoLatestUnread(m_adapterCard.m_items, false, m_pageNumber,
               m_listFragment);
      }

      if(0 != top)
      {
         m_listView.setSelectionFromTop(index, top - (/* TODO 8 */ 16 << 1));
      }
   }
}
