package net.gabrielwong.groceryguard;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

public class ParseItemAdapter extends ArrayAdapter<ParseObject>{

	private final Context context;
	private final ParseObject[] values;
	private final HashMap<Integer, ParseObject> objMap;
	private final HashMap<Integer, AsyncTask<String, Integer, Drawable>> imgMap;
	private final ItemsFragment mItemsFragment;
	
	private static final int GREEN = 0xFF33CC33,
							 RED = 0xFFCC3333,
							 ORANGE = 0xFFDD6D22;
	
	private static final String TAG = "ParseItemAdapter";

	public ParseItemAdapter(Activity activity, ItemsFragment itemsFragment, ParseObject[] values, HashMap<Integer, ParseObject> objMap,
			HashMap<Integer, AsyncTask<String, Integer, Drawable>> imgMap) {
		super(activity, R.layout.view_item, values);
		this.context = activity;
		this.values = values;
		this.objMap = objMap;
		this.imgMap = imgMap;
		this.mItemsFragment = itemsFragment;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.view_item, parent, false);
		
		TextView textView = (TextView) rowView.findViewById(R.id.item_name);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.item_picture);
		ImageButton clearButton = (ImageButton) rowView.findViewById(R.id.clear_button);
		TextView timeLeft = (TextView) rowView.findViewById(R.id.time_left);

		ParseObject obj = objMap.get(values[position].getInt(MainActivity.PLU));

		String name = obj.getString(MainActivity.ITEM_NAME);
		textView.setText(name);
		
		int daysLeft = getDaysLeft(values[position]);
		timeLeft.setText(daysLeft +"d");
		
		clearButton.setOnClickListener(mItemsFragment);
		textView.setOnClickListener(mItemsFragment);

		Drawable image = null;
		try {
			AsyncTask<String, Integer, Drawable> task = imgMap.get(values[position].getInt(MainActivity.PLU));
			if (task != null)
				image = task.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		if (image != null)
			imageView.setImageDrawable(image);
		
		timeLeft.setBackgroundColor(getColor(daysLeft));

		return rowView;
	}
	
	private int getDaysLeft(ParseObject obj){
		Date expiry = obj.getDate(MainActivity.DATE_EXPIRING);
		Date now = new Date();
		
		long millisLeft = expiry.getTime() - now.getTime();
		return (int)((millisLeft + MainActivity.MILLIS_IN_DAY / 2) / MainActivity.MILLIS_IN_DAY);
	}
	
	private int getColor(int daysLeft){
		int orangeThresh = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("orange_threshold", "4"));
		int redThresh = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("red_threshold", "2"));
		if (daysLeft > orangeThresh)
			return GREEN;
		if (daysLeft > redThresh)
			return ORANGE;
		return RED;
	}
}
