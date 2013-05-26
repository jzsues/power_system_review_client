/**
 * 
 */
package com.zvidia.reviewer.http;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * @author jiangzm
 * 
 */
public class ServerHttpClient {
	private static final String BASE_URL = "http://xsxt.lyjydz.cn:83/dl";

	private static AsyncHttpClient client = new AsyncHttpClient();
	{
		client.setTimeout(10000);
	}

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(Context context, String url, JSONObject json, AsyncHttpResponseHandler responseHandler)
			throws UnsupportedEncodingException {
		StringEntity se = new StringEntity(json.toString());
		client.post(context, getAbsoluteUrl(url), se, "application/json", responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}
}
