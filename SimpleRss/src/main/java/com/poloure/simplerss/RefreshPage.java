package com.poloure.simplerss;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

class RefreshPage extends AsyncTask<Integer, Object, Animation>
{
   private static final int MAX_DESCRIPTION_LENGTH = 360;
   private static final int MIN_IMAGE_WIDTH        = 32;
   private final BaseAdapter     m_navigationAdapter;
   private final FragmentManager m_fragmentManager;
   private final Context         m_context;
   private       int             m_pageNumber;
   private       boolean         m_flash;
   private       AdapterTags     m_adapterTag;
   private       ListView        m_listView;
   private int position = -3;

   RefreshPage(BaseAdapter navigationAdapter, FragmentManager fragmentManager, Context context)
   {
      m_navigationAdapter = navigationAdapter;
      m_fragmentManager = fragmentManager;
      m_context = context;
   }

   @Override
   protected
   Animation doInBackground(Integer... page)
   {
      m_pageNumber = page[0];
      String tag = PagerAdapterFeeds.getTagsArray()[m_pageNumber];

      String[][] contents = Read.csv(m_context);
      if(0 == contents.length)
      {
         return null;
      }
      String[] feeds = contents[0];
      String[] tags = contents[2];

      Animation animFadeIn = AnimationUtils.loadAnimation(m_context, android.R.anim.fade_in);

      int viewPagerId = FragmentFeeds.s_viewPager.getId();
      String fragmentTag = "android:switcher:" + viewPagerId + ':' + m_pageNumber;

      ListFragment listFragment = (ListFragment) m_fragmentManager.findFragmentByTag(fragmentTag);
      m_adapterTag = (AdapterTags) listFragment.getListAdapter();
      m_listView = listFragment.getListView();

      Map<Long, FeedItem> map = new TreeMap<Long, FeedItem>();
      String allTag = m_context.getString(R.string.all_tag);
      boolean isAllTag = tag.equals(allTag);

      int feedsLength = feeds.length;
      for(int j = 0; j < feedsLength; j++)
      {
         if(isAllTag || tags[j].contains(tag))
         {
            String[][] content = Read.csv(feeds[j] + Constants.SEPAR + Constants.CONTENT, m_context,
                  't', 'd', 'l', 'i', 'w', 'h', 'p');
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
               /* Edit the data. */
               if(null != images[i])
               {

                  if(MIN_IMAGE_WIDTH < (null == widths[i] || 0 == widths[i].length()
                        ? 0
                        : Integer.parseInt(widths[i])))
                  {
                     int lastSlash = images[i].lastIndexOf(Constants.SEPAR) + 1;
                     images[i] = feeds[j] + Constants.SEPAR + Constants.THUMBNAIL_DIR +
                           images[i].substring(lastSlash);
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
               else if(MAX_DESCRIPTION_LENGTH <= descriptions[i].length())
               {
                  descriptions[i] = descriptions[i].substring(0, MAX_DESCRIPTION_LENGTH);
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
               data.time = Long.parseLong(times[i]);

               data.width = null == widths[i] || 0 == widths[i].length()
                     ? 0
                     : Integer.parseInt(widths[i]);

               data.height = null == heights[i] || 0 == heights[i].length()
                     ? 0
                     : Integer.parseInt(heights[i]);

               // Do not add duplicates. */
               if(-1 == Util.index(m_adapterTag.m_items, data))
               {
                  map.put(data.time, data);
               }
            }
         }
      }

      /* Do not count items as Read while we are updating the list. */
      m_adapterTag.m_touchedScreen = false;

      int mapSize = map.size();
      NavigableMap<Long, FeedItem> navigableMap
            = ((NavigableMap<Long, FeedItem>) map).descendingMap();
      Collection<FeedItem> collection = navigableMap.values();
      Object[] items = collection.toArray(new FeedItem[mapSize]);

      if(0 < items.length)
      {
         publishProgress(items);
      }

      return animFadeIn;
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
      Update.navigation(m_navigationAdapter, null, 0, m_context);

      if(null == m_listView)
      {
         return;
      }

      /* If there were no items to start with (the ListView is invisible).*/
      if(m_flash)
      {
         m_listView.setSelection(position);
         m_listView.setAnimation(result);
         m_listView.setVisibility(View.VISIBLE);
      }
      /* Resume Read item checking. */
      m_adapterTag.m_touchedScreen = true;
   }

   @Override
   protected
   void onProgressUpdate(Object[] values)
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

      m_adapterTag.prependArray(values);
      m_adapterTag.notifyDataSetChanged();

      if(m_flash)
      {
         position = Util.gotoLatestUnread(m_adapterTag.m_items, false, m_pageNumber,
               m_fragmentManager);
      }

      if(0 != top)
      {
         m_listView.setSelectionFromTop(index, top - (/* TODO 8 */ 16 << 1));
      }
   }
}
