/**
 * 
 */
package com.zvidia.reviewer.activity;

/**
 * @author jiangzm
 * 
 */
public enum ReviewResult {
	normal("正常"), abnormal("异常");

	private String name;

	private ReviewResult(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}