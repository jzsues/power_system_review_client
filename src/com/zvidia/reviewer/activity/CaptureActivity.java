package com.zvidia.reviewer.activity;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.zvidia.review.history.HistoryItem;
import com.zvidia.review.history.HistoryManager;
import com.zvidia.reviewer.R;
import com.zvidia.reviewer.camera.CameraManager;
import com.zvidia.reviewer.qrcode.Intents;
import com.zvidia.reviewer.qrcode.decode.DecodeFormatManager;
import com.zvidia.reviewer.qrcode.decode.DecodeHintManager;
import com.zvidia.reviewer.result.ResultHandlerFactory;

import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class CaptureActivity extends Activity implements SurfaceHolder.Callback {

	String TAG = CaptureActivity.class.getSimpleName();

	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private ProgressDialog progressDialog;

	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;
	private HistoryManager historyManager;
	private CaptureAction captureAction;

	private String characterSet;
	private boolean hasSurface;

	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;

	private Button doScanBtn;

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public CaptureActivityHandler getHandler() {
		return handler;
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_capture);

		hasSurface = false;
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);
		historyManager = new HistoryManager(this);
		historyManager.trimHistory();

		doScanBtn = (Button) findViewById(R.id.do_scan_btn);
		doScanBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showProgress(true);
				cameraManager.requestPreviewFrame(captureAction.getHandler(), R.id.decode);
			}
		});
	}

	/**
	 * Shows the progress UI and hides the capture form.
	 */
	public void showProgress(final boolean show) {
		if (show) {
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(CaptureActivity.this, "请等待", "扫描中", true, false);
			} else {
				progressDialog.show();
			}
		} else {
			progressDialog.dismiss();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(this);

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		statusView = (TextView) findViewById(R.id.status_view);

		handler = null;

		resetStatusView();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}
		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);

		Intent intent = getIntent();

		PreferenceManager.getDefaultSharedPreferences(this);
		decodeFormats = null;
		characterSet = null;
		if (intent != null) {

			String action = intent.getAction();
			String dataString = intent.getDataString();

			if (Intents.Scan.ACTION.equals(action)) {

				// Scan the formats the intent requested, and return the result
				// to the calling activity.
				decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
				decodeHints = DecodeHintManager.parseDecodeHints(intent);

				String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
				if (customPromptMessage != null) {
					statusView.setText(customPromptMessage);
				}

			} else {
				Log.d(TAG, "intent come,action:" + action + ",dataString:" + dataString);
			}

			characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

		}
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			if (requestCode == HISTORY_REQUEST_CODE) {
				int itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER, -1);
				if (itemNumber >= 0) {
					HistoryItem historyItem = historyManager.buildHistoryItem(itemNumber);
					decodeOrStoreSavedBitmap(null, historyItem.getResult());
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.capture, menu);
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				captureAction = new CaptureAction(this, decodeFormats, decodeHints, characterSet, new ViewfinderResultPointCallback(
						viewfinderView));
				handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager,
						captureAction.getHandler());
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	private void resetStatusView() {
		statusView.setText(R.string.msg_default_status);
		statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	public void handleDecode(Result rawResult, byte[] compressedBitmap, float scaleFactor) {
		ResultHandlerFactory.makeResultHandler(this, rawResult);

		boolean fromLiveScan = compressedBitmap != null;
		if (fromLiveScan) {
			beepManager.playBeepSoundAndVibrate();
			Intent reviewIntent = new Intent(this, ReviewActivity.class);
			reviewIntent.putExtra(Intents.Scan.QRCODE_SCAN_RESULT, rawResult.getText());
			// reviewIntent.putExtra(CaptureAction.BARCODE_BITMAP,
			// compressedBitmap);
			// reviewIntent.putExtra(CaptureAction.BARCODE_SCALED_FACTOR,
			// scaleFactor);

			reviewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			// Show the review page on a clean install, and the what's new
			// page on an upgrade.
			startActivity(reviewIntent);
			finish();
		}

	}
}
