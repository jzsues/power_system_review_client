/**
 * 
 */
package com.zvidia.reviewer.activity;

import com.zvidia.reviewer.R;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;

/**
 * @author jiangzm
 * 
 */
public class ReviewHandler extends Handler {

	protected ReviewActivity reviewActivity;

	public ReviewHandler(ReviewActivity reviewActivity) {
		super();
		this.reviewActivity = reviewActivity;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		AlertDialog dialog = reviewActivity.getDialog();
		switch (msg.what) {
		case R.id.review_submit_done:
			dialog.dismiss();
			if (reviewActivity.isValidate()) {
				reviewActivity.finish();
			}
			break;
		case R.id.review_submit_return:
			dialog.dismiss();
			break;
		}

	}
}
