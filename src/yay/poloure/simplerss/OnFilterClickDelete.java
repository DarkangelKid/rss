package yay.poloure.simplerss;
import android.content.DialogInterface;
import android.widget.Adapter;
import android.widget.BaseAdapter;

class OnFilterClickDelete implements DialogInterface.OnClickListener
{
   private final Adapter m_adapter;

   OnFilterClickDelete(Adapter adapter)
   {
      m_adapter = adapter;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      Write.removeLine(Constants.FILTER_LIST, (CharSequence) m_adapter.getItem(position), false);
      ((AdapterManageFilters) m_adapter).removePosition(position);
      ((BaseAdapter) m_adapter).notifyDataSetChanged();
   }
}
