package de.andrano.bootdream;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.media.tv.TvContract.Channels.Logo;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;


public class BootDreamsService extends DreamService{

	DreamHandler handler;
	AnimationDrawable animation = null;
	Resources resources;
	
	ImageView img;
		
	@Override
	public void onAttachedToWindow() {
		handler = new DreamHandler(getBaseContext());
		resources = getResources();
		
		Log.d("de.andrano.bootdream", "setContentView");
		setContentView(R.layout.dream_layout);
		img = (ImageView)findViewById(R.id.imageView1);
		//Layout
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		//Fullscreen
		Boolean fullscreen = sharedPref.getBoolean(resources.getString(R.string.key_checkbox_fullscreen), false);
		if (fullscreen) {
			setFullscreen(true);
		} else {
			setFullscreen(false);
		}
		
		//Android Logo
		Boolean android_logo = sharedPref.getBoolean(resources.getString(R.string.key_checkbox_android_logo), false);
		if (android_logo) {
			setInteractive(false);
		} else {
			setInteractive(false);
		}
		
		//Load dream
		if (animation == null) {
			animation = new AnimationDrawable();
			DreamFrame[] frames = handler.getFrames();
			
			if (frames != null) {
				for (int i = 0; i < frames.length; i++) {
					animation.addFrame(frames[i].frame, frames[i].duration);
				}
				img.setImageDrawable(animation);
				Log.d("de.andrano.bootdream", "animation.start");
				animation.setOneShot(false);
				animation.start();
			}
		}
		super.onAttachedToWindow();
	}
	
	@Override
	public void onDreamingStarted() {
		super.onDreamingStarted();
	}
	
	@Override
	public void onDreamingStopped() {
		Log.d("de.andrano.bootdream", "onDreamingStopped");
		finish();
		super.onDreamingStopped();
	}
	
	@Override
	public void onDetachedFromWindow() {
		Log.d("de.andrano.bootdream", "onDetachedFromWindow");
		super.onDetachedFromWindow();
	}
		
}
