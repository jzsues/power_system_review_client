/**
 * 
 */
package com.zvidia.review.location;

/**
 * @author jiangzm
 * 
 */
public class CellIDInfo {
	private int cellId;
	private int locationAreaCode;

	private String mobileCountryCode;

	private String mobileNetworkCode;

	private String radioType;

	public CellIDInfo() {
		super();
	}

	public CellIDInfo(int cellId, int locationAreaCode, String mobileCountryCode, String mobileNetworkCode, String radioType) {
		super();
		this.cellId = cellId;
		this.mobileCountryCode = mobileCountryCode;
		this.mobileNetworkCode = mobileNetworkCode;
		this.locationAreaCode = locationAreaCode;
		this.radioType = radioType;
	}

	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public String getMobileCountryCode() {
		return mobileCountryCode;
	}

	public void setMobileCountryCode(String mobileCountryCode) {
		this.mobileCountryCode = mobileCountryCode;
	}

	public String getMobileNetworkCode() {
		return mobileNetworkCode;
	}

	public void setMobileNetworkCode(String mobileNetworkCode) {
		this.mobileNetworkCode = mobileNetworkCode;
	}

	public int getLocationAreaCode() {
		return locationAreaCode;
	}

	public void setLocationAreaCode(int locationAreaCode) {
		this.locationAreaCode = locationAreaCode;
	}

	public String getRadioType() {
		return radioType;
	}

	public void setRadioType(String radioType) {
		this.radioType = radioType;
	}

}
