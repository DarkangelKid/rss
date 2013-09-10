package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class pageradapter_feeds extends FragmentPagerAdapter
{
   public pageradapter_feeds(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public int getCount()
   {
      return main.cgroups.length;
   }

   @Override
   public Fragment getItem(int position)
   {
      fragment_card f = new fragment_card();
      Bundle args = new Bundle();
      args.putInt("num", position);
      f.setArguments(args);
      return f;
   }

   @Override
   public String getPageTitle(int position)
   {
      return main.cgroups[position];
   }
}
