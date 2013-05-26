/**
 * 
 */
package com.zvidia.reviewer.activity;

/**
 * @author jiangzm
 * 
 */
public enum DefectType {
	type1("Ⅰ类缺陷"), type2("Ⅱ类缺陷"), type3("Ⅲ类缺陷"), type4("Ⅳ类缺陷"), type5("外部隐患");

	private String name;

	private DefectType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
