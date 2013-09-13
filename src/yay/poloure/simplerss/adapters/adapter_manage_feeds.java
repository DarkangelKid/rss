package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

class adapter_manage_feeds extends BaseAdapter
{
   static String[] title_array = new String[0];
   static String[] info_array  = new String[0];

   adapter_manage_feeds()
   {
   }

   void set_items(String[] new_titles, String[] new_infos)
   {
      title_array = new_titles;
      info_array = new_infos;
      notifyDataSetChanged();
   }

   void set_position(int pos, String new_title, String new_info)
   {
      title_array[pos]  = new_title;
      info_array[pos]   = new_info;
      notifyDataSetChanged();
   }

   @Override
   public int getCount()
   {
      return title_array.length;
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   void remove_item(int position)
   {
      title_array = util.remove_element(title_array, position);
      info_array  = util.remove_element(info_array, position);
   }

   @Override
   public String getItem(int position)
   {
      return title_array[position];
   }

   String get_info(int position)
   {
      return info_array[position];
   }

   @Override
   public View getView(int position, View cv, ViewGroup parent)
   {
      ViewHolder holder;
      if(cv == null)
      {
         cv = util.get_inflater().inflate(R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.title_view = (TextView) cv.findViewById(R.id.title_item);
         holder.info_view = (TextView) cv.findViewById(R.id.info_item);
         cv.setTag(holder);
      }
      else
         holder = (ViewHolder) cv.getTag();

      holder.title_view.setText(title_array[position]);
      holder.info_view.setText(info_array[position]);

      return cv;
   }

   static class ViewHolder
   {
      TextView title_view;
      TextView info_view;
   }

}
