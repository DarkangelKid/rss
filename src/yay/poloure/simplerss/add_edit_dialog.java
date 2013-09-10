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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Arrays;

public class add_edit_dialog
{
   public static class check_feed_exists extends AsyncTask<String, Void, String[]>
   {
      boolean existing_group = false, real = false;
      String group, name;
      final AlertDialog dialog;
      final String mode, spinner_group, current_group, current_title;
      final int pos;
      static final Pattern illegal_file_chars = Pattern.compile("[/\\?%*|<>:]");

      public check_feed_exists(AlertDialog edit_dialog, String new_group, String feed_name, String moder, String current_tit, String current_grop, String spin_group, int position)
      {
         dialog         = edit_dialog;
         group          = new_group;
         name           = feed_name;
         mode           = moder;
         spinner_group  = spin_group;
         current_group  = current_grop;
         current_title  = current_tit;
         pos            = position;
         Button button  = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
         if(button != null)
            button.setEnabled(false);
      }

      @Override
      protected String[] doInBackground(String... passed_url)
      {
         /* If the group entry has text, check to see if it is an old group
          * or if it is new. */
         String url = "", feed_title = "";
         if(group.length() > 0)
         {
            String[] cgroups = read.file(util.get_storage() + main.GROUP_LIST);
            for(String gro : cgroups)
            {
               if((gro.toLowerCase(Locale.getDefault())).equals(group.toLowerCase(Locale.getDefault())))
               {
                  group = gro;
                  existing_group = true;
               }
            }
            if(!existing_group)
            {
               String[] words = group.split(" ");
               group = "";

               for(String word: words)
                  group += (word.substring(0, 1).toUpperCase(Locale.getDefault())).concat(word.substring(1).toLowerCase(Locale.getDefault())) + " ";
               group = group.substring(0, group.length() - 1);
            }

         }
         else
         {
            group = (spinner_group.equals("")) ? "Unsorted" : spinner_group;
            existing_group = (spinner_group.equals("")) ? false : true;
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
            if(!existing_group)
               add_group(group);
            if(name.equals(""))
               name = ton[1];

            name = illegal_file_chars.matcher(name).replaceAll("");

            if(mode.equals("edit"))
               edit_feed(current_title, name, ton[0], current_group, group, pos);
            else
               add_feed(name, ton[0], group);

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
                  String filter_path      = util.get_storage() + main.FILTER_LIST;
                  String[] filters        = read.file(filter_path);
                  if(util.index(filters, feed_name) != -1)
                     write.single(filter_path, feed_name + main.NL);
                  ((adapter_manage_filter) pageradapter_manage.fragments[2].getListAdapter()).set_items(read.file(filter_path));
                  /*pageradapter_manage.fragments[2].getListAdapter().notifyDataSetChanged();*/
                  add_filter_dialog.hide();
               }
            });
      add_filter_dialog.show();
   }

   static void show_add_dialog(final String[] cgroups)
   {
      Context        con      = util.get_context();
      LayoutInflater inf      = LayoutInflater.from(con);
      View add_rss_dialog     = inf.inflate(R.layout.add_rss_dialog, null);
      String[] spinner_groups = Arrays.copyOfRange(cgroups, 1, cgroups.length);
      final TextView group_edit     = (TextView) add_rss_dialog.findViewById(R.id.group_edit);
      final TextView URL_edit       = (TextView) add_rss_dialog.findViewById(R.id.URL_edit);
      final TextView name_edit      = (TextView) add_rss_dialog.findViewById(R.id.name_edit);
      final AdapterView<SpinnerAdapter> group_spinner = (AdapterView<SpinnerAdapter>) add_rss_dialog.findViewById(R.id.group_spinner);

      ArrayAdapter<String> adapter  = new ArrayAdapter<String>(con, R.layout.group_spinner_text, spinner_groups);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      group_spinner.setAdapter(adapter);

      final AlertDialog add_feed_dialog = new AlertDialog.Builder(con).create();
      add_feed_dialog.setTitle("Add Feed");
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
            String new_group = util.getstr(group_edit).toLowerCase(Locale.getDefault());
            String URL_check = util.getstr(URL_edit);
            String feed_name = util.getstr(name_edit);
            String spinner_group;
            try
            {
               spinner_group = group_spinner.getSelectedItem().toString();
            }
            catch(Exception e)
            {
               spinner_group = "";
            }
            update.check_feed(add_feed_dialog, new_group, feed_name, "add", "", "", spinner_group, 0,URL_check);
         }
      });
      add_feed_dialog.show();
   }

   static void show_edit_dialog(final String[] cgroups, Context con, final int position)
   {
      String storage             = util.get_storage();
      LayoutInflater inf         = LayoutInflater.from(con);
      View edit_rss_dialog       = inf.inflate(R.layout.add_rss_dialog, null);
      String[][] content         = read.csv(storage + main.GROUPS_DIR + cgroups[0] + main.SEPAR + cgroups[0] + main.TXT, 'n', 'u', 'g');
      final String current_title = content[0][position];
      final String current_url   = content[1][position];
      final String current_group = content[2][position];

      final TextView group_edit  = (TextView) edit_rss_dialog.findViewById(R.id.group_edit);
      final TextView URL_edit    = (TextView) edit_rss_dialog.findViewById(R.id.URL_edit);
      final TextView name_edit   = (TextView) edit_rss_dialog.findViewById(R.id.name_edit);
      final AdapterView<SpinnerAdapter> group_spinner = (AdapterView<SpinnerAdapter>) edit_rss_dialog.findViewById(R.id.group_spinner);

      String[] spinner_groups    = Arrays.copyOfRange(cgroups, 1, cgroups.length);

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text, spinner_groups);
      adapter      .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      group_spinner.setAdapter(adapter);
      URL_edit     .setText(current_url);
      name_edit    .setText(current_title);
      group_spinner.setSelection(util.index(spinner_groups, current_group));

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

         edit_feed_dialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            con.getString(R.string.accept_dialog),
            new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialog, int which)
               {
                     String new_group     = util.getstr(group_edit).toLowerCase(Locale.getDefault());
                     String URL_check     = util.getstr(URL_edit);
                     String feed_name     = util.getstr(name_edit);
                     String spinner_group = group_spinner.getSelectedItem().toString();
                     update.check_feed(edit_feed_dialog, new_group, feed_name, "edit", current_title, current_group, spinner_group, position, URL_check);
               }
            });

            edit_feed_dialog.show();
   }

   static void add_feed(String name, String url, String group)
   {
      String sep           = main.SEPAR;
      String g_dir         = util.get_storage() + main.GROUPS_DIR;
      String all           = main.ALL;

      /* storage/Groups/Mariam/Mariam.txt */
      String group_index    = g_dir + group + sep + group + main.TXT;
      /* storage/Groups/Mariam/mrm/ */
      String feed_path      = g_dir + group + sep + name + sep;
      /* storage/Groups/All/All.txt */
      String all_index      = g_dir + all + sep + all + main.TXT;

      /* Create folders if they do not exist. */
      util.mkdir(feed_path);
      util.mkdir(g_dir + all);

      util.mkdir(feed_path + "images");
      util.mkdir(feed_path + "thumbnails");

      /* Create the csv. */
      String feed_info = "name|" +  name + "|url|" + url + "|group|"
                         + group + "|" + main.NL;

      /* Save the feed to the all file and the group file. */
      write.single(group_index, feed_info);
      write.single(all_index, feed_info);

      /* Update the manage listviews with the new information. */
      if(pageradapter_manage.fragments[1].getListAdapter() != null)
         update.manage_feeds();
      if(pageradapter_manage.fragments[1].getListAdapter() != null)
         update.manage_groups();
   }

   /* TODO EDIT FEED UPDATE FOR INTERNAL. */
   static void edit_feed(String old_name, String new_name, String new_url, String old_group, String new_group, int position)
   {
      String sep     = main.SEPAR;
      String txt     = main.TXT;
      String count   = main.COUNT;
      String content = main.CONTENT;
      String storage = util.get_storage();
      String g_dir   = storage + main.GROUPS_DIR;
      String all     = main.ALL;

      String all_index            = g_dir + all + sep + all + txt;
      String old_group_folder     = g_dir + old_group;
      String new_group_folder     = g_dir + new_group;
      String old_index            = old_group_folder + sep + old_group + txt;
      String new_index            = new_group_folder + sep + new_group + txt;
      String old_feed_folder      = old_group_folder + sep + old_name;
      String new_feed_folder      = new_group_folder + sep + new_name;
      String old_feed_folder_post = new_group_folder + sep + old_name;
      String new_feed_folder_post = new_group_folder + sep + new_name;

      if(!old_name.equals(new_name))
      {
         util.mv(old_feed_folder + sep + old_name + txt,
                 old_feed_folder + sep + new_name + txt );
         util.mv(old_feed_folder + sep + old_name + content,
                 old_feed_folder + sep + new_name + content );
      }
      if(!old_group.equals(new_group))
      {

         util.mv(old_feed_folder, new_feed_folder);

         write.remove_string(old_index, old_name, true);
         write.single(new_index, "name|" +  new_name + "|url|" + new_url + "|group|" + new_group + "|" + main.NL);

         util.rm_empty(old_index);
         if(!util.exists(old_index))
         {
            util.rmdir(new File(old_group_folder));
            write.remove_string(storage + main.GROUP_LIST, old_group, false);
         }
      }
      if(!old_name.equals(new_name))
         util.mv(old_feed_folder_post, new_feed_folder_post);

      /* Replace the new_group file with the new data. */
      write.remove_string(new_index, old_name, true);
      write.single(new_index, "name|" +  new_name + "|url|" + new_url
                   + "|group|" + new_group + "|" + main.NL);

      /* Replace the all_group file with the new group and data. */
      write.remove_string(all_index, old_name, true);
      write.single(all_index, "name|" +  new_name + "|url|" + new_url
                   + "|group|" + new_group + "|" + main.NL);

      /// Delete the group count file and delete the group_content_file
      String all_content_file = storage + main.GROUPS_DIR + all
                                      + sep + all + content;

      util.rm(new_group_folder + sep + new_group + content);
      util.rm(new_group_folder + sep + new_group + content + count);
      util.rm(old_group_folder + sep + old_group + content);
      util.rm(old_group_folder + sep + old_group + content + count);
      util.rm(all_content_file);
      util.rm(all_content_file + count);

      /// This is because the group file contains the feed name and feed group (for location of images).
      if(util.exists(old_index))
         write.sort_content(old_group, all);
      if(!old_group.equals(new_group))
         write.sort_content(new_group, all);
      write.sort_content(all, all);

      adapter_manage_feeds adpt = ((adapter_manage_feeds) pageradapter_manage.fragments[1].getListAdapter());

      adpt.set_position(position, new_name, new_url + main.NL + new_group + " • " + Integer.toString(read.count(storage + main.GROUPS_DIR + new_group + main.SEPAR + new_name + main.SEPAR + new_name + main.CONTENT) - 1) + " items");

      /// To refresh the counts and the order of the groups.
      util.update_groups();
      update.manage_groups();
   }

   static void add_group(String group_name)
   {
      String storage = util.get_storage();
      write.single(storage + main.GROUP_LIST, group_name + main.NL);

      util.mkdir(storage + main.GROUPS_DIR + group_name);

      util.update_groups();
   }
}
