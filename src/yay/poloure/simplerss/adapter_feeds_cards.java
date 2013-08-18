package yay.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.widget.DrawerLayout;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.widget.Toast;
import android.widget.ListView;
import android.view.ViewGroup.LayoutParams;
import java.util.List;
import android.widget.AbsListView;
import java.util.ArrayList;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.webkit.WebView;
import android.os.Bundle;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import android.util.DisplayMetrics;
import android.os.Handler;
import android.graphics.Color;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.Set;
import java.util.HashSet;

import android.os.Debug;

public class adapter_feeds_cards extends BaseAdapter
{
	private final List<String> content_titles		= new ArrayList<String>();
	private final List<String> content_des			= new ArrayList<String>();
	private final List<String> content_images		= new ArrayList<String>();
	private final List<Integer> content_height	= new ArrayList<Integer>();
	private final List<Integer> content_width		= new ArrayList<Integer>();
	private final List<Boolean> content_marker	= new ArrayList<Boolean>();
	public final List<String> content_links		= new ArrayList<String>();
	public static Set<String> read_items;

	private static LayoutInflater inflater;
	private final Context context;
	private ListView listview;

	private static int eight = 0;
	private final int screen_width;
	public int total = 0;
	private Boolean first = true;
	public int unread_count = 0;
	public int top_item = 0;
	private boolean touched = false;

	public adapter_feeds_cards(Context context_main)
	{
		context = context_main;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		screen_width = metrics.widthPixels;
		eight = (int) ((8 * (metrics.density) + 0.5f));
	}

	public void add_list(List<String> new_title, List<String> new_des, List<String> new_link, List<String> new_image, List<Integer> new_height, List<Integer> new_width)
	{
		content_titles.addAll(new_title);
		content_des.addAll(new_des);
		content_links.addAll(new_link);
		content_images.addAll(new_image);
		content_height.addAll(new_height);
		content_width.addAll(new_width);
		total = content_titles.size();
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
		return content_titles.get(position);
	}

	/* If the listview starts at the very top of a list with 20 items, position 19 is the only on calling getView(). */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
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
					if(!touched)
						touched = true;
					if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
						main.update_navigation_data(null, false);
				}
			});
			first = false;
		}
		position = total - position - 1;

		ViewHolder holder;
		utilities.log(main.storage, Integer.toString(position));
		if(convertView == null)
		{
			convertView 				= inflater.inflate(R.layout.card_layout, parent, false);
			holder 						= new ViewHolder();
			holder.title_view 		= (TextView) convertView.findViewById(R.id.title);
			holder.time_view 			= (TextView) convertView.findViewById(R.id.time);
			holder.description_view = (TextView) convertView.findViewById(R.id.description);
			holder.image_view 		= (ImageView) convertView.findViewById(R.id.image);
			holder.left					= (ImageView) convertView.findViewById(R.id.white_left_shadow);
			holder.right				= (ImageView) convertView.findViewById(R.id.white_right_shadow);
			convertView				.setOnClickListener(new webview_mode());
			convertView				.setOnLongClickListener(new long_press());
			convertView				.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		/* If the item is read, grey it out */
		if(read_items.contains(content_links.get(position)))
		{
			holder.title_view.setAlpha(0.6f);
			holder.description_view.setAlpha(0.6f);
			holder.time_view.setAlpha(0.6f);
			holder.image_view.setAlpha(0.6f);
		}
		else
		{
			holder.title_view.setAlpha(1.0f);
			holder.description_view.setAlpha(1.0f);
			holder.time_view.setAlpha(1.0f);
			holder.image_view.setAlpha(1.0f);
		}
		/* The logic that tells whether the item is read or not. */
		if(touched)
		{
			if(position - 1 >= 0)
				read_items.add(content_links.get(position - 1));
			/* The very top item is read only when the padding exists above it. */
			if(listview.getChildAt(0).getTop() == eight)
				read_items.add(content_links.get(position - 1));
		}

		String title 					= content_titles.get(position);
		String description 			= content_des.get(position);
		final String link				= content_links.get(position);
		final int height 				= content_height.get(position);
		final int width				= content_width.get(position);
		boolean image_exists 		= false;

		if(width > 32)
			image_exists = true;

		if(image_exists)
		{
			holder.left				.setVisibility(View.GONE);
			holder.right			.setVisibility(View.GONE);
			holder.image_view		.setVisibility(View.VISIBLE);
			ViewGroup.LayoutParams iv = holder.image_view.getLayoutParams();
			iv.height	= (int) ((((double) screen_width)/(width)) * (height));
			iv.width	= LayoutParams.MATCH_PARENT;
			holder.image_view.setLayoutParams(iv);
			holder.image_view		.setPadding(0, eight/2, 0, 0);

			load(content_images.get(position), holder.image_view);
		}
		else
			holder.image_view.setVisibility(View.GONE);

		if(!description.equals(""))
		{
			holder.description_view.setVisibility(View.VISIBLE);
			if(image_exists)
			{
				holder.left				.setVisibility(View.VISIBLE);
				holder.right			.setVisibility(View.VISIBLE);
				holder.description_view	.setPadding(eight	, eight/2	, eight	, eight);
				holder.image_view		.setPadding(0		, eight/2	, 0		, eight/2);
			}
			else
				holder.description_view.setPadding(eight, eight/4, eight, eight);
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

	static class ViewHolder
	{
		TextView title_view;
		TextView time_view;
		TextView description_view;
		ImageView image_view;
		ImageView left;
		ImageView right;
	}

	private void load(String path, ImageView imageView)
	{
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(path);

		if(bitmap == null)
			force_load(path, imageView);
		else
		{
			cancelPotentialDownload(path, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}

	private void force_load(String url, ImageView imageView)
	{
		if (url == null)
		{
			imageView.setImageDrawable(null);
			return;
		}

		if(cancelPotentialDownload(url, imageView))
		{
			load_image task = new load_image(imageView);
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
			imageView.setImageDrawable(downloadedDrawable);
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				task.execute(url);
			else
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
		}
	}

	private static boolean cancelPotentialDownload(String url, ImageView imageView)
	{
		load_image bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null)
		{
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url)))
				bitmapDownloaderTask.cancel(true);
			else
				return false;
		}
		return true;
	}


	private static load_image getBitmapDownloaderTask(ImageView imageView)
	{
		if (imageView != null)
		{
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable)
			{
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	class load_image extends AsyncTask<String, Void, Bitmap>
	{
		private String url;
		private final WeakReference<ImageView> imageViewReference;

		public load_image(ImageView imageView)
		{
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... ton)
		{
			url = ton[0];
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = 1;
			Bitmap bit = BitmapFactory.decodeFile(url, o);
			addBitmapToCache(url, bit);
			return bit;
		}

		@Override
		protected void onPostExecute(Bitmap im)
		{
			if(isCancelled())
			{
				im = null;
				return;
			}

			if (imageViewReference != null)
			{
				ImageView image_view = imageViewReference.get();
				load_image bitmapDownloaderTask = getBitmapDownloaderTask(image_view);
				if(this == bitmapDownloaderTask)
				{
					Animation fadeIn = new AlphaAnimation(0, 1);
					fadeIn.setDuration(210);
					fadeIn.setInterpolator(new DecelerateInterpolator());
					image_view.setImageBitmap(im);
					image_view.startAnimation(fadeIn);
					image_view.setOnClickListener(new image_call(url.replaceAll("thumbnails", "images")));
				}
			}
		}
	}

	static class DownloadedDrawable extends ColorDrawable
	{
		private final WeakReference<load_image> bitmapDownloaderTaskReference;

		public DownloadedDrawable(load_image bitmapDownloaderTask)
		{
			super(Color.WHITE);
			bitmapDownloaderTaskReference =
				new WeakReference<load_image>(bitmapDownloaderTask);
		}

		public load_image getBitmapDownloaderTask(){
			return bitmapDownloaderTaskReference.get();
		}
	}

	private static final int HARD_CACHE_CAPACITY = 6;
	private static final int DELAY_BEFORE_PURGE = 10 * 1000;

	private final HashMap<String, Bitmap> sHardBitmapCache =
		new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true)
		{
			@Override
			protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest)
			{
				if(size() > HARD_CACHE_CAPACITY)
				{
					sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
					return true;
				}
				else
					return false;
			}
		};

	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
		new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

	private final Handler purgeHandler = new Handler();

	private final Runnable purger = new Runnable()
	{
		public void run(){
			clearCache();
		}
	};

	private void addBitmapToCache(String url, Bitmap bitmap)
	{
		if(bitmap != null)
		{
			synchronized(sHardBitmapCache){
				sHardBitmapCache.put(url, bitmap);
			}
		}
	}

	private Bitmap getBitmapFromCache(String url)
	{
		synchronized(sHardBitmapCache)
		{
			final Bitmap bitmap = sHardBitmapCache.get(url);
			if(bitmap != null)
			{
				sHardBitmapCache.remove(url);
				sHardBitmapCache.put(url, bitmap);
				return bitmap;
			}
		}

		SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
		if(bitmapReference != null)
		{
			final Bitmap bitmap = bitmapReference.get();
			if (bitmap != null)
				return bitmap;
			else
				sSoftBitmapCache.remove(url);
		}

		return null;
	}

	private void clearCache()
	{
		sHardBitmapCache.clear();
		sSoftBitmapCache.clear();
	}

	private void resetPurgeTimer()
	{
		purgeHandler.removeCallbacks(purger);
		purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
	}

	private class webview_mode implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			main.action_bar.setTitle("Offline");
			main.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			main.drawer_toggle.setDrawerIndicatorEnabled(false);
			main.action_bar.setDisplayHomeAsUpEnabled(true);
			main.fragment_manager.beginTransaction()
					.hide(main.fragment_manager.findFragmentByTag(main.NAVIGATION_TITLES[0]))
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

	public static void show_card_dialog(final Context activity_context, final String URL, final int image_visibility)
	{
		final LayoutInflater inflater		= LayoutInflater.from(activity_context);
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
			super.onPause();
			web_view.onPause();
		}

		@Override
		public void onResume()
		{
			web_view.onResume();
			super.onResume();
		}

		@Override
		public void onDestroyView()
		{
			super.onDestroyView();
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
