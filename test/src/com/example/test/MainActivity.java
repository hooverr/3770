
package com.example.test;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;


public class MainActivity extends Activity implements Camera.PreviewCallback {

	private Camera mCamera;
	boolean landScapeMode = false;	
	private FacialProcessing faceProc;
	private CameraPreview mPreview;
	private FaceData[] faceArray = null;
	int surfaceWidth=0;
	int surfaceHeight=0;	
	
	private int leftBlinks = 0;
	private int rightBlinks = 0;
	static boolean cameraSwitch = false;

	boolean _qcSDKEnabled = false;	
	int deviceOrientation;
	int presentOrientation;
	int displayAngle;
	Display display;
	OrientationEventListener orientationEventListener;
	
	
	
	private TextView leftEyeBlinkCount;
	private TextView rightEyeBlinkCount;
	private FrameLayout preview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		leftEyeBlinkCount = (TextView) findViewById(R.id.left_eye_blink_count);
		rightEyeBlinkCount = (TextView) findViewById(R.id.right_eye_blink_count);
		
		try {
	        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	       
	    }
		
		mCamera.setDisplayOrientation(90);
		mPreview = new CameraPreview(MainActivity.this,mCamera);
		preview.addView(mPreview);
		
		mCamera.setPreviewCallback(MainActivity.this);
		
		orientationListener();
		display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		CameraInfo cameraInfo = new Camera.CameraInfo();
	    Camera.getCameraInfo(0, cameraInfo);

		if(faceProc == null){
			faceProc = FacialProcessing.getInstance();
		}
		
		Parameters params = mCamera.getParameters();
		Size previewSize = params.getPreviewSize();		
		surfaceWidth = mPreview.getWidth();
		surfaceHeight = mPreview.getHeight();			
		

		faceProc.setFrame(data, previewSize.width, previewSize.height, true, PREVIEW_ROTATION_ANGLE.ROT_90 );

		int numFaces = faceProc.getNumFaces();
		if(numFaces!=0)
		{
			faceArray= faceProc.getFaceData();
			if(faceArray == null)
			{
					Log.e("TAG", "Face array is null");
			}
			else{
				faceProc.normalizeCoordinates(surfaceWidth, surfaceHeight);
				if(faceArray[0].getLeftEyeBlink() > 65){
					leftBlinks++;
					leftEyeBlinkCount.setText("Left Eye Blink Count:" + leftBlinks);
				}
				if(faceArray[0].getRightEyeBlink() > 65){
					rightBlinks++;
					rightEyeBlinkCount.setText("Right Eye Blink Count:" + rightBlinks);
				}
				
			
			}
		}
		
	}
	private void orientationListener(){
		orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) 
	    { 
		     @Override
		     public void onOrientationChanged(int orientation) 
		     {
		    	 deviceOrientation = orientation;
		     }
	    };
	
	    if(orientationEventListener.canDetectOrientation())
	    {
	    	orientationEventListener.enable();
	    }	    
	        
	    presentOrientation = 90*(deviceOrientation/360)%360;
	}

}
