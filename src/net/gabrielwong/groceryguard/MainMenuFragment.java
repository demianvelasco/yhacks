package net.gabrielwong.groceryguard;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class MainMenuFragment extends Fragment implements OnClickListener{

	private Listener mListener;
	boolean mSignedIn = false;
	
	public interface Listener {
		public void onScanButton();
		public void onItemsButton();
		public void onSettingsButton();
		public void onRecipeButton();
	}

	public MainMenuFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_main_menu_new, container,
				false);
		
		v.findViewById(R.id.scan_button).setOnClickListener(this);
	    v.findViewById(R.id.items_button).setOnClickListener(this);
	    v.findViewById(R.id.settings_button).setOnClickListener(this);
	    v.findViewById(R.id.recipe_button).setOnClickListener(this);
	    
	    return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (Listener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.scan_button:
			mListener.onScanButton();
			break;
		case R.id.items_button:
			mListener.onItemsButton();
			break;
		case R.id.settings_button:
			mListener.onSettingsButton();
			break;
		case R.id.recipe_button:
			mListener.onRecipeButton();
			break;
		}
	}

}
