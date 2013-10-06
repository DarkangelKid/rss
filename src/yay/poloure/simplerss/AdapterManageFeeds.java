package yay.poloure.simplerss;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFeeds extends BaseAdapter
{
   String[] m_titleArray = Util.EMPTY_STRING_ARRAY;
   String[] m_infoArray  = Util.EMPTY_STRING_ARRAY;

   void setArrays(String[] titles, String... infos)
   {
      m_titleArray = titles;
      m_infoArray = infos;
   }

   void setPosition(int pos, String title, String info)
   {
      m_titleArray[pos] = title;
      m_infoArray[pos] = info;
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
   View getView(int position, View view, ViewGroup parent)
   {
      View view1 = view;
      ViewHolder holder;
      if(null == view1)
      {
         view1 = Util.getLayoutInflater().inflate(R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.m_title = (TextView) view1.findViewById(R.id.title_item);
         holder.m_info = (TextView) view1.findViewById(R.id.info_item);
         view1.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) view1.getTag();
      }

      holder.m_title.setText(m_titleArray[position]);
      holder.m_info.setText(m_infoArray[position]);

      return view1;
   }

   private static
   class ViewHolder
   {
      TextView m_title;
      TextView m_info;
   }

}
