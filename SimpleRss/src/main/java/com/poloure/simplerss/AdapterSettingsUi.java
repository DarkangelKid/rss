package com.poloure.simplerss;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterSettingsUi extends BaseAdapter
{
   private static final int TYPE_HEADING = 0;
   private static final int TYPE_CHECKBOX = 1;
   private static final int TYPE_SEEK_BAR = 2;
   private static final int[] TYPES = {TYPE_HEADING, TYPE_CHECKBOX, TYPE_SEEK_BAR};
   private final String[] m_interfaceTitles;
   private final String[] m_interfaceSummaries;
   private final String m_applicationFolder;
   private final Context m_context;

   AdapterSettingsUi(Context context, String applicationFolder, String[] adapterTitles,
         String[] adapterSummaries)
   {
      m_context = context;
      m_applicationFolder = applicationFolder;
      m_interfaceTitles = adapterTitles.clone();
      m_interfaceSummaries = adapterSummaries.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_interfaceTitles.length;
   }

   @Override
   public
   String getItem(int position)
   {
      return m_interfaceTitles[position];
   }

   @Override
   public
   long getItemId(int position)
   {
      return (long) position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      int viewType = getItemViewType(position);
      String title = m_interfaceTitles[position];
      String summary = m_interfaceSummaries[position];

      if(TYPE_HEADING == viewType)
      {
         view = null == view ? ViewSettingsHeader.newInstance(m_context) : convertView;
         ((TextView) view).setText(title);
      }
      else if(TYPE_CHECKBOX == viewType)
      {
         view = null == convertView ? new LayoutCheckBox(m_context) : convertView;
         ((LayoutCheckBox) view).showItem(title, summary, m_applicationFolder);
      }
      else if(TYPE_SEEK_BAR == viewType)
      {
         view = null == convertView ? new LayoutSeekBar(m_context) : convertView;
         ((LayoutSeekBar) view).showItem(title, summary, 100, m_applicationFolder);
      }
      return view;
   }

   @Override
   public
   boolean isEnabled(int position)
   {
      return false;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      return 0 == position ? TYPE_HEADING : TYPE_SEEK_BAR;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }

}
