/**
 * 
 */
package com.zvidia.review.location;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * @author jiangzm
 * 
 */
public class BaiduLocation {
	private static final String TAG = BaiduLocation.class.getSimpleName();

	public static class BaiduLocationInfo {
		public String poiTime;
		public String locType;
		public String latitude;
		public String lontitude;
		public String radius;
		public String address;
		public String errCode;
	}

	private Context mContext;

	private LocationClient mLocationClient = null;

	public BaiduLocation(Context context) {
		super();
		this.mContext = context;
		this.mLocationClient = new LocationClient(this.mContext.getApplicationContext()); // 声明LocationClient类
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(false);
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.disableCache(true);// 禁止启用缓存定位
		option.setPoiNumber(2); // 最多返回POI个数
		option.setPoiDistance(1000); // poi查询距离
		option.setPoiExtraInfo(false); // 是否需要POI的电话和地址等详细信息
		option.setPriority(LocationClientOption.NetWorkFirst);      //设置网络优先
		this.mLocationClient.setLocOption(option);
	}

	public void register(BDLocationListener bdLocationListener) {
		if (!this.mLocationClient.isStarted()) {
			this.mLocationClient.registerLocationListener(bdLocationListener);
		} else {
			Log.e(TAG, "Can not register bdLocationListener,locClient is started.");
		}
	}
	
	

	public String getVersion() {
		return mLocationClient.getVersion();
	}

	public void start() {
		mLocationClient.start();
	}

	public void stop() {
		mLocationClient.stop();
	}

	public void requestLocation() {
		if (this.mLocationClient != null && this.mLocationClient.isStarted())
			this.mLocationClient.requestLocation();
		else
			Log.d(TAG, "LocClient is null or not started");
	}

}
