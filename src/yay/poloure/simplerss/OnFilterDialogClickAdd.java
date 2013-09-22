package yay.poloure.simplerss;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

class OnFilterDialogClickAdd implements DialogInterface.OnClickListener
{
   private final View        m_addFilterLayout;
   private final AlertDialog m_addFilterDialog;

   public
   OnFilterDialogClickAdd(View addFilterLayout, AlertDialog addFilterDialog)
   {
      m_addFilterLayout = addFilterLayout;
      m_addFilterDialog = addFilterDialog;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      String feed = Util.getText((TextView) m_addFilterLayout);
      String path = Constants.FILTER_LIST;
      String[] filters = Read.file(path);
      if(-1 != Util.index(filters, feed))
      {
         Write.single(path, feed + Constants.NL);
      }
      AdapterManageFilters.setTitles(filters);
      m_addFilterDialog.hide();
   }
}
