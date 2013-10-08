package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class FragmentManage extends Fragment
{
   private final BaseAdapter m_navigationAdapter;

   FragmentManage(BaseAdapter baseAdapter)
   {
      m_navigationAdapter = baseAdapter;
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setRetainInstance(false);
      setHasOptionsMenu(true);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      ViewPager pager = new ViewPager(Util.getContext());
      Constants.PAGER_TAB_STRIPS[1] = Util.newPagerTabStrip(Util.getContext());

      ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
      layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
      layoutParams.gravity = Gravity.TOP;

      FragmentManager fragmentManager = FeedsActivity.getActivity().getSupportFragmentManager();
      pager.setAdapter(new PagerAdapterManage(m_navigationAdapter, fragmentManager));
      pager.addView(Constants.PAGER_TAB_STRIPS[1], layoutParams);
      pager.setId(0x2000);

      return pager;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      FeedsActivity.s_optionsMenu.clear();

      inflater.inflate(R.menu.manage_overflow, FeedsActivity.s_optionsMenu);
      super.onCreateOptionsMenu(FeedsActivity.s_optionsMenu, inflater);
   }
}
