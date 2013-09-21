package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

class FragmentManageFilters extends ListFragment
{
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Override
   public View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
   {
      return inf.inflate(R.layout.listview_cards, cont, false);
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView listview = getListView();
      final AdapterManageFilters adapter = new AdapterManageFilters();

      setListAdapter(adapter);

      adapter.setTitles(Read.file(FeedsActivity.FILTER_LIST));

      final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
      build.setCancelable(true)
           .setPositiveButton(getString(R.string.delete_dialog), new FilterDeleteClick(adapter));

      listview.setOnItemLongClickListener(new OnItemLongClickListener()
      {
         @Override
         public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                        long id)
         {
            build.show();
            return true;
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.DRAWER_TOGGLE.onOptionsItemSelected(item))
      {
         return true;
      }
      if("add".equals(item.getTitle()))
      {
         FeedDialog.showAddFilterDialog();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   static class FilterDeleteClick implements DialogInterface.OnClickListener
   {
      AdapterManageFilters m_adapter;

      FilterDeleteClick(AdapterManageFilters adapter)
      {
         m_adapter = adapter;
      }

      @Override
      public void onClick(DialogInterface dialog, int id_no)
      {
         Write.removeLine(FeedsActivity.FILTER_LIST, m_adapter.getItem(id_no), false);
         m_adapter.removePosition(id_no);
      }
   }
}
