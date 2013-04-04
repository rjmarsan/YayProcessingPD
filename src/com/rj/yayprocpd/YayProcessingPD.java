package com.rj.yayprocpd;

import java.io.File;
import java.io.IOException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import processing.core.PApplet;
import android.util.Log;
import android.widget.Toast;


public class YayProcessingPD extends PApplet {
	private final String TAG = "YayProcessingPD";
	public int sketchWidth() { return this.displayWidth; }
	public int sketchHeight() { return this.displayHeight; }
	public String sketchRenderer() { return PApplet.OPENGL; }

	@Override
	public void setup() {
		Log.d(TAG, "Setting up");
		openPatch("test1.pd");
	}
	
	@Override
	public void draw() {
		background(0);
		fill(200,0,0);
		stroke(255,0,0);
		ellipseMode(CENTER);
		ellipse(mouseX,mouseY,100,100);
		PdBase.sendFloat("pitch", (float)mouseX/(float)width);
		PdBase.sendFloat("volume", (float)mouseY/(float)height);
	}
	
	
	
	
	
	/**
	 * Pure Data stuff
	 */

	private static final int SAMPLE_RATE = 44100;

	private Toast toast = null;
	
	private void toast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
				}
				toast.setText(TAG + ": " + msg);
				toast.show();
			}
		});
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "Starting LibPD");
		if (AudioParameters.suggestSampleRate() < SAMPLE_RATE) {
			toast("required sample rate not available; exiting");
			finish();
			return;
		}
		final int nOut = Math.min(AudioParameters.suggestOutputChannels(), 2);
		if (nOut == 0) {
			toast("audio output not available; exiting");
			finish();
			return;
		}
		try {
			PdAudio.initAudio(SAMPLE_RATE, 0, nOut, 1, true);
			PdAudio.startAudio(this);
			} catch (final IOException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	public void onPause() {
		super.onPause();
		PdAudio.stopAudio();
	}
	
	public void onDestroy() {
		cleanup();
		super.onDestroy();
	}
	
	public void finish() {
		Log.d(TAG, "Finishing for some reason");
		cleanup();
		super.finish();
	}

	
	public int openPatch(final String patch) {
		final File dir = this.getFilesDir();
		final File patchFile = new File(dir, patch);
		int out=-1;
		try {
			IoUtils.extractZipResource(this.getResources().openRawResource(com.rj.yayprocpd.R.raw.patch), dir, true);
			out = PdBase.openPatch(patchFile.getAbsolutePath());
		} catch (final IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString() + "; exiting now");
			finish();
		}
		return out;
	}		

	public void cleanup() {
		// make sure to release all resources
		PdAudio.stopAudio();
		PdBase.release();
	}

}