package yay.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class pageradapter_settings extends FragmentPagerAdapter
{
   static final Fragment[] fragments = new Fragment[]
   {
      new fragment_settings_function(),
      new fragment_settings_interface(),
   };

   static final String[] titles = util.get_array(R.array.settings_titles);

   public pageradapter_settings(FragmentManager fm)
   {
      super(fm);
   }

   @Override
   public int getCount()
   {
      return titles.length;
   }

   @Override
   public Fragment getItem(int position)
   {
      return fragments[position];
   }

   @Override
   public String getPageTitle(int position)
   {
      return titles[position];
   }
}
