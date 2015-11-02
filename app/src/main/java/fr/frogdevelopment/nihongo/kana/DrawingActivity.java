/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.frogdevelopment.nihongo.kana;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.view.MotionEvent;

import fr.frogdevelopment.nihongo.R;

public class DrawingActivity extends Activity {

	private Gesture mGesture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawing);

		GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.drawing);
		Intent intent = getIntent();
		Kana kana = intent.getParcelableExtra("kana");
		overlay.setBackgroundResource(kana.resource);
		overlay.addOnGestureListener(new GesturesProcessor());

		setTitle(kana.name + " - " + kana.label);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	protected void onDestroy() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mGesture != null) {
			outState.putParcelable("gesture", mGesture);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mGesture = savedInstanceState.getParcelable("gesture");
		if (mGesture != null) {
			final GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.drawing);
			overlay.post(new Runnable() {
				public void run() {
					overlay.setGesture(mGesture);
				}
			});
		}
	}

	private class GesturesProcessor implements GestureOverlayView.OnGestureListener {

		@Override
		public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
			mGesture = null;
		}

		@Override
		public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		}

		@Override
		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			mGesture = overlay.getGesture();
		}

		@Override
		public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
		}

	}

}