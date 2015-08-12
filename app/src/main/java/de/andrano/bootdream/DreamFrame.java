package de.andrano.bootdream;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class DreamFrame {
	Drawable frame;
	int duration;
	
	public DreamFrame(Drawable frame, int duration) {
		this.frame = frame;
		this.duration = duration;
	}
	
	@SuppressWarnings("deprecation")
	public DreamFrame(Bitmap frame, int duration) {
		this.frame = new BitmapDrawable(frame);
		this.duration = duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
}
