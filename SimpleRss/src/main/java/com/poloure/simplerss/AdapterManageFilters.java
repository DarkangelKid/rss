package com.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFilters extends BaseAdapter
{
   static final String   FILTER_LIST    = "filter_list.txt";
   private      String[] m_filterTitles = Util.EMPTY_STRING_ARRAY;

   private final LayoutInflater m_layoutInflater;
   private       TextView       m_titleView;
   private final Context        m_context;

   AdapterManageFilters(Context context)
   {
      m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      m_context = context;
   }

   @Override
   public
   void notifyDataSetChanged()
   {
      m_filterTitles = Read.file(FILTER_LIST, m_context);
      super.notifyDataSetChanged();
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
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      if(null == view)
      {
         view = m_layoutInflater.inflate(R.layout.manage_feed_item, parent, false);
         m_titleView = (TextView) view.findViewById(R.id.title_item);
      }

      m_titleView.setText(m_filterTitles[position]);

      return view;
   }
}
