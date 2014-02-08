package com.mediatech.mallprojectsplashscreen;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
	private Boolean mCountDownThreadFinished = false;
	
	private TextView mInfoTextView;
	private ImageView mLogoImageView;
	private Drawable mLogoImage;
	private Bitmap mLogoBitmap;
	private Bitmap mResizedLogoBitmap;
	
	private TextView messageTextView0;
	private TextView messageTextView1;
	private TextView messageTextView2;
	private TextView messageTextView3;
	private TextView messageTextView4;
	
	private GetMessagesAsyncTask mGetMessagesAsyncTask = new GetMessagesAsyncTask();
	private List<String> mInfoTextsArrayList = new ArrayList<String>();
	private List<TextView> mTextViewArrayList = new ArrayList<TextView>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen_main);		
		
		mLogoImageView = (ImageView) findViewById(R.id.logoImageView);
		mLogoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cinemall_logo);
		mLoadingCircle  = (ProgressBar) findViewById(R.id.loadingCircleProgressBar);
		mInfoTextView = (TextView) findViewById(R.id.informationBarTextView);
		
		messageTextView0 = (TextView) findViewById(R.id.messageTextView0);
		mTextViewArrayList.add(messageTextView0);
		messageTextView1 = (TextView) findViewById(R.id.messageTextView1);
		mTextViewArrayList.add(messageTextView1);
		messageTextView2 = (TextView) findViewById(R.id.messageTextView2);
		mTextViewArrayList.add(messageTextView2);
		messageTextView3 = (TextView) findViewById(R.id.messageTextView3);
		mTextViewArrayList.add(messageTextView3);
		messageTextView4 = (TextView) findViewById(R.id.messageTextView4);
		mTextViewArrayList.add(messageTextView4);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// --- Begin execution ---
		getActionBar().hide();
		prepareSplashLogoImage();
		mInfoTextsArrayList = populateInfoTextArrayList(100);
		
		// Instantiate the Handler and the Thread then execute the thread.
		mCountDownHandler = new Handler();
		mCountDownThread = new Thread(new CountDownThread());
		mCountDownHandler.post(mCountDownThread);
		// ---------------------------------------------------------------
		
		// Execute the GetMessages AsyncTask
//		try {
//			mGetMessagesAsyncTask.execute();
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.e(LOGTAG, "ERROR: Could not retrieve splash-screen messages from the server; Splash messages will NOT be displayed!");
//		}
		// ---------------------------------------------------------------
		
		scheduleGetMessagesAsyncTask();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		mCountDownTimer.cancel();
		mCountDownThreadFinished = true;
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		// Stop the CountDownTimer, GetMessages AsyncTask, and finish this activity.
		mCountDownTimer.cancel();
		mCountDownThreadFinished = true;
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
	
	// Get the splash messages from the server and display them in a rotation.
	private void getSplashMessages() {
				
		String mNextMessageToDisplay = null;
		TextView mNextTextViewToUse = null;
			
		int mMessagesCounter = 0;
		int mTextViewCounter = 0;
		int mMessagesArrayListSize = mInfoTextsArrayList.size();
		int mTextViewArrayListSize = mTextViewArrayList.size();
				
		// Verify the messages ArrayList has been successfully populated and execute message-rotation logic.
		mMessagesArrayListSize = mInfoTextsArrayList.size();
		mTextViewArrayListSize = mTextViewArrayList.size();
		
		while (mTextViewCounter < 5) {
			mMessagesCounter = mTextViewCounter;
			
			// Get new messages to display if all previously pulled messages have been displayed and the ArrayList is empty now.
			if (mInfoTextsArrayList.isEmpty()) {
				populateInfoTextArrayList(100);
			}
			
			// Get next TextView to use and message contents and display them in the UI
			mNextMessageToDisplay = mInfoTextsArrayList.get(mMessagesCounter);
			mNextTextViewToUse = mTextViewArrayList.get(mTextViewCounter);
			mNextTextViewToUse.setText(mNextMessageToDisplay);
			
			mInfoTextsArrayList.remove(mMessagesCounter);
			mTextViewCounter++;
			
			// Debugging
			Log.i(LOGTAG, "Next Message: " + mMessagesCounter +  " " + mInfoTextsArrayList.get(mMessagesCounter));
		}
	}
	
	// Random String Generator --- temporary for simulating the MessageTextView contents //
	private static final String RANDOM_STRING_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm ";
		
	private String getRandomString(final int sizeOfRandomString) {
		final Random mRandomGenerator = new Random();
		final StringBuilder mRandomStringBuilder = new StringBuilder();
		for (int i = 0; i < sizeOfRandomString; i++) {
			mRandomStringBuilder.append(RANDOM_STRING_CHARACTERS.charAt(mRandomGenerator.nextInt(RANDOM_STRING_CHARACTERS.length())));
		}

		return mRandomStringBuilder.toString();
	}
			
	public ArrayList<String> populateInfoTextArrayList(int amountOfEntries) {
		ArrayList<String> mTempArrayList = new ArrayList<String>();
		String mTempString;
		
		while (mTempArrayList.size() < amountOfEntries) {
			mTempString = getRandomString(125);
			mTempArrayList.add(mTempString);
		}
		
		return mTempArrayList;
	}
	
	public void scheduleGetMessagesAsyncTask() {
		final Handler mScheduleAsyncTaskHandler = new Handler();
		Timer mTimer = new Timer();
		final long REPEAT_INTERVAL = 7000;
		TimerTask executeAsyncTask = new TimerTask() {

			@Override
			public void run() {
				mScheduleAsyncTaskHandler.post(new Runnable() {
					
					@Override
					public void run() {
						mGetMessagesAsyncTask = new GetMessagesAsyncTask();
						if (mCountDownThreadFinished) {
							mGetMessagesAsyncTask.cancel(true);
						}
						mGetMessagesAsyncTask.execute();
					}
				});
			}
		};
		mTimer.schedule(executeAsyncTask, 0, REPEAT_INTERVAL);
	}
	
	
	
/*  
    ------------------------------------------------	 
	----- CountDown Thread Inner Class -----
	------------------------------------------------
*/
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
					mCountDownThreadFinished = true;
				}
			}.
			start();
		}
		
	}	
	
	
/*  
    ------------------------------------------------	 
	----- Splash Message AsyncTask Inner Class -----
	------------------------------------------------
		
	This class fetches the messages to display in the Splash Screen from a server and presents them
	In a rotation on screen in the relevant TextViews.
*/

	private class GetMessagesAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			getSplashMessages();
		}
	}


	
	
	
	
}





