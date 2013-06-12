package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class card_adapter extends ArrayAdapter<String>{
	private final Context context;
	private final String[] content;

	public card_adapter(Context context, String[] content)
	{
		super(context, R.layout.card_layout, content);
		this.context = context;
		this.content = content;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(position<content.length/3)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View card_view = inflater.inflate(R.layout.card_layout, parent, false);
			TextView title_view = (TextView) card_view.findViewById(R.id.title);
			TextView time_view = (TextView) card_view.findViewById(R.id.time);
			TextView description_view = (TextView) card_view.findViewById(R.id.description);
			ImageView imageView = (ImageView) card_view.findViewById(R.id.image);
			title_view.setText(content[3*position]);
			time_view.setText(content[(3*position)+1]);
			description_view.setText(content[(3*position)+2]);
			imageView.setImageResource(R.drawable.ok);
			return card_view;
		}
		View no_view = new View(context);
		return no_view;
	}
} 
