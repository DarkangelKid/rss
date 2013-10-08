package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFilters extends BaseAdapter
{
   private static String[] s_filterTitles = Util.EMPTY_STRING_ARRAY;

   static
   void setTitles(String... titles)
   {
      s_filterTitles = titles;
   }

   @Override
   public
   int getCount()
   {
      return s_filterTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return s_filterTitles[position];
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

         String inflate = Context.LAYOUT_INFLATER_SERVICE;
         view1 = ((LayoutInflater) Util.getContext().getSystemService(inflate)).inflate(
               R.layout.manage_feed_item, parent, false);
         holder = new ViewHolder();
         holder.m_title = (TextView) view1.findViewById(R.id.title_item);
         view1.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) view1.getTag();
      }

      holder.m_title.setText(s_filterTitles[position]);

      return view1;
   }

   void removePosition(int position)
   {
      Util.arrayRemove(s_filterTitles, position);
      notifyDataSetChanged();
   }

   static
   class ViewHolder
   {
      // TODO
      TextView m_title;
   }

}
