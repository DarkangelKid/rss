package yay.poloure.simplerss;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFilters extends BaseAdapter
{
   static String[] s_filterTitles;

   static void setTitles(String... titles)
   {
      s_filterTitles = titles;
   }

   @Override
   public int getCount()
   {
      return s_filterTitles == null ? 0 : s_filterTitles.length;
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   void removePosition(int position)
   {
      Util.arrayRemove(s_filterTitles, position);
      notifyDataSetChanged();
   }

   @Override
   public String getItem(int position)
   {
      return s_filterTitles[position];
   }

   @Override
   public View getView(int position, View view, ViewGroup parent)
   {
      ViewHolder holder;
      if(null == view)
      {

         view = Util.getLayoutInflater().inflate(R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.m_title = (TextView) view.findViewById(R.id.title_item);
         view.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) view.getTag();
      }

      holder.m_title.setText(s_filterTitles[position]);

      return view;
   }

   static class ViewHolder
   {
      TextView m_title;
   }

}
