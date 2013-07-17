package yay.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.ColorDrawable;
import android.content.res.Resources;
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
import android.widget.AbsListView.OnScrollListener;
import java.util.ArrayList;
import android.net.Uri;
import android.os.Process;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import android.os.Handler;
import android.graphics.Color;

import android.os.Debug;

public class card_adapter extends BaseAdapter
{
	private List<String> content_titles = new ArrayList<String>();
	private List<String> content_des = new ArrayList<String>();
	private List<String> content_links = new ArrayList<String>();
	private List<String> content_images = new ArrayList<String>();
	private List<Integer> content_height = new ArrayList<Integer>();
	private List<Integer> content_width = new ArrayList<Integer>();
	private List<Boolean> content_marker = new ArrayList<Boolean>();

	private LayoutInflater inflater;
	private Context context;
	private ListView listview;

	private static int eight = 0;
	private int top_item_position = -1;
	private int screen_width;

	public card_adapter(Context context_main)
	{
		context = context_main;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		eight = (int) ((8 * main_view.get_pixel_density() + 0.5f));
		screen_width = main_view.get_width();
	}

	public void add_list(String new_title, String new_des, String new_link, String new_image, int new_height, int new_width, Boolean new_marker)
	{
		content_titles.add(0, new_title);
		content_des.add(0, new_des.replaceAll("<([^;]*)>", ""));
		content_links.add(0, new_link);
		content_images.add(0, new_image);
		content_height.add(0, new_height);
		content_width.add(0, new_width);
		content_marker.add(0, new_marker);
	}

	public List<String> return_links(){
		return content_links;
	}

	public int return_unread_item_count(){
		return top_item_position;
	}

	@Override
	public int getCount(){
		return content_titles.size();
	}

	@Override
	public long getItemId(int position){
		return position;
	}

	@Override
	public String getItem(int position){
		return content_titles.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if(convertView == null)
		{
			listview 				= (ListView) parent;
			convertView 			= inflater.inflate(R.layout.card_layout, parent, false);
			holder 					= new ViewHolder();
			holder.title_view 		= (TextView) convertView.findViewById(R.id.title);
			holder.time_view 		= (TextView) convertView.findViewById(R.id.time);
			holder.description_view = (TextView) convertView.findViewById(R.id.description);
			holder.image_view 		= (ImageView) convertView.findViewById(R.id.image);
			convertView			.setTag(holder);
			convertView			.setOnClickListener(new browser_call());
			convertView			.setOnLongClickListener(new long_press());
			((ListView) parent)	.setOnScrollListener(new AbsListView.OnScrollListener()
			{
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
				}

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState)
				{
					if((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)||(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL))
					{
						if(top_item_position == - 1)
						{
							final int size = content_marker.size();
							for(int i = 0; i < size; i++)
							{
								if(content_marker.get(i))
								{
									top_item_position = i;
									break;
								}
							}
							if(top_item_position == -1)
								top_item_position = size - 1;
						}

						final int firstVisibleItem = listview.getFirstVisiblePosition();
						if((firstVisibleItem != -1)&&(firstVisibleItem < top_item_position))
							top_item_position = firstVisibleItem;
					}
					if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
					{
						drawer_adapter nav_adapter = main_view.return_nav_adapter();
						nav_adapter.add_count(main_view.get_unread_counts());
						nav_adapter.notifyDataSetChanged();
					}
				}
			});
		}
		else
			holder = (ViewHolder) convertView.getTag();

		final String title 					= content_titles.get(position);
		String description 					= content_des.get(position);
		final String link					= content_links.get(position);
		final int height 					= content_height.get(position);
		final int width						= content_width.get(position);
		boolean image_exists 				= false;

		if(width > 32)
			image_exists = true;

		if(image_exists)
		{
			final String image_path = content_images.get(position);
				load(image_path, holder.image_view);
		}
		if(description.contains(title))
			description = "";

		ViewGroup.LayoutParams iv = holder.image_view.getLayoutParams();
		if((image_exists)&&(!description.isEmpty()))
		{
			/// The height for this needs to be divided by the shrink ratio.
			holder.description_view.setPadding(eight, 0, 0, 0);
			iv.height 					= height;
			iv.width 					= LayoutParams.WRAP_CONTENT;
			holder.image_view.setLayoutParams(iv);
		}
		else if(image_exists)
		{
			iv.height 					= (int) (((screen_width)/width) * height);
			iv.width 					= LayoutParams.MATCH_PARENT;
			holder.image_view.setLayoutParams(iv);
			holder.description_view.setPadding(0, 0, 0, 0);
		}
		else
		{
			holder.description_view.setPadding(0, 0, 0, 0);
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
		if(top_item_position == -1)
			return "";
		return content_links.get(top_item_position);
	}

	static class ViewHolder
	{
		TextView title_view;
		TextView time_view;
		TextView description_view;
		ImageView image_view;
	}

	private static Bitmap decodeFile(String filePath)
	{
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inSampleSize = 1;
		return BitmapFactory.decodeFile(filePath, o);
	}

	public void load(String path, ImageView imageView)
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
			task.execute(url);
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
			return decodeFile(url);
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

	private static final int HARD_CACHE_CAPACITY = 10;
	private static final int DELAY_BEFORE_PURGE = 10 * 1000;

	private final HashMap<String, Bitmap> sHardBitmapCache =
		new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true)
		{
			@Override
			protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
				if (size() > HARD_CACHE_CAPACITY)
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

	public void clearCache()
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
		private String image_path;
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
