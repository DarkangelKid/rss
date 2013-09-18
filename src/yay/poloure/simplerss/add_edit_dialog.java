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

public class add_edit_dialog
{
   public static class check_feed_exists extends AsyncTask<String, Void, String[]>
   {
      boolean existing_tag = false, real = false;
      String tag, name;
      final AlertDialog dialog;
      final String mode, spinner_tag, current_tag, current_title;
      final int pos;
      static final Pattern illegal_file_chars = Pattern.compile("[/\\?%*|<>:]");

      public check_feed_exists(AlertDialog edit_dialog, String new_tag, String feed_name, String moder, String current_tit, String current_grop, String spin_tag, int position)
      {
         dialog         = edit_dialog;
         tag          = new_tag;
         name           = feed_name;
         mode           = moder;
         spinner_tag  = spin_tag;
         current_tag  = current_grop;
         current_title  = current_tit;
         pos            = position;
         Button button  = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
         if(button != null)
            button.setEnabled(false);
      }

      @Override
      protected String[] doInBackground(String... passed_url)
      {
         /* If the tag entry has text, check to see if it is an old tag
          * or if it is new. */
         String url = "", feed_title = "";
         if(tag.length() > 0)
         {
            String[] ctags = read.file(main.GROUP_LIST);
            for(String gro : ctags)
            {
               if((gro.toLowerCase(Locale.getDefault())).equals(tag.toLowerCase(Locale.getDefault())))
               {
                  tag = gro;
                  existing_tag = true;
               }
            }
            if(!existing_tag)
            {
               String[] words = tag.split(" ");
               tag = "";

               for(String word: words)
                  tag += (word.substring(0, 1).toUpperCase(Locale.getDefault())).concat(word.substring(1).toLowerCase(Locale.getDefault())) + " ";
               tag = tag.substring(0, tag.length() - 1);
            }

         }
         else
         {
            tag = (spinner_tag.equals("")) ? "Unsorted" : spinner_tag;
            existing_tag = spinner_tag.equals("");
         }

         String[] check_list = (!passed_url[0].contains("http")) ?
            new String[]{"http://" + passed_url[0], "https://" + passed_url[0]}
          : new String[]{passed_url[0]};

         try
         {
            for(String check : check_list)
            {
               final BufferedInputStream in = new BufferedInputStream((new URL(check)).openStream());
               byte data[] = new byte[512], data2[];
               in.read(data, 0, 512);

               String line = new String(data);
               if((line.contains("rss"))||((line.contains("Atom"))||(line.contains("atom"))))
               {
                  while((!line.contains("<title"))&&(!line.contains("</title>")))
                  {
                     data2 = new byte[512];
                     in.read(data2, 0, 512);
                     data = util.concat(data, data2);
                     line = new String(data);
                  }
                  final int ind = line.indexOf(">", line.indexOf("<title")) + 1;
                  feed_title = line.substring(ind, line.indexOf("</", ind));
                  real = true;
                  url = check;
                  break;
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
         if(!real)
         {
            util.post(main.con.getString(R.string.feed_invalid));
            Button button  = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if(button != null)
               button.setEnabled(true);
         }
         else
         {
            if(!existing_tag)
               add_tag(tag);
            if(name.equals(""))
               name = ton[1];

            name = illegal_file_chars.matcher(name).replaceAll("");

            if(mode.equals("edit"))
               edit_feed(current_title, name, ton[0], current_tag, tag, pos);
            else
               add_feed(name, ton[0], tag);

            dialog.dismiss();
         }
      }
   }

   static void show_add_filter_dialog()
   {
      Context con                  = util.get_context();
      LayoutInflater inf           = LayoutInflater.from(con);
      final View add_filter_layout = inf.inflate(R.layout.add_filter_dialog, null);

      final AlertDialog add_filter_dialog = new AlertDialog.Builder(con)
            .setTitle("Add Filter")
            .setView(add_filter_layout)
            .setCancelable(true)
            .setNegativeButton
            (con.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener()
               {
                  @Override
                  public void onClick(DialogInterface dialog,int id)
                  {
                  }
               }
            )
            .create();

            add_filter_dialog.getWindow()
                  .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            add_filter_dialog.setButton(AlertDialog.BUTTON_POSITIVE, (con.getString(R.string.add_dialog)),
            new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialog, int which)
               {
                  final String feed_name  = util.getstr((TextView) add_filter_layout);
                  String filter_path      = main.FILTER_LIST;
                  String[] filters        = read.file(filter_path);
                  if(util.index(filters, feed_name) != -1)
                     write.single(filter_path, feed_name + main.NL);
                  ((adapter_manage_filter) pageradapter_manage.fragments[2].getListAdapter()).set_items(filters);
                  add_filter_dialog.hide();
               }
            });
      add_filter_dialog.show();
   }

   static void show_add_dialog(final String[] ctags)
   {
      Context        con      = util.get_context();
      LayoutInflater inf      = LayoutInflater.from(con);
      final View add_rss_dialog = inf.inflate(R.layout.add_rss_dialog, null);
      String[] spinner_tags = Arrays.copyOfRange(ctags, 1, ctags.length);
      final AdapterView<SpinnerAdapter> tag_spinner = (AdapterView<SpinnerAdapter>) add_rss_dialog.findViewById(R.id.tag_spinner);

      ArrayAdapter<String> adapter  = new ArrayAdapter<String>(con, R.layout.group_spinner_text, spinner_tags);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      tag_spinner.setAdapter(adapter);

      final AlertDialog add_feed_dialog = new AlertDialog.Builder(con).create();
      add_feed_dialog.setTitle(R.string.add_dialog_title);
      add_feed_dialog.setView(add_rss_dialog);
      add_feed_dialog.setCancelable(true);
      add_feed_dialog.setButton(AlertDialog.BUTTON_NEGATIVE, con.getString(R.string.cancel_dialog),
      new DialogInterface.OnClickListener()
      {
         @Override
         public void onClick(DialogInterface dialog,int id)
         {
         }
      });

      add_feed_dialog.setButton(AlertDialog.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
      new DialogInterface.OnClickListener()
      {
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
            String new_tag = util.getstr(add_rss_dialog, R.id.tag_edit);
            String URL_check = util.getstr(add_rss_dialog, R.id.URL_edit);
            String feed_name = util.getstr(add_rss_dialog, R.id.name_edit);

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
            update.check_feed(add_feed_dialog, new_tag, feed_name, "add", "", "", spinner_tag, 0, URL_check);
         }
      });
      add_feed_dialog.show();
   }

   static void show_edit_dialog(final String[] ctags, Context con, final int position)
   {
      LayoutInflater inf         = LayoutInflater.from(con);
      final View edit_rss_dialog = inf.inflate(R.layout.add_rss_dialog, null);
      String[][] content         = read.csv(main.INDEX);
      final String current_title = content[0][position];
      final String current_url   = content[1][position];
      final String current_tag   = content[2][position];

      final AdapterView<SpinnerAdapter> tag_spinner = (AdapterView<SpinnerAdapter>) edit_rss_dialog.findViewById(R.id.tag_spinner);

      String[] spinner_tags    = Arrays.copyOfRange(ctags, 1, ctags.length);

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text, spinner_tags);
      adapter      .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      tag_spinner.setAdapter(adapter);
      util.set_text(current_url, edit_rss_dialog, R.id.URL_edit);
      util.set_text(current_title, edit_rss_dialog, R.id.URL_edit);

      tag_spinner.setSelection(util.index(spinner_tags, current_tag));

      final AlertDialog edit_feed_dialog = new AlertDialog.Builder(con)
         .setTitle(con.getString(R.string.edit_dialog_title))
         .setView(edit_rss_dialog)
         .setCancelable(true)
         .setNegativeButton
         (con.getString(R.string.cancel_dialog),new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialog,int id)
               {
               }
            }
         )
         .create();

      edit_feed_dialog.setButton( AlertDialog.BUTTON_POSITIVE,
                                    con.getString(R.string.accept_dialog),
      new DialogInterface.OnClickListener()
      {
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
            String new_tag = util.getstr(edit_rss_dialog, R.id.tag_edit);
            String URL_check = util.getstr(edit_rss_dialog, R.id.URL_edit);
            String feed_name = util.getstr(edit_rss_dialog, R.id.name_edit);
            String spinner_tag = tag_spinner.getSelectedItem().toString();
            new_tag = new_tag.toLowerCase(Locale.getDefault());

            update.check_feed(edit_feed_dialog, new_tag, feed_name, "edit", current_title, current_tag, spinner_tag, position, URL_check);
         }
      });

      edit_feed_dialog.show();
   }

   static void add_feed(String feed, String url, String tag)
   {
      String index      = main.INDEX;

      /* Create folders if they do not exist. */
      util.mkdir(util.get_path(feed, "images"));
      util.mkdir(util.get_path(feed, "thumbnails"));

      /* Create the csv. */
      String feed_info = "feed|" +  feed + "|url|" + url + "|tag|"
                         + tag + "|" + main.NL;

      /* Save the feed to the index. */
      write.single(index, feed_info);

      /* Update the manage listviews with the new information. */
      if(pageradapter_manage.fragments[1].getListAdapter() != null)
         update.manage_feeds();
      if(pageradapter_manage.fragments[0].getListAdapter() != null)
         update.manage_tags();
   }

   /* TODO EDIT FEED UPDATE FOR INTERNAL. */
   static void edit_feed(String old_name, String new_name, String new_url, String old_tag, String new_tag, int position)
   {
      String index   = main.INDEX;

      String old_feed_folder  = util.get_path(old_name, "");
      String new_feed_folder  = util.get_path(new_name, "");

      if(!old_name.equals(new_name))
         util.mv(old_feed_folder, new_feed_folder);

      /* Replace the all_tag file with the new tag and data. */
      write.remove_string(index, old_name, true);
      write.single(index, "name|" +  new_name + "|url|" + new_url
                   + "|tag|" + new_tag + "|" + main.NL);

      adapter_manage_feeds adpt = ((adapter_manage_feeds) pageradapter_manage.fragments[1].getListAdapter());

      adpt.set_position(position, new_name, new_url + main.NL + new_tag + " • " + Integer.toString(read.count(main.GROUPS_DIR + new_tag + main.SEPAR + new_name + main.SEPAR + new_name + main.CONTENT) - 1) + " items");

      /// To refresh the counts and the order of the tags.
      util.update_tags();
      update.manage_tags();
   }

   static void add_tag(String tag_name)
   {
      write.single(main.GROUP_LIST, tag_name + main.NL);
      util.update_tags();
   }
}
