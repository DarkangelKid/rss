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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import android.util.DisplayMetrics;
import android.os.Handler;
import android.graphics.Color;

import android.os.Debug;

public class card_adapter extends BaseAdapter
{
	private final List<String> content_titles = new ArrayList<String>();
	private final List<String> content_des = new ArrayList<String>();
	private final List<String> content_links = new ArrayList<String>();
	private final List<String> content_images = new ArrayList<String>();
	private final List<Integer> content_height = new ArrayList<Integer>();
	private final List<Integer> content_width = new ArrayList<Integer>();
	private final List<Boolean> content_marker = new ArrayList<Boolean>();

	private static LayoutInflater inflater;
	private final Context context;
	private ListView listview;

	private static int eight = 0, twelve = 0;
	private int top_item_position = -1;
	private final int screen_width;
	private int total = 0;
	private Boolean first = true;

	public card_adapter(Context context_main)
	{
		context = context_main;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		screen_width = metrics.widthPixels;
		eight = (int) ((8 * (metrics.density) + 0.5f));
		twelve = (int) ((12 * (metrics.density) + 0.5f));
	}

	public void add_list(List<String> new_title, List<String> new_des, List<String> new_link, List<String> new_image, List<Integer> new_height, List<Integer> new_width, List<Boolean> new_marker)
	{
		content_titles.addAll(new_title);
		content_des.addAll(new_des);
		content_links.addAll(new_link);
		content_images.addAll(new_image);
		content_height.addAll(new_height);
		content_width.addAll(new_width);
		content_marker.addAll(new_marker);
		total = content_titles.size();
	}

	public void set_latest_item(int position)
	{
		top_item_position = position;
	}

	public List<String> return_links()
	{
		return content_links;
	}

	public int return_unread_item_count()
	{
		/// oldest = 0, newest = 20;
		return top_item_position;
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
					if((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)||(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL))
					{
						int firstVisibleItem = listview.getFirstVisiblePosition();

						firstVisibleItem = total - firstVisibleItem;
						if(firstVisibleItem == total)
						{
							if(listview.getChildAt(0).getTop() == twelve)
								top_item_position = total - 1;
							else if(top_item_position != total - 1)
								top_item_position = total - 2;
						}
						else if(firstVisibleItem - 2 > top_item_position)
							top_item_position = firstVisibleItem - 2;
					}
					if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
					{
						drawer_adapter nav_adapter = main_view.nav_adapter;
						nav_adapter.add_count(utilities.get_unread_counts(main_view.fragment_manager, main_view.viewpager, main_view.storage));
						nav_adapter.notifyDataSetChanged();
					}
				}
			});
			first = false;
		}

		ViewHolder holder;
		position = total - position - 1;
		if(convertView == null)
		{
			convertView 			= inflater.inflate(R.layout.card_layout, parent, false);
			holder 					= new ViewHolder();
			holder.title_view 		= (TextView) convertView.findViewById(R.id.title);
			holder.time_view 		= (TextView) convertView.findViewById(R.id.time);
			holder.description_view = (TextView) convertView.findViewById(R.id.description);
			holder.image_view 		= (ImageView) convertView.findViewById(R.id.image);
			convertView			.setOnClickListener(new browser_call());
			convertView			.setOnLongClickListener(new long_press());
			convertView			.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		String title 				= content_titles.get(position);
		String description 			= content_des.get(position);
		final String link			= content_links.get(position);
		final int height 			= content_height.get(position);
		final int width				= content_width.get(position);
		boolean image_exists 		= false;

		if(width > 32)
			image_exists = true;

		if(image_exists)
		{
			final String image_path = content_images.get(position);
				load(image_path, holder.image_view);
		}

		ViewGroup.LayoutParams iv = holder.image_view.getLayoutParams();
		if((image_exists)&&(!description.isEmpty()))
		{
			/// The height for this needs to be divided by the shrink ratio.
			holder.description_view.setPadding(eight, 0, eight, eight);
			iv.height 					= height;
			iv.width 					= LayoutParams.WRAP_CONTENT;
			holder.image_view.setLayoutParams(iv);
		}
		else if(image_exists)
		{
			iv.height 					= (int) (((screen_width + 0.1)/(width + 0.1)) * (height + 0.1));
			iv.width 					= LayoutParams.MATCH_PARENT;
			holder.image_view.setLayoutParams(iv);
		}
		else
		{
			holder.description_view.setPadding(eight, 0, eight, eight);
			iv.height 					= 0;
			iv.width 					= 0;
			holder.image_view.setLayoutParams(iv);
		}

		holder.title_view.setText(title);
		holder.time_view.setText(link);
		holder.description_view.setText(description);
		return convertView;
	}

	public String return_latest_url()
	{
		return content_links.get(top_item_position);
	}

	static class ViewHolder
	{
		TextView title_view;
		TextView time_view;
		TextView description_view;
		ImageView image_view;
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
			return BitmapFactory.decodeFile(url, o);
		}

		@Override
		protected void onPostExecute(Bitmap im)
		{
			if (isCancelled())
				im = null;

			addBitmapToCache(url, im);

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

	private class browser_call implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(((TextView) v.findViewById(R.id.time)).getText().toString())));
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
			intent.setDataAndTypeAndNormalize(Uri.fromFile(new File(image_path)), "image/" + type);
			context.startActivity(intent);
		}
	}

	private class long_press implements View.OnLongClickListener
	{
		@Override
		public boolean onLongClick(View v)
		{
			String long_press_url = ((TextView) v.findViewById(R.id.time)).getText().toString();
			ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("label", long_press_url);
			clipboard.setPrimaryClip(clip);
			Toast message_toast = Toast.makeText(context, "Copied link url to clipboard.", Toast.LENGTH_SHORT);
			message_toast.show();
			return true;
		}
	}
}
