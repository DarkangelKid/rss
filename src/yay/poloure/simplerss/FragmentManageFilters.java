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
import android.widget.ListView;

class FragmentManageFilters extends ListFragment
{
   private static
   void showAddFilterDialog()
   {
      Context con = Util.getContext();
      LayoutInflater inf = LayoutInflater.from(con);
      View addFilterLayout = inf.inflate(R.layout.add_filter_dialog, null);

      AlertDialog.Builder build = new AlertDialog.Builder(con);
      build.setTitle("Add Filter")
            .setView(addFilterLayout)
            .setCancelable(true)
            .setNegativeButton(con.getString(R.string.cancel_dialog), new OnDialogClickCancel());
      AlertDialog addFilterDialog = build.create();

      addFilterDialog.getWindow()
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

      addFilterDialog.setButton(DialogInterface.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
            new OnFilterDialogClickAdd(addFilterLayout, addFilterDialog));
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
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      return inflater.inflate(R.layout.listview_cards, container, false);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      AdapterManageFilters adapter = new AdapterManageFilters();
      setListAdapter(adapter);

      ListView listview = getListView();

      AdapterManageFilters.setTitles(Read.file(Constants.FILTER_LIST));

      listview.setOnItemLongClickListener(new OnFilterLongClick(this, adapter));
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem item)
   {
      if(NavDrawer.s_drawerToggle.onOptionsItemSelected(item))
      {
         return true;
      }
      if(Util.getString(R.string.add_feed).equals(item.getTitle()))
      {
         showAddFilterDialog();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

}
