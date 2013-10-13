package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerTabStrip;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class OnClickSettingsColor implements View.OnClickListener
{
   private final BaseAdapter m_adapter;
   private final int         m_clickedColour;
   private final Context     m_context;

   OnClickSettingsColor(BaseAdapter adapterSettingsUi, int colour, Context context)
   {
      m_context = context;
      m_adapter = adapterSettingsUi;
      m_clickedColour = colour;
   }

   @Override
   public
   void onClick(View v)
   {
         /* Write the new colour to file. */
      String colorSettingsPath = Constants.SETTINGS_DIR + Constants.STRIP_COLOR;
      Util.remove(colorSettingsPath, m_context);
      Resources resources = m_context.getResources();
      Write.single(colorSettingsPath,
            resources.getStringArray(R.array.settings_colours)[m_clickedColour], m_context);

         /* Change the selected square to alpha 1 and the rest to 0.5. */
      for(ImageView colour : ((AdapterSettingsUi) m_adapter).m_colorViews)
      {
         colour.setAlpha(AdapterSettingsUi.COLOR_SELECT_OPACITY);
      }
      v.setAlpha(1.0f);

         /* Set the new colour. */
      for(PagerTabStrip strip : Constants.PAGER_TAB_STRIPS)
      {
         Util.setStripColor(strip, m_context);
      }
   }
}
