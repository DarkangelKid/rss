package yay.poloure.simplerss;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

class OnCardContextMenuClick implements DialogInterface.OnClickListener
{
   private final String m_url;

   public
   OnCardContextMenuClick(String url)
   {
      m_url = url;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int position)
   {
      Context con = Util.getContext();
      if(0 == position)
      {
         ClipboardManager clipboard = (ClipboardManager) con.getSystemService(
               Context.CLIPBOARD_SERVICE);

         if(Constants.HONEYCOMB)
         {
            ClipData clip = ClipData.newPlainText("Url", m_url);
            clipboard.setPrimaryClip(clip);
         }
         else
         {
            clipboard.setText(m_url);
         }
      }
      else if(1 == position)
      {
         con.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(m_url)));
      }
   }
}
