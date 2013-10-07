package yay.poloure.simplerss;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.BaseAdapter;

class PagerAdapterFeeds extends FragmentPagerAdapter
{
   private       String[]    m_currentTags;
   private final BaseAdapter m_navigationAdapter;

   PagerAdapterFeeds(BaseAdapter navigationAdapter, FragmentManager fm)
   {
      super(fm);
      m_navigationAdapter = navigationAdapter;
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
      return new FragmentCard(m_navigationAdapter);
   }

   void updatePages()
   {
      m_currentTags = Read.file(Constants.TAG_LIST);
   }
}
