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

class fragment_manage_filters extends ListFragment
{
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
      final adapter_manage_filter adapter = new adapter_manage_filter();

      setListAdapter(adapter);

      final String filter_path = util.get_storage() + main.FILTER_LIST;

      adapter.set_items(read.file(filter_path));

      listview.setOnItemLongClickListener
      (
         new OnItemLongClickListener()
         {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
               AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
               builder.setCancelable(true)
               .setPositiveButton(getString(R.string.delete_dialog), new DialogInterface.OnClickListener()
               {
                  @Override
                  public void onClick(DialogInterface dialog, int id)
                  {
                     write.remove_string(filter_path, adapter.getItem(position), false);
                     adapter.remove_item(position);
                  }
               });
               AlertDialog alert = builder.create();
               alert.show();
               return true;
            }
         }
      );
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      if(navigation_drawer.drawer_toggle.onOptionsItemSelected(item))
         return true;
      else if(item.getTitle().equals("add"))
      {
         add_edit_dialog.show_add_filter_dialog();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
}
