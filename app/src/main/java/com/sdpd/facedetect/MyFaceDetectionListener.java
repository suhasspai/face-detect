package com.sdpd.facedetect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import com.kairos.Kairos;
import com.kairos.KairosListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static android.content.ContentValues.TAG;

@SuppressWarnings("deprecation")
class MyFaceDetectionListener implements Camera.FaceDetectionListener {
    private float prevPos;
    private int count;
    private CameraActivity activity;
    private Kairos kairos;
    private KairosListener listener;
    private Camera.PictureCallback mPicture=null;
    private Camera mCamera=null;
    private void train() {
        kairos=new Kairos();
        String app_id="965b63ae";
        String api_key="424e9d44236631b1a02e13d4e784dcfe";
        kairos.setAuthentication(activity, app_id, api_key);
        /*try {
            Bitmap image=BitmapFactory.decodeResource(activity.getResources()
            , R.drawable.n13);
            String subjectId = "Nidhi";
            String galleryId = "friends";
            String selector = "FULL";
            String multipleFaces = "false";
            String minHeadScale = "0.25";
            myKairos.enroll(image, subjectId, galleryId, selector,
            multipleFaces, minHeadScale, listener);
            image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.n23);
            myKairos.enroll(image, subjectId, galleryId, selector, multipleFaces, minHeadScale,
                    listener);
            image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.n33);
            myKairos.enroll(image, subjectId, galleryId, selector, multipleFaces, minHeadScale,
                    listener);
            /*subjectId="Suhas";
            image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ds);
            myKairos.enroll(image, subjectId, galleryId, selector, multipleFaces, minHeadScale,
                    listener);
            image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.me);
            myKairos.enroll(image, subjectId, galleryId, selector, multipleFaces, minHeadScale,
                    listener);
            image = BitmapFactory.decodeResource(activity.getResources(), R.drawable.me3);
            myKairos.enroll(image, subjectId, galleryId, selector, multipleFaces, minHeadScale,
                    listener);
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
    }
    public void getPerson() {
        Log.d(TAG, "Picture clicked!");
        Bitmap image=BitmapFactory.decodeFile(CameraActivity.getOutputMediaFile().getPath());
        Matrix matrix=new Matrix();
        matrix.setRotate(90, image.getWidth()/2, image.getHeight()/2);
        image=Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
        String galleryId="friends";
        String selector="FULL";
        String threshold="0.75";
        String minHeadScale="0.25";
        String maxNumResults="25";
        String TAG = "MyFaceDetectionListener";
        try {
            kairos.recognize(image, galleryId, selector, threshold, minHeadScale, maxNumResults,
                    listener);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception while training image", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported Encoding Exception while training image", e);
        }
        Log.d(TAG, "Recognise sent");
    }

    MyFaceDetectionListener(CameraActivity activity, Camera.PictureCallback mPicture, Camera mCamera) {
        prevPos=-1.0f;
        count=0;
        this.mPicture=mPicture;
        this.mCamera=mCamera;
        listener = new KairosListener() {
            @Override
            public void onSuccess(String response) {
                //Log.d("KAIROS DEMO", response);
                Toast.makeText(MyFaceDetectionListener.this.activity, "Success", Toast.LENGTH_LONG);
                try {
                    JSONObject json=new JSONObject(response);
                    //Log.d(TAG, json.toString(4));
                    JSONArray array=json.getJSONArray("images");
                    //for (int i=0; i<status.length(); i++)
                    String status=array.getJSONObject(0).getJSONObject("transaction").get("status").toString();
                    //MediaPlayer ring= MediaPlayer.create(MyFaceDetectionListener.this.activity, R.raw.failure);
                    //ring.start();
                    Log.d(TAG, status);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to convert to JSON", e);
                }
            }
            @Override
            public void onFail(String response) {
                //Log.d("KAIROS DEMO", response);
                Toast.makeText(MyFaceDetectionListener.this.activity, "Failure", Toast.LENGTH_LONG);
                try {
                    JSONObject json=new JSONObject(response);
                    Log.d(TAG, json.toString(4));
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to convert to JSON", e);
                }
            }
        };
        this.activity=activity;
        //train();
    }
    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length>0) {
            if (prevPos==-1.0f) {
                prevPos=CameraActivity.getMagnetometerReading();
                Log.d(TAG, "Before vibration");
                CameraActivity.getVibrator().vibrate(200);
                //mCamera.takePicture(null, null, mPicture);
                //getPerson();
            }
            float curPos=CameraActivity.getMagnetometerReading();
            if (Math.abs(curPos-prevPos)>=15) {
                Log.d(TAG, "Before vibration");
                CameraActivity.getVibrator().vibrate(200);
                prevPos=curPos;
                //mCamera.takePicture(null, null, mPicture);
                //getPerson();
            }
        }
    }
}
