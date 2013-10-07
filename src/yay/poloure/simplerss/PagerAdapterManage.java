package yay.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.widget.BaseAdapter;

class PagerAdapterManage extends FragmentPagerAdapter
{
   static final         ListFragment[] MANAGE_FRAGMENTS = new ListFragment[3];
   private static final String[]       MANAGE_TITLES    = Util.getArray(R.array.manage_titles);

   PagerAdapterManage(BaseAdapter navigationAdapter, FragmentManager fm)
   {
      super(fm);
      MANAGE_FRAGMENTS[0] = new FragmentManageTags();
      MANAGE_FRAGMENTS[1] = new FragmentManageFeeds(navigationAdapter);
      MANAGE_FRAGMENTS[2] = new FragmentManageFilters();
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
