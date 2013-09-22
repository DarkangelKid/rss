package yay.poloure.simplerss;
import android.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;

class OnFilterLongClick implements AdapterView.OnItemLongClickListener
{
   private AdapterManageFilters  m_filterAdapter;
   private FragmentManageFilters m_filters;

   public
   OnFilterLongClick(FragmentManageFilters filters, AdapterManageFilters adapter)
   {
      m_filters = filters;
      m_filterAdapter = adapter;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
   {
      AlertDialog.Builder build = new AlertDialog.Builder(m_filters.getActivity());
      build.setCancelable(true)
            .setPositiveButton(Util.getString(R.string.delete_dialog),
                  new OnFilterClickDelete(m_filterAdapter))
            .show();
      return true;
   }
}
