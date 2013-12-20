package com.mediatech.mallprojectsplashscreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	private String LOGTAG = "MallSplashScreen";
	
	private ProgressBar mLoadingCircle;
	private CountDownTimer mCountDownTimer;
	private Handler mCountDownHandler;
	private Thread mCountDownThread;
	
	private TextView mInfoTextView;
	private ImageView mLogoImageView;
	private Drawable mLogoImage;
	private Bitmap mLogoBitmap;
	private Bitmap mResizedLogoBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen_main);		
		
		mLogoImageView = (ImageView) findViewById(R.id.logoImageView);
		mLogoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cinemall_logo);
		mLoadingCircle  = (ProgressBar) findViewById(R.id.loadingCircleProgressBar);
		mInfoTextView = (TextView) findViewById(R.id.informationBarTextView);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		getActionBar().hide();		
		prepareSplashLogoImage();
		
		// Instantiate the Handler and the Thread then execute the thread.
		mCountDownHandler = new Handler();
		mCountDownThread = new Thread(new CountDownThread());
		mCountDownHandler.post(mCountDownThread);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mCountDownTimer.cancel();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		// Stop the CountDownTimer running in CountDownThread and finish this activity.
		mCountDownTimer.cancel();
		this.finish();
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void prepareSplashLogoImage() {		
		mResizedLogoBitmap = Bitmap.createScaledBitmap(mLogoBitmap, 2580, 980, false); // Base Width,Height: 420,140
		mLogoImage = new BitmapDrawable(mResizedLogoBitmap);
		
		// Get the Android SDK Version and set the Drawable into the ImageView
		// using a newer, non-deprecated method if the SDK is JellyBean or later.
		Integer mAndroidVersion = android.os.Build.VERSION.SDK_INT;
		Log.i(LOGTAG, "Android SDK version detected: " + mAndroidVersion.toString());
		
		if (mAndroidVersion < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			mLogoImageView.setBackgroundDrawable(mLogoImage);
		} else {
			mLogoImageView.setBackground(mLogoImage);
		}
	}
	
	
	
	
	 //	  ----------------------------------------	 \\
	///// ----- CountDown Thread Inner Class ----- \\\\\
   //	  ----------------------------------------	   \\
	private class CountDownThread implements Runnable {

		int mCountDownLength = 30000;
		int mCountDownInterval = 1000;
		
		@Override
		public void run() {
			// Make the "Loading" circle visible and start the CountDown Timer
			mLoadingCircle.setVisibility(View.VISIBLE);
			mInfoTextView.setText(R.string.loading);
			startCountDownTimer(mCountDownLength);
		}
		
		// Prepare the 30 seconds count-down timer and start it
		// Updates (onTick) every 1 second (1000ms)
		public void startCountDownTimer(final int mTimeToCountDown) {
			
			mCountDownTimer = new CountDownTimer(mTimeToCountDown, mCountDownInterval) {
				Integer mCountDownSecondsRound = (mTimeToCountDown / 1000);
				Float mCountDownToDisplay = (float) (mCountDownSecondsRound / 1.2);
				
				@Override
				public void onTick(long millisUntilFinished) {
					Integer mCountDownTickRound = (int) (millisUntilFinished / 1000);
					
					// Change text to display the count-down after a period of time
					if (mCountDownTickRound < mCountDownToDisplay) {
						mInfoTextView.setText(mCountDownTickRound + " " + getResources().getString(R.string.seconds_left));
					}
				}
				
				@Override
				public void onFinish() {
					// Hide the "Loading" circle and print "Done!" upon completion
					mLoadingCircle.setVisibility(View.INVISIBLE);
					mInfoTextView.setText(R.string.loading_complete);
				}
			}.
			start();
		}
		
	}	
	
	
}













