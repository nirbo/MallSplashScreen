package com.mediatech.mallprojectsplashscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class OpeningActivity extends Activity {
	
	private String LOGTAG = "MallOpeningImage";
	
	private final static int TIME_WAIT_LENGTH = 3000;
	private ImageView mOpeningImageView;
	private Handler mTimeWaitHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opening_imageview);
		
		overridePendingTransition(R.anim.appear_from_middle, R.anim.collapse_to_middle);
		mOpeningImageView = (ImageView) findViewById(R.id.opening_imageview);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		mOpeningImageView.setImageResource(R.drawable.opening_1280x800);
		mOpeningImageView.setScaleType(ScaleType.FIT_XY);
		
		mTimeWaitHandler.postDelayed(mTimeWaitRunnable, TIME_WAIT_LENGTH);
	}
	
	@Override
	public void onBackPressed() {
		mTimeWaitHandler.removeCallbacks(mTimeWaitRunnable);
		this.finish();
	}
	
	private Runnable mTimeWaitRunnable = new Runnable() {
		
		@Override
		public void run() {
			Intent mSplashLaunchIntent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(mSplashLaunchIntent);
			finishOpeningActivity();
		}
	};
	
	private void finishOpeningActivity() {
		this.finish();
	}
}
