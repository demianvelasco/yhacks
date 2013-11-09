package net.gabrielwong.groceryguard;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.parse.ParseObject;

public class ItemsFragment extends ListFragment implements OnClickListener{
	
	ParseObject[] values = null;
	HashMap<Integer, ParseObject> objMap = null;
	HashMap<Integer, AsyncTask<String, Integer, Drawable>> imgMap = null;
	private Listener mListener = null;
	
	private static final String TAG = "ItemsFragment";
	
	public interface Listener{
		public void onClearPressed(int position);
		public void onItemPressed(int position);
	}
	
	@Override
	public void onCreate(Bundle savedItemState){
		super.onCreate(savedItemState);
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try {
			mListener = (Listener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
		reloadList(activity);
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	public void reloadList(Activity activity){
		ParseItemAdapter adapter = new ParseItemAdapter(activity, this, values, objMap, imgMap);
		setListAdapter(adapter);
	}
	
	public void setItems(List<ParseObject> items, HashMap<Integer, ParseObject> objMap){
		HashMap<Integer,ParseObject> oldMap = this.objMap;
		
		ParseObject[] values = items.toArray(new ParseObject[items.size()]);
		this.values = values;
		this.objMap = objMap;
		if (imgMap == null)
			imgMap = new HashMap<Integer, AsyncTask<String, Integer, Drawable>>();
		
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

	@Override
	public void onClick(View v) {
		int position = getListView().getPositionForView((View)v.getParent());
		
		switch(v.getId()){
		case R.id.clear_button:
			mListener.onClearPressed(position);
			break;
		case R.id.item_name:
			mListener.onItemPressed(position);
			break;
		}
	}
}
