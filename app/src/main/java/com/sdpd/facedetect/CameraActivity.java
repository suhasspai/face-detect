package com.sdpd.facedetect;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SensorEventListener {
    private static final String TAG="FaceDetect";
    private static Camera mCamera;
    private SensorManager mSensorManager;
    private static final float[] mAccelerometerReading=new float[3],
		mMagnetometerReading=new float[3], mRotationMatrix=new float[9],
		mOrientationAngles=new float[3];
    private static Vibrator mVibrator;
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager
			.FEATURE_CAMERA);
    }
    private void releaseCamera(){
        if (mCamera!=null){
            mCamera.release();
            mCamera=null;
        }
    }
	private static File getFileStorageDir(String name) {
		String state=Environment.getExternalStorageState();
		File file=null;
		if (Environment.MEDIA_MOUNTED.equals(state)){
			file=new File(Environment.getExternalStorageDirectory()
				+"/layout", name);
			if ((!file.mkdirs())&&(!file.isDirectory())){
				Log.v(TAG, "Directory Creation Failed");
				return null;
			}
			Log.v(TAG, "Directory Created = "+file.getAbsolutePath());
		}else{
			Log.v(TAG, "External Storage Not Mounted! Problem!!!");
		}
		return file;
	}
	private Camera.PictureCallback mPicture=new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "Picture taken");
			File pictureFile=getOutputMediaFile();
			if (pictureFile==null){
				Log.d(TAG, "Error creating media file, check storage "+
					"permissions: ");
				return;
			}
			try {
				FileOutputStream fos=new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: "+e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: "+e.getMessage());
			}
			//CameraActivity.this.faceDetectionListener.getPerson();
			mCamera.stopPreview();
			mCamera.setDisplayOrientation(90);
			mCamera.startPreview();
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (checkCameraHardware(this)) {
            mCamera=getCameraInstance();
            if (mCamera==null) {
                finish();
                System.exit(0);
            }
            Camera.Parameters params=mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            faceDetectionListener=new MyFaceDetectionListener(this, mPicture,
				mCamera);
            mCamera.setFaceDetectionListener(faceDetectionListener);
            mVibrator=(Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
            CameraPreview mPreview=new CameraPreview(this, mCamera);
            FrameLayout preview=(FrameLayout)findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            mSensorManager=(SensorManager)getSystemService(Context
				.SENSOR_SERVICE);
        } else {
            finish();
            System.exit(0);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        mSensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor
			(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL,
			SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor
			(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL,
			SensorManager.SENSOR_DELAY_UI);
    }

    public MyFaceDetectionListener faceDetectionListener;
    public static File getOutputMediaFile() {
        File mediaStorageDir=getFileStorageDir("FaceDetect");
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(TAG, "Failed to create directory");
                return null;
            }
        }
        File mediaFile;
        mediaFile=new File(mediaStorageDir.getPath()+File.separator+"img.jpg");
        return mediaFile;
    }

    public Camera getCameraInstance(){
        Camera c=null;
        try {
            c=Camera.open();
        } catch (Exception e){
            Log.e(TAG, "Camera not available", e);
        }
        return c;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor==mSensorManager.getDefaultSensor(Sensor
			.TYPE_MAGNETIC_FIELD)) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0,
				mMagnetometerReading.length);
            updateOrientationAngles();
        }
    }
    public void updateOrientationAngles() {
        SensorManager.getRotationMatrix(mRotationMatrix, null,
			mAccelerometerReading, mMagnetometerReading);
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
    }
    public static float getMagnetometerReading() {
		return mMagnetometerReading[0];
    }
    public static Vibrator getVibrator() {
        return mVibrator;
    }
}
