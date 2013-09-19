package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

class FeedDialog
{
   static class CheckFeed extends AsyncTask<String, Void, String[]>
   {
      boolean existing_tag;
      boolean real;
      String  tag;
      String  name;
      final AlertDialog dialog;
      final String      mode;
      final String      spinner_tag;
      final String      current_tag;
      final String      current_title;
      final int         pos;
      static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[/\\?%*|<>:]");

      public CheckFeed(AlertDialog edit_dialog, String new_tag, String feed_name, String moder,
                       String current_tit, String current_grop, String spin_tag, int position)
      {
         dialog = edit_dialog;
         tag = new_tag;
         name = feed_name;
         mode = moder;
         spinner_tag = spin_tag;
         current_tag = current_grop;
         current_title = current_tit;
         pos = position;
         Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
         if(null != button)
         {
            button.setEnabled(false);
         }
      }

      @Override
      protected String[] doInBackground(String... passed_url)
      {
         /* If the tag entry has text, check to see if it is an old tag
          * or if it is new. */
         //noinspection ConstantOnRightSideOfComparison
         if(!tag.isEmpty())
         {
            String[] ctags = Read.file(FeedsActivity.GROUP_LIST);
            for(String gro : ctags)
            {
               if(gro.toLowerCase(Locale.getDefault()).equals(tag.toLowerCase(Locale.getDefault())))
               {
                  tag = gro;
                  existing_tag = true;
               }
            }
            if(!existing_tag)
            {
               String[] words = tag.split(" ");
               tag = "";

               for(String word : words)
               {
                  tag += word.substring(0, 1).toUpperCase(Locale.getDefault()) +
                         word.substring(1).toLowerCase(Locale.getDefault()) + ' ';
               }
               tag = tag.substring(0, tag.length() - 1);
            }

         }
         else
         {
            tag = spinner_tag.isEmpty() ? "Unsorted" : spinner_tag;
            existing_tag = spinner_tag.isEmpty();
         }

         String[] checkList = (!passed_url[0].contains("http")) ? new String[]{
               "http://" + passed_url[0], "https://" + passed_url[0]
         } : new String[]{passed_url[0]};

         String url = "";
         String feed_title = "";
         try
         {
            for(String check : checkList)
            {
               BufferedInputStream in = null;
               try
               {
                  try
                  {
                     in = new BufferedInputStream(new URL(check).openStream());
                     byte[] data = new byte[512];
                     byte[] data2;
                     in.read(data, 0, 512);

                     String line = new String(data);
                     if(line.contains("rss") || line.contains("Atom") || line.contains("atom"))
                     {
                        while(!line.contains("<m_title") && !line.contains("</m_title>"))
                        {
                           data2 = new byte[512];
                           in.read(data2, 0, 512);
                           data = Util.concat(data, data2);
                           line = new String(data);
                        }
                        int ind = line.indexOf('>', line.indexOf("<m_title")) + 1;
                        feed_title = line.substring(ind, line.indexOf("</", ind));
                        real = true;
                        url = check;
                        break;
                     }
                  }
                  finally
                  {
                     if(in != null)
                     {
                        in.close();
                     }
                  }
               }
               catch(Exception e)
               {
               }


            }
         }
         catch(Exception e)
         {
         }
         return new String[]{url, feed_title};
      }

      @Override
      protected void onPostExecute(String[] ton)
      {
         if(real)
         {
            if(!existing_tag)
            {
               addTag(tag);
            }
            if(name.isEmpty())
            {
               name = ton[1];
            }

            name = ILLEGAL_FILE_CHARS.matcher(name).replaceAll("");

            if(mode.equals("edit"))
            {
               editFeed(current_title, name, ton[0], current_tag, tag, pos);
            }
            else
            {
               addFeed(name, ton[0], tag);
            }

            dialog.dismiss();
         }
         else
         {
            Util.post(Util.getString(R.string.feed_invalid));
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if(button != null)
            {
               button.setEnabled(true);
            }
         }
      }
   }

   static void showAddFilterDialog()
   {
      Context con = Util.getContext();
      LayoutInflater inf = LayoutInflater.from(con);
      final View add_filter_layout = inf.inflate(R.layout.add_filter_dialog, null);

      final AlertDialog add_filter_dialog = new AlertDialog.Builder(con).setTitle("Add Filter")
                                                                        .setView(add_filter_layout)
                                                                        .setCancelable(true)
                                                                        .setNegativeButton(
                                                                              con.getString(
                                                                                    R.string
                                                                                          .cancel_dialog),
                                                                              new DialogInterface
                                                                                    .OnClickListener()
                                                                              {
                                                                                 @Override
                                                                                 public void
                                                                                 onClick(
                                                                                       DialogInterface dialog,
                                                                                       int id)
                                                                                 {
                                                                                 }
                                                                              }).create();

      add_filter_dialog.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

      add_filter_dialog.setButton(AlertDialog.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
                                  new DialogInterface.OnClickListener()
                                  {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which)
                                     {
                                        String feed_name = Util
                                              .getText((TextView) add_filter_layout);
                                        String filter_path = FeedsActivity.FILTER_LIST;
                                        String[] filters = Read.file(filter_path);
                                        if(-1 != Util.index(filters, feed_name))
                                        {
                                           Write.single(filter_path, feed_name + FeedsActivity.NL);
                                        }
                                        ((AdapterManageFilters) PagerAdapterManage
                                              .MANAGE_FRAGMENTS[2]
                                              .getListAdapter()).set_items(filters);
                                        add_filter_dialog.hide();
                                     }
                                  });
      add_filter_dialog.show();
   }

   static void showAddDialog(String... ctags)
   {
      Context con = Util.getContext();
      LayoutInflater inf = LayoutInflater.from(con);
      final View add_rss_dialog = inf.inflate(R.layout.add_rss_dialog, null);
      String[] spinner_tags = Arrays.copyOfRange(ctags, 1, ctags.length);
      final AdapterView<SpinnerAdapter> tag_spinner = (AdapterView<SpinnerAdapter>) add_rss_dialog
            .findViewById(R.id.tag_spinner);

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
                                                              spinner_tags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      tag_spinner.setAdapter(adapter);

      final AlertDialog add_feed_dialog = new AlertDialog.Builder(con).create();
      add_feed_dialog.setTitle(R.string.add_dialog_title);
      add_feed_dialog.setView(add_rss_dialog);
      add_feed_dialog.setCancelable(true);
      add_feed_dialog.setButton(AlertDialog.BUTTON_NEGATIVE, con.getString(R.string.cancel_dialog),
                                new DialogInterface.OnClickListener()
                                {
                                   @Override
                                   public void onClick(DialogInterface dialog, int id)
                                   {
                                   }
                                });

      add_feed_dialog.setButton(AlertDialog.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
                                new DialogInterface.OnClickListener()
                                {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which)
                                   {
                                      String new_tag = Util.getText(add_rss_dialog, R.id.tag_edit);
                                      String URL_check = Util
                                            .getText(add_rss_dialog, R.id.URL_edit);
                                      String feed_name = Util
                                            .getText(add_rss_dialog, R.id.name_edit);

                                      new_tag = new_tag.toLowerCase(Locale.getDefault());
                                      String spinner_tag;
                                      try
                                      {
                                         spinner_tag = tag_spinner.getSelectedItem().toString();
                                      }
                                      catch(Exception e)
                                      {
                                         spinner_tag = "";
                                      }
                                      Update.checkFeedExists(add_feed_dialog, new_tag, feed_name,
                                                             "add", "", "", spinner_tag, 0,
                                                             URL_check);
                                   }
                                });
      add_feed_dialog.show();
   }

   static void showEditDialog(String[] ctags, Context con, final int position)
   {
      LayoutInflater inf = LayoutInflater.from(con);
      final View edit_rss_dialog = inf.inflate(R.layout.add_rss_dialog, null);
      String[][] content = Read.csv();
      final String current_title = content[0][position];
      String current_url = content[1][position];
      final String current_tag = content[2][position];

      final AdapterView<SpinnerAdapter> tag_spinner = (AdapterView<SpinnerAdapter>) edit_rss_dialog
            .findViewById(R.id.tag_spinner);

      String[] spinner_tags = Arrays.copyOfRange(ctags, 1, ctags.length);

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
                                                              spinner_tags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      tag_spinner.setAdapter(adapter);
      Util.setText(current_url, edit_rss_dialog, R.id.URL_edit);
      Util.setText(current_title, edit_rss_dialog, R.id.URL_edit);

      tag_spinner.setSelection(Util.index(spinner_tags, current_tag));

      final AlertDialog edit_feed_dialog = new AlertDialog.Builder(con)
            .setTitle(con.getString(R.string.edit_dialog_title)).setView(edit_rss_dialog)
            .setCancelable(true).setNegativeButton(con.getString(R.string.cancel_dialog),
                                                   new DialogInterface.OnClickListener()
                                                   {
                                                      @Override
                                                      public void onClick(DialogInterface dialog,
                                                                          int id)
                                                      {
                                                      }
                                                   }).create();

      edit_feed_dialog.setButton(AlertDialog.BUTTON_POSITIVE, con.getString(R.string.accept_dialog),
                                 new DialogInterface.OnClickListener()
                                 {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                       String new_tag = Util
                                             .getText(edit_rss_dialog, R.id.tag_edit);
                                       String URL_check = Util
                                             .getText(edit_rss_dialog, R.id.URL_edit);
                                       String feed_name = Util
                                             .getText(edit_rss_dialog, R.id.name_edit);
                                       String spinner_tag = tag_spinner.getSelectedItem()
                                                                       .toString();
                                       new_tag = new_tag.toLowerCase(Locale.getDefault());

                                       Update.checkFeedExists(edit_feed_dialog, new_tag, feed_name,
                                                              "edit", current_title, current_tag,
                                                              spinner_tag, position, URL_check);
                                    }
                                 });

      edit_feed_dialog.show();
   }

   private static void addFeed(String feed, String url, String tag)
   {

      /* Create folders if they do not exist. */
      Util.mkdir(Util.getPath(feed, "images"));
      Util.mkdir(Util.getPath(feed, "thumbnails"));

      /* Create the csv. */
      String feed_info = "feed|" + feed + "|url|" + url + "|tag|" + tag + '|' + FeedsActivity.NL;

      /* Save the feed to the index. */
      String index = FeedsActivity.INDEX;
      Write.single(index, feed_info);

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

   private static void editFeed(String old_name, String new_name, String new_url, String old_tag,
                                String new_tag, int position)
   {

      String old_feed_folder = Util.getPath(old_name, "");
      String new_feed_folder = Util.getPath(new_name, "");

      if(!old_name.equals(new_name))
      {
         Util.move(old_feed_folder, new_feed_folder);
      }

      /* Replace the all_tag file with the new tag and data. */
      String index = FeedsActivity.INDEX;
      Write.removeLine(index, old_name, true);
      Write.single(index, "name|" + new_name + "|url|" + new_url + "|tag|" + new_tag + '|' +
                          FeedsActivity.NL);

      AdapterManageFeeds adpt = (AdapterManageFeeds) PagerAdapterManage.MANAGE_FRAGMENTS[1]
            .getListAdapter();

      adpt.setPosition(position, new_name, new_url + FeedsActivity.NL + new_tag + " • " + Integer
            .toString(
                  Read.count(FeedsActivity.GROUPS_DIR + new_tag + FeedsActivity.SEPAR + new_name +
                             FeedsActivity.SEPAR + new_name +
                             FeedsActivity.CONTENT) - 1) + " items");

      /// To ManageRefresh the counts and the order of the tags.
      Util.updateTags();
      Update.manageTags();
   }

   private static void addTag(String tag_name)
   {
      Write.single(FeedsActivity.GROUP_LIST, tag_name + FeedsActivity.NL);
      Util.updateTags();
   }
}
