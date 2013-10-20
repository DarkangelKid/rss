package com.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFeeds extends BaseAdapter
{
   private final LayoutInflater m_layoutInflater;
   private String[] m_titleArray = Util.EMPTY_STRING_ARRAY;
   private String[] m_infoArray  = Util.EMPTY_STRING_ARRAY;

   AdapterManageFeeds(Context context)
   {
      m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   void setArrays(String[] titles, String... infos)
   {
      m_titleArray = titles;
      m_infoArray = infos;
   }

   @Override
   public
   int getCount()
   {
      return m_titleArray.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return m_titleArray[position];
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
      ViewHolder holder;
      if(null == view)
      {
         view = m_layoutInflater.inflate(R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.m_title = (TextView) view.findViewById(R.id.title_item);
         holder.m_info = (TextView) view.findViewById(R.id.info_item);
         view.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) view.getTag();
      }

      holder.m_title.setText(m_titleArray[position]);
      holder.m_info.setText(m_infoArray[position]);

      return view;
   }

   static
   class ViewHolder
   {
      TextView m_title;
      TextView m_info;
   }

}
