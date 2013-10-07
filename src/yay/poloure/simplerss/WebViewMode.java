package yay.poloure.simplerss;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.View;

class WebViewMode implements View.OnClickListener
{
   @Override
   public
   void onClick(View v)
   {
      ActionBar actionBar = FeedsActivity.getActivity().getSupportActionBar();
      actionBar.setTitle(Constants.OFFLINE);
      NavDrawer.s_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
      NavDrawer.s_drawerToggle.setDrawerIndicatorEnabled(false);
      actionBar.setDisplayHomeAsUpEnabled(true);
      FragmentManager fragmentManager = FeedsActivity.getActivity().getSupportFragmentManager();
      fragmentManager.beginTransaction()
            .hide(fragmentManager.findFragmentByTag(NavDrawer.NAV_TITLES[0]))
            .add(R.id.drawer_layout, new FragmentWebView(), Constants.OFFLINE)
            .addToBackStack("BACK")
            .commit();
   }
}
