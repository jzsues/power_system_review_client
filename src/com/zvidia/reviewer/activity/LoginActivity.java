package com.zvidia.reviewer.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zvidia.reviewer.R;
import com.zvidia.reviewer.http.ServerHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	String TAG = LoginActivity.class.getSimpleName();

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_PHONE = "18859202910";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mPhone;
	private String mPassword;

	// UI references.
	private EditText mPhoneView;
	private EditText mPasswordView;
	protected ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mPhone = getIntent().getStringExtra(EXTRA_PHONE);
		mPhoneView = (EditText) findViewById(R.id.phone);
		mPhoneView.setText(mPhone);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mPhoneView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mPhone = mPhoneView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mPhone)) {
			mPhoneView.setError(getString(R.string.error_field_required));
			focusView = mPhoneView;
			cancel = true;
		} else if (mPhone.length() != 11) {
			mPhoneView.setError(getString(R.string.error_invalid_phone));
			focusView = mPhoneView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
			// doAuth(this);
		}
	}

	private void doAuth(final Context context) {
		try {
			RequestParams params = new RequestParams();
			params.put("username", mPhone);
			params.put("password", mPassword);
			JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONObject response) {
					showProgress(false);
					try {
						int code = response.getInt("code");
						if (code == -1) {
							JSONObject userInfo = response.getJSONObject("addition");
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
							prefs.edit().putString(PreferencesActivity.KEY_LOGIN_ID, userInfo.getString("id")).commit();
							prefs.edit().putString(PreferencesActivity.KEY_LOGIN_USERNAME, userInfo.getString("username")).commit();
							prefs.edit().putString(PreferencesActivity.KEY_LOGIN_PASSWORD, mPassword).commit();
							finish();
						} else {
							mPasswordView.setError(getString(R.string.error_incorrect_password));
							mPasswordView.requestFocus();
						}
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage(), e);
						mPasswordView.setError(getString(R.string.error_incorrect_password));
						mPasswordView.requestFocus();
					}

				}

				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					showProgress(false);
					mPasswordView.setError(getString(R.string.error_exception));
					mPasswordView.requestFocus();
				}
			};
			ServerHttpClient.post("/login/do", params, responseHandler);
		} catch (Exception e) {
			showProgress(false);
			Log.e(TAG, e.getMessage(), e);
			mPasswordView.setError(getString(R.string.error_exception));
			mPasswordView.requestFocus();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final boolean show) {
		if (show) {
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(LoginActivity.this, "请等待", "验证中", true, false);
			} else {
				progressDialog.show();
			}
		} else {
			progressDialog.dismiss();
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			doAuth(LoginActivity.this);
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}
}
