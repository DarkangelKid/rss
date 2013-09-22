package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SpinnerAdapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

class FeedDialog
{

   static
   void showAddDialog(String... ctags)
   {
      Context con = Util.getContext();
      LayoutInflater inf = LayoutInflater.from(con);
      final View add_rss_dialog = inf.inflate(R.layout.add_rss_dialog, null);
      String[] spinner_tags = Arrays.copyOfRange(ctags, 1, ctags.length);
      final AdapterView<SpinnerAdapter> tag_spinner
            = (AdapterView<SpinnerAdapter>) add_rss_dialog.findViewById(R.id.tag_spinner);

      SpinnerAdapter adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
            spinner_tags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      tag_spinner.setAdapter(adapter);

      final AlertDialog add_feed_dialog = new AlertDialog.Builder(con).create();
      add_feed_dialog.setTitle(R.string.add_dialog_title);
      add_feed_dialog.setView(add_rss_dialog);
      add_feed_dialog.setCancelable(true);
      add_feed_dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            con.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener()
      {
         @Override
         public
         void onClick(DialogInterface dialog, int id)
         {
         }
      });

      add_feed_dialog.setButton(DialogInterface.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
            new DialogInterface.OnClickListener()
            {
               @Override
               public
               void onClick(DialogInterface dialog, int which)
               {
                  String new_tag = Util.getText(add_rss_dialog, R.id.tag_edit);
                  String URL_check = Util.getText(add_rss_dialog, R.id.URL_edit);
                  String feed_name = Util.getText(add_rss_dialog, R.id.name_edit);

                  new_tag = new_tag.toLowerCase(FeedsActivity.locale);
                  String spinner_tag;
                  try
                  {
                     spinner_tag = tag_spinner.getSelectedItem().toString();
                  }
                  catch(RuntimeException e)
                  {
                     e.printStackTrace();
                     spinner_tag = "";
                  }
                  Update.checkFeedExists(add_feed_dialog, new_tag, feed_name, "add", "",
                        spinner_tag, 0, URL_check);
               }
            });
      add_feed_dialog.show();
   }

   static
   class CheckFeed extends AsyncTask<String, Void, String[]>
   {
      static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[/\\?%*|<>:]");
      static final Pattern SPLIT_SPACE        = Pattern.compile(" ");
      final AlertDialog dialog;
      final String      mode;
      final String      spinner_tag;
      final String      current_title;
      final int         pos;
      boolean existing_tag;
      boolean real;
      String  tag;
      String  name;


      CheckFeed(AlertDialog edit_dialog, String new_tag, String feed_name, String moder,
            String current_tit, String spin_tag, int position)
      {
         dialog = edit_dialog;
         tag = new_tag;
         name = feed_name;
         mode = moder;
         spinner_tag = spin_tag;
         current_title = current_tit;
         pos = position;
         Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
         if(null != button)
         {
            button.setEnabled(false);
         }
      }

      static
      void editFeed(String oldFeed, String newFeed, String newUrl, String newTag, int position)
      {

         String oldFeedFolder = Util.getPath(oldFeed, "");
         String newFeedFolder = Util.getPath(newFeed, "");

         if(!oldFeed.equals(newFeed))
         {
            Util.move(oldFeedFolder, newFeedFolder);
         }

         /* Replace the all_tag file with the new image and data. */
         String index = FeedsActivity.INDEX;
         String entry = String.format(FeedsActivity.INDEX_FORMAT, newFeed, newUrl, newTag);

         Write.removeLine(index, oldFeed, true);
         Write.single(index, entry + FeedsActivity.NL);

         AdapterManageFeeds adpt
               = (AdapterManageFeeds) PagerAdapterManage.MANAGE_FRAGMENTS[1].getListAdapter();

         adpt.setPosition(position, newFeed, newUrl + FeedsActivity.NL + newTag + " • " +
               Integer.toString(Read.count(FeedsActivity.GROUPS_DIR + newTag +
                     FeedsActivity.SEPAR + newFeed +
                     FeedsActivity.SEPAR + newFeed +
                     FeedsActivity.CONTENT) - 1) +
               " items");

         /// To ManageRefresh the counts and the order of the tags.
         Util.updateTags();
         Update.manageTags();
      }

      static
      byte[] concat(byte[] first, byte... second)
      {
         if(null == first)
         {
            return second;
         }
         if(null == second)
         {
            return first;
         }
         byte[] result = Arrays.copyOf(first, first.length + second.length);
         System.arraycopy(second, 0, result, first.length, second.length);
         return result;
      }

      @Override
      protected
      String[] doInBackground(String... passed_url)
      {
         /* If the m_imageViewTag entry has text, check to see if it is an old m_imageViewTag
          * or if it is new. */
         if(tag.isEmpty())
         {
            tag = spinner_tag.isEmpty() ? "Unsorted" : spinner_tag;
            existing_tag = spinner_tag.isEmpty();
         }
         else
         {
            String[] ctags = Read.file(FeedsActivity.GROUP_LIST);
            for(String gro : ctags)
            {
               if(gro.toLowerCase(FeedsActivity.locale)
                     .equals(tag.toLowerCase(FeedsActivity.locale)))
               {
                  tag = gro;
                  existing_tag = true;
               }
            }
            if(!existing_tag)
            {
               String[] words = SPLIT_SPACE.split(tag);
               tag = "";

               for(String word : words)
               {
                  tag += word.substring(0, 1).toUpperCase(FeedsActivity.locale) +
                        word.substring(1).toLowerCase(FeedsActivity.locale) + ' ';
               }
               tag = tag.substring(0, tag.length() - 1);
            }

         }

         String[] checkList = passed_url[0].contains("http") ? new String[]{passed_url[0]}
               : new String[]{
                     "http://" + passed_url[0], "https://" + passed_url[0]
               };

         String url = "";
         String feed_title = "";
         try
         {
            for(String check : checkList)
            {
               try
               {
                  BufferedInputStream in = null;
                  try
                  {
                     in = new BufferedInputStream(new URL(check).openStream());
                     byte[] data = new byte[512];
                     in.read(data, 0, 512);

                     String line = new String(data);
                     if(line.contains("rss") || line.contains("Atom") || line.contains("atom"))
                     {
                        while(!line.contains(FeedsActivity.TAG_TITLE) &&
                              !line.contains(FeedsActivity.ENDTAG_TITLE))
                        {
                           byte[] data2 = new byte[512];
                           in.read(data2, 0, 512);
                           data = concat(data, data2);
                           line = new String(data);
                        }
                        int ind = line.indexOf('>', line.indexOf(FeedsActivity.TAG_TITLE) + 1);
                        feed_title = line.substring(ind, line.indexOf("</", ind));
                        real = true;
                        url = check;
                        break;
                     }
                  }
                  finally
                  {
                     if(null != in)
                     {
                        in.close();
                     }
                  }
               }
               catch(MalformedURLException e)
               {
                  e.printStackTrace();
               }
               catch(IOException e)
               {
                  e.printStackTrace();
               }
            }
         }
         catch(RuntimeException e)
         {
            e.printStackTrace();
         }
         return new String[]{url, feed_title};
      }

      @Override
      protected
      void onPostExecute(String[] ton)
      {
         if(real)
         {
            if(!existing_tag)
            {
               Write.single(FeedsActivity.GROUP_LIST, tag + FeedsActivity.NL);
               Util.updateTags();
            }
            if(name.isEmpty())
            {
               name = ton[1];
            }

            name = ILLEGAL_FILE_CHARS.matcher(name).replaceAll("");

            if("edit".equals(mode))
            {
               editFeed(current_title, name, ton[0], tag, pos);
            }
            else
            {

      /* Create folders if they do not exist. */
               Util.mkdir(Util.getPath(name, "images"));
               Util.mkdir(Util.getPath(name, "thumbnails"));

      /* Create the csv. */
               String feedInfo = String.format(FeedsActivity.INDEX_FORMAT, name, ton[0], tag) +
                     FeedsActivity.NL;

      /* Save the feed to the index. */
               String index = FeedsActivity.INDEX;
               Write.single(index, feedInfo);

      /* Update the manage listviews with the new information. */
               if(null != PagerAdapterManage.MANAGE_FRAGMENTS[1].getListAdapter())
               {
                  Update.manageFeeds();
               }
               if(null != PagerAdapterManage.MANAGE_FRAGMENTS[0].getListAdapter())
               {
                  Update.manageTags();
               }
            }

            dialog.dismiss();
         }
         else
         {
            Util.post(Util.getString(R.string.feed_invalid));
            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if(null != button)
            {
               button.setEnabled(true);
            }
         }
      }
   }

   static
   class addFeedClick implements DialogInterface.OnClickListener
   {
      View                        edit_rss_dialog;
      AdapterView<SpinnerAdapter> tag_spinner;
      AlertDialog                 edit_feed_dialog;
      String                      current_title;
      int                         position;

      public
      addFeedClick(View edit_rss_dialog, AdapterView<SpinnerAdapter> tag_spinner,
            AlertDialog edit_feed_dialog, String current_title, String current_tag, int position)
      {
         this.edit_rss_dialog = edit_rss_dialog;
         this.tag_spinner = tag_spinner;
         this.edit_feed_dialog = edit_feed_dialog;
         this.current_title = current_title;
         this.position = position;
      }

      @Override
      public
      void onClick(DialogInterface dialog, int which)
      {
         String new_tag = Util.getText(edit_rss_dialog, R.id.tag_edit);
         String URL_check = Util.getText(edit_rss_dialog, R.id.URL_edit);
         String feed_name = Util.getText(edit_rss_dialog, R.id.name_edit);
         String spinner_tag = tag_spinner.getSelectedItem().toString();
         new_tag = new_tag.toLowerCase(FeedsActivity.locale);

         Update.checkFeedExists(edit_feed_dialog, new_tag, feed_name, "edit", current_title,
               spinner_tag, position, URL_check);
      }
   }
}
