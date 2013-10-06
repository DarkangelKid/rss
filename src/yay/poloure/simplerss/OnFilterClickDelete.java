package yay.poloure.simplerss;
import android.content.DialogInterface;

class OnFilterClickDelete implements DialogInterface.OnClickListener
{
   private final AdapterManageFilters m_adapter;

   OnFilterClickDelete(AdapterManageFilters adapter)
   {
      m_adapter = adapter;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      Write.removeLine(Constants.FILTER_LIST, m_adapter.getItem(position), false);
      m_adapter.removePosition(position);
      m_adapter.notifyDataSetChanged();
   }
}
