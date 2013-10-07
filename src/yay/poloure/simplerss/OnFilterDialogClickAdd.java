package yay.poloure.simplerss;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

class OnFilterDialogClickAdd implements DialogInterface.OnClickListener
{
   private final View m_addFilterLayout;

   OnFilterDialogClickAdd(View addFilterLayout)
   {
      m_addFilterLayout = addFilterLayout;
   }

   private static
   String getText(TextView view)
   {
      return view.getText().toString().trim();
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String filter = getText((TextView) m_addFilterLayout);
      String path = Constants.FILTER_LIST;
      String[] filters = Read.file(path);
      if(-1 == Util.index(filters, filter))
      {
         Write.single(path, filter + Constants.NL);
      }
      AdapterManageFilters.setTitles(filters);
      ((Dialog) dialog).hide();
   }
}
