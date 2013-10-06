package yay.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   private String[] m_currentTags;

   PagerAdapterFeeds(FragmentManager fm)
   {
      super(fm);
      m_currentTags = Read.file(Constants.TAG_LIST);
   }

   @Override
   public
   int getCount()
   {
      return m_currentTags.length;
   }

   @Override
   public
   String getPageTitle(int position)
   {
      return m_currentTags[position];
   }

   @Override
   public
   Fragment getItem(int position)
   {
      return new FragmentCard();
   }

   void updatePages()
   {
      m_currentTags = Read.file(Constants.TAG_LIST);
   }
}
