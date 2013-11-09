package net.gabrielwong.groceryguard;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

public class ParseItemAdapter extends ArrayAdapter<ParseObject>{

	private final Context context;
	private final ParseObject[] values;
	private final HashMap<Integer, ParseObject> objMap;
	
	private static final String TAG = "ParseItemAdapter";

	public ParseItemAdapter(Context context, ParseObject[] values, HashMap<Integer, ParseObject> objMap) {
		super(context, R.layout.view_item, values);
		this.context = context;
		this.values = values;
		this.objMap = objMap;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.view_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.item_name);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.item_picture);

		ParseObject obj = objMap.get(values[position].getInt(MainActivity.PLU));

		String name = obj.getString(MainActivity.ITEM_NAME);
		textView.setText(name);

		String imagePath = obj.getString(MainActivity.IMAGE_LINK);
		if (imagePath != null){
			Drawable image = null;
			AsyncTask<String, Integer, Drawable> task = new RetrieveImageTask().execute(imagePath);
			try {
				image = task.get();
				if (imagePath != null)
					imageView.setImageDrawable(image);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return rowView;
	}
	
	class RetrieveImageTask extends AsyncTask<String, Integer, Drawable>{

		@Override
		protected Drawable doInBackground(String... urls) {
			URL url;
			try {
				url = new URL(urls[0]);
			} catch (MalformedURLException e1) {
				return null;
			}
			try {
				InputStream is = (InputStream) url.getContent();
				Drawable d = Drawable.createFromStream(is, "src name");
				Log.v(TAG, "Picture loaded from " + url);
				return d;
			} catch (Exception e) {
				Log.e(TAG, e.getClass().getSimpleName());
				return null;
			}
		}
		
	}

}
