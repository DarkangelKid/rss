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

      adapter.set_items(Read.file(FeedsActivity.FILTER_LIST));

      listview.setOnItemLongClickListener(new OnItemLongClickListener()
      {
         @Override
         public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                        long id)
         {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true).setPositiveButton(getString(R.string.delete_dialog),
                                                          new DialogInterface.OnClickListener()
                                                          {
                                                             @Override
                                                             public void onClick(
                                                                   DialogInterface dialog,
                                                                   int id_no)
                                                             {
                                                                Write.removeLine(
                                                                      FeedsActivity.FILTER_LIST,
                                                                      adapter.getItem(position),
                                                                      false);
                                                                adapter.removePosition(position);
                                                             }
                                                          });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.drawer_toggle.onOptionsItemSelected(item))
         return true;
      if(item.getTitle().equals("add"))
      {
         FeedDialog.showAddFilterDialog();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
}
