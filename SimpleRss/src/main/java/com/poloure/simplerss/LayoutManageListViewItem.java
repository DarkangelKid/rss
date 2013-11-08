package com.poloure.simplerss;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

class LayoutManageListViewItem extends RelativeLayout
{
   private static final AbsListView.LayoutParams LAYOUT_PARAMS = new AbsListView.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
      setLayoutParams(LAYOUT_PARAMS);

      /* Set the background color of the ListView items. */
      setBackgroundColor(Color.WHITE);
   }

   void showItem(CharSequence title, CharSequence subtitle)
   {
      m_titleView.setText(title);
      Spanned spanned = Html.fromHtml((String) subtitle);
      m_subTitleView.setText(spanned);
   }
}
