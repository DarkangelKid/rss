package yay.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapterManage extends FragmentPagerAdapter
{
   static final         Fragment[] MANAGE_FRAGMENTS = {
         new FragmentManageTags(), new FragmentManageFeeds(), new FragmentManageFilters(),
   };
   private static final String[]   MANAGE_TITLES    = Util.getArray(R.array.manage_titles);

   PagerAdapterManage(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public
   int getCount()
   {
      return MANAGE_FRAGMENTS.length;
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return MANAGE_TITLES[position];
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return MANAGE_FRAGMENTS[position];
   }
}
