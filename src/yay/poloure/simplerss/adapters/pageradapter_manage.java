package yay.poloure.simplerss;

import android.support.v4.app.ListFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class pageradapter_manage extends FragmentPagerAdapter
{
   static final ListFragment[] fragments = new ListFragment[]
   {
      new fragment_manage_groups(),
      new fragment_manage_feeds(),
      new fragment_manage_filters(),
   };

   static final String[] titles = util.get_array(R.array.manage_titles);

   public pageradapter_manage(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public int getCount()
   {
      return fragments.length;
   }

   @Override
   public ListFragment getItem(int position)
   {
      return fragments[position];
   }

   @Override
   public String getPageTitle(int position)
   {
      return titles[position];
   }
}
