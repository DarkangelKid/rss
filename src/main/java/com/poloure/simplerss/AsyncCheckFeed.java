/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

class AsyncCheckFeed extends AsyncTask<Void, Void, IndexItem>
{
    /* Formats */
    private static final Pattern SPLIT_SPACE = Pattern.compile(" ");
    private static final Pattern SPLIT_COMMA = Pattern.compile(",");
    private final Dialog m_dialog;
    private final IndexItem m_oldIndexItem;
    private final FeedsActivity m_activity;

    private
    AsyncCheckFeed(FeedsActivity activity, Dialog dialog, IndexItem oldIndexItem)
    {
        m_dialog = dialog;
        m_oldIndexItem = oldIndexItem;
        m_activity = activity;

        Button button = (Button) m_dialog.findViewById(R.id.dialog_button_positive);
        button.setText(R.string.dialog_checking);
        button.setEnabled(false);
    }

    static
    AsyncTask<Void, Void, IndexItem> newInstance(FeedsActivity activity, Dialog dialog, IndexItem oldIndexItem)
    {
        AsyncTask<Void, Void, IndexItem> task = new AsyncCheckFeed(activity, dialog, oldIndexItem);

        task.executeOnExecutor(SERIAL_EXECUTOR);
        return task;
    }

    @Override
    protected
    IndexItem doInBackground(Void... nothing)
    {
        if(isCancelled())
        {
            return null;
        }
      /* Get the user's input. */
        CharSequence inputUrl = ((TextView) m_dialog.findViewById(R.id.dialog_url)).getText();
        CharSequence inputTags = ((TextView) m_dialog.findViewById(R.id.dialog_tags)).getText();

        inputUrl = null == inputUrl ? "" : inputUrl;

      /* Form the array of urls we will check the validity of. */
        CharSequence[] urlCheckList = {inputUrl, "https://" + inputUrl, "http://" + inputUrl};

        String url = "";
        long uid = 0L;

        for(CharSequence urlToCheck : urlCheckList)
        {
            if(isCancelled())
            {
                return null;
            }
            if(isValidFeed(urlToCheck))
            {
            /* Check to see if this url is new .*/
                boolean newFeed = true;
                for(IndexItem indexItem : m_activity.m_index)
                {
                    if(indexItem.m_url.equals(urlToCheck.toString()))
                    {
                        newFeed = false;
                        break;
                    }
                }

                if(newFeed)
                {
                    uid = System.currentTimeMillis();
                }
                url = urlToCheck.toString();
                break;
            }
        }
        return new IndexItem(uid, url, formatUserTagsInput(inputTags));
    }

    private static
    boolean isValidFeed(CharSequence urlString)
    {
        boolean isValid = false;
        try
        {
            XmlPullParser parser = Utilities.createXmlParser(urlString.toString());

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
        catch(IOException ignored)
        {
            // TODO
        }
        catch(XmlPullParserException ignored)
        {
            // TODO
        }
        return isValid;
    }

    private
    String[] formatUserTagsInput(CharSequence userInputTags)
    {
        String inputTags = userInputTags.toString();
        String allTag = m_activity.getString(R.string.all_tag);

        if(inputTags.isEmpty())
        {
            return new String[]{allTag};
        }

        List<String> tagList = new ArrayList<String>(8);

        Locale locale = Locale.getDefault();
        String lowerTags = inputTags.toLowerCase(locale);

      /* + 10 in case the user did not put spaces after the commas. */
        StringBuilder tagBuilder = new StringBuilder(lowerTags.length());
        String[] tags = SPLIT_COMMA.split(lowerTags);

      /* For each tag. */
        for(String tag : tags)
        {
            String tagTrimmed = tag.trim();
            if(!tagTrimmed.isEmpty())
            {
                tagBuilder.setLength(0);

            /* In case the tag is multiple words. */
                String[] words = SPLIT_SPACE.split(tag);

            /* The input tag is all lowercase. */
                for(String word : words)
                {
                    if(!word.isEmpty())
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

                tagList.add(tagBuilder.toString());
            }
        }

        return tagList.toArray(new String[tagList.size()]);
    }

    @Override
    protected
    void onPostExecute(IndexItem result)
    {
        if(isCancelled())
        {
            return;
        }

        Context context = m_dialog.getContext();

        if(result.m_url.isEmpty())
        {
            Button button = (Button) m_dialog.findViewById(R.id.dialog_button_positive);
            button.setText(R.string.dialog_accept);
            button.setEnabled(true);

            Toast toast = Toast.makeText(context, R.string.toast_invalid_feed, Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
         /* Create the csv. */
            int oldPos = m_activity.m_index.indexOf(m_oldIndexItem);

            if(-1 == oldPos)
            {
                m_activity.m_index.add(result);
            }
            else
            {
                m_activity.m_index.set(oldPos, result);
            }

         /* Must update the tags first. */
            PagerAdapterTags.run(m_activity);
            AsyncNavigationAdapter.run(m_activity);
            AsyncManageAdapter.run(m_activity);

            String text = context.getString(R.string.toast_added_feed, result.m_url);
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            toast.show();

         /* Save the index file to disk so the ServiceUpdate can load it. */
            ObjectIO out = new ObjectIO(m_activity, FeedsActivity.INDEX);
            out.write(m_activity.m_index);

            m_dialog.dismiss();
        }
    }
}
