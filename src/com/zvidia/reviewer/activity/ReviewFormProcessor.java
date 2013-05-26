/**
 * 
 */
package com.zvidia.reviewer.activity;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zvidia.reviewer.R;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * @author jiangzm
 * 
 */
public class ReviewFormProcessor {
	String TAG = ReviewFormProcessor.class.getSimpleName();

	ReviewActivity reviewActivity;

	public ReviewFormProcessor(ReviewActivity reviewActivity) {
		super();
		this.reviewActivity = reviewActivity;
	}

	public JSONObject getStation() {
		try {
			JSONObject station = new JSONObject();
			station.put("id", reviewActivity.getmStationId().getText());
			station.put("name", reviewActivity.getmStationName().getText());
			station.put("address", reviewActivity.getmStationAddress().getText());
			Log.d(TAG, "get station info:");
			Log.d(TAG, station.toString());
			return station;
		} catch (Exception e) {
			Log.e(TAG, "get station with exception", e);
		}

		return null;
	}

	public boolean validate() {
		LinearLayout mCheckpingList = reviewActivity.getmCheckpingList();
		int childCount = mCheckpingList.getChildCount();
		int errorCount = 0;
		for (int i = 0; i < childCount; i++) {
			View childAt = mCheckpingList.getChildAt(i);
			RadioGroup radioGroup = (RadioGroup) childAt.findViewById(R.id.checkbox_res_group);
			int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
			if (checkedRadioButtonId == -1) {
				RadioButton btn = (RadioButton) radioGroup.getChildAt(0);
				btn.setError(reviewActivity.getString(R.string.review_res_error_exception));
				errorCount++;
			}
		}
		return errorCount == 0;
	}

	public JSONArray getCheckpointResults() {
		try {
			JSONArray res = new JSONArray();
			LinearLayout mCheckpingList = reviewActivity.getmCheckpingList();
			int childCount = mCheckpingList.getChildCount();
			for (int i = 0; i < childCount; i++) {
				View childAt = mCheckpingList.getChildAt(i);
				JSONObject result = new JSONObject();
				JSONObject checkpoint = new JSONObject();
				TextView mId = (TextView) childAt.findViewById(R.id.checkponit_id);
				TextView mAlarm = (TextView) childAt.findViewById(R.id.checkponit_alarmable);
				checkpoint.put("id", mId.getText());
				CharSequence alarmable = mAlarm.getText();
				checkpoint.put("alarm", alarmable);
				RadioGroup radioGroup = (RadioGroup) childAt.findViewById(R.id.checkbox_res_group);
				int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
				if (checkedRadioButtonId != -1) {
					result.put("checkpointInfo", checkpoint);
					if (checkedRadioButtonId == R.id.checkbox_normal) {
						result.put("result", ReviewResult.normal);
					} else if (checkedRadioButtonId == R.id.checkbox_abnormal) {
						result.put("result", ReviewResult.abnormal);
					} else {
						result.put("result", ReviewResult.normal);
					}
					result.put("alarm", alarmable);
					res.put(result);
				}
			}
			Log.d(TAG, "get checkpoint info:");
			Log.d(TAG, res.toString());
			return res;
		} catch (Exception e) {
			Log.e(TAG, "get checkpoint results with exception", e);
		}
		return null;
	}

	public JSONObject getUserInfo() {
		try {
			JSONObject user = new JSONObject();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(reviewActivity);
			String id = prefs.getString(PreferencesActivity.KEY_LOGIN_ID, "");
			String username = prefs.getString(PreferencesActivity.KEY_LOGIN_USERNAME, "");
			String password = prefs.getString(PreferencesActivity.KEY_LOGIN_PASSWORD, "");
			user.put("id", id);
			user.put("username", username);
			user.put("password", password);
			Log.d(TAG, "get user info:");
			Log.d(TAG, user.toString());
			return user;
		} catch (Exception e) {
			Log.e(TAG, "get user info with exception", e);
		}

		return null;
	}

	public JSONObject getReviewResult() {
		try {
			JSONObject result = new JSONObject();
			JSONObject stationInfo = getStation();
			JSONObject userInfo = getUserInfo();
			JSONArray checkpointResults = getCheckpointResults();
			CharSequence reviewId = reviewActivity.getmReviewId().getText();
			result.put("id", reviewId);
			result.put("userInfo", userInfo);
			result.put("stationInfo", stationInfo);
			result.put("reviewItemInfos", checkpointResults);
			result.put("reviewTime", (new Date()).getTime());
			Log.d(TAG, "get  review result info:");
			Log.d(TAG, result.toString());
			return result;
		} catch (JSONException e) {
			Log.e(TAG, "get review result with exception", e);
		}
		return null;
	}
}
