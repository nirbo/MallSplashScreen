package com.mediatech.mallprojectsplashscreen;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MallAppPlaceholder extends Activity {

	ImageView mPlaceholderImageView; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mall_app_placeholder);

		overridePendingTransition(R.anim.appear_from_middle, R.anim.collapse_to_middle);
		mPlaceholderImageView = (ImageView) findViewById(R.id.placeholder_image);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		mPlaceholderImageView.setImageResource(R.drawable.under_construction);
		mPlaceholderImageView.setScaleType(ScaleType.FIT_XY);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		this.finish();
	}

	@Override
	public void finish() {
		super.finish();
		
		overridePendingTransition(0, R.anim.collapse_to_middle);
	}
	
}
