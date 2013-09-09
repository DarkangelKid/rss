package yay.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

class navigation_drawer
{
   ListView navigation_list;

   static String   current_title;
   static String[] NAV_TITLES = new String[0];
   static String   NAVIGATION;

   static adapter_navigation_drawer nav_adapter;
   static DrawerLayout              drawer_layout;
   static ActionBarDrawerToggle     drawer_toggle;

   public navigation_drawer(DrawerLayout draw_layout, ListView nav_list)
   {
      Context con = util.get_context();

      /* Get the main three fragment titles from the values resource. */
      NAV_TITLES  = new String[]
      {
         con.getString(R.string.feeds_title),
         con.getString(R.string.manage_title),
         con.getString(R.string.settings_title)
      };

      NAVIGATION = con.getString(R.string.navigation_title);

      /* Save the drawer layout to the public static variable then set the shadow of the drawer. */
      (drawer_layout = draw_layout).setDrawerShadow(R.drawable.drawer_shadow, android.view.Gravity.END);

      /* Create the action bar toggle and set it as the drawer open/closer after. */
      drawer_toggle = new ActionBarDrawerToggle((Activity) con, drawer_layout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
      {
         @Override
         public void onDrawerClosed(View drawerView)
         {
            /* If the title is still "Navigation", then change it back to current_title. */
            if(((String) main.action_bar.getTitle()).equals(NAVIGATION))
               main.action_bar.setTitle(current_title);
         }
         @Override
         public void onDrawerOpened(View drawerView)
         {
            /* Save the action bar's title to current title. Then change the title to NAVIGATION. */
            current_title = (String) main.action_bar.getTitle();
            main.action_bar.setTitle(NAVIGATION);
         }
      };

      /* Set the listeners (and save the navigation list to the public static variable). */
      drawer_layout                 .setDrawerListener(drawer_toggle);
      (navigation_list = nav_list)  .setOnItemClickListener(new click_navigation_drawer());

      drawer_toggle.syncState();

      /* Save a new adapter as the public static nav_adapter variable and set it as this lists adapter. */
      navigation_list.setAdapter(nav_adapter = new adapter_navigation_drawer(con));
   }

   static class update_navigation_adapter extends AsyncTask<int[], Void, int[]>
   {
      @Override
      protected int[] doInBackground(int[]... counts)
      {
         /* If null was passed into the task, count the unread items. */
         return (counts[0] != null) ? counts[0] : util.get_unread_counts(main.cgroups);
      }

      @Override
      protected void onPostExecute(int[] pop)
      {
         /* Set the titles & counts arrays in this file and notifiy the adapter. */
         nav_adapter.set_titles(main.cgroups);
         nav_adapter.set_counts(pop);
         nav_adapter.notifyDataSetChanged();
      }
   }

   class click_navigation_drawer implements ListView.OnItemClickListener
   {
      @Override
      public void onItemClick(AdapterView parent, View view, int position, long id)
      {
         /* Close the drawer on any click of a navigation item. */
         drawer_layout.closeDrawer(navigation_list);

         /* Determine the new title based on the position of the item clicked. */
         String selected_title = (position > 3) ? NAV_TITLES[0] : NAV_TITLES[position];

         /* If the item selected was a group, change the viewpager to that group. */
         if(position > 3)
            main.viewpager.setCurrentItem(position - 4);

         position = (position > 3) ? 0 : position;

         /* If the selected title is the title of the current page, exit. */
         if(current_title.equals(selected_title))
            return;

         /* Hide the current fragment and display the selected one. */
         util.show_fragment(main.main_fragments[position]);

         /* Set the title text of the actionbar to the selected item. */
         main.action_bar.setTitle(selected_title);
      }
   }
}
