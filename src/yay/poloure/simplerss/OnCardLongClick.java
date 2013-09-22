package yay.poloure.simplerss;
import android.app.AlertDialog;
import android.view.View;

/* This is the context menu that appears when you long click a feed item (card). */
class OnCardLongClick implements View.OnLongClickListener
{
   private static final String[] MENU_ITEMS = Util.getArray(R.array.card_menu_image);

   @Override
   public
   boolean onLongClick(View v)
   {
      String url = Util.getText(v, R.id.url);
      AlertDialog.Builder build = new AlertDialog.Builder(Util.getContext());
      build.setCancelable(true).setItems(MENU_ITEMS, new OnCardContextMenuClick(url)).show();
      return true;
   }
}
