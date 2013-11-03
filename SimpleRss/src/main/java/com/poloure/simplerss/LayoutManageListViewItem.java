package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class LayoutManageListViewItem extends RelativeLayout
{
   private final TextView m_titleView;
   private final TextView m_subTitleView;

   LayoutManageListViewItem(Context context)
   {
      super(context);
      inflate(context, R.layout.manage_list_view_item, this);

      /* Save the inflated views from the xml file as class fields. */
      m_titleView = (TextView) findViewById(R.id.first_text);
      m_subTitleView = (TextView) findViewById(R.id.second_text);

      /* Set the LayoutParams to match parent, match_parent. */
      int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
      AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(matchParent,
            matchParent);
      setLayoutParams(layoutParams);

      /* Set the background color of the ListView items. */
      setBackgroundColor(Color.WHITE);
   }

   void showItem(CharSequence title, CharSequence subtitle)
   {
      m_titleView.setText(title);
      m_subTitleView.setText(subtitle);
   }
}
