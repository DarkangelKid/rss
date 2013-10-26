package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<Void, Void, String[]>
{
   /* Formats */
   private static final String  INDEX_FORMAT       = "feed|%s|url|%s|tag|%s|";
   private static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[/\\?%*|<>:]");
   private static final Pattern SPLIT_SPACE        = Pattern.compile(" ");
   private final Dialog m_dialog;
   private final String m_oldFeedName;
   private final String m_applicationFolder;
   private final String m_allTag;

   private
   AsyncCheckFeed(Dialog dialog, String currentTitle, String applicationFolder, String allTag)
   {
      m_dialog = dialog;
      m_oldFeedName = currentTitle;
      m_applicationFolder = applicationFolder;
      m_allTag = allTag;

      Button button = (Button) m_dialog.findViewById(R.id.positive_button);
      button.setText(R.string.dialog_checking_feed);
      button.setEnabled(false);
   }

   static
   void newInstance(Dialog dialog, String oldFeedTitle, String applicationFolder, String allTag)
   {
      AsyncTask<Void, Void, String[]> task = new AsyncCheckFeed(dialog, oldFeedTitle,
            applicationFolder, allTag);

      if(Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT)
      {
         task.executeOnExecutor(THREAD_POOL_EXECUTOR);
      }
      else
      {
         task.execute();
      }
   }

   @Override
   protected
   String[] doInBackground(Void... nothing)
   {
      /* Get the user's input. */
      CharSequence inputName = ((TextView) m_dialog.findViewById(R.id.name_edit)).getText();
      String inputTags = ((TextView) m_dialog.findViewById(R.id.tag_edit)).getText().toString();
      String inputUrl = ((TextView) m_dialog.findViewById(R.id.feed_url_edit)).getText().toString();

      /* Form the array of urls we will check the validility of. */
      String[] urlCheckList = inputUrl.contains("http") ? new String[]{inputUrl} : new String[]{
            "http://" + inputUrl, "https://" + inputUrl
      };

      String url = "";
      String title = "";

      for(String urlToCheck : urlCheckList)
      {
         if(isValidFeed(urlToCheck))
         {
            /* Did the user enter a feed name? If not, use the feed title found from the check. */
            String tempTitle = 0 == inputName.length()
                  ? getFeedTitle(urlToCheck)
                  : inputName.toString();

            /* Replace any characters that are not allowed in file names. */
            Matcher matcher = ILLEGAL_FILE_CHARS.matcher(tempTitle);
            title = matcher.replaceAll("");

            url = urlToCheck;
            break;
         }
      }

      String tags = formatUserTagsInput(inputTags, m_allTag);

      return new String[]{url, title, tags};
   }

   @Override
   protected
   void onPostExecute(String[] result)
   {
      String feedUrlFromCheck = result[0];
      String finalTitle = result[1];
      String finalTag = result[2];

      boolean isFeedValid = 0 != feedUrlFromCheck.length();
      Context context = m_dialog.getContext();

      if(isFeedValid)
      {
         /* Create the csv. */
         String feedInfo = String.format(INDEX_FORMAT, finalTitle, feedUrlFromCheck, finalTag) +
               System.getProperty("line.separator");

         if(0 != m_oldFeedName.length())
         {
            editFeed(m_oldFeedName, finalTitle, m_applicationFolder);
         }

         /* Save the feed to the index. */
         Write.single(Read.INDEX, feedInfo, m_applicationFolder);

         /* Update the tags. */
         /* TODO updateTags((Activity) m_context); */

         /* TODO AsyncManageTagsRefresh.newInstance(tagListView); */
         /* TODO AsyncManageFeedsRefresh.newInstance(feedsListView, m_context); */

         /* Show added feed toast notification. */
         String addedText = context.getString(R.string.added_feed) + ' ' + finalTitle;
         Toast.makeText(context, addedText, Toast.LENGTH_SHORT).show();

         m_dialog.dismiss();
      }
      else
      {
         Button button = (Button) m_dialog.findViewById(R.id.positive_button);
         button.setText(R.string.add_dialog);
         button.setEnabled(true);
         Toast.makeText(context, R.string.invalid_feed, Toast.LENGTH_SHORT).show();
      }
   }

   private static
   void editFeed(CharSequence oldFeed, String newFeed, String applicationFolder)
   {
      /* Rename the folder if it is different. */
      String oldFeedFolder = oldFeed + File.separator;
      String newFeedFolder = newFeed + File.separatorChar;

      if(!oldFeed.equals(newFeed))
      {
         Write.moveFile(oldFeedFolder, newFeedFolder, applicationFolder);
      }

      /* Replace the all_tag file with the new image and data. */
      Write.removeLine(Read.INDEX, oldFeed, true, applicationFolder);

   }

   private static
   String formatUserTagsInput(String userInputTags, String allTag)
   {
      Locale defaultLocale = Locale.getDefault();

      String initialTags = 0 == userInputTags.length()
            ? allTag
            : userInputTags.toLowerCase(defaultLocale);

      int tagInitialCapacity = initialTags.length();

      /* Capitalise each word. */
      String[] words = SPLIT_SPACE.split(initialTags);
      StringBuilder tagBuilder = new StringBuilder(tagInitialCapacity);

      for(String word : words)
      {
         String firstLetter = word.substring(0, 1);
         String restOfWord = word.substring(1);

         String firstLetterUpper = firstLetter.toUpperCase(defaultLocale);
         String restOfWordLower = restOfWord.toLowerCase(defaultLocale);

         tagBuilder.append(firstLetterUpper);
         tagBuilder.append(restOfWordLower);
         tagBuilder.append(' ');
      }

      int tagLength = tagBuilder.length();
      int lastChar = tagLength - 1;
      tagBuilder.delete(lastChar, tagLength);
      return tagBuilder.toString();
   }

   private static
   boolean isValidFeed(String urlString)
   {
      boolean isValid = false;
      try
      {
         XmlPullParser parser = createXmlParser(urlString);

         parser.next();
         int eventType = parser.getEventType();
         if(XmlPullParser.START_TAG == eventType)
         {
            String tag = parser.getName();
            if("rss".equals(tag) || "feed".equals(tag))
            {
               isValid = true;
            }
         }
      }
      catch(Exception ignored)
      {
      }
      return isValid;
   }

   private static
   String getFeedTitle(String urlString)
   {
      String feedTitle = "";
      try
      {
         XmlPullParser parser = createXmlParser(urlString);
         int eventType;

         do
         {
            parser.next();
            eventType = parser.getEventType();
            if(XmlPullParser.START_TAG == eventType)
            {
               String tag = parser.getName();
               if("title".equals(tag))
               {
                  parser.next();
                  feedTitle = parser.getText();
               }
            }
         }
         while(0 == feedTitle.length() && XmlPullParser.END_DOCUMENT != eventType);
      }
      catch(Exception ignored)
      {
      }
      return feedTitle;
   }

   private static
   XmlPullParser createXmlParser(String urlString) throws Exception
   {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser parser = factory.newPullParser();

      URL url = new URL(urlString);
      InputStream inputStream = url.openStream();
      parser.setInput(inputStream, null);
      return parser;
   }
}
