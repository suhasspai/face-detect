package com.sdpd.facedetect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SensorEventListener {
    private static final String TAG="FaceDetect";
    //private static final int TAKE_PHOTO_CODE=0;
    private File output=null;
    private static Camera mCamera;
    private SensorManager mSensorManager;
    //private Sensor mAccelerometer, mMagnetometer;
    private static final float[] mAccelerometerReading=new float[3], mMagnetometerReading=new float[3],
        mRotationMatrix=new float[9], mOrientationAngles=new float[3];
    private static Vibrator mVibrator;
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
    private void releaseCamera(){
        if (mCamera!=null){
            mCamera.release();        // release the camera for other applications
            mCamera=null;
        }
    }

    public MyFaceDetectionListener faceDetectionListener;
    public static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = getFileStorageDir("FaceDetect");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "Failed to create directory");
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath()+File.separator +"img.jpg");
        return mediaFile;
    }
    private static File getFileStorageDir(String name) {

        String state = Environment.getExternalStorageState();
        File file = null;

        if (Environment.MEDIA_MOUNTED.equals(state)){

            file = new File(Environment.getExternalStorageDirectory()+"/layout", name);
            if ((!file.mkdirs()) && (!file.isDirectory())){
                Log.v(TAG, "Directory Creation Failed");
                return null;
            }

            Log.v(TAG, "Directory Created = " + file.getAbsolutePath());
        }else{
            Log.v(TAG, "External Storage Not Mounted! Problem!!!");
        }
        return file;
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "Picture taken");
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
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
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            faceDetectionListener = new MyFaceDetectionListener(this, mPicture, mCamera);
            mCamera.setFaceDetectionListener(faceDetectionListener);
            mVibrator=(Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
            CameraPreview mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview=(FrameLayout)findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            mSensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
            //mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //mMagnetometer=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        } else {
            finish();
            System.exit(0);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
        mSensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(output), "image/jpeg");
                startActivity(i);
            }
        }
    }

    public void clickPicture() {
        Log.d(TAG, "clickPicture");
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //output=new File(dir, "img.jpeg");
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getOutputMediaFile()));
        startActivity(i);
    }
    public Camera getCameraInstance(){
        Camera c=null;
        try {
            c=Camera.open(); // attempt to get a Camera instance
        } catch (Exception e){
            Log.e(TAG, "Camera not available", e);// Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor==mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
            updateOrientationAngles();
            /*String mag="Mag: ";
            for (float x: mMagnetometerReading)
                mag+=x+" ";
            Log.d(TAG, mag);*/
        }
    }
    public void updateOrientationAngles() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
    }
    public static float getMagnetometerReading() {
        return mMagnetometerReading[0];
    }
    public static Vibrator getVibrator() {
        return mVibrator;
    }
}
