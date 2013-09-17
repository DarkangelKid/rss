package yay.poloure.simplerss;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

class fragment_manage_tags extends ListFragment
{
   static boolean multi_mode = false;
   static ActionMode actionmode;
   static ActionMode.Callback actionmode_callback;

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

      setListAdapter(new adapter_manage_tags());

      final ListView listview = getListView();

      listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
      listview.setItemsCanFocus(false);

      update.manage_tags();

      if(!main.HONEYCOMB)
         registerForContextMenu(listview);
      else
      {
         actionmode_callback = new ActionMode.Callback()
         {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
               getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
               return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
               /// false only if nothing.
               return false;
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
               switch (item.getItemId())
               {
                  default:
                     return false;
               }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
               for(int i = 0; i < listview.getAdapter().getCount(); i++)
               {
                  listview.setItemChecked(i, false);
                  try{
                     listview.getChildAt(i).setBackgroundColor(Color.WHITE);
                  }
                  catch(Exception e){
                  }
               }

               multi_mode = false;
               actionmode = null;
            }
         };
      }

      listview.setOnItemLongClickListener
      (
         new OnItemLongClickListener()
         {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id)
            {
               if(pos == 0)
               {
                  listview.setItemChecked(pos, false);
                  return false;
               }
               view = listview.getChildAt(pos);

               if(!listview.isItemChecked(pos))
                  listview.setItemChecked(pos, true);

               if(!multi_mode)
               {
                  multi_mode = true;
                  if(!main.HONEYCOMB)
                  {
                     Activity main_instance = (Activity) main.con;
                     actionmode = main_instance.startActionMode(actionmode_callback);
                  }
               }
               view.setBackgroundColor(Color.parseColor("#8033b5e5"));
               return true;
            }
         }
      );

      listview.setOnItemClickListener
      (
         new OnItemClickListener()
         {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
               if(position == 0)
               {
                  listview.setItemChecked(position, false);
                  return;
               }
               view = listview.getChildAt(position);

               if(multi_mode)
               {
                  if(!listview.isItemChecked(position))
                  {
                     view.setBackgroundColor(Color.parseColor("#ffffffff"));

                     if(listview.getCheckedItemPositions().indexOfValue(true) < 0)
                     {
                        actionmode.finish();
                        multi_mode = false;
                     }
                  }
                  else
                     view.setBackgroundColor(Color.parseColor("#8033b5e5"));
               }
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
         add_edit_dialog.show_add_dialog(main.ctags);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   static class refresh extends AsyncTask<Void, String[], Void>
   {
      Animation animFadeIn = AnimationUtils.loadAnimation(main.con, android.R.anim.fade_in);
      ListView listview   = pageradapter_manage.fragments[0].getListView();
      adapter_manage_tags adapter = (adapter_manage_tags) pageradapter_manage.fragments[0].getListAdapter();

      public refresh()
      {
         if(adapter.getCount() == 0)
            listview.setVisibility(View.INVISIBLE);
      }

      @Override
      protected Void doInBackground(Void... nothing)
      {
         String[][] content = util.create_info_arrays(main.ctags);
         publishProgress(content[1], content[0]);
         return null;
      }

      @Override
      protected void onProgressUpdate(String[][] progress)
      {
         if(adapter != null)
         {
            adapter.set_items(progress[0], progress[1]);
            adapter.notifyDataSetChanged();
         }
      }

      @Override
      protected void onPostExecute(Void nothing)
      {
         listview.setAnimation(animFadeIn);
         listview.setVisibility(View.VISIBLE);
      }
   }
}
