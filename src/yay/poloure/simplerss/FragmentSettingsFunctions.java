package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class FragmentSettingsFunctions extends ListFragment
{

   @Override
   public
   View onCreateView(LayoutInflater inf, ViewGroup cont, Bundle b)
   {
      return inf.inflate(R.layout.listview_settings_function, cont, false);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      setListAdapter(new AdapterSettingsFunctions());
   }
}
