package net.gabrielwong.groceryguard;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import eu.janmuller.android.simplecropimage.CropImage;

public class MainActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "MainActivity";
	
	protected static final int RC_IMAGE_CAPTURE = 100,
							   RC_CROP = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/GroceryCart/";
	
	public static final String lang = "eng";
	
	private Uri mImgUri = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 Parse.initialize(this, "XeSyqoRxoRPE7t1xhs8cQQTKFjnVizRQJj53PdMf", "UEPNUKWiPgoUmJmsE7JEbcRCAH0ddcmYluyTs7dy"); 
		 ParseAnalytics.trackAppOpened(getIntent());
		
		// button for camera
		final Button button = (Button) findViewById(R.id.cameraButton);
        button.setOnClickListener(this);
	}	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onCameraButton(){
		Log.v(TAG, "Starting camera intent");
		// Open up the camera
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mImgUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		//Uri fileUri = Uri.fromFile(new File(DATA_PATH + "ocr.jpg"));
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImgUri);
		startActivityForResult(intent, RC_IMAGE_CAPTURE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == RC_IMAGE_CAPTURE){
			if (resultCode == RESULT_OK){
				// TODO Process image
				Log.v(TAG,"Picture save in:\n" + mImgUri.toString());
				onPhotoTaken();
			} else if (resultCode == RESULT_CANCELED){
				// TODO User cancelled image capture
			} else{
				// Capture failed
				// Image captured and saved to fileUri specified in the Intent
	            Toast.makeText(this, "Image capture failed.", Toast.LENGTH_LONG).show();
			}
		} else if (requestCode == RC_CROP){
			if (resultCode == RESULT_OK){
				onPhotoTaken();
			}
		}
	}
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "GroceryCart");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	protected void onPhotoTaken(){
		String text = analyzeImage();
		parseText(text);
	}
	
	private void cropImage(){
		Intent intent = new Intent(this, CropImage.class);
		
		String path = mImgUri.getPath();
		intent.putExtra(CropImage.IMAGE_PATH, path);
		intent.putExtra(CropImage.SCALE, true); // Allow rescaling
		intent.putExtra(CropImage.ASPECT_X, 4);
	    intent.putExtra(CropImage.ASPECT_Y, 3);
		startActivityForResult(intent, RC_CROP);
		
	}
	
	protected String analyzeImage(){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(mImgUri.getPath(), options);
		Log.v(TAG, mImgUri.getPath());
		
		// Rotate the picture based on exif data
		try {
			ExifInterface exif = new ExifInterface(mImgUri.getPath());
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			rotate = 90;

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}
		
		Log.v(TAG, "Binarizing using Leptonica");
		Pix pixs = ReadFile.readBitmap(bitmap);
		pixs = Binarize.otsuAdaptiveThreshold(pixs);
		bitmap = WriteFile.writeBitmap(pixs);
		
		// Convert to ARGB_8888, required by tess
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		Log.v(TAG, "Calling Tessaract");
		
		Log.v(TAG, DATA_PATH);
		
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setImage(bitmap);
		
		String text = baseApi.getUTF8Text();
		Log.v(TAG, text);
		
		baseApi.end();
		
		return text;
	}
	
	public void parseText(String text){
		Log.v(TAG, "Parsing text");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("produceList"); // Query produceList class
		ArrayList<Integer> plu = new ArrayList<Integer>();
		String[] words = text.split("[ \n]");
		for (String s : words){
			if (s.length() == 4){
				try{
					plu.add(Integer.parseInt(s));
					Log.v(TAG, "Adding int: " + s);
				} catch(NumberFormatException e){}
			}
		}
		query.whereContainedIn("plu", plu);
		query.findInBackground(new FindCallback<ParseObject>(){
			public void done(List<ParseObject> results, ParseException e) {
			      if (e != null) {
			        e.printStackTrace();
			      } else {
			    	  for (ParseObject obj : results){
			    		  Log.v(TAG, "PLU: " + Integer.toString(obj.getInt("plu")));
			    	  }
			      }
			}
		});
	}
	
	public void onClick(View v) {
        // Perform action on click
    	onCameraButton();
    }
}
