// $Id$
package com.zoho.creator.jframework;


public class ZCComponent  {

	public static final String FORM = "form"; //No I18N
	public static final String REPORT = "view"; //No I18N
	public static final String CALENDAR = "calendar"; //No I18N
	public static final String PAGE = "html"; //No I18N
	
	
	private String appOwner = null;
	private String appLinkName = null;
	private String type = null;
	private String componentName = null;
	private String componentLinkName = null;
	private int sequenceNumber = -1;
	
	ZCComponent(String appOwner, String appLinkName, String type, String componentName, String componentLinkName, int sequenceNumber) {
		this.appOwner = appOwner;	
		this.appLinkName = appLinkName;
		this.type = type;
		this.componentName = componentName;
		this.componentLinkName = componentLinkName;
		this.sequenceNumber = sequenceNumber;
	}
	
	public String toString() {
		return componentName;  //No I18N
	}

	public String getType() {
		return type;
	}


	public String getComponentName() {
		return componentName;
	}


	public String getComponentLinkName() {
		return componentLinkName;
	}


	public int getSequenceNumber() {
		return sequenceNumber;
	}


	public String getAppOwner() {
		return appOwner;
	}


	public String getAppLinkName() {
		return appLinkName;
	}

}
