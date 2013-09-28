package com.triladroid.selfphoto;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.ads.AdRequest;
import com.google.ads.AdView;



import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

public class MainActivity extends Activity {

	private boolean frontcamerapresent;
	PowerManager pm;
	PowerManager.WakeLock wl;
	private Camera mCamera;
	private int currentZoomLevel = 0, maxZoomLevel = 0;
	private boolean stopped = false;
	private static boolean isinproc = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		setContentView(R.layout.activity_main);

		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness = 1F;
		getWindow().setAttributes(layout);
        
		
		frontcamerapresent = checkCameraHardware(getApplicationContext());
		if (!frontcamerapresent)
		{
			
			finish();
			new AlertDialog.Builder(this)
		    .setTitle("No front camera")
		    .setMessage("No front camera found on this device. Application will be closed :(")
		    .setPositiveButton("Okay ", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	finish();
		        }
		     })

		     .show();

		}

		

		AdView ad = (AdView) findViewById(R.id.adView);
        ad.loadAd(new AdRequest());
        
   

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (frontcamerapresent)
		{

//			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
//			wl.acquire();


		}
		else
		{
			new AlertDialog.Builder(this)
		    .setTitle("No front camera")
		    .setMessage("No front camera found on this device. Application will be closed :(")
		    .setPositiveButton("Okay ", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	finish();
		        }
		     })

		     .show();

		}

	}


	@Override
    protected void onResume() {
        super.onResume();
        
        mCamera = getfrontCameraInstance();
        Camera.Parameters mCameraparams = mCamera.getParameters();
        
        Camera.Size pictureSize = getBiggestPictureSize(mCameraparams);
        Camera.Size previewSize = getBiggestPreviewSize(mCameraparams);
                
        FrameLayout rl = (FrameLayout) findViewById(R.id.camera_preview);
        Display display = getWindowManager().getDefaultDisplay();
        
        int dwidth =  display.getWidth();
        int dheight = display.getHeight();
        int rheight;
        Log.i("test", "This is DISPLAY width " + dwidth + " This is height " + dheight  );
   
        Log.i("test", "This is PREVIEW WIDTH  " + previewSize.width + " This is DISPLAY WIDTH  " + dwidth  );
        
        
        if (previewSize.height < dwidth || previewSize.width < dheight)
        {
        	 double piccoef = 1.0*pictureSize.width/pictureSize.height;
             rheight = (int) (dwidth*piccoef);
             Log.i("test", "2 This is width " + dwidth + " This is height " + rheight  );	
        	
        }
        
        else
        {
        	double piccoef = 1.0*previewSize.width/previewSize.height;
            rheight = (int) (dwidth*piccoef);
            Log.i("test", "2 This is width " + dwidth + " This is height " + rheight  );
        	
        }
        
        rl.getLayoutParams().height = rheight;
        rl.getLayoutParams().width = dwidth;
        
        //rl.getLayoutParams().height = pictureSize.width;
        //rl.getLayoutParams().width = pictureSize.height;
        
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        rl.removeAllViews();
        rl.addView(mPreview);
        
       
        final Button PhotoButton = (Button) findViewById(R.id.button1);
        PhotoButton.setBackgroundResource(R.drawable.pause2);
        PhotoButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				
				if (! isinproc) {
		 			isinproc = true;
		 			mCamera.takePicture(null, null, mPicture);

		 		}	
				
			}
		}
				);
		
         
}
	
	 @Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) 
 	{ 
 	   if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) { 

 		if (! isinproc) {
 			isinproc = true;
 			mCamera.takePicture(null, null, mPicture);

 		}

 		   return true;
 	   } else {
 	       return super.onKeyDown(keyCode, event); 
 	   }
 	}
     

	@Override
    protected void onPause() {
        super.onPause();
        releaseCamera(); 
        
        // sanity check for null as this is a public method
            if (wl != null) {
                Log.v("Mirror:", "Releasing wakelock");
                try {
                        	wl.release();
                } catch (Throwable th) {
                    // ignoring this exception, probably wakeLock was already released
                }
            	} else {
            		// should never happen during normal workflow
            		Log.e("Mirror", "Wakelock reference is null");
            }
        }
    

	private void releaseCamera(){
        if (mCamera != null){
        	
        	mCamera.stopPreview(); 
        	//mCamera.setPreviewCallback(null);
        	//mPreview.getHolder().removeCallback(mPreview);
        	mCamera.lock();
           	mCamera.release();        // release the camera
            mCamera = null;
        }
    }

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//	

	public static Camera getfrontCameraInstance(){
	        Camera c = null;
	        int cameraCount = 0;
	        cameraCount = Camera.getNumberOfCameras();
	        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

	        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	            Camera.getCameraInfo(camIdx, cameraInfo);
	            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
	                try {
	                    c = Camera.open(camIdx);
	                } catch (RuntimeException e) {
	                    Log.e("1", "Camera failed to open: " + e.getLocalizedMessage());
	                }
	            }
	        }

	        Camera.Parameters parameters = c.getParameters();
	        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
	        parameters.setRotation(270);
	        //parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
	        List<String> EffectsList = parameters.getSupportedColorEffects();

	        c.setParameters(parameters);
	        

	        return c; // returns null if camera is unavailable
	    }


	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
	        // this device has a camera
	    	Log.i("test", "FRONT TRUE");
	    	return true;
	        
	    } else {
	        // no camera on this device
	    	Log.i("test", "FRONT FALSE");
	    	return false;
	    }
	}

private static Camera.Size getBiggestPictureSize(Camera.Parameters parameters) {
        
    	Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
        	
        	
        	if (result == null) {
            result=size;
          }
          else {
            int resultArea=result.width * result.height;
            int newArea=size.width * size.height;

            //Log.i("test", "This is resultArea " + resultArea );
            //Log.i("test", "This is newArea " + newArea );
            
            if (newArea >= resultArea) {
              result=size;
              Log.i("test", "This is width" + result.width);
            }
          }
        }

        Log.i("test", "This is BIGGEST width " + result.width +"This is  height" + result.height );
        return(result);
       
      }

private static Camera.Size getBiggestPreviewSize(Camera.Parameters parameters) {
    
	Camera.Size result=null;

    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
    	
    	
    	if (result == null) {
        result=size;
      }
      else {
        int resultArea=result.width * result.height;
        int newArea=size.width * size.height;

        //Log.i("test", "This is resultArea " + resultArea );
        //Log.i("test", "This is newArea " + newArea );
        
        if (newArea >= resultArea) {
          result=size;
          Log.i("test", "This is width" + result.width);
        }
      }
    }

    Log.i("test", "This is BIGGEST width " + result.width +"This is  height" + result.height );
    return(result);
   
  }


//this is where we write our pic to destination file
	private PictureCallback mPicture = new PictureCallback() {

		private static final String TAG = "MyActivity";

		@Override
	    public void onPictureTaken(byte[] data, Camera camera) {


			File pictureFile = getOutputMediaFile();
	        if (pictureFile == null){
	            Log.d(TAG, "Error creating media file, check storage permissions: " );
	            return;
	        }

	       try {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            fos.write(data);
	            fos.close();
	        } 
	        catch (FileNotFoundException e) {
	            Log.d(TAG, "File not found:  " + e.getMessage());
	        } 
	       catch (IOException e) {
	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        }
	       finally {

	    	   camera = null;


	           startActivity(new Intent(MainActivity.this, MainActivity.class));
	           finish();

	           Handler handler = new Handler(); 
	           handler.postDelayed(new Runnable() { 
	                public void run() { 
	                     isinproc = false; 
	                } 
	           }, 1000); 
	       }

	    }
	};

	private static File getOutputMediaFile(){  
		// To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
	    //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SpyCamera");

    	File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "SelfPhoto"); 

		// This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.
	    // Create the storage directory if it does not exist

	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("SelfPhoto", "failed to create directory");
	            Log.i("SelfPhoto", "failed to create directory");
	            return null;
	        }
	    }

	 // Create a media file name
	   // Calendar c = Calendar.getInstance(); 
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
	    //String timeStamp =  df.format(c.getTime());
	    String timeStamp =  df.format(new Date());
	    //String timeStamp =  df.format(new Date());



	    File mediaFile;
	    //mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
	    //mediaFile = new File(mediaStorageDir.getPath() + "/IMG_"+ timeStamp + ".jpg");

	    mediaFile = new File(mediaStorageDir.getPath()  + "/" + timeStamp + "IMG.jpg");
	    return mediaFile;
	}

}