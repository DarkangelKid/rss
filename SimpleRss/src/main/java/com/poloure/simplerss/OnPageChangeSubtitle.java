package com.poloure.simplerss;

import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;

class OnPageChangeSubtitle extends SimpleOnPageChangeListener
{
   private final String[]  m_pageTitles;
   private final ActionBar m_actionBar;

   OnPageChangeSubtitle(ActionBar actionBar, String[] pageTitles)
   {
      m_actionBar = actionBar;
      m_pageTitles = pageTitles.clone();
   }

   @Override
   public
   void onPageSelected(int position)
   {
      m_actionBar.setSubtitle(m_pageTitles[position]);
   }
}
