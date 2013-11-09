package net.gabrielwong.groceryguard;

import java.util.HashMap;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;

import com.parse.ParseObject;

public class ItemsFragment extends ListFragment {
	
	ParseObject[] values = null;
	HashMap<Integer, ParseObject> objMap = null;
	
	private static final String TAG = "ItemsFragment";
	
	public void onCreate(Bundle savedItemState){
		super.onCreate(savedItemState);
	}
	
	public void onAttach(Activity activity){
		super.onAttach(activity);
		ParseItemAdapter adapter = new ParseItemAdapter(activity, values, objMap);
		setListAdapter(adapter);
	}
	
	public void setItems(ParseObject[] values, HashMap<Integer, ParseObject> objMap){
		this.values = values;
		this.objMap = objMap;
	}
}
