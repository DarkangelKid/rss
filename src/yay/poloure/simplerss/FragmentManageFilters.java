package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

class FragmentManageFilters extends ListFragment
{
   static
   void showAddFilterDialog()
   {
      Context con = Util.getContext();
      LayoutInflater inf = LayoutInflater.from(con);
      final View addFilterLayout = inf.inflate(R.layout.add_filter_dialog, null);

      AlertDialog.Builder build = new AlertDialog.Builder(con);
      build.setTitle("Add Filter")
            .setView(addFilterLayout)
            .setCancelable(true)
            .setNegativeButton(con.getString(R.string.cancel_dialog),
                  new DialogInterface.OnClickListener()
                  {
                     @Override
                     public
                     void onClick(DialogInterface dialog, int id)
                     {
                     }
                  });
      final AlertDialog addFilterDialog = build.create();

      addFilterDialog.getWindow()
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

      addFilterDialog.setButton(DialogInterface.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
            new DialogInterface.OnClickListener()
            {
               @Override
               public
               void onClick(DialogInterface dialog, int which)
               {
                  String feed = Util.getText((TextView) addFilterLayout);
                  String path = FeedsActivity.FILTER_LIST;
                  String[] filters = Read.file(path);
                  if(-1 != Util.index(filters, feed))
                  {
                     Write.single(path, feed + FeedsActivity.NL);
                  }
                  AdapterManageFilters.setTitles(filters);
                  addFilterDialog.hide();
               }
            });
      addFilterDialog.show();
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Override
   public
   View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
   {
      return inf.inflate(R.layout.listview_cards, cont, false);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView listview = getListView();
      AdapterManageFilters adapter = new AdapterManageFilters();

      setListAdapter(adapter);

      AdapterManageFilters.setTitles(Read.file(FeedsActivity.FILTER_LIST));

      final AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
      build.setCancelable(true)
            .setPositiveButton(getString(R.string.delete_dialog), new FilterDeleteClick(adapter));

      listview.setOnItemLongClickListener(new OnItemLongClickListener()
      {
         @Override
         public
         boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
         {
            build.show();
            return true;
         }
      });
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.DRAWER_TOGGLE.onOptionsItemSelected(item))
      {
         return true;
      }
      if("add".equals(item.getTitle()))
      {
         showAddFilterDialog();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   static
   class FilterDeleteClick implements DialogInterface.OnClickListener
   {
      AdapterManageFilters m_adapter;

      FilterDeleteClick(AdapterManageFilters adapter)
      {
         m_adapter = adapter;
      }

      @Override
      public
      void onClick(DialogInterface dialog, int id_no)
      {
         Write.removeLine(FeedsActivity.FILTER_LIST, m_adapter.getItem(id_no), false);
         m_adapter.removePosition(id_no);
      }
   }
}
