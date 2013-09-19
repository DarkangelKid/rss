package yay.poloure.simplerss;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFilters extends BaseAdapter
{
   private static String[] title_list;

   public AdapterManageFilters()
   {
   }

   static void set_items(String... new_titles)
   {
      title_list = new_titles;
   }

   @Override
   public int getCount()
   {
      return title_list.length;
   }

   @Override
   public long getItemId(int position)
   {
      return (long) position;
   }

   void removePosition(int position)
   {
      Util.arrayRemove(title_list, position);
      notifyDataSetChanged();
   }

   @Override
   public String getItem(int position)
   {
      return title_list[position];
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      ViewHolder holder;
      if(null == convertView)
      {

         convertView = Util.getLayoutInflater().inflate(R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.m_title = (TextView) convertView.findViewById(R.id.title_item);
         convertView.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) convertView.getTag();
      }

      holder.m_title.setText(title_list[position]);

      return convertView;
   }

   static class ViewHolder
   {
      TextView m_title;
   }

}
