package yay.poloure.simplerss;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;

class PagerAdapterManage extends FragmentPagerAdapter
{
   static final ListFragment[] MANAGE_FRAGMENTS = {
         new FragmentManageTags(), new FragmentManageFeeds(), new FragmentManageFilters(),
   };

   private static final String[] MANAGE_TITLES = Util.getArray(R.array.manage_titles);

   public PagerAdapterManage(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public int getCount()
   {
      return MANAGE_FRAGMENTS.length;
   }

   @Override
   public ListFragment getItem(int position)
   {
      return MANAGE_FRAGMENTS[position];
   }

   @Override
   public String getPageTitle(int position)
   {
      return MANAGE_TITLES[position];
   }
}
