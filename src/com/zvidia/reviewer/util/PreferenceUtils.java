/**
 * 
 */
package com.zvidia.reviewer.util;

import com.zvidia.reviewer.activity.PreferencesActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author jiangzm
 * 
 */
public class PreferenceUtils {
	static String TAG = PreferenceUtils.class.getSimpleName();

	/**
	 * 检查是否登录
	 * 
	 * @param context
	 * @return true->已登录，false->未登录
	 */
	public static boolean checkAuthStatus(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String username = prefs.getString(PreferencesActivity.KEY_LOGIN_USERNAME, null);
		return username != null && !"".equals(username);
	}
}
