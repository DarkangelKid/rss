package yay.poloure.simplerss;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

class OnCardContextMenuClick implements DialogInterface.OnClickListener
{
   private String m_url;

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
      switch(position)
      {
         case 0:
            ClipboardManager clipboard = (ClipboardManager) con.getSystemService(
                  Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", m_url);
            clipboard.setPrimaryClip(clip);
            break;
         case 1:
            con.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(m_url)));
         /*case(2):
           break;*/
      }
   }
}
