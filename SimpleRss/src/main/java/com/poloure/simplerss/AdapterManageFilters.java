package com.poloure.simplerss;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFilters extends BaseAdapter
{
   private String[] m_filterTitles = new String[0];

   private final LayoutInflater m_layoutInflater;
   private TextView m_titleView;
   private final String m_filterFileName;
   private final String m_applicationFolder;

   AdapterManageFilters(String applicationFolder, String filterFileName,
         LayoutInflater layoutInflater)
   {
      m_applicationFolder = applicationFolder;
      m_filterFileName = filterFileName;
      m_layoutInflater = layoutInflater;
   }

   @Override
   public
   void notifyDataSetChanged()
   {
      m_filterTitles = Read.file(m_filterFileName, m_applicationFolder);
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
      return (long) position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      if(null == view)
      {
         view = m_layoutInflater.inflate(R.layout.manage_list_view_item, parent, false);
         m_titleView = (TextView) view.findViewById(R.id.first_text);
      }

      m_titleView.setText(m_filterTitles[position]);

      return view;
   }
}
