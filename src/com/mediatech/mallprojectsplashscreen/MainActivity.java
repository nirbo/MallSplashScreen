package com.mediatech.mallprojectsplashscreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class MainActivity extends Activity {
	
	private ProgressBar mLoadingCircle;
	private CountDownTimer mCountDownTimer;
	private Handler mCountDownHandler;
	private Thread mCountDownThread;
	private Boolean mCountDownThreadFinished;

	private TextView mInfoTextView;
	private ImageView mLogoImageView;
	
	private TextSwitcher mTextSwitcher0;
	private TextSwitcher mTextSwitcher1;
	private TextSwitcher mTextSwitcher2;
	private TextSwitcher mTextSwitcher3;
	private TextSwitcher mTextSwitcher4;
	
	private GetMessagesAsyncTask mGetMessagesAsyncTask;
	private ArrayList<Message> mInfoTextsArrayList;
	private List<TextSwitcher> mTextSwitcherArrayList;
	private TextSwitcher mNextTextSwitcherToUse;
	private String mNextMessageToAdd;
	private String mNextMessageToDisplay;
	private Animation mFadeInAnimation;
	private Animation mFadeOutAnimation;
	private Handler mMessageDisplayRepeatHandler;
	private List<String> mFiveMessages;
	private int mCurrentMessageIndex = 0;
	private Boolean mStopMessageDisplayRepeat = false;
	
	private static String mUrl = "http://nirbo.no-ip.org:8080/cinemall_splash_messages/client_api.jsp";
	private FetchJSONAsyncTask mGetJSONMessages = new FetchJSONAsyncTask();
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen_main);
		overridePendingTransition(R.anim.appear_from_middle, R.anim.collapse_to_middle);
		
		mInfoTextsArrayList = new ArrayList<Message>();
		mTextSwitcherArrayList = new ArrayList<TextSwitcher>();
		
		mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeOutAnimation  = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		mMessageDisplayRepeatHandler = new Handler();
		
		mLogoImageView = (ImageView) findViewById(R.id.logoImageView);
		mLoadingCircle  = (ProgressBar) findViewById(R.id.loadingCircleProgressBar);
		mInfoTextView = (TextView) findViewById(R.id.informationBarTextView);
		
		mTextSwitcher0 = (TextSwitcher) findViewById(R.id.textSwitcher0);
		mTextSwitcherArrayList.add(mTextSwitcher0);
		mTextSwitcher1 = (TextSwitcher) findViewById(R.id.textSwitcher1);
		mTextSwitcherArrayList.add(mTextSwitcher1);
		mTextSwitcher2 = (TextSwitcher) findViewById(R.id.textSwitcher2);
		mTextSwitcherArrayList.add(mTextSwitcher2);
		mTextSwitcher3 = (TextSwitcher) findViewById(R.id.textSwitcher3);
		mTextSwitcherArrayList.add(mTextSwitcher3);
		mTextSwitcher4 = (TextSwitcher) findViewById(R.id.textSwitcher4);
		mTextSwitcherArrayList.add(mTextSwitcher4);
		
		prepareTextSwitchers();
		mGetJSONMessages.execute();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// --- Begin execution ---
		getActionBar().hide();
		prepareSplashLogoImage();
		mGetMessagesAsyncTask = new GetMessagesAsyncTask();
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

	// This method is where the magic happens, it starts the AsyncTask that pulls the messages and performs their rotation every REPEAT_INTERVAL amount of milliseconds.
	public void scheduleGetMessagesAsyncTask() {
		final Handler mScheduleAsyncTaskHandler = new Handler();
		Timer mTimer = new Timer();
		final long RESCHEDULE_INTERVAL = 3900;
		
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
	
	// Get the next message and next TextView to use and animate the change of text from the previously displayed to the new.
	public void updateTextView(List<String> mMessages) {
		mNextTextSwitcherToUse = mTextSwitcherArrayList.get(mCurrentMessageIndex);
		mNextMessageToDisplay = mMessages.get(mCurrentMessageIndex);
		
		mNextTextSwitcherToUse.setText(mNextMessageToDisplay);
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
	
	public void prepareTextSwitchers() {
		for (TextSwitcher mSwitcher : mTextSwitcherArrayList) {
			mSwitcher.setFactory(new ViewFactory() {
				
				@Override
				public View makeView() {
					TextView mTextFormat = new TextView(MainActivity.this);
					int mTextColor = getResources().getColor(R.color.White);
					int mTextShadowColor = getResources().getColor(R.color.SplashMessageShadowColor);

					mTextFormat.setTextSize(18);
					mTextFormat.setTextColor(mTextColor);
					mTextFormat.setShadowLayer(25, 0, 0, mTextShadowColor);
					mTextFormat.setGravity(Gravity.CENTER);
					
					return mTextFormat;
				}
			});
			
			mSwitcher.setInAnimation(mFadeInAnimation);
			mSwitcher.setOutAnimation(mFadeOutAnimation);
		}
	}
	
	
/*  
    ------------------------------------------------	 
	--------- CountDown Thread Inner Class ---------
	------------------------------------------------
*/
	private class CountDownThread implements Runnable {

		int mCountDownLength = 10000;
		int mCountDownInterval = 1000;
		
		@Override
		public void run() {
			// Make the "Loading" circle visible and start the CountDown Timer
			mLoadingCircle.setVisibility(View.VISIBLE);
			mInfoTextView.setText(getResources().getText(R.string.loading));
			startCountDownTimer(mCountDownLength);
		}
		
		// Prepare the count-down timer and start it
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
		
	This class prepares the messages to be displayed in the Splash Screen and presents them
	In a rotation on screen in the relevant TextSwitchers.
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
				if (mInfoTextsArrayList.size() <= 1) {
					new FetchJSONAsyncTask().execute();
				}
				
				// Get the next TextSwitcher and message contents to display in the UI
				if (mIndexCounter < mInfoTextsArrayList.size()) {
					mNextMessageToAdd = mInfoTextsArrayList.get(mIndexCounter).getContent();
					mTempFiveMessageList.add(mNextMessageToAdd);
					mInfoTextsArrayList.remove(mIndexCounter);
				} else {
					mNextMessageToAdd = " ";
					mTempFiveMessageList.add(mNextMessageToAdd);
				}
				
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

	
	
/*  
    ------------------------------------------------	 
	-------	Fetch JSON AsyncTask Inner Class -------
	------------------------------------------------
		
	This class fetches the messages via JSON from the database, the result is an ArrayList<Message>
	Which is being assigned to a global object.
*/

	private class FetchJSONAsyncTask extends AsyncTask<Void, Void, ArrayList<Message>> {

		@Override
		protected ArrayList<Message> doInBackground(Void... params) {
			JSONParser mFetchJSON = new JSONParser();
			ArrayList<Message> mJSONMessageResults = mFetchJSON.getJSONFromUrl(mUrl);
			
			return mJSONMessageResults;
		}

		@Override
		protected void onPostExecute(ArrayList<Message> result) {
			super.onPostExecute(result);
			
			mInfoTextsArrayList = result;
		}
		
		
		
	}
	
	
	
}





