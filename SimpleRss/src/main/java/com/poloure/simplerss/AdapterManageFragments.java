package com.poloure.simplerss;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class AdapterManageFragments extends BaseAdapter
{
   private final Context m_context;
   private CharSequence[] m_firstArray = new String[0];
   private CharSequence[] m_secondArray = new String[0];

   AdapterManageFragments(Context context)
   {
      m_context = context;
   }

   void setArrays(CharSequence[] titleArray, CharSequence... infoArray)
   {
      m_firstArray = titleArray.clone();
      m_secondArray = infoArray.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_firstArray.length;
   }

   @Override
   public
   CharSequence getItem(int position)
   {
      return m_firstArray[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return (long) position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = null == convertView ? new LayoutManageListViewItem(m_context) : convertView;
      ((LayoutManageListViewItem) view).showItem(m_firstArray[position], m_secondArray[position]);
      return view;
   }
}
