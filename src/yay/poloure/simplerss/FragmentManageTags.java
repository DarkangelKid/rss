package yay.poloure.simplerss;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

class FragmentManageTags extends ListFragment
{
   private static boolean             s_multiMode;
   private static ActionMode          s_actionmode;
   private static ActionMode.Callback s_actionmode_callback;

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      setListAdapter(new AdapterManagerTags());

      ListView listview = getListView();

      listview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
      listview.setItemsCanFocus(false);

      if(Constants.HONEYCOMB)
      {
         s_actionmode_callback = new ActionCallback(listview);
      }
      else
      {
         registerForContextMenu(listview);
      }

      listview.setOnItemLongClickListener(new ContextLongClick(listview));
      listview.setOnItemClickListener(new ContextClick(listview));
      Update.manageTags(getListView(), getListAdapter());
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
         FeedDialog.showAddDialog(FeedsActivity.s_currentTags);
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      return inflater.inflate(R.layout.listview_cards, container, false);
   }

   private static
   class ContextLongClick implements OnItemLongClickListener
   {
      private final ListView m_listview;

      public
      ContextLongClick(ListView listview)
      {
         m_listview = listview;
      }

      @Override
      public
      boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
      {
         if(0 == pos)
         {
            m_listview.setItemChecked(0, false);
            return false;
         }
         view = m_listview.getChildAt(pos);

         if(!m_listview.isItemChecked(pos))
         {
            m_listview.setItemChecked(pos, true);
         }

         if(!s_multiMode)
         {
            s_multiMode = true;
            if(!Constants.HONEYCOMB)
            {
               s_actionmode = FeedsActivity.getActivity().startActionMode(s_actionmode_callback);
            }
         }
         view.setBackgroundColor(Color.parseColor("#8033b5e5"));
         return true;
      }
   }

   static
   class ContextClick implements OnItemClickListener
   {
      ListView m_listview;

      ContextClick(ListView listview)
      {
         m_listview = listview;
      }

      @Override
      public
      void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
         if(0 == position)
         {
            m_listview.setItemChecked(0, false);
            return;
         }
         view = m_listview.getChildAt(position);

         if(s_multiMode)
         {
            if(m_listview.isItemChecked(position))
            {
               view.setBackgroundColor(Color.parseColor("#8033b5e5"));
            }
            else
            {
               view.setBackgroundColor(Color.parseColor("#ffffffff"));

               if(0 > m_listview.getCheckedItemPositions().indexOfValue(true))
               {
                  s_actionmode.finish();
                  s_multiMode = false;
               }
            }
         }
      }
   }

   static
   class ActionCallback implements ActionMode.Callback
   {
      ListView m_listview;

      ActionCallback(ListView listview)
      {
         m_listview = listview;
      }

      @Override
      public
      boolean onCreateActionMode(ActionMode mode, Menu menu)
      {
         //Util.getLayoutInflater().inflate(R.menu.context_menu, menu);
         return true;
      }

      @Override
      public
      boolean onPrepareActionMode(ActionMode mode, Menu menu)
      {
         /// false only if nothing.
         return false;
      }

      // Called when the user selects a contextual menu item
      @Override
      public
      boolean onActionItemClicked(ActionMode mode, MenuItem item)
      {
         switch(item.getItemId())
         {
            default:
               return false;
         }
      }

      @Override
      public
      void onDestroyActionMode(ActionMode mode)
      {
         for(int i = 0; i < m_listview.getAdapter().getCount(); i++)
         {
            m_listview.setItemChecked(i, false);
            try
            {
               m_listview.getChildAt(i).setBackgroundColor(Color.WHITE);
            }
            catch(RuntimeException e)
            {
               e.printStackTrace();
            }
         }

         s_multiMode = false;
         s_actionmode = null;
      }
   }
}
