package yay.poloure.simplerss;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
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
import android.view.ViewGroup.LayoutParams;
import java.util.List;
import java.util.ArrayList;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.lang.ref.WeakReference;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;

public class card_adapter extends BaseAdapter
{
	private List<String> content_titles = new ArrayList<String>();
	private List<String> content_des = new ArrayList<String>();
	private List<String> content_links = new ArrayList<String>();
	private List<String> content_images = new ArrayList<String>();
	private List<Integer> content_height = new ArrayList<Integer>();
	private List<Integer> content_width = new ArrayList<Integer>();
	
	private final LayoutInflater inflater;
	private Context context;

	private static int eight = 0;

	public card_adapter(Context context_main)
	{
		context = context_main;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void add_list(String new_title, String new_des, String new_link, String new_image, int new_height, int new_width)
	{
		content_titles.add(0, new_title);
		content_des.add(0, new_des.replaceAll("<([^;]*)>", ""));
		content_links.add(0, new_link);
		content_images.add(0, new_image);
		content_height.add(0, new_height);
		content_width.add(0, new_width);
		if(eight == 0)
			eight = (int) ((8 * main_view.get_pixel_density() + 0.5f));

	}
	
	public List<String> return_links(){
		return content_links;
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
			convertView = inflater.inflate(R.layout.card_layout, parent, false);
			holder = new ViewHolder();
			holder.title_view = (TextView) convertView.findViewById(R.id.title);
			holder.time_view = (TextView) convertView.findViewById(R.id.time);
			holder.description_view = (TextView) convertView.findViewById(R.id.description);
			holder.image_view = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		boolean image_exists = false;
		if(content_width.get(position) > 32)
			image_exists = true;

		if(image_exists)
			loadBitmap(position, holder.image_view, content_height.get(position), content_width.get(position));

		convertView.setOnClickListener(new browser_call());
		convertView.setOnLongClickListener(new long_press());
		
		holder.title_view.setText(content_titles.get(position));
		holder.time_view.setText(content_links.get(position));
		
		String des = "";
		if((!content_des.get(position).replace("&n&", "").contains(content_titles.get(position).substring(0, content_titles.get(position).length() - 3)))||(!image_exists))
			des = content_des.get(position).replace("  ", "\n").replace("&t&", "\n").replace("&n&", "\n").trim();

		if((des.length() > 1)&&(image_exists))
		{
			holder.description_view.setPadding(eight, 0, 0, 0);
			ViewGroup.LayoutParams iv = holder.image_view.getLayoutParams();
			iv.height = LayoutParams.WRAP_CONTENT;
			iv.width = LayoutParams.WRAP_CONTENT;
			holder.image_view.setLayoutParams(iv);
		}
		else if(image_exists)
		{
				ViewGroup.LayoutParams iv = holder.image_view.getLayoutParams();
				iv.height = LayoutParams.MATCH_PARENT;
				iv.width = LayoutParams.MATCH_PARENT;
				holder.image_view.setLayoutParams(iv);
				holder.description_view.setPadding(0, 0, 0, 0);
		}
		else
		{
			holder.description_view.setPadding(0, 0, 0, 0);
			ViewGroup.LayoutParams iv = holder.image_view.getLayoutParams();
			iv.height = 0;
			iv.width = 0;
			holder.image_view.setLayoutParams(iv);
		}
		holder.description_view.setText(des);
		return convertView;
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

	void loadBitmap(int position, ImageView image_view, int height, int width)
	{
		if(cancelPotentialWork(position, image_view))
		{
			final display_image task = new display_image(image_view);
			Bitmap.Config conf = Bitmap.Config.ALPHA_8;
			final AsyncDrawable asyncDrawable = new AsyncDrawable(main_view.get_resources(), Bitmap.createBitmap(width, height, conf), task);
			image_view.setImageDrawable(asyncDrawable);
			task.execute(position);
		}
	}

	private static boolean cancelPotentialWork(int position, ImageView image_view) {
		final display_image worker_task = get_task(image_view);

		if (worker_task != null)
		{
			final int check = worker_task.position;
			if (check != position)
				worker_task.cancel(true);
			else
				return false;
		}
		return true;
	}

	private static display_image get_task(ImageView image_view) {
	   if (image_view != null)
	   {
		   final Drawable drawable = image_view.getDrawable();
		   if (drawable instanceof AsyncDrawable) {
			   final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
			   return asyncDrawable.get_display_image_task();
		   }
		}
		return null;
	}

	static class AsyncDrawable extends BitmapDrawable
	{
		private final WeakReference<display_image> display_image_reference;

		public AsyncDrawable(Resources res, Bitmap bitmap, display_image task)
		{
			super(res, bitmap);
			display_image_reference = new WeakReference<display_image>(task);
		}

		public display_image get_display_image_task()
		{
			return display_image_reference.get();
		}
	}
	
	private class image
	{
		public Bitmap img;
		public String path;
	}

	class display_image extends AsyncTask<Integer, Void, image>
	{
		private final WeakReference<ImageView> image_view_reference;
		private int position = 0;

		public display_image(ImageView imageView) {
			image_view_reference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected image doInBackground(Integer... ton)
		{
			position = ton[0];
			image i = new image();
			String image_path = content_images.get(ton[0]);
			i.img = decodeFile(image_path);
			i.path = image_path.replaceAll("thumbnails", "images");
			return i;
		}

		@Override
		protected void onPostExecute(image im)
		{
			 if(isCancelled())
				im.img = null;
				
			if(image_view_reference != null && im.img != null)
			{
				final ImageView image_view = image_view_reference.get();
				final display_image worker_task = get_task(image_view);
				if(this == worker_task && image_view != null)
				{
					final TransitionDrawable td = new TransitionDrawable(new Drawable[]
					{
						image_view.getDrawable(),
						new BitmapDrawable(main_view.get_resources(), im.img)
					});
					image_view.setImageDrawable(td);
					td.startTransition(230);
					
					image_view.setOnClickListener(new image_call(im.path));
				}
			}
		}
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
