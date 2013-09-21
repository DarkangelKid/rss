package yay.poloure.simplerss;

import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.text.format.Time;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import java.util.Map;
import java.util.TreeMap;

class RefreshPage extends AsyncTask<Integer, Datum, Animation>
{
   int          page_number;
   boolean      flash;
   ListFragment l;
   AdapterCard  ith;
   ListView     lv;
   int position = -3;

   @Override
   protected Animation doInBackground(Integer... page)
   {
      page_number = page[0];
      String tag = FeedsActivity.ctags[page_number];

      Time time = new Time();
      Map<Long, Datum> map = new TreeMap<Long, Datum>();

      String[][] contents = Read.csv();
      if(0 == contents.length)
      {
         return null;
      }
      String[] feeds = contents[0];
      String[] tags = contents[2];

      Animation animFadeIn = AnimationUtils.loadAnimation(Util.getContext(),
                                                          android.R.anim.fade_in);

      while(null == lv)
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

         if(null != FeedsActivity.viewpager && null == l)
         {
            String fragmentTag = String.format(FeedsActivity.FRAGMENT_TAG,
                                               FeedsActivity.viewpager.getId(), page_number);
            l = (ListFragment) FeedsActivity.fman.findFragmentByTag(fragmentTag);
         }
         if(null != l && null == ith)
         {
            ith = (AdapterCard) l.getListAdapter();
         }
         if(null != l && null == lv)
         {
            try
            {
               lv = l.getListView();
            }
            catch(IllegalStateException e)
            {
               e.printStackTrace();
               lv = null;
            }
         }
      }

      String[] titles;
      String[] descriptions;
      String[] links;
      String[] images;
      for(int j = 0; j < feeds.length; j++)
      {
         if(tags[j].equals(tag) || tag.equals(FeedsActivity.all))
         {
            String[][] content = Read.csv(feeds[j], 't', 'd', 'l', 'i', 'w', 'h', 'p');
            if(0 == content.length)
            {
               return null;
            }
            titles = content[0];
            descriptions = content[1];
            links = content[2];
            images = content[3];
            String[] widths = content[4];
            Write.log(widths[0]);
            String[] heights = content[5];
            String[] times = content[6];

            for(int i = 0; i < times.length; i++)
            {
               try
               {
                  time.parse3339(times[i]);
               }
               catch(RuntimeException e)
               {
                  e.printStackTrace();
                  Util.post("Unable to parse date.");
                  return null;
               }
               /* TODO Do not allow duplicates in the adapter. */
               /*if(-1 == Util.index(Util.getCardAdapter(page_number).m_links, links[i]))*/
               {
                  /* Edit the data. */
                  if(null != images[i])
                  {
                     if(32 < Util.stoi(widths[i]))
                     {
                        images[i] = Util.getPath(feeds[j], "thumbnails") + images[i].substring(
                              images[i].lastIndexOf(FeedsActivity.SEPAR) + 1);
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

                  Datum data       = new Datum();
                  data.title       = titles[i];
                  data.url         = links[i];
                  data.description = descriptions[i];
                  data.image       = images[i];
                  data.width       = widths[i]  == null ? 0 : Integer.parseInt(widths[i]);
                  data.height      = heights[i] == null ? 0 : Integer.parseInt(heights[i]);

                  map.put(time.toMillis(false) - i, data);
               }
            }
         }
      }

      /* Do not count items as Read while we are updating the list. */
      ith.m_touchedScreen = false;

      Datum[] items = map.values().toArray(new Datum[map.size()]);

      if(0 < items.length)
      {
         publishProgress(items);
      }

      return animFadeIn;
   }

   @Override
   protected void onProgressUpdate(Datum[] items)
   {
      /* If these are the first items to be added to the list. */
      if(0 == lv.getCount())
      {
         lv.setVisibility(View.INVISIBLE);
         flash = true;
      }

      int index = 0;
      int top = 0;
      /* Find the exact position in the list. */
      if(!flash)
      {
         index = lv.getFirstVisiblePosition() + 1;
         View v = lv.getChildAt(0);
         top = null == v ? 0 : v.getTop();
         if(0 == top)
         {
            index++;
         }
         else if(0 > top && null != lv.getChildAt(1))
         {
            index++;
            View childAt = lv.getChildAt(1);
            top = childAt.getTop();
         }
      }

      ith.prependArray(items);
      ith.notifyDataSetChanged();

      if(flash)
      {
         position = Util.gotoLatestUnread(ith.m_items, false, page_number);
      }

      if(0 != top)
      {
         lv.setSelectionFromTop(index, top - (ith.s_eight << 1));
      }
   }

   @Override
   protected void onPostExecute(Animation tun)
   {
      if(null == tun)
      {
         return;
      }

      /* Update the unread counts in the navigation drawer. */
      Update.navigation();

      if(null == lv)
      {
         return;
      }

      /* If there were no items to start with (the m_listview is invisible).*/
      if(flash)
      {
         lv.setSelection(position);
         lv.setAnimation(tun);
         lv.setVisibility(View.VISIBLE);
      }
      /* Resume Read item checking. */
      ith.m_touchedScreen = true;
   }
}
