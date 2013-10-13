package com.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFilters extends BaseAdapter
{
   private String[] m_filterTitles = Util.EMPTY_STRING_ARRAY;

   private final LayoutInflater m_layoutInflater;

   AdapterManageFilters(Context context)
   {
      m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   void setTitles(String... titles)
   {
      m_filterTitles = titles;
   }

   @Override
   public
   int getCount()
   {
      return m_filterTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return m_filterTitles[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View view, ViewGroup parent)
   {
      View view1 = view;
      ViewHolder holder;
      if(null == view1)
      {
         view1 = m_layoutInflater.inflate(R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.m_title = (TextView) view1.findViewById(R.id.title_item);
         view1.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) view1.getTag();
      }

      holder.m_title.setText(m_filterTitles[position]);

      return view1;
   }

   void removePosition(int position)
   {
      Util.arrayRemove(m_filterTitles, position);
      notifyDataSetChanged();
   }

   static
   class ViewHolder
   {
      // TODO
      TextView m_title;
   }

}
