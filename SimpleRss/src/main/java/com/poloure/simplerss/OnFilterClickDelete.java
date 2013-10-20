package com.poloure.simplerss;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.BaseAdapter;

class OnFilterClickDelete implements DialogInterface.OnClickListener
{
   private final BaseAdapter m_adapter;
   private final Context     m_context;

   OnFilterClickDelete(BaseAdapter adapter, Context context)
   {
      m_adapter = adapter;
      m_context = context;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      CharSequence item = (CharSequence) m_adapter.getItem(position);
      Write.removeLine(Constants.FILTER_LIST, item, false, m_context);
      ((AdapterManageFilters) m_adapter).notifyDataSetChanged();
   }
}
