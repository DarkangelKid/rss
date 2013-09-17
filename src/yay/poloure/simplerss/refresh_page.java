package yay.poloure.simplerss;

import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import android.text.format.Time;

class refresh_page extends AsyncTask<Integer, Object, Animation>
{
   int page_number;
   boolean flash = false;
   ListFragment l;
   adapter_card ith;
   ListView lv;
   int position = -3;

   class data
   {
      String title;
      String description;
      String link;
      String image;
      int width;
      int height;
      long time;
   }

   @Override
   protected Animation doInBackground(Integer... page)
   {
      page_number         = page[0];
      String tag          = main.ctags[page_number];

      String[] titles, descriptions, links, images, widths, heights, times;
      data datum;
      Time time               = new Time();
      Map<Long, data> map     = new TreeMap<Long, data>();

      String[][] contents   = read.csv(main.INDEX);
      if(contents.length == 0) return null;
      String[]   feeds      = contents[0];
      String[]   tags       = contents[2];

      Animation animFadeIn = AnimationUtils.loadAnimation(util.get_context(), android.R.anim.fade_in);

      while(lv == null)
      {
         try
         {
            Thread.sleep(5);
         }
         catch(Exception e){
         }
         if( main.viewpager != null && l == null )
            l = (ListFragment) main.fman.findFragmentByTag("android:switcher:" + main.viewpager.getId() + ":" + Integer.toString(page_number));
         if( l != null && ith == null )
            ith = (adapter_card) l.getListAdapter();
         if( l != null && lv == null )
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

      for(int j = 0; j < feeds.length; j++)
      {
         if(tags[j].equals(tag) || tag.equals(main.ALL))
         {
            String[][] content  = read.csv(feeds[j], 't', 'd', 'l', 'i', 'w', 'h', 'p');
            if(content.length == 0) return null;
            titles       = content[0];
            descriptions = content[1];
            links        = content[2];
            images       = content[3];
            widths       = content[4];
            heights      = content[5];
            times        = content[6];

            for(int i = 0; i < times.length; i++)
            {
               try
               {
                  time.parse3339(times[i]);
               }
               catch(Exception e)
               {
                  util.post("Unable to parse date.");
                  return null;
               }
               if(util.index(util.get_card_adapter(page_number).links, links[i]) == -1)
               {
                  /* Edit the data. */
                  if(images[i] != null)
                  {
                     if(util.stoi(widths[i]) > 32)
                     {
                        images[i] = util.get_path(feeds[j], "thumbnails")
                        + images[i].substring(images[i].lastIndexOf(main.SEPAR) + 1);
                     }
                     else
                     {
                        images[i]  = "";
                        widths[i]  = "";
                        heights[i] = "";
                     }
                  }

                  if(descriptions[i] == null || descriptions[i].length() < 8)
                     descriptions[i] = "";
                  else if( descriptions[i].length() >= 360 )
                     descriptions[i] = descriptions[i].substring(0, 360);

                  datum             = new data();
                  datum.title       = titles[i];
                  datum.description = descriptions[i];
                  datum.link        = links[i];
                  datum.image       = (titles[i] == null) ? "" : images[i];
                  datum.width  = (widths[i] == null) ? 0 : Integer.parseInt(widths[i]);
                  datum.height = (heights[i] == null) ? 0 : Integer.parseInt(heights[i]);

                  map.put(time.toMillis(false) - i, datum);
               }
            }
         }
      }

      /* Do not count items as read while we are updating the list. */
      ith.touched = false;

      data[] list = map.values().toArray(new data[map.size()]);
      int count   = map.size();

      titles             = new String[count];
      descriptions       = new String[count];
      links              = new String[count];
      images             = new String[count];
      Integer[] iwidths  = new Integer[count];
      Integer[] iheights = new Integer[count];

      for(int i = count - 1; i >= 0; i--)
      {
         int a = count - 1 - i;
         titles[a]       = list[i].title;
         descriptions[a] = list[i].description;
         links[a]        = list[i].link;
         images[a]       = list[i].image;
         iwidths[a]      = list[i].width;
         iheights[a]     = list[i].height;
         //times[i]        = list.get(i)[6]
      }

      if(titles.length > 0)
         publishProgress(titles, descriptions, links, images, iheights, iwidths);

      return animFadeIn;
   }

   @Override
   protected void onProgressUpdate(Object[] progress)
   {
      /* If these are the first items to be added to the list. */
      if(lv.getCount() == 0)
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
         top = (v == null) ? 0 : v.getTop();
         if(top == 0)
            index++;
         else if (top < 0 && lv.getChildAt(1) != null)
         {
            index++;
            v = lv.getChildAt(1);
            top = v.getTop();
         }
      }

      ith.add_array((String[]) progress[0], (String[]) progress[1], (String[]) progress[2], (String[]) progress[3], (Integer[]) progress[4], (Integer[]) progress[5]);
      ith.notifyDataSetChanged();

      if(flash)
         position = util.jump_to_latest_unread(ith.links, false, page_number);

      if(top != 0)
         lv.setSelectionFromTop(index, top - (ith.eight*2));
   }

   @Override
   protected void onPostExecute(Animation tun)
   {
      if(tun == null)
         return;

      /* Update the unread counts in the navigation drawer. */
      update.navigation(null);

      if(lv == null)
         return;

      /* If there were no items to start with (the listview is invisible).*/
      if(flash)
      {
         lv.setSelection(position);
         lv.setAnimation(tun);
         lv.setVisibility(View.VISIBLE);
      }
      /* Resume read item checking. */
      ith.touched = true;
   }
}
