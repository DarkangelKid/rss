package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

class FragmentManageTags extends ListFragment
{
   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      setListAdapter(new AdapterManagerTags());

      Update.AsyncCompatManageTagsRefresh(getListView(), getListAdapter());
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      return NavDrawer.s_drawerToggle.onOptionsItemSelected(item) ||
            Util.getString(R.string.add_feed).equals(item.getTitle()) ||
            super.onOptionsItemSelected(item);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      return inflater.inflate(R.layout.listview_cards, container, false);
   }

}
