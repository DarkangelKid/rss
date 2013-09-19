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

class RefreshPage extends AsyncTask<Integer, Object, Animation>
{
   private int          page_number;
   private boolean      flash;
   private ListFragment l;
   private AdapterCard  ith;
   private ListView     lv;
   private int position = -3;

   @Override
   protected Animation doInBackground(Integer... page)
   {
      page_number = page[0];
      String tag = FeedsActivity.ctags[page_number];

      Time time = new Time();
      Map<Long, String[]> map = new TreeMap<Long, String[]>();

      String[][] contents = Read.csv();
      if(0 == contents.length)
      {
         return null;
      }
      String[] feeds = contents[0];
      String[] tags = contents[2];

      Animation animFadeIn = AnimationUtils.loadAnimation(Util.getContext(), android.R.anim.fade_in);

      while(null == lv)
      {
         /* Anti-pattern. */
         try
         {
            Thread.sleep(5L);
         }
         catch(Exception e)
         {
         }
         if((null != FeedsActivity.viewpager) && (null == l))
         {
            l = (ListFragment) FeedsActivity.fman.findFragmentByTag(
                  "android:switcher:" + FeedsActivity.viewpager.getId() + ':' +
                  Integer.toString(page_number));
         }
         if((null != l) && (null == ith))
         {
            ith = (AdapterCard) l.getListAdapter();
         }
         if((null != l) && (null == lv))
         {
            try
            {
               lv = l.getListView();
            }
            catch(IllegalStateException e)
            {
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
            String[] heights = content[5];
            String[] times = content[6];

            for(int i = 0; i < times.length; i++)
            {
               try
               {
                  time.parse3339(times[i]);
               }
               catch(Exception e)
               {
                  Util.post("Unable to parse date.");
                  return null;
               }
               if(-1 == Util.index(Util.getCardAdapter(page_number).m_links, links[i]))
               {
                  /* Edit the data. */
                  if(null != images[i])
                  {
                     if(32 < Util.stoi(widths[i]))
                     {
                        images[i] = Util.getPath(feeds[j], "thumbnails") + images[i]
                              .substring(images[i].lastIndexOf(FeedsActivity.SEPAR) + 1);
                     }
                     else
                     {
                        images[i] = "";
                        widths[i] = "";
                        heights[i] = "";
                     }
                  }

                  if((null == descriptions[i]) || (8 > descriptions[i].length()))
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

                  String[] datum = new String[]{
                        titles[i],
                        descriptions[i],
                        links[i],
                        images[i],
                        widths[i],
                        heights[i],
                        times[i]
                  };

                  map.put(time.toMillis(false) - (long) i, datum);
               }
            }
         }
      }

      /* Do not count items as Read while we are updating the list. */
      ith.m_touchedScreen = false;

      String[][] list = map.values().toArray(new String[map.size()][7]);
      int count = list.length;

      titles = new String[count];
      descriptions = new String[count];
      links = new String[count];
      images = new String[count];
      Integer[] iwidths = new Integer[count];
      Integer[] iheights = new Integer[count];

      for(int i = count - 1; 0 <= i; i--)
      {
         int a = count - 1 - i;
         titles[a] = list[i][0];
         descriptions[a] = list[i][1];
         links[a] = list[i][2];
         images[a] = list[i][3];
         iwidths[a] = null == list[i][4] ? 0 : Integer.parseInt(list[i][4]);
         iheights[a] = null == list[i][5] ? 0 : Integer.parseInt(list[i][5]);
         //times[a]      = list.get(i)[6]
      }

      if(0 < titles.length)
      {
         publishProgress(titles, descriptions, links, images, iheights, iwidths);
      }

      return animFadeIn;
   }

   @Override
   protected void onProgressUpdate(Object[] progress)
   {
      /* If these are the first items to be added to the list. */
      if(0 == lv.getCount())
      {
         lv.setVisibility(View.INVISIBLE);
         flash = true;
      }

      int index = 0, top = 0;
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
         else if((0 > top) && (null != lv.getChildAt(1)))
         {
            index++;
            View childAt = lv.getChildAt(1);
            top = childAt.getTop();
         }
      }

      ith.prependArray((String[]) progress[0], (String[]) progress[1], (String[]) progress[2],
                       (String[]) progress[3], (Integer[]) progress[4], (Integer[]) progress[5]);
      ith.notifyDataSetChanged();

      if(flash)
      {
         position = Util.gotoLatestUnread(ith.m_links, false, page_number);
      }

      if(0 != top)
      {
         lv.setSelectionFromTop(index, top - ith.eight * 2);
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

      /* If there were no items to start with (the listview is invisible).*/
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
