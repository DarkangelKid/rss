package com.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManagerTags extends BaseAdapter
{
   private static final long      TAG_SWAP_FADE_DURATION = 120L;
   private static final Animation FADE_IN                = new AlphaAnimation(0.0F, 1.0F);
   private static final Animation FADE_OUT               = new AlphaAnimation(1.0F, 0.0F);
   private              String[]  m_tagArray             = new String[0];
   private              String[]  m_infoArray            = new String[0];
   private final LayoutInflater m_layoutInflater;

   AdapterManagerTags(Context context)
   {
      m_layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      FADE_OUT.setDuration(TAG_SWAP_FADE_DURATION);
      FADE_OUT.setInterpolator(new DecelerateInterpolator());

      FADE_IN.setDuration(TAG_SWAP_FADE_DURATION);
      FADE_IN.setInterpolator(new DecelerateInterpolator());
   }

   void setArrays(String[] tags, String... tagInformation)
   {
      m_tagArray = tags;
      m_infoArray = tagInformation;
   }

   @Override
   public
   int getCount()
   {
      return m_tagArray.length;
   }

   @Override
   public
   String getItem(int pos)
   {
      return m_tagArray[pos];
   }

   @Override
   public
   long getItemId(int pos)
   {
      return pos;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      ViewHolder holder;
      if(null == view)
      {
         view = m_layoutInflater.inflate(R.layout.manage_group_item, parent, false);
         holder = new ViewHolder();
         holder.m_tagView = (TextView) view.findViewById(R.id.tag_item);
         holder.m_infoView = (TextView) view.findViewById(R.id.tag_feeds);
         view.setTag(holder);
      }
      else
      {
         holder = (ViewHolder) view.getTag();
      }

      holder.m_tagView.setText(m_tagArray[position]);
      holder.m_infoView.setText(m_infoArray[position]);

      return view;
   }

   static
   class ViewHolder
   {
      TextView m_tagView;
      TextView m_infoView;
   }
}
