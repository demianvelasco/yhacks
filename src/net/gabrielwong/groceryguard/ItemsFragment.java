package net.gabrielwong.groceryguard;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListFragment;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.parse.ParseObject;

public class ItemsFragment extends ListFragment {
	
	ParseObject[] values = null;
	HashMap<Integer, ParseObject> objMap = null;
	HashMap<Integer, AsyncTask<String, Integer, Drawable>> imgMap = null;
	
	private static final String TAG = "ItemsFragment";
	
	public void onCreate(Bundle savedItemState){
		super.onCreate(savedItemState);
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		ParseItemAdapter adapter = new ParseItemAdapter(activity, values, objMap, imgMap);
		setListAdapter(adapter);
	}
	
	public void setItems(ParseObject[] values, HashMap<Integer, ParseObject> objMap){
		this.values = values;
		this.objMap = objMap;
		this.imgMap = new HashMap<Integer, AsyncTask<String, Integer, Drawable>>();
		
		for(ParseObject obj : values){
			int plu = obj.getInt(MainActivity.PLU);
			if (imgMap.get(plu) == null){
				String imgPath = objMap.get(plu).getString(MainActivity.IMAGE_LINK);
				Log.v(TAG, "Loading image for PLU " + plu + " imgPath is " + ((imgPath == null)? "null" : "fine"));
				if (imgPath != null){
					AsyncTask<String, Integer, Drawable> task = new RetrieveImageTask().execute(imgPath);
					imgMap.put(plu, task);
				}
			}
		}
	}
	
	public HashMap<Integer, ParseObject> getObjMap(){
		return objMap;
	}
	
	class RetrieveImageTask extends AsyncTask<String, Integer, Drawable>{

		@Override
		protected Drawable doInBackground(String... urls) {
			URL url;
			try {
				url = new URL(urls[0]);
			} catch (MalformedURLException e1) {
				Log.e(TAG, "Malformed URL");
				return null;
			}
			Log.v(TAG, "Loading image from " + urls[0]);
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
