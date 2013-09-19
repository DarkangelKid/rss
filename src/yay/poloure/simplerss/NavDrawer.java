package yay.poloure.simplerss;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

class NavDrawer
{
   private final ListView navigation_list;

   private static String current_title;
   static final         String[] NAV_TITLES = Util.getArray(R.array.nav_titles);
   private static final String   NAVIGATION = Util.getString(R.string.navigation_title);

   private static AdapterNavDrawer      nav_adapter;
   static         DrawerLayout          drawer_layout;
   static         ActionBarDrawerToggle drawer_toggle;

   public NavDrawer(DrawerLayout draw_layout, ListView nav_list)
   {
      drawer_layout = draw_layout;

      /* Create the action bar toggle and set it as the drawer open/closer after. */
      drawer_toggle = new DrawerToggleClick();

      /* Set the listeners (and save the navigation list to the public static variable). */
      drawer_layout.setDrawerListener(drawer_toggle);
      (navigation_list = nav_list).setOnItemClickListener(new NavDrawerItemClick());

      drawer_toggle.syncState();

      /* Save a new adapter as the public static nav_adapter variable and set it as this lists
      adapter. */
      navigation_list.setAdapter(nav_adapter = new AdapterNavDrawer());
   }

   static class RefreshNavAdapter extends AsyncTask<int[], Void, int[]>
   {
      @SuppressWarnings("ConstantOnRightSideOfComparison")
      @Override
      protected int[] doInBackground(int[]... counts)
      {
         /* If null was passed into the task, count the unread items. */
         return null != counts[0] ? counts[0] : Util.getUnreadCounts(FeedsActivity.ctags);
      }

      @Override
      protected void onPostExecute(int[] pop)
      {
         /* Set the titles & counts arrays in this file and notifiy the adapter. */
         nav_adapter.setTitles(FeedsActivity.ctags);
         nav_adapter.setCounts(pop);
         nav_adapter.notifyDataSetChanged();
      }
   }

   private static class DrawerToggleClick extends ActionBarDrawerToggle
   {
      public DrawerToggleClick()
      {
         super((Activity) Util.getContext(), drawer_layout, R.drawable.ic_drawer, R.string.drawer_open,
               R.string.drawer_close);
      }

      @Override
      public void onDrawerClosed(View drawerView)
      {
           /* If the m_title is still "Navigation", then change it back to current_title. */
         if(FeedsActivity.bar.getTitle().equals(NAVIGATION))
         {
            FeedsActivity.bar.setTitle(current_title);
         }
      }

      @Override
      public void onDrawerOpened(View drawerView)
      {
           /* Save the action bar's m_title to current m_title. Then change the m_title to
           NAVIGATION. */
         current_title = (String) FeedsActivity.bar.getTitle();
         FeedsActivity.bar.setTitle(NAVIGATION);
      }
   }

   private class NavDrawerItemClick implements ListView.OnItemClickListener
   {
      @SuppressWarnings("ConstantOnRightSideOfComparison")
      @Override
      public void onItemClick(AdapterView parent, View view, int position, long id)
      {
         /* Close the drawer on any click of a navigation item. */
         drawer_layout.closeDrawer(navigation_list);

         /* Determine the new m_title based on the position of the item clicked. */
         String selectedTitle = 3 < position ? NAV_TITLES[0] : NAV_TITLES[position];

         /* If the item selected was a tag, change the viewpager to that tag. */
         if(3 < position)
         {
            FeedsActivity.viewpager.setCurrentItem(position - 4);
         }

         position = 3 < position ? 0 : position;

         /* If the selected m_title is the m_title of the current page, exit. */
         if(current_title.equals(selectedTitle))
         {
            return;
         }

         /* Hide the current fragment and display the selected one. */
         Util.showFragment(FeedsActivity.main_fragments[position]);

         /* Set the m_title text of the actionbar to the selected item. */
         FeedsActivity.bar.setTitle(selectedTitle);
      }
   }
}
