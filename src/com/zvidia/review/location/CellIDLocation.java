/**
 * 
 */
package com.zvidia.review.location;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**
 * @author jiangzm
 * 
 */
public class CellIDLocation {

	private static final String TAG = CellIDLocation.class.getSimpleName();

	public static ArrayList<CellIDInfo> getCellIDInfo(Context context) throws Exception {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		ArrayList<CellIDInfo> CellID = new ArrayList<CellIDInfo>();
		Location loc = null;
		int type = tm.getNetworkType();
		Log.d(TAG, "getCellIDInfo-->         NetworkType = " + type);
		int phoneType = tm.getPhoneType();
		Log.d(TAG, "getCellIDInfo-->         phoneType = " + phoneType);

		// 中国电信为CTC
		// NETWORK_TYPE_EVDO_A是中国电信3G的getNetworkType
		// NETWORK_TYPE_CDMA电信2G是CDMA
		if (type == TelephonyManager.NETWORK_TYPE_CDMA || type == TelephonyManager.NETWORK_TYPE_1xRTT
				|| type == TelephonyManager.NETWORK_TYPE_EVDO_0 || type == TelephonyManager.NETWORK_TYPE_EVDO_A) {
			CdmaCellLocation location = (CdmaCellLocation) tm.getCellLocation();
			int cellIDs = location.getBaseStationId();
			int networkID = location.getNetworkId();
			StringBuilder nsb = new StringBuilder();
			nsb.append(location.getSystemId());
			CellIDInfo info = new CellIDInfo(cellIDs, networkID, nsb.toString(), tm.getNetworkOperator().substring(0, 3), "cdma");
			CellID.add(info);
		}
		// 移动2G卡 + CMCC + 2
		// type = NETWORK_TYPE_EDGE
		else if (type == TelephonyManager.NETWORK_TYPE_EDGE) {
			GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
			int cellIDs = location.getCid();
			int lac = location.getLac();
			CellIDInfo info = new CellIDInfo(cellIDs, lac, tm.getNetworkOperator().substring(3, 5),
					tm.getNetworkOperator().substring(0, 3), "gsm");
			CellID.add(info);
		}
		// 联通的2G经过测试 China Unicom 1 NETWORK_TYPE_GPRS
		else if (type == TelephonyManager.NETWORK_TYPE_GPRS) {
			GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
			int cellIDs = location.getCid();
			int lac = location.getLac();
			CellIDInfo info = new CellIDInfo(cellIDs, lac, "", "", "gsm");
			// 经过测试，获取联通数据以下两行必须去掉，否则会出现错误，错误类型为JSON Parsing Error
			// info.mobileNetworkCode = tm.getNetworkOperator().substring(3, 5);
			// info.mobileCountryCode = tm.getNetworkOperator().substring(0, 3);
			CellID.add(info);
		} else {
			throw new Exception("不支持当前移动网络定位");
		}

		return CellID;
	}

}
