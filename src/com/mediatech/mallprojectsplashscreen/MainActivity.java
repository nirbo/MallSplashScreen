package com.mediatech.mallprojectsplashscreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private ProgressBar mLoadingCircle;
	private CountDownTimer mCountDownTimer;
	private Handler mCountDownHandler;
	private Thread mCountDownThread;
	private Boolean mCountDownThreadFinished;

	private TextView mInfoTextView;
	private ImageView mLogoImageView;
	
	private TextView messageTextView0;
	private TextView messageTextView1;
	private TextView messageTextView2;
	private TextView messageTextView3;
	private TextView messageTextView4;
	
	private GetMessagesAsyncTask mGetMessagesAsyncTask = new GetMessagesAsyncTask();
	private List<String> mInfoTextsArrayList;
	private List<TextView> mTextViewArrayList;
	private String mNextMessageToAdd;
	private String mNextMessageToDisplay;
	private TextView mNextTextViewToUse;
	private Animation mFadeInAnimation;
	private Animation mFadeOutAnimation;
	private Handler mMessageDisplayRepeatHandler;
	private List<String> mFiveMessages;
	private int mCurrentMessageIndex = 0;
	private Boolean mStopMessageDisplayRepeat = false;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen_main);
		overridePendingTransition(R.anim.appear_from_middle, R.anim.collapse_to_middle);
		
		mInfoTextsArrayList = new ArrayList<String>();
		mTextViewArrayList = new ArrayList<TextView>();
		
		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeOutAnimation  = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mFadeOutAnimation.setAnimationListener(mFadeOutAnimListener);
		mMessageDisplayRepeatHandler = new Handler();
		
		mLogoImageView = (ImageView) findViewById(R.id.logoImageView);
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
		mCountDownThreadFinished = false;
		
		// Instantiate the Handler and the Thread then execute the thread.
		mCountDownHandler = new Handler();
		mCountDownThread = new Thread(new CountDownThread());
		mCountDownHandler.post(mCountDownThread);
		// ---------------------------------------------------------------
		
		// Execute GetMessagesAsyncTask to get and display the splash screen messages
		// Short delay included to allow the Activity transition animation to finish.
		Handler mDelay = new Handler();
		mDelay.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				scheduleGetMessagesAsyncTask();
			}
		}, 650);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Stop the CountDown Thread and GetMessages AsyncTask
		mCountDownTimer.cancel();
		mCountDownThreadFinished = true;
		this.finish();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		// Stop the CountDownTimer, GetMessages AsyncTask, and finish this activity.
		mCountDownTimer.cancel();
		mCountDownThreadFinished = true;
		this.finish();
	}

	@Override
	public void finish() {
		super.finish();
		
		overridePendingTransition(0, R.anim.collapse_to_middle);
	}
	
	// Populate the splash screen Cinemall logo ImageView
	public void prepareSplashLogoImage() {		
		mLogoImageView.setImageResource(R.drawable.cinemall_logo_highres);
	}
	
	// Random String Generator --- temporary for simulating the MessageTextView contents
	// TODO: Replace this random generator with actual SQL queries from the mall's database.
//	private static final String RANDOM_STRING_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm ";
	private static final String RANDOM_STRING_CHARACTERS ="0123456789אבגדהוזחטיכלמנסעפצקרשת ";
		
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
			mTempString = getRandomString(20);
			mTempArrayList.add(mTempString);
		}
		
		return mTempArrayList;
	}
	
	// This method is where the magic happens, it starts the AsyncTask that pulls the messages and performs their rotation every REPEAT_INTERVAL amount of milliseconds.
	public void scheduleGetMessagesAsyncTask() {
		final Handler mScheduleAsyncTaskHandler = new Handler();
		Timer mTimer = new Timer();
		final long RESCHEDULE_INTERVAL = 7000;
		
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
		
		mTimer.schedule(executeAsyncTask, 0, RESCHEDULE_INTERVAL);
	}
	
	public AnimationListener mFadeOutAnimListener = new AnimationListener() {
		public void onAnimationStart(Animation animation) {}
		public void onAnimationRepeat(Animation animation) {}
		public void onAnimationEnd(Animation animation) {
			mNextTextViewToUse.setVisibility(View.INVISIBLE);
			mNextTextViewToUse.setText(mNextMessageToDisplay);
			mNextTextViewToUse.setVisibility(View.VISIBLE);
			mNextTextViewToUse.startAnimation(mFadeInAnimation);
		}
	};
	
	// Get the next message and next TextView to use and animate the change of text from the previously displayed to the new.
	public void updateTextView(List<String> mMessages) {
		mNextTextViewToUse = mTextViewArrayList.get(mCurrentMessageIndex);
		mNextMessageToDisplay = mMessages.get(mCurrentMessageIndex);
		
		mNextTextViewToUse.startAnimation(mFadeOutAnimation);
		if (mCurrentMessageIndex == 4) {
			mStopMessageDisplayRepeat = true;
		} else {
			mCurrentMessageIndex++;
		}
	}
	
	// This Runnable is in charge of displaying the batches of 5 messages at a time.
	public Runnable mUpdateTextViewRunnable = new Runnable() {
		final static int POSTDELAY_INTERVAL = 600;
		
		@Override
		public void run() {
			updateTextView(mFiveMessages);
			if (! mStopMessageDisplayRepeat) {
				mMessageDisplayRepeatHandler.postDelayed(mUpdateTextViewRunnable, POSTDELAY_INTERVAL);
			}
		}
	};
	
	// This method calls a recursive repeat of the mUpdateTextViewRunnable Runnable 
	// and stops when all 5 messages in the batch have been displayed.
	public void startTextViewRunnableRepeat() {
		mUpdateTextViewRunnable.run();
	}
	
	private void finishMainActivity() {
		this.finish();
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
					
					Handler mDelay = new Handler();
					mDelay.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							Intent mPlaceholderActivity = new Intent(getApplicationContext(), MallAppPlaceholder.class);
							startActivity(mPlaceholderActivity);
							finishMainActivity();
						}
					}, 2000);
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
			// Reset the mStopMessageDisplayRepeat boolean flag and the mCurrentMessageIndex global counter back to false and 0 
			// to allow the recursive repeat of messages display with animation can be performed on the next batch of messages.
			mCurrentMessageIndex = 0;
			mStopMessageDisplayRepeat = false;
			
			// Prepare 5 messages to be displayed and pass them to onPostExecute()
			for (int mIndexCounter = 0; mIndexCounter < 5; mIndexCounter++) {
				
				// Get new messages to display if all previously pulled messages have been displayed and the ArrayList is empty now.
				if (mInfoTextsArrayList.isEmpty()) {
					populateInfoTextArrayList(100);
				}
				
				// Get the next TextView and message contents to display in the UI
				mNextMessageToAdd = mInfoTextsArrayList.get(mIndexCounter);
				mTempFiveMessageList.add(mNextMessageToAdd);
				mInfoTextsArrayList.remove(mIndexCounter);
			}
			
			return mTempFiveMessageList;
		}
		
		@Override
		protected void onPostExecute(List<String> mFiveMessagesToDisplay) {
			super.onPostExecute(mFiveMessagesToDisplay);
			
			mFiveMessages = mFiveMessagesToDisplay;	
			startTextViewRunnableRepeat();
		}		
	}


}





