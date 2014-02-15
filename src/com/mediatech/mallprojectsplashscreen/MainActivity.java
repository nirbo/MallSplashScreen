package com.mediatech.mallprojectsplashscreen;

import java.util.ArrayList;
import java.util.List;
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
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
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
	private String mNextMessageToAdd;
	private String mNextMessageToDisplay;
	private TextView mNextTextViewToUse;
	private Animation mFadeInAnimation;
	private Animation mFadeOutAnimation;
	
	private AnimationListener mFadeOutAnimListener = new AnimationListener() {
		public void onAnimationStart(Animation animation) {}
		public void onAnimationRepeat(Animation animation) {}
		public void onAnimationEnd(Animation animation) {
			mNextTextViewToUse.setVisibility(View.INVISIBLE);
			mNextTextViewToUse.setText(mNextMessageToDisplay);
			mNextTextViewToUse.setVisibility(View.VISIBLE);
			mNextTextViewToUse.startAnimation(mFadeInAnimation);
			
			Log.i(LOGTAG, "Next TextView Number: " + mNextTextViewToUse);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen_main);
		
		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeOutAnimation  = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeOutAnimation.setAnimationListener(mFadeOutAnimListener);
		
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
		
		// Execute GetMessagesAsyncTask to get and display the splash screen messages
		scheduleGetMessagesAsyncTask();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Stop the CountDown Thread and GetMessages AsyncTask
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
	
	// Random String Generator --- temporary for simulating the MessageTextView contents
	// TODO: Replace this random generator with actual SQL queries from the mall's database.
	private static final String RANDOM_STRING_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm ";
		
	private String getRandomString(final int sizeOfRandomString) {
		final Random mRandomGenerator = new Random();
		final StringBuilder mRandomStringBuilder = new StringBuilder();
		for (int i = 0; i < sizeOfRandomString; i++) {
			mRandomStringBuilder.append(RANDOM_STRING_CHARACTERS.charAt(mRandomGenerator.nextInt(RANDOM_STRING_CHARACTERS.length())));
		}

		return mRandomStringBuilder.toString();
	}
	
	// Populates the messages ArrayList, currently from a random String generator.
	// TODO: Changes from the String random generator to pulling messages from the mall's SQL database.
	public ArrayList<String> populateInfoTextArrayList(int amountOfEntries) {
		ArrayList<String> mTempArrayList = new ArrayList<String>();
		String mTempString;
		
		while (mTempArrayList.size() < amountOfEntries) {
			mTempString = getRandomString(125);
			mTempArrayList.add(mTempString);
		}
		
		return mTempArrayList;
	}
	
	// This method is where the magic happens, it starts the AsyncTask that pulls the messages and performs their rotation every REPEAT_INTERVAL amount of milliseconds.
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
	--------- CountDown Thread Inner Class ---------
	------------------------------------------------
*/
	private class CountDownThread implements Runnable {

		int mCountDownLength = 30000;
		int mCountDownInterval = 1000;
		
		@Override
		public void run() {
			// Make the "Loading" circle visible and start the CountDown Timer
			mLoadingCircle.setVisibility(View.VISIBLE);
			mInfoTextView.setText(getResources().getText(R.string.loading));
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
					mInfoTextView.setText(getResources().getText(R.string.loading_complete));
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

	private class GetMessagesAsyncTask extends AsyncTask<Void, Void, List<String>> {
		List<String> mTempFiveMessageList = new ArrayList<String>();
		
		@Override
		protected List<String> doInBackground(Void... params) {
			
			// Prepare 5 messages to be displayed and pass them to onPostExecute()
			for (int mMessagesCounter = 0; mMessagesCounter < 5; mMessagesCounter++) {
				
				// Get new messages to display if all previously pulled messages have been displayed and the ArrayList is empty now.
				if (mInfoTextsArrayList.isEmpty()) {
					populateInfoTextArrayList(100);
				}
				
				// Get the next TextView and message contents to display in the UI
				mNextMessageToAdd = mInfoTextsArrayList.get(mMessagesCounter);
				mTempFiveMessageList.add(mNextMessageToAdd);
				
				mInfoTextsArrayList.remove(mMessagesCounter);
			}
			
			return mTempFiveMessageList;
		}
		
		@Override
		protected void onPostExecute(List<String> mFiveMessagesToDisplay) {
			super.onPostExecute(mFiveMessagesToDisplay);
					
			for (int mTextViewCounter = 0; mTextViewCounter < 5; mTextViewCounter++) {
				mNextTextViewToUse = mTextViewArrayList.get(mTextViewCounter);
				mNextMessageToDisplay = mFiveMessagesToDisplay.get(mTextViewCounter);
				
				// Post and animate the message changes in the TextView
				mNextTextViewToUse.startAnimation(mFadeOutAnimation);
			}
		}
	}



}





