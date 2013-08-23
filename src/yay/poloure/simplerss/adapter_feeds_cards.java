package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

public class adapter_feeds_cards extends BaseAdapter
{
	public  String[] links			= new String[0];
	private String[] titles			= new String[0];
	private String[] descriptions	= new String[0];
	private String[] images			= new String[0];
	private int[]    heights		= new int[0];
	private int[]    widths			= new int[0];

	public static Set<String> read_items;

	private static final Pattern thumb_img = Pattern.compile("thumbnails");
	private static Context context;
	private static LayoutInflater inflater;
	private static int two = 0, four = 0, eight = 0;
	private static int screen_width;

	private static final int link_black  = Color.rgb(128, 128, 128);
	private static final int link_grey   = Color.rgb(194, 194, 194);

	private static final int des_black   = Color.rgb(78, 78, 78);
	private static final int des_grey    = Color.rgb(167, 167, 167);

	private static final int title_black = Color.rgb(0, 0, 0);
	private static final int title_grey  = Color.rgb(128, 128, 128);

	private  int			total				= 0;
	public  int			unread_count	= 0;
	private boolean	first				= true;
	private ListView	listview;


	public adapter_feeds_cards(Context context_main)
	{
		if(context == null)
		{
			context						= context_main;
			DisplayMetrics metrics	= context.getResources().getDisplayMetrics();
			inflater						= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			screen_width 				= metrics.widthPixels;
			if(two == 0)
			{
				two		= (int) ((2  * (metrics.density) + 0.5f));
				four		= (int) ((4  * (metrics.density) + 0.5f));
				eight		= (int) ((8  * (metrics.density) + 0.5f));
			}
		}
	}

	public void add_array(String[] new_title, String[] new_des, String[] new_link, String[] new_image, int[] new_height, int[] new_width)
	{
		titles			= utilities.concat_string_arrays(titles,			new_title);
		descriptions	= utilities.concat_string_arrays(descriptions,	new_des);
		links				= utilities.concat_string_arrays(links,			new_link);
		images			= utilities.concat_string_arrays(images,			new_image);
		heights			= utilities.concat_int_arrays(heights,				new_height);
		widths			= utilities.concat_int_arrays(widths,				new_width);
		total = titles.length;
	}

	@Override
	public int getCount()
	{
		return total;
	}

	@Override
	public long getItemId(int position)
	{
		position = total - position - 1;
		return position;
	}

	@Override
	public String getItem(int position)
	{
		position = total - position - 1;
		return titles[position];
	}

	/* If the listview starts at the very top of a list with 20 items, position 19 is the only on calling getView(). */
	@Override
	public View getView(int pos, View convertView, ViewGroup parent)
	{
		final int position = total - pos - 1;

		if(first)
		{
			listview = (ListView) parent;
			listview.setOnScrollListener(new AbsListView.OnScrollListener()
			{
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
				}

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState)
				{
					/* The very top item is read only when the padding exists above it. */
					/* links.get(0) == the last link in the list. position is always 76*/
					if(listview.getChildAt(0).getTop() == eight)
						read_items.add(links[links.length - 1]);

					/*if(listview.getChildAt(position).getTop() == eight)
						read_items.add(links.get(position));*/
					if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
						navigation_drawer.update_navigation_data(null, false);
				}
			});
		}

		ViewHolder holder;
		if(convertView == null)
		{
			convertView 				= inflater.inflate(R.layout.card_layout, parent, false);
			holder 						= new ViewHolder();
			holder.title_view 		= (TextView)  convertView.findViewById(R.id.title					);
			holder.time_view 			= (TextView)  convertView.findViewById(R.id.time					);
			holder.description_view = (TextView)  convertView.findViewById(R.id.description			);
			holder.image_view 		= (ImageView) convertView.findViewById(R.id.image					);
			holder.left					= (ImageView) convertView.findViewById(R.id.white_left_shadow	);
			holder.right				= (ImageView) convertView.findViewById(R.id.white_right_shadow	);
			if(first)
			{
				holder.title_view			.setTextColor(title_black);
				holder.description_view	.setTextColor(des_black);
				holder.time_view			.setTextColor(link_black);
				first = false;
			}
			convertView					.setOnClickListener(new webview_mode());
			convertView					.setOnLongClickListener(new long_press());
			convertView					.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		final String link				= links[position];
		final int height 				= heights[position];
		final int width				= widths[position];
		boolean image_exists 		= false;

		if(width == 0)
			holder.image_view.setVisibility(View.GONE);
		else
			image_exists = true;

		if(image_exists)
		{
			holder.image_view				.setImageDrawable(new ColorDrawable(Color.WHITE));
			holder.image_view				.setVisibility(View.VISIBLE);
			holder.left						.setVisibility(View.GONE);
			holder.right					.setVisibility(View.GONE);
			ViewGroup.LayoutParams iv	= holder.image_view.getLayoutParams();
			iv.height						= (int) ((((double) screen_width)/(width)) * (height));
			iv.width							= LayoutParams.MATCH_PARENT;
			holder.image_view				.setLayoutParams(iv);
			holder.image_view				.setPadding(0, four, 0, 0);
			holder.image_view				.setTag(position);

			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				(new image()).execute(holder, holder.image_view, holder.image_view.getTag());
			else
				(new image()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder.image_view, holder.image_view.getTag());
		}

		/* Alpha MIN API 11 - Also may be a performance hog - If the item is read, grey it out */
		/*int colour = holder.time_view.getCurrentTextColor();*/
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			if(read_items.contains(links[position])/* && colour == link_black*/)
			{
				holder.title_view			.setTextColor(title_grey);
				holder.description_view	.setTextColor(des_grey);
				holder.time_view			.setTextColor(link_grey);
				if(image_exists)
					holder.image_view.setAlpha(0.5f);
			}
			else/* if(colour == link_grey)*/
			{
				holder.title_view			.setTextColor(title_black);
				holder.description_view	.setTextColor(des_black);
				holder.time_view			.setTextColor(link_black);
				if(image_exists)
					holder.image_view.setAlpha(1.0f);
			}
		}

		/* The logic that tells whether the item is read or not. */
		if(listview.getVisibility() == View.VISIBLE && position - 1 >= 0)
				read_items.add(links[position - 1]);

		String title 					= titles[position];
		String description 			= descriptions[position];

		if(description != null && !description.equals(""))
		{
			holder.description_view.setVisibility(View.VISIBLE);
			if(image_exists)
			{
				holder.left					.setVisibility(View.VISIBLE);
				holder.right				.setVisibility(View.VISIBLE);
				holder.description_view	.setPadding(eight	, four	, eight	, eight);
				holder.image_view			.setPadding(0		, four	, 0		, four);
			}
			else
				holder.description_view.setPadding(eight, two, eight, eight);

			holder.description_view.setText(description);
		}
		else
		{
			holder.description_view.setVisibility(View.GONE);
			if(!image_exists)
			{
				holder.time_view.setPadding(eight, 0, eight, eight);
			}
		}

		holder.title_view.setText(title);
		holder.time_view.setText(link);
		return convertView;
	}

	class image extends AsyncTask<Object, Void, Object[]>
	{
		private ImageView iv;
		private int tag;

		@Override
		protected Object[] doInBackground(Object... params)
		{
			iv		= (ImageView)	params[0];
			tag	= (Integer)		params[1];
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = 1;
			Animation fadeIn = new AlphaAnimation(0, 1);
			fadeIn.setDuration(240);
			fadeIn.setInterpolator(new DecelerateInterpolator());
			iv.setOnClickListener(new image_call(thumb_img.matcher(images[tag]).replaceAll("images")));
			Object[] ob = {BitmapFactory.decodeFile(images[tag], o), fadeIn};
			return ob;
		}

		@Override
		protected void onPostExecute(Object... result)
		{
			if((Integer) iv.getTag() != tag)
				return;
			if(iv != null && result[0] != null)
			{
				iv.setImageBitmap((Bitmap) result[0]);
				iv.startAnimation((Animation) result[1]);
				if((Integer) iv.getTag() != tag)
					return;
				iv.setVisibility(View.VISIBLE);
			}
		}
	}


	static class ViewHolder
	{
		TextView title_view;
		TextView time_view;
		TextView description_view;
		ImageView image_view;
		ImageView left;
		ImageView right;
	}

	private class webview_mode implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			main.action_bar.setTitle("Offline");
			navigation_drawer.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			navigation_drawer.drawer_toggle.setDrawerIndicatorEnabled(false);
			main.action_bar.setDisplayHomeAsUpEnabled(true);
			main.fragment_manager.beginTransaction()
					.hide(main.fragment_manager.findFragmentByTag(navigation_drawer.NAVIGATION_TITLES[0]))
					.add(R.id.drawer_layout, new fragment_webview(), "OFFLINE")
					.addToBackStack("BACK")
					.commit();
		}
	}

	private class image_call implements View.OnClickListener
	{
		private final String image_path;
		public image_call(String im)
		{
			image_path = im;
		}

		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			String type = image_path.substring(image_path.lastIndexOf('.') + 1, image_path.length());

			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
				intent.setDataAndType(Uri.fromFile(new File(image_path)), "image/" + type);
			else
				intent.setDataAndTypeAndNormalize(Uri.fromFile(new File(image_path)), "image/" + type);

			context.startActivity(intent);
		}
	}

	private class long_press implements View.OnLongClickListener
	{
		@Override
		public boolean onLongClick(View view)
		{
			String long_press_url = ((TextView) view.findViewById(R.id.time)).getText().toString();
			show_card_dialog(context, long_press_url, ((ViewHolder) view.getTag()).image_view.getVisibility());
			return true;
		}
	}

	private static void show_card_dialog(final Context activity_context, final String URL, final int image_visibility)
	{
		String[] menu_items;
		if(image_visibility != View.VISIBLE)
			menu_items = activity_context.getResources().getStringArray(R.array.card_menu);
		else
			menu_items = activity_context.getResources().getStringArray(R.array.card_menu_image);


		final AlertDialog card_dialog = new AlertDialog.Builder(activity_context)
				.setCancelable(true)
				.setItems(menu_items, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int position)
					{
						switch(position)
						{
							case(0):
								ClipboardManager clipboard = (ClipboardManager) activity_context.getSystemService(Context.CLIPBOARD_SERVICE);
								ClipData clip = ClipData.newPlainText("label", URL);
								clipboard.setPrimaryClip(clip);
								break;
							case(1):
								activity_context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
							/*case(2):
								break;*/
						}
					}
				})
				.create();

				card_dialog.show();
	}

	private class fragment_webview extends Fragment
	{
		private WebView web_view;
		private FrameLayout view;
		private TextView text;

		public fragment_webview()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			if(web_view != null)
				web_view.destroy();

			view = new FrameLayout(getActivity());
			web_view = new WebView(getActivity());
			view.addView(web_view, LayoutParams.MATCH_PARENT);

			/*text = new TextView(getActivity());
			text.setText("webview");
			text.setGravity(Gravity.CENTER);
			text.setVisibility(View.GONE);
			view.addView(text, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);*/

			return view;
		}

		@Override
		public void onPause()
		{
		/* min api 11
			web_view.onPause();*/
			web_view.onPause();
		}

		@Override
		public void onResume()
		{
			/* min api 11
			web_view.onResume();*/
			super.onResume();
		}

		@Override
		public void onDestroy()
		{
			if(web_view != null)
			{
				view.removeAllViews();
				web_view.removeAllViews();
				web_view.destroy();
				web_view = null;
				view = null;
			}
			super.onDestroy();
		}

		public WebView get_webview()
		{
			return web_view;
		}
	}
}
