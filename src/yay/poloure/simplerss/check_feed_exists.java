package yay.poloure.simplerss;

import java.net.URL;
import java.io.BufferedInputStream;
import android.app.AlertDialog;
import android.widget.Button;
import android.os.AsyncTask;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class check_feed_exists extends AsyncTask<String, Void, String[]>
{
	private Boolean existing_group = false, real = false;
	private AlertDialog dialog;
	private String group, name, mode, all_string;
	private String spinner_group, current_group, current_title;
	private int pos;
	private static final Pattern illegal_file_chars	= Pattern.compile("[/\\?%*|<>:]");

	public check_feed_exists(AlertDialog edit_dialog, String new_group, String feed_name, String moder, String spin_group, String current_tit, String current_grop, int position, String all_str)
	{
		dialog			= edit_dialog;
		group			= new_group;
		name			= feed_name;
		mode			= moder;
		spinner_group	= spin_group;
		current_group	= current_grop;
		current_title	= current_tit;
		pos				= position;
		all_string		= all_str;
		Button button	= dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		if(button != null)
			button.setEnabled(false);
	}

	@Override
	protected String[] doInBackground(String... passed_url)
	{
		/// If the group entry has text, check to see if it is an old group or if it is new.
		String url = "", feed_title = "";
		if(group.length()>0)
		{
			final List<String> current_groups = utilities.read_file_to_list(main_view.storage + main_view.GROUP_LIST);
			for(String gro : current_groups)
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
			group = spinner_group;
			existing_group = true;
		}

		String[] check_list;
		if(!passed_url[0].contains("http"))
			check_list = new String[]{"http://" + passed_url[0], "https://" + passed_url[0]};
		else
			check_list = new String[]{passed_url[0]};

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
						data = utilities.concat_byte_arrays(data, data2);
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
			utilities.toast_message(main_view.activity_context, main_view.activity_context.getString(R.string.feed_invalid), false);
			Button button	= dialog.getButton(AlertDialog.BUTTON_POSITIVE);
			if(button != null)
				button.setEnabled(true);
		}
		else
		{
			String storage = main_view.storage;
			if(!existing_group)
				utilities.add_group(storage, group);
			if(name.isEmpty())
				name = ton[1];

			name = illegal_file_chars.matcher(name).replaceAll("");

			if(mode.equals("edit"))
				utilities.edit_feed(storage, current_title, name, ton[0], current_group, group, pos, all_string);
			else
				utilities.add_feed(storage, name, ton[0], group, all_string);

			dialog.dismiss();
		}
	}
}
