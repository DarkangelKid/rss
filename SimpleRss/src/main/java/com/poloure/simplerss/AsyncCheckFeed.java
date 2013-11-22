package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<Void, Void, String[]>
{
   /* Formats */
   private static final String INDEX_FORMAT = "|f|%s|u|%s|t|%s|";
   private static final String NEW_LINE = System.getProperty("line.separator");
   private static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[/\\?%*|<>:]");
   private static final Pattern SPLIT_SPACE = Pattern.compile(" ");
   private static final Pattern SPLIT_COMMA = Pattern.compile(",");
   private final Dialog m_dialog;
   private final String m_oldFeedName;
   private final String m_applicationFolder;
   private final String m_allTag;
   private final FragmentPagerAdapter m_pagerAdapterFeeds;
   private final BaseAdapter m_navigationAdapter;
   private final ListView m_listView;

   private
   AsyncCheckFeed(Dialog dialog, ListView listView, FragmentPagerAdapter pagerAdapterFeeds,
         BaseAdapter navigationAdapter, String currentTitle, String applicationFolder,
         String allTag)
   {
      m_dialog = dialog;
      m_listView = listView;
      m_pagerAdapterFeeds = pagerAdapterFeeds;
      m_navigationAdapter = navigationAdapter;
      m_oldFeedName = currentTitle;
      m_applicationFolder = applicationFolder;
      m_allTag = allTag;

      Button button = (Button) m_dialog.findViewById(R.id.positive_button);
      button.setText(R.string.dialog_checking_feed);
      button.setEnabled(false);
   }

   static
   void newInstance(Dialog dialog, ListView listView, FragmentPagerAdapter pagerAdapterFeeds,
         BaseAdapter navigationAdapter, String oldFeedTitle, String applicationFolder,
         String allTag)
   {
      AsyncTask<Void, Void, String[]> task = new AsyncCheckFeed(dialog, listView, pagerAdapterFeeds,
            navigationAdapter, oldFeedTitle, applicationFolder, allTag);

      task.executeOnExecutor(THREAD_POOL_EXECUTOR);
   }

   @Override
   protected
   String[] doInBackground(Void... nothing)
   {
      /* Get the user's input. */
      CharSequence inputName = ((TextView) m_dialog.findViewById(R.id.name_edit)).getText();
      CharSequence inputTags = ((TextView) m_dialog.findViewById(R.id.tag_edit)).getText();
      CharSequence inputUrlChar = ((TextView) m_dialog.findViewById(R.id.feed_url_edit)).getText();
      String inputUrl = inputUrlChar.toString();

      /* Form the array of urls we will check the validity of. */
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
      catch(MalformedURLException ignored)
      {
      }
      catch(IOException ignored)
      {
      }
      catch(XmlPullParserException ignored)
      {
      }

      return feedTitle;
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
      catch(MalformedURLException ignored)
      {
      }
      catch(IOException ignored)
      {
      }
      catch(XmlPullParserException ignored)
      {
      }
      return isValid;
   }

   private static
   XmlPullParser createXmlParser(String urlString)
         throws MalformedURLException, IOException, XmlPullParserException
   {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser parser = factory.newPullParser();

      URL url = new URL(urlString);
      InputStream inputStream = url.openStream();
      parser.setInput(inputStream, null);
      return parser;
   }

   private static
   String formatUserTagsInput(CharSequence userInputTags, String allTag)
   {
      String inputTags = userInputTags.toString();
      Locale defaultLocale = Locale.getDefault();

      String lowerTags = 0 == inputTags.length() ? allTag : inputTags.toLowerCase(defaultLocale);

      /* + 10 in case the user did not put spaces after the commas. */
      int tagInitialCapacity = lowerTags.length();
      StringBuilder tagBuilder = new StringBuilder(tagInitialCapacity + 10);

      String[] tags = SPLIT_COMMA.split(lowerTags);

      /* For each tag. */
      for(String tag : tags)
      {
         /* In case the tag is multiple words. */
         String[] words = SPLIT_SPACE.split(tag);

         /* The input tag is all lowercase. */
         for(String word : words)
         {
            if(0 < word.length())
            {
               char firstLetter = word.charAt(0);
               char firstLetterUpper = Character.toUpperCase(firstLetter);

               String restOfWord = word.substring(1);

               tagBuilder.append(firstLetterUpper);
               tagBuilder.append(restOfWord);
               tagBuilder.append(' ');
            }
         }
         /* Delete the last space. */
         int builderLength = tagBuilder.length();
         tagBuilder.deleteCharAt(builderLength - 1);

         tagBuilder.append(", ");
      }

      /* Delete the last comma and space. */
      int builderLength = tagBuilder.length();
      tagBuilder.setLength(builderLength - 2);

      return tagBuilder.toString();
   }

   @Override
   protected
   void onPostExecute(String[] result)
   {
      String feedUrlFromCheck = result[0];
      String finalTitle = result[1];
      String finalTag = result[2];

      boolean isFeedValid = 0 != feedUrlFromCheck.length();
      boolean isExistingFeed = 0 != m_oldFeedName.length();
      Context context = m_dialog.getContext();

      if(isFeedValid)
      {
         /* Create the csv. */
         String feedInfo = String.format(INDEX_FORMAT, finalTitle, feedUrlFromCheck, finalTag) +
               NEW_LINE;

         if(isExistingFeed)
         {
            /* Rename the folder if it is different. */
            String oldFeedFolder = m_oldFeedName + File.separatorChar;
            String newFeedFolder = finalTitle + File.separatorChar;

            if(!m_oldFeedName.equals(finalTitle))
            {
               Write.moveFile(oldFeedFolder, newFeedFolder, m_applicationFolder);
            }

            Write.editLine(Read.INDEX, m_oldFeedName, true, m_applicationFolder, Write.MODE_REPLACE,
                  feedInfo);

         }
         else
         {
            /* Save the feed to the index. */
            Write.single(Read.INDEX, feedInfo, m_applicationFolder);
         }

         /* Update the PagerAdapter for the tag fragments. */
         PagerAdapterFeeds.getTagsFromDisk(m_applicationFolder, m_allTag);
         m_pagerAdapterFeeds.notifyDataSetChanged();

         /* Update the NavigationDrawer adapter.
          * The subtitle of the actionbar should never change on an add of a feed.*/
         AsyncRefreshNavigationAdapter.newInstance(m_navigationAdapter, m_applicationFolder);

         /* TODO AsyncManageTagsRefresh.newInstance(tagListView); */
         if(null != m_listView)
         {
            AsyncManageFeedsRefresh.newInstance(m_listView, m_applicationFolder);
         }

         m_dialog.dismiss();
      }
      else
      {
         Button button = (Button) m_dialog.findViewById(R.id.positive_button);
         button.setText(R.string.add_dialog);
         button.setEnabled(true);
      }

      /* Show added feed toast notification. */
      String text = isFeedValid
            ? context.getString(R.string.added_feed) + ' ' + finalTitle
            : context.getString(R.string.invalid_feed);

      Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
      toast.show();
   }
}
