package yay.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   PagerAdapterFeeds(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public
   int getCount()
   {
      return FeedsActivity.s_currentTags.length;
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return new FragmentCard();
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return FeedsActivity.s_currentTags[position];
   }

   /*TODO @Override
   public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
   }

   @Override
   public void onPageSelected(int position) {
      mActionBar.setSelectedNavigationItem(position);
   }*/
}
