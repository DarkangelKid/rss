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
      View addFeedLayout = inf.inflate(R.layout.add_rss_dialog, null);
      String[] spinnerTags = Arrays.copyOfRange(ctags, 1, ctags.length);
      AdapterView<SpinnerAdapter> spinnerTag
            = (AdapterView<SpinnerAdapter>) addFeedLayout.findViewById(R.id.tag_spinner);

      SpinnerAdapter adapter = new ArrayAdapter<String>(con, R.layout.group_spinner_text,
            spinnerTags);
      //adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      spinnerTag.setAdapter(adapter);

      AlertDialog.Builder build = new AlertDialog.Builder(con);
      build.setTitle(R.string.add_dialog_title).setView(addFeedLayout).setCancelable(true);

      AlertDialog addFeedDialog = build.create();

      addFeedDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            con.getString(R.string.cancel_dialog), new OnDialogClickCancel());

      addFeedDialog.setButton(DialogInterface.BUTTON_POSITIVE, con.getString(R.string.add_dialog),
            new OnDialogClickAdd(addFeedLayout, spinnerTag, addFeedDialog));

      addFeedDialog.show();
   }

   static
   class CheckFeed extends AsyncTask<String, Void, String[]>
   {
      static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[/\\?%*|<>:]");
      static final Pattern SPLIT_SPACE        = Pattern.compile(" ");
      AlertDialog m_dialog;
      String      m_mode;
      String      m_tag;
      String      m_title;
      boolean     m_feedExists;
      String      name;
      int         m_position;
      private boolean m_existingTag;


      CheckFeed(AlertDialog dialog, String tag, String feedName, String mode, String currentTitle,
            int position)
      {
         m_dialog = dialog;
         m_tag = tag;
         name = feedName;
         m_mode = mode;
         m_title = currentTitle;
         m_position = position;
         Button button = m_dialog.getButton(DialogInterface.BUTTON_POSITIVE);
         if(null != button)
         {
            button.setEnabled(false);
         }
      }

      @Override
      protected
      String[] doInBackground(String... url)
      {
         /* If the m_imageViewTag entry has text, check to see if it is an old m_imageViewTag
          * or if it is new. */

         /* Capitalise each word. */
         String[] words = SPLIT_SPACE.split(m_tag);
         m_tag = "";

         for(String word : words)
         {
            m_tag += word.substring(0, 1).toUpperCase(Constants.LOCALE) +
                  word.substring(1).toLowerCase(Constants.LOCALE) + ' ';
         }
         m_tag = m_tag.substring(0, m_tag.length() - 1);

         /* Check to see if the tag already exists. */
         String[] currentTags = Read.file(Constants.TAG_LIST);
         if(-1 != Util.index(currentTags, m_tag))
         {
            m_existingTag = true;
         }

         String[] checkList = url[0].contains("http") ? new String[]{url[0]} : new String[]{
               "http://" + url[0], "https://" + url[0]
         };

         String feedUrl = "";
         String feedTitle = "";

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
                     while(!line.contains(Constants.TAG_TITLE) &&
                           !line.contains(Constants.ENDTAG_TITLE))
                     {
                        byte[] data2 = new byte[512];
                        in.read(data2, 0, 512);
                        data = concat(data, data2);
                        line = new String(data);
                     }
                     int ind = line.indexOf('>', line.indexOf(Constants.TAG_TITLE) + 1);
                     feedTitle = line.substring(ind, line.indexOf("</", ind));
                     m_feedExists = true;
                     feedUrl = check;
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
         return new String[]{feedUrl, feedTitle};
      }

      @Override
      protected
      void onPostExecute(String[] result)
      {
         if(!m_feedExists)
         {
            Util.post(Util.getString(R.string.feed_invalid));
            Button button = m_dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if(null != button)
            {
               button.setEnabled(true);
            }
            return;
         }

         if(!m_existingTag)
         {
            Write.single(Constants.TAG_LIST, m_tag + Constants.NL);
            Util.updateTags();
         }
         if(name.isEmpty())
         {
            name = result[1];
         }

         name = ILLEGAL_FILE_CHARS.matcher(name).replaceAll("");

         if(Constants.EDIT.equals(m_mode))
         {
            editFeed(m_title, name, result[0], m_tag);
         }
         else if(Constants.ADD.equals(m_mode))
         {

      /* Create folders if they do not exist. */
            Util.mkdir(Util.getPath(name, Constants.IMAGES));
            Util.mkdir(Util.getPath(name, Constants.THUMBNAILS));

      /* Create the csv. */
            String feedInfo = String.format(Constants.INDEX_FORMAT, name, result[0], m_tag) +
                  Constants.NL;

      /* Save the feed to the index. */
            Write.single(Constants.INDEX, feedInfo);

      /* Update the manage listviews with the new information. */
            //if(null != PagerAdapterManage.MANAGE_FRAGMENTS[1].getListAdapter())
            {
               //  Update.manageFeeds();
            }
            // if(null != PagerAdapterManage.MANAGE_FRAGMENTS[0].getListAdapter())
            {
               //    Update.manageTags();
            }
         }

         m_dialog.dismiss();
      }

      static
      void editFeed(String oldFeed, String newFeed, String newUrl, String newTag)
      {

         String oldFeedFolder = Util.getPath(oldFeed, "");
         String newFeedFolder = Util.getPath(newFeed, "");

         if(!oldFeed.equals(newFeed))
         {
            Util.move(oldFeedFolder, newFeedFolder);
         }

         /* Replace the all_tag file with the new image and data. */
         String index = Constants.INDEX;
         String entry = String.format(Constants.INDEX_FORMAT, newFeed, newUrl, newTag);

         int position = Write.removeLine(index, oldFeed, true);
         Write.single(index, entry + Constants.NL);

         FragmentManageFeeds fragmentManageFeeds
               = (FragmentManageFeeds) PagerAdapterManage.MANAGE_FRAGMENTS[1];
         AdapterManageFeeds adapterManageFeeds
               = (AdapterManageFeeds) fragmentManageFeeds.getListAdapter();

         adapterManageFeeds.setPosition(position, newFeed,
               String.format(Constants.LOCALE, Constants.FEED_INFO, newUrl, newTag,
                     Read.count(Util.getPath(newFeed, Constants.CONTENT))));

         /// To ManageFeedsRefresh the counts and the order of the tags.
         Util.updateTags();
         Update.manageTags(fragmentManageFeeds.getListView(), adapterManageFeeds);
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
   }

}
