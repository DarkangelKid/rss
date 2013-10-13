package com.poloure.simplerss;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

class OnPageChangeTags implements ViewPager.OnPageChangeListener
{
   private final Fragment    m_fragment;
   private final BaseAdapter m_navigationAdapter;

   OnPageChangeTags(Fragment fragment, BaseAdapter navigationAdapter)
   {
      m_fragment = fragment;
      m_navigationAdapter = navigationAdapter;
   }

   @Override
   public
   void onPageScrolled(int pos, float offset, int offsetPx)
   {
   }

   @Override
   public
   void onPageSelected(int pos)
   {
      ListAdapter adapter = Util.getCardAdapter(pos, m_fragment);
      if(0 == adapter.getCount())
      {
         FragmentManager fragmentManager = m_fragment.getFragmentManager();
         Context context = m_fragment.getActivity();
         Update.page(m_navigationAdapter, pos, fragmentManager, context);
      }
   }

   @Override
   public
   void onPageScrollStateChanged(int state)
   {
   }
}
