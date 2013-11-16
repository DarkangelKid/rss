package com.poloure.simplerss;

import android.content.Context;
import android.text.Editable;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class LayoutManageListViewItem extends RelativeLayout
{
   private static final AbsListView.LayoutParams LAYOUT_PARAMS = new AbsListView.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
   private final TextView m_titleView;

   LayoutManageListViewItem(Context context)
   {
      super(context);
      inflate(context, R.layout.manage_item, this);

      /* Save the inflated views from the xml file as class fields. */
      m_titleView = (TextView) findViewById(R.id.first_text);

      /* Set the LayoutParams to match parent, match_parent. */
      setLayoutParams(LAYOUT_PARAMS);
   }

   void showItem(Editable text)
   {
      m_titleView.setText(text);
   }
}
