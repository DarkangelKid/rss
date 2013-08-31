package yay.poloure.simplerss;

import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class refresh_page extends AsyncTask<Integer, Object, Animation>
{
   private int page_number;
   private Boolean flash = false;
   private ListFragment l;
   private adapter_feeds_cards ith;
   private ListView lv;
   private int position = -3;

   @Override
   protected Animation doInBackground(Integer... page)
   {
      page_number               = page[0];
      String sep                = main.SEPAR;
      String group              = main.current_groups[page_number];
      String group_path         = main.storage + main.GROUPS_DIR + group + sep;
      String group_file_path    = group_path + group + main.TXT;
      String group_content_path = group_path + group + main.CONTENT;
      String thumbnail_path;

      if( !util.exists(group_file_path) || !util.exists(group_content_path) )
         return null;

      String[][] contenter  = read.csv(group_content_path, 't', 'd', 'l', 'i', 'w', 'h', 'g', 'f');
      String[] titles       = contenter[0];
      String[] descriptions = contenter[1];
      String[] links        = contenter[2];
      String[] images       = contenter[3];
      String[] widths       = contenter[4];
      String[] heights      = contenter[5];
      String[] groups       = contenter[6];
      String[] sources      = contenter[7];

      if( links.length == 0 || links[0] == null )
         return null;

      Set<String> existing_items = new HashSet<String>();
      try
      {
         existing_items = new HashSet<String>();
         Collections.addAll(existing_items, util.get_card_adapter(main.fman, main.viewpager, page_number).links);
      }
      catch(Exception e)
      {
      }

      Animation animFadeIn = AnimationUtils.loadAnimation(main.activity, android.R.anim.fade_in);

      int width, height, count = 0;

      for(int m = 0; m < titles.length; m++)
      {
         if(!existing_items.contains(links[m]))
            count++;
      }

      String[]  new_titles  = new String[count];
      String[]  new_des     = new String[count];
      String[]  new_images  = new String[count];
      String[]  new_links   = new String[count];
      Integer[] new_heights = new Integer[count];
      Integer[] new_widths  = new Integer[count];

      count = -1;

      for(int m = titles.length - 1; m >= 0; m--)
      {
         if(existing_items.add(links[m]))
         {
            count++;
            thumbnail_path = "";
            width = 0;
            height = 0;

            if(images[m] != null)
            {
               width = util.stoi(widths[m]);
               if(width > 32)
               {
                  height = util.stoi(heights[m]);
                  thumbnail_path = main.storage + main.GROUPS_DIR + groups[m]
                     + sep + sources[m] + sep + main.THUMBNAIL_DIR
                     + images[m].substring(images[m].lastIndexOf(sep) + 1, images[m].length());
               }
               else
                  width = 0;
            }

            if(descriptions[m] == null || descriptions[m].length() < 8)
               descriptions[m] = "";
            else if( descriptions[m].length() >= 360 )
               descriptions[m] = descriptions[m].substring(0, 360);
            if(titles[m] == null)
               titles[m] = "";

            new_titles  [count] = titles[m];
            new_links   [count] = links[m];
            new_des     [count] = descriptions[m];
            new_images  [count] = thumbnail_path;
            new_heights [count] = height;
            new_widths  [count] = width;
         }
      }

      while(lv == null)
      {
         try
         {
            Thread.sleep(5);
         }
         catch(Exception e){
         }
         if((main.viewpager != null)&&(l == null))
            l = (ListFragment) main.fman.findFragmentByTag("android:switcher:" + main.viewpager.getId() + ":" + Integer.toString(page_number));
         if((l != null)&&(ith == null))
            ith = ((adapter_feeds_cards) l.getListAdapter());
         if((l != null)&&(lv == null))
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

      /* Do not count items as read while we are updating the list. */
      ith.touched = false;

      if(new_titles.length > 0)
         publishProgress(new_titles, new_des, new_links, new_images, new_heights, new_widths);

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
         position = main.jump_to_latest_unread(ith.links, false, page_number);

      if(top != 0)
         lv.setSelectionFromTop(index, top - (ith.four*4));
   }

   @Override
   protected void onPostExecute(Animation tun)
   {
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
