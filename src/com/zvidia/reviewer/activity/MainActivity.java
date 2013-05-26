package com.zvidia.reviewer.activity;

import com.zvidia.reviewer.R;
import com.zvidia.reviewer.util.PreferenceUtils;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	protected Button scanBtn;
	protected ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		showLoginOnFirstLaunch();
	}

	@Override
	protected void onResume() {
		showProgress(false);
		super.onResume();
		scanBtn = (Button) findViewById(R.id.button_scan);
		scanBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showProgress(true);
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		switch (item.getItemId()) {
		case R.id.menu_settings:
			intent.setClassName(this, PreferencesActivity.class.getName());
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;

	}

	/**
	 * We want the help screen to be shown automatically the first time a new
	 * version of the app is run. The easiest way to do this is to check
	 * android:versionCode from the manifest, and compare it to a value stored
	 * as a preference.
	 */
	private boolean showLoginOnFirstLaunch() {
		try {
			boolean checkAuthStatus = PreferenceUtils.checkAuthStatus(this);
			if (!checkAuthStatus) {
				Intent intent = new Intent(this, LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				// Show the default page on a clean install, and the what's new
				// page on an upgrade.
				startActivity(intent);
				return true;
			}
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		return false;
	}

	/**
	 * Shows the progress UI and hides the review form.
	 */
	public void showProgress(final boolean show) {
		if (show) {
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(MainActivity.this, "请等待", "正在打开摄像头", true, false);
			} else {
				progressDialog.show();
			}
		} else {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		}
	}

}
