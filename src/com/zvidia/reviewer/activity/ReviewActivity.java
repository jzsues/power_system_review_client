package com.zvidia.reviewer.activity;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.zvidia.reviewer.R;
import com.zvidia.reviewer.http.ServerHttpClient;
import com.zvidia.reviewer.qrcode.Intents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReviewActivity extends Activity {

	private String TAG = ReviewActivity.class.getSimpleName();

	protected TextView mStationLabel;
	protected TextView mStationNameLabel;
	protected TextView mStationAddressLabel;
	protected TextView mCheckpointLabel;

	protected TextView mReviewId;
	protected TextView mStationId;
	protected TextView mStationName;
	protected TextView mStationAddress;
	protected LinearLayout mCheckpingList;
	protected ProgressDialog progressDialog;
	protected AlertDialog dialog;
	protected ReviewFormProcessor formProcessor;

	protected boolean validate;

	protected Handler handler;

	public TextView getmReviewId() {
		return mReviewId;
	}

	public TextView getmStationId() {
		return mStationId;
	}

	public TextView getmStationName() {
		return mStationName;
	}

	public TextView getmStationAddress() {
		return mStationAddress;
	}

	public LinearLayout getmCheckpingList() {
		return mCheckpingList;
	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public ViewPager getmViewPager() {
		return mViewPager;
	}

	public TextView getmStationLabel() {
		return mStationLabel;
	}

	public TextView getmStationNameLabel() {
		return mStationNameLabel;
	}

	public TextView getmStationAddressLabel() {
		return mStationAddressLabel;
	}

	public TextView getmCheckpointLabel() {
		return mCheckpointLabel;
	}

	public AlertDialog getDialog() {
		return dialog;
	}

	public ReviewFormProcessor getFormProcessor() {
		return formProcessor;
	}

	public Handler getHandler() {
		return handler;
	}

	public boolean isValidate() {
		return validate;
	}

	/**
	 * Shows the progress UI and hides the review form.
	 */
	public void showProgress(final boolean show) {
		if (show) {
			if (progressDialog == null) {
				progressDialog = ProgressDialog.show(ReviewActivity.this, "请等待", "解析中", true, false);
			} else {
				progressDialog.show();
			}
		} else {
			progressDialog.dismiss();
		}
	}

	private void setFontBold(TextView textView) {
		TextPaint tp = textView.getPaint();
		tp.setFakeBoldText(true);
	}

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review);
		handler = new ReviewHandler(this);
		formProcessor = new ReviewFormProcessor(this);
		mCheckpointLabel = (TextView) findViewById(R.id.checkpoint_label);
		mStationLabel = (TextView) findViewById(R.id.station_label);
		mStationNameLabel = (TextView) findViewById(R.id.station_name_label);
		mStationAddressLabel = (TextView) findViewById(R.id.station_address_label);
		// 设置黑体，中文字符在xml设置无效
		setFontBold(mCheckpointLabel);
		setFontBold(mStationLabel);
		setFontBold(mStationNameLabel);
		setFontBold(mStationAddressLabel);
		showProgress(true);
		Intent intent = getIntent();
		// byte[] byteArray =
		// intent.getByteArrayExtra(CaptureAction.BARCODE_BITMAP);
		String result = intent.getStringExtra(Intents.Scan.QRCODE_SCAN_RESULT);
		// float factor =
		// intent.getFloatExtra(CaptureAction.BARCODE_SCALED_FACTOR, 1L);
		Log.d(TAG, result);
		mReviewId = (TextView) findViewById(R.id.review_id);
		mStationId = (TextView) findViewById(R.id.station_id);
		mStationName = (TextView) findViewById(R.id.station_name);
		mStationAddress = (TextView) findViewById(R.id.station_address);
		mCheckpingList = (LinearLayout) findViewById(R.id.checkpoint_list);
		final JsonHttpResponseHandler checkpointResponseHandler = new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject response) {
				try {
					int code = response.getInt("code");
					if (code == -1) {
						JSONArray checkpoints = response.getJSONArray("addition");
						Log.d(TAG, "checkpoint response:");
						Log.d(TAG, checkpoints.toString());
						int size = checkpoints.length();
						for (int i = 0; i < size; i++) {
							JSONObject checkpoint = checkpoints.getJSONObject(i);
							JSONObject deviceClassInfo = checkpoint.getJSONObject("deviceClassInfo");
							String defectType = DefectType.valueOf(checkpoint.getString("defectType")).getName();
							String defectDetail = checkpoint.getString("defectDetail");
							String alarmable = checkpoint.getString("alarm");
							String deviceClassName = deviceClassInfo.getString("name");
							String id = checkpoint.getString("id");
							LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View listItem = inflater.inflate(R.layout.checkpoint_list_item, null);
							TextView mId = (TextView) listItem.findViewById(R.id.checkponit_id);
							TextView mText = (TextView) listItem.findViewById(R.id.checkponit_text);
							TextView mAlarm = (TextView) listItem.findViewById(R.id.checkponit_alarmable);
							mId.setText(id);
							mAlarm.setText(alarmable);
							mText.setText(defectType + "-" + deviceClassName + "-" + defectDetail);
							mCheckpingList.addView(listItem);
						}
					} else {
						Log.e(TAG, "get checkpoint failed");
					}
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
				}
				showProgress(false);
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				Log.e(TAG, error.getMessage(), error);
				showProgress(false);
			}
		};

		JsonHttpResponseHandler stationResponseHandler = new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject response) {
				try {
					int code = response.getInt("code");
					if (code == -1) {
						JSONObject station = response.getJSONObject("addition");
						Log.d(TAG, "check station response:");
						Log.d(TAG, station.toString());
						mStationId.setText(station.getString("id"));
						mStationName.setText(station.getString("name"));
						mStationAddress.setText(station.getString("address"));
						ServerHttpClient.get("/client/checkpoint", null, checkpointResponseHandler);
					} else {
						Log.e(TAG, "check station failed");
						showProgress(false);
					}
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				super.onFailure(e, errorResponse);
				showProgress(false);
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				Log.e(TAG, error.getMessage(), error);
				showProgress(false);
			}
		};
		ServerHttpClient.get("/client/check/" + result, null, stationResponseHandler);

		dialog = new AlertDialog.Builder(this).create();
		Message ok = Message.obtain(handler, R.id.review_submit_done);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", ok);
		Message cancel = Message.obtain(handler, R.id.review_submit_return);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "返回修改", cancel);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review, menu);
		MenuItem actionItem = menu.add("Action Button");

		// Items that show as actions should favor the "if room" setting, which
		// will
		// prevent too many buttons from crowding the bar. Extra items will show
		// in the
		// overflow area.
		actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		// Items that show as actions are strongly encouraged to use an icon.
		// These icons are shown without a text description, and therefore
		// should
		// be sufficiently descriptive on their own.
		actionItem.setIcon(R.drawable.upload);
		OnMenuItemClickListener menuItemClickListener = new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				validate = formProcessor.validate();
				if (validate) {
					showProgress(true);
					JSONObject reviewResult = formProcessor.getReviewResult();
					JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
						@Override
						public void onSuccess(JSONObject response) {
							super.onSuccess(response);
							Log.d(TAG, response.toString());
							try {
								int code = response.getInt("code");
								if (code == -1) {
									dialog.setMessage("巡检结果提交成功");
									dialog.show();
									mReviewId.setText(response.getJSONObject("addition").getString("id"));
								} else {
									validate = false;
									dialog.setMessage("巡检结果提交失败");

								}
							} catch (JSONException e) {
								validate = false;
								dialog.setMessage("巡检结果提交失败");
								Log.e(TAG, e.getMessage(), e);
							}
							dialog.show();
							showProgress(false);
						}

						@Override
						public void onFailure(Throwable error, String content) {
							super.onFailure(error, content);
							Log.e(TAG, error.getMessage(), error);
							showProgress(false);
							dialog.setMessage("巡检结果提交失败");
							dialog.show();
						}
					};
					try {
						ServerHttpClient.post(ReviewActivity.this, "/client/review", reviewResult, responseHandler);
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, e.getMessage(), e);
					}
					return true;
				} else {
					dialog.setMessage("请选择每项缺陷巡检结果,否则将无法提交巡检结果");
					dialog.show();
					return true;
				}
			}
		};
		actionItem.setOnMenuItemClickListener(menuItemClickListener);
		return true;
	}
}
