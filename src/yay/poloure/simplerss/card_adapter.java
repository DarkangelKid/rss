package yay.poloure.simplerss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class card_adapter extends ArrayAdapter<String> {
  private final Context context;
  private final String[] values;

  public card_adapter(Context context, String[] values) {
    super(context, R.layout.card_layout, values);
    this.context = context;
    this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View card_view = inflater.inflate(R.layout.card_layout, parent, false);
    TextView textView = (TextView) card_view.findViewById(R.id.label);
    ImageView imageView = (ImageView) card_view.findViewById(R.id.image);
    textView.setText(values[position]);
    
    String s = values[position];
    if (s == "Android") {
      imageView.setImageResource(R.drawable.no);
    } else {
      imageView.setImageResource(R.drawable.ok);
    }

    return card_view;
  }
} 
