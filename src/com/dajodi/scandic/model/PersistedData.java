package com.dajodi.scandic.model;

public class PersistedData {

	private int version = 1;
	
	/** START VERSION 1 **/
	private MemberInfo memberInfo;
	/** END VERSION 1 **/
	
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public MemberInfo getMemberInfo() {
		return memberInfo;
	}
	public void setMemberInfo(MemberInfo memberInfo) {
		this.memberInfo = memberInfo;
	}
	
}
