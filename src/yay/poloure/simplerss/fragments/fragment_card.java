package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class fragment_card extends ListFragment
{
   public fragment_card()
   {
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setListAdapter(new adapter_card());
   }

   @Override
   public View onCreateView(LayoutInflater in, ViewGroup container, Bundle b)
   {
      return in.inflate(R.layout.listview_cards, container, false);
   }
}
