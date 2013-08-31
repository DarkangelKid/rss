package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class adapter_manage_filter extends BaseAdapter
{
   private static String[] title_list = new String[0];

   private static LayoutInflater inflater;

   public adapter_manage_filter(Context context_main)
   {
      inflater = (LayoutInflater) context_main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }

   public void set_items(String[] new_titles)
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
      return position;
   }

   public void remove_item(int position)
   {
      util.remove_element(title_list, position);
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
         if(convertView == null)
         {

            convertView = inflater.inflate(R.layout.manage_feed_item, parent, false);
            holder = new ViewHolder();
            holder.title_view = (TextView) convertView.findViewById(R.id.title_item);
            convertView.setTag(holder);
         }
         else
            holder = (ViewHolder) convertView.getTag();

         holder.title_view.setText(title_list[position]);

         return convertView;
   }

   static class ViewHolder
   {
      TextView title_view;
   }

}
