package yay.poloure.simplerss;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFeeds extends BaseAdapter
{
   static String[] s_titleArray = Util.EMPTY_STRING_ARRAY;
   static String[] s_infoArray  = Util.EMPTY_STRING_ARRAY;

   void setArrays(String[] titles, String... infos)
   {
      s_titleArray = titles;
      s_infoArray = infos;
   }

   void setPosition(int pos, String title, String info)
     {
         s_titleArray[pos] = title;
         s_infoArray[pos] = info;
         notifyDataSetChanged();
     }

   @Override
   public
   int getCount()
   {
      return s_titleArray.length;
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   String getItem(int position)
   {
      return s_titleArray[position];
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

      holder.m_title.setText(s_titleArray[position]);
      holder.m_info.setText(s_infoArray[position]);

      return view1;
   }

   static
   class ViewHolder
   {
      TextView m_title;
      TextView m_info;
   }

}
