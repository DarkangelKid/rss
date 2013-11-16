package com.poloure.simplerss;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class AdapterManageFragments extends BaseAdapter
{
   private final Context m_context;
   private Editable[] m_editables = new Editable[0];

   AdapterManageFragments(Context context)
   {
      m_context = context;
   }

   void setEditable(Editable[] titleArray)
   {
      m_editables = titleArray.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_editables.length;
   }

   @Override
   public
   Editable getItem(int position)
   {
      return m_editables[position];
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
      ((LayoutManageListViewItem) view).showItem(m_editables[position]);
      return view;
   }
}
