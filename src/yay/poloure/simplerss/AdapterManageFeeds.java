package yay.poloure.simplerss;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressWarnings("ConstantOnRightSideOfComparison")
class AdapterManageFeeds extends BaseAdapter
{
   private static String[] title_array;
   private static String[] info_array;

   void setArrays(String[] new_titles, String... new_infos)
   {
      title_array = new_titles;
      info_array = new_infos;
      notifyDataSetChanged();
   }

   void setPosition(int pos, String new_title, String new_info)
   {
      title_array[pos] = new_title;
      info_array[pos] = new_info;
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
      return (long) position;
   }

   static void remove_item(int position)
   {
      title_array = Util.arrayRemove(title_array, position);
      info_array = Util.arrayRemove(info_array, position);
   }

   @Override
   public String getItem(int position)
   {
      return title_array[position];
   }

   static String get_info(int position)
   {
      return info_array[position];
   }

   @Override
   public View getView(int position, View cv, ViewGroup parent)
   {
      ViewHolder holder;
      if(null == cv)
      {
         cv = Util.getLayoutInflater().inflate(R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.m_title = (TextView) cv.findViewById(R.id.title_item);
         holder.m_info = (TextView) cv.findViewById(R.id.info_item);
         cv.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) cv.getTag();
      }

      holder.m_title.setText(title_array[position]);
      holder.m_info.setText(info_array[position]);

      return cv;
   }

   static class ViewHolder
   {
      TextView m_title;
      TextView m_info;
   }

}
