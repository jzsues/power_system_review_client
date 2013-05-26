/*
 * Copyright (C) 2008 ZXing authors
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

package com.zvidia.reviewer.activity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.zvidia.reviewer.R;
import com.zvidia.reviewer.camera.CameraManager;
import com.zvidia.reviewer.qrcode.decode.DecodeHandler;
import com.zvidia.reviewer.qrcode.decode.DecodeThread;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Collection;
import java.util.Map;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

	private static final String TAG = CaptureActivityHandler.class.getSimpleName();

	private final CaptureActivity activity;
	// private final DecodeThread decodeThread;
	private State state;
	private CameraManager cameraManager;
	private DecodeHandler decodeHandler;

	public State getState() {
		return state;
	}

	public enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(CaptureActivity activity, Collection<BarcodeFormat> decodeFormats, Map<DecodeHintType, ?> baseHints,
			String characterSet, CameraManager cameraManager, DecodeHandler decodeHandler) {
		this.activity = activity;
		this.decodeHandler = decodeHandler;

		this.state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		this.cameraManager = cameraManager;
		cameraManager.startPreview();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.decode_succeeded:
			Log.d(TAG, "Got decode succeeded message");
			activity.showProgress(false);
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = null;
			float scaleFactor = 1.0f;
			byte[] compressedBitmap = null;
			if (bundle != null) {
				compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
//				if (compressedBitmap != null) {
//					barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
//					// Mutable copy:
//					barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
//				}
//				scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
			}
			activity.handleDecode((Result) message.obj, compressedBitmap, scaleFactor);
			break;
		case R.id.decode_failed:
			activity.showProgress(false);
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			this.state = State.PREVIEW;
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeHandler, R.id.quit);
		quit.sendToTarget();

		// Be absolutely sure we don't send any queued up messages
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

}
