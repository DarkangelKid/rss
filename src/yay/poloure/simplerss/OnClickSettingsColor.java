package yay.poloure.simplerss;
import android.support.v4.view.PagerTabStrip;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;


class OnClickSettingsColor implements View.OnClickListener
{
   private final BaseAdapter m_adapter;
   private final int         m_clickedColour;

   OnClickSettingsColor(BaseAdapter adapterSettingsUi, int colour)
   {
      m_adapter = adapterSettingsUi;
      m_clickedColour = colour;
   }

   @Override
   public
   void onClick(View v)
   {
         /* Write the new colour to file. */
      String colorSettingsPath = Constants.SETTINGS_DIR + Constants.STRIP_COLOR;
      Util.remove(colorSettingsPath);
      Write.single(colorSettingsPath, AdapterSettingsUi.HOLO_COLORS[m_clickedColour]);

         /* Change the selected square to alpha 1 and the rest to 0.5. */
      for(ImageView colour : ((AdapterSettingsUi) m_adapter).m_colorViews)
      {
         colour.setAlpha(AdapterSettingsUi.COLOR_SELECT_OPACITY);
      }
      v.setAlpha(1.0f);

         /* Set the new colour. */
      for(PagerTabStrip strip : Constants.PAGER_TAB_STRIPS)
      {
         Util.setStripColor(strip);
      }
   }
}
