// $Id$
package com.zoho.creator.jframework;

import java.io.File;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class XMLParser {

	private static String getStringValue(Node node, String defaultValue) {
		if(node != null && node.getFirstChild() != null) {
			return node.getFirstChild().getNodeValue();
		}
		return defaultValue;
	}

	private static int getIntValue(Node node, int defaultValue) {
		if(node != null && node.getFirstChild() != null) {
			return Integer.parseInt(node.getFirstChild().getNodeValue());
		}
		return defaultValue;
	}

	private static long getLongValue(Node node, long defaultValue) {
		if(node != null && node.getFirstChild() != null) {
			return Long.parseLong(node.getFirstChild().getNodeValue());
		}
		return defaultValue;
	}

	private static boolean getBooleanValue(Node node, boolean defaultValue) {
		if(node != null && node.getFirstChild() != null) {
			return Boolean.valueOf(node.getFirstChild().getNodeValue());
		}
		return defaultValue;
	}

	private static Date getDateValue(String dateString, String dateFormat) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
			return formatter.parse(dateString);
		} catch (java.text.ParseException e) {
			//////////System.out.println("?????");
			//e.printStackTrace();
		}
		return null;
	}

	private static Date getDateValue(Node node, Date defaultValue, String dateFormat) {
		if(node != null && node.getFirstChild() != null) {
			String dateString = node.getFirstChild().getNodeValue();
			Date toReturn  = getDateValue(dateString, dateFormat);
			if(toReturn != null) {
				return toReturn;
			}
		}
		return defaultValue;
	}



	static ZCNavList parseForNavigationListForApps(Document rootDocument) {

		List<ZCSharedGroup> sharedWithGroupList = new ArrayList<ZCSharedGroup>();
		List<String> sharedWithWorkSpaceList = new ArrayList<String>();
		NodeList nl = rootDocument.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			Node resultNode = nl.item(i);
			//////System.out.println("******* " + resultNode.getNodeName());
			if(resultNode.getNodeName().equals("result")) {
				NodeList resultNodes = resultNode.getChildNodes();
				for(int j=0; j<resultNodes.getLength(); j++) {
					Node resultNodeChild = resultNodes.item(j);
					//////System.out.println(resultNodeChild.getNodeName());
					if(resultNodeChild.getNodeName().equals("allworkspaces")) {
						NodeList workspacesNodes = resultNodeChild.getChildNodes();
						for(int k=0; k<workspacesNodes.getLength(); k++) {
							Node workspacesNode = workspacesNodes.item(k);
							//////System.out.println(workspacesNode.getNodeName());
							if(workspacesNode.getNodeName().equals("workspaces")) {
								NodeList workspaceNodes = workspacesNode.getChildNodes();
								for(int l=0; l<workspaceNodes.getLength(); l++) {
									Node workspaceNode = workspaceNodes.item(l);
									//////System.out.println(workspaceNode.getNodeName());
									if(workspaceNode.getNodeName().equals("workspace")) {
										NodeList workspaceownerNodes = workspaceNode.getChildNodes();
										for(int m=0; m<workspaceownerNodes.getLength(); m++) {
											Node workspaceownerNode = workspaceownerNodes.item(m);
											//////System.out.println(workspaceownerNode.getNodeName());
											if(workspaceownerNode.getNodeName().equals("workspaceowner")) {
												String workspaceowner = getStringValue(workspaceownerNode, null);
												sharedWithWorkSpaceList.add(workspaceowner);
											}
										}
									}
								}
							}
						}
					} else if(resultNodeChild.getNodeName().equals("allsharedcategory")) {
						NodeList groupsNodes = resultNodeChild.getChildNodes();
						for(int k=0; k<groupsNodes.getLength(); k++) {
							Node groupsNode = groupsNodes.item(k);
							//////System.out.println(groupsNode.getNodeName());
							if(groupsNode.getNodeName().equals("groups")) {
								NodeList groupNodes = groupsNode.getChildNodes();
								for(int l=0; l<groupNodes.getLength(); l++) {
									Node groupNode = groupNodes.item(l);
									//////System.out.println(groupNode.getNodeName());
									if(groupNode.getNodeName().equals("group")) {
										NodeList groupNodeChildNodes = groupNode.getChildNodes();
										String groupName = null;
										Long groupId = -1L;
										for(int m=0; m<groupNodeChildNodes.getLength(); m++) {
											Node groupNodeChildNode = groupNodeChildNodes.item(m);
											//////System.out.println(groupNodeChildNode.getNodeName());
											if(groupNodeChildNode.getNodeName().equals("groupname")) {
												groupName = getStringValue(groupNodeChildNode, groupName);
											} else if(groupNodeChildNode.getNodeName().equals("groupid")) {
												groupId = getLongValue(groupNodeChildNode, groupId);
												//////System.out.println(groupId);
											}
										}
										sharedWithGroupList.add(new ZCSharedGroup(groupName, groupId));
									}
								}
							}
						}
					}
				}
			}
		}

		return new ZCNavList(sharedWithGroupList, sharedWithWorkSpaceList);
	}

	static List<ZCApplication> parseForApplicationList(Document rootDocument) {
		List<ZCApplication> toReturn = new ArrayList<ZCApplication>();
		NodeList nl = rootDocument.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			Node responseNode = nl.item(i);
			//////System.out.println("******* " + responseNode.getNodeName());
			if(responseNode.getNodeName().equals("response")) {
				NodeList responseNodes = responseNode.getChildNodes();
				for(int j=0; j<responseNodes.getLength(); j++) {
					Node resultNode = responseNodes.item(j);
					//////System.out.println(resultNode.getNodeName());
					if(resultNode.getNodeName().equals("result")) {
						NodeList resultNodes = resultNode.getChildNodes();
						String appOwner = "";
						for(int k=0; k<resultNodes.getLength(); k++) {
							Node resultNodeChild = resultNodes.item(k);
							if(resultNodeChild.getNodeName().equals("application_owner")) {
								appOwner = getStringValue(resultNodeChild, ""); //No I18N
							} else if(resultNodeChild.getNodeName().equals("application_list")) {
								NodeList appListNodes = resultNodeChild.getChildNodes();
								for(int l=0; l<appListNodes.getLength(); l++) {
									Node applicationsNode = appListNodes.item(l);
									//////System.out.println(applicationsNode.getNodeName());
									if(applicationsNode.getNodeName().equals("applications")) {
										//////System.out.println(ZOHOCreator.getString(applicationsNode));
										NodeList applicationsNodes = applicationsNode.getChildNodes();
										//////System.out.println(applicationsNodes.getLength());
										for(int m=0; m<applicationsNodes.getLength(); m++) {
											Node applicationNode = applicationsNodes.item(m);
											//////System.out.println(applicationNode.getNodeName());
											if(applicationNode.getNodeName().equals("application")) {
												NodeList applicationNodes = applicationNode.getChildNodes();
												String appName = null;
												String linkName = null;
												Date createdTime = null;
												boolean isPrivate = true;
												for(int n=0; n<applicationNodes.getLength(); n++) {
													Node appPropertyNode = applicationNodes.item(n);
													String nodeName = appPropertyNode.getNodeName();
													//////System.out.println(nodeName);
													if(nodeName.equals("application_name")) {
														appName = getStringValue(appPropertyNode, appName); //No I18N
													} else if(nodeName.equals("link_name")) {
														linkName = getStringValue(appPropertyNode, linkName); //No I18N
													} else if(nodeName.equals("access")) {
														isPrivate = getStringValue(appPropertyNode, "private").equals("private"); //No I18N
													} else if(nodeName.equals("created_time")) {
														// 2013-04-01 14:28:58.0
														createdTime = getDateValue(appPropertyNode, createdTime, "yyyy-MM-dd HH:mm:ss.S"); //No I18N
													} else if(nodeName.equals("sharedBy")) {
														appOwner = getStringValue(appPropertyNode, appOwner);
													}
												}
												ZCApplication zcApp = new ZCApplication(appOwner, appName, linkName, isPrivate, createdTime);
												toReturn.add(zcApp);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return toReturn;
	}




	static List<ZCSection> parseForSectionList(Document rootDocument, String appLinkName, String appOwner) throws ZCException {
		List<ZCSection> toReturn = new ArrayList<ZCSection>();

		NodeList nl = rootDocument.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			Node responseNode = nl.item(i);
			//////////System.out.println(responseNode.getNodeName());
			if(responseNode.getNodeName().equals("Response")) {
				NodeList responseNodes = responseNode.getChildNodes();
				for(int j=0; j<responseNodes.getLength(); j++) {
					Node responseNodeChild = responseNodes.item(j);
					//////////System.out.println(responseNodeChild.getNodeName());
					if(responseNodeChild.getNodeName().equals("Sections")) {

						NodeList sectionsNodes = responseNodeChild.getChildNodes();

						for(int k=0; k<sectionsNodes.getLength(); k++) {
							Node sectionNode = sectionsNodes.item(k);
							if(sectionNode.getNodeName().equals("Section")) {
								NodeList sectionNodes = sectionNode.getChildNodes();
								String sectionName = null;
								boolean isHidden = false;
								String sectionLinkName = null;
								long sectionID = -1L;
								Date modifiedTime = null;
								List<ZCComponent> comps = new ArrayList<ZCComponent>();
								for(int l=0; l<sectionNodes.getLength(); l++) {
									Node sectionNodeChild = sectionNodes.item(l);
									String nodeName = sectionNodeChild.getNodeName();
									//////////System.out.println(nodeName);
									if(nodeName.equals("Display_Name")) {
										sectionName = getStringValue(sectionNodeChild, ""); //No I18N
									} else if(nodeName.equals("Is_Hidden")) {
										isHidden = getBooleanValue(sectionNodeChild, isHidden); //No I18N
									} else if(nodeName.equals("Link_Name")) {
										sectionLinkName = getStringValue(sectionNodeChild, ""); //No I18N
									} else if(nodeName.equals("Section_ID")) {
										sectionID = getLongValue(sectionNodeChild, sectionID); //No I18N
									} else if(nodeName.equals("Modified_Time")) {
										modifiedTime = getDateValue(sectionNodeChild, modifiedTime, "yyyy-MM-dd HH:mm:ss.S"); //No I18N
										// 2013-04-01 14:28:58.0
									} else if(nodeName.equals("Components")) {
										NodeList ComponentsNodes = sectionNodeChild.getChildNodes();
										for(int m=0; m<ComponentsNodes.getLength(); m++) {
											Node componentNode = ComponentsNodes.item(m);
											if(componentNode.getNodeName().equals("Component")) {
												NamedNodeMap attrMap = componentNode.getAttributes();
												Node typeNode = attrMap.getNamedItem("type");//No I18N
												String type = typeNode.getNodeValue();
												NodeList componentNodes = componentNode.getChildNodes();
												String componentName = null;
												String componentLinkName = null;
												int sequenceNumber = -1;
												long componentID = -1L;
												for(int n=0; n<componentNodes.getLength(); n++) {
													Node componentNodeChild = componentNodes.item(n);
													String componentNodeName = componentNodeChild.getNodeName();
													String nodeValue = getStringValue(componentNodeChild, "");
													if(componentNodeName.equals("Display_Name")) {
														componentName = nodeValue;
													} else if(componentNodeName.equals("Link_Name")) {
														componentLinkName = nodeValue;
													} else if(componentNodeName.equals("Secquence")) {
														sequenceNumber = Integer.parseInt(nodeValue);
													} else if(componentNodeName.equals("Component_ID")) {
														componentID = Long.valueOf(nodeValue);
													}
												}
												comps.add(new ZCComponent (appOwner, appLinkName, type, componentName, componentLinkName, sequenceNumber));
											}
										}
									}
								}
								ZCSection zcSection = new ZCSection(appOwner, appLinkName, sectionName,  sectionLinkName,  isHidden,  sectionID,  modifiedTime);
								zcSection.addComponents(comps);
								toReturn.add(zcSection);
							}
						}
					}
				}
			} else if(responseNode.getNodeName().equals("LicenseEnabled")) { //No I18N
				//boolean licenceEnabled = getBooleanValue(responseNode, false); //No I18N
				//if(!licence)
				throw new ZCException("Mobile plan not enabled for " + appOwner, ZCException.LICENCE_ERROR); //No I18N
			}
		}
		return toReturn;		
	}

	static ZCForm parseForForm(Document rootDocument, String appLinkName, String appOwner) throws ZCException {
		NodeList nl = rootDocument.getChildNodes();
		ZCForm toReturn = null;
		for(int i=0; i<nl.getLength(); i++) {
			Node responseNode = nl.item(i);
			if(responseNode.getNodeName().equals("response")) {
				boolean hasAddOnLoad = false;
				boolean hasEditOnLoad = false;
				String successMessage = "";
				String dateFormat = "";
				List<ZCField> fields = new ArrayList<ZCField>();
				List<ZCButton> buttons = new ArrayList<ZCButton>();
				boolean isStateLess = true;
				int formLinkId = -1;

				String componentName = null;
				String componentLinkName = null; 
				int sequenceNumber = -1;
				Long componentID = -1L; 

				NodeList responseNodes = responseNode.getChildNodes();
				for(int j=0; j<responseNodes.getLength(); j++) {
					Node responseNodeChild = responseNodes.item(j);
					//////////System.out.println(resultNode.getNodeName());
					if(responseNodeChild.getNodeName().equals("hasAddOnLoad")) { 
						String nodeValue = getStringValue(responseNodeChild, "");
						hasAddOnLoad = Boolean.valueOf(nodeValue);
						//////System.out.println("node value "+nodeValue+"boolean"+hasAddOnLoad);
					} else if(responseNodeChild.getNodeName().equals("hasEditOnLoad")) { 
						String nodeValue = getStringValue(responseNodeChild, "");
						hasEditOnLoad = Boolean.valueOf(nodeValue);
					} else if(responseNodeChild.getNodeName().equals("successMessage")) { 
						successMessage = getStringValue(responseNodeChild, "");
					} else if(responseNodeChild.getNodeName().equals("dateFormat")) {
						dateFormat = getStringValue(responseNodeChild,"");
					} else if(responseNodeChild.getNodeName().equals("errorlist")) {
						throw new ZCException(parseErrorMessage(responseNodeChild), ZCException.ACCESS_ERROR);
					} else if(responseNodeChild.getNodeName().equals("result")) {
						NodeList resultNodes = responseNodeChild.getChildNodes();
						for(int k=0; k<resultNodes.getLength(); k++) {
							Node resultNodeChild = resultNodes.item(k);
							if(resultNodeChild.getNodeName().equals("tableName")) {
								String tableName = getStringValue(resultNodeChild, "");
								isStateLess = (tableName.equals(""));
							} else if(resultNodeChild.getNodeName().equals("formLinkId")) {
								formLinkId = getIntValue(resultNodeChild, formLinkId);
							} else if(resultNodeChild.getNodeName().equals("DisplayName")) {
								componentName = getStringValue(resultNodeChild, "");
							} else if(resultNodeChild.getNodeName().equals("formLinkName")) {
								componentLinkName = getStringValue(resultNodeChild, "");
							} else if(resultNodeChild.getNodeName().equals("formid")) {
								componentID = getLongValue(resultNodeChild, componentID);
							} else if(resultNodeChild.getNodeName().equals("Fields")) {
								ZCField field = parseField(resultNodeChild, appOwner, false);
								if(field != null) {
									fields.add(field);
								}
							} else if(resultNodeChild.getNodeName().equals("buttons")) {
								NodeList buttonPropetyNodes = resultNodeChild.getChildNodes();
								String buttonDisplayName = null;
								String buttonLinkName = null;
								ZCButtonType buttonType = null;
								int buttonSequenceNumber = -1;
								int actiontype = 1;
								for(int l=0; l<buttonPropetyNodes.getLength(); l++) {
									Node buttonPropetyNode = buttonPropetyNodes.item(l);
									if(buttonPropetyNode.getNodeName().equals("buttonname")) {
										buttonLinkName = getStringValue(buttonPropetyNode, buttonLinkName);
									} else if(buttonPropetyNode.getNodeName().equals("sequence")) {
										buttonSequenceNumber = getIntValue(buttonPropetyNode, buttonSequenceNumber);
									} else if(buttonPropetyNode.getNodeName().equals("actiontype")) {
										actiontype = getIntValue(buttonPropetyNode, actiontype);
									} else if(buttonPropetyNode.getNodeName().equals("type")) {
										buttonType = ZCButtonType.getButtonType(getIntValue(buttonPropetyNode, 62));
									} else if(buttonPropetyNode.getNodeName().equals("displayname")) {
										buttonDisplayName = getStringValue(buttonPropetyNode, buttonDisplayName);
									} 
								}
								ZCButton button = new ZCButton(buttonDisplayName, buttonLinkName, buttonType);
								buttons.add(button);
							}
						} 
					}
				}
				Collections.sort(buttons);

				// String appOwner, String appLinkName, String type, String componentName, String componentLinkName, int sequenceNumber, Long componentID, boolean hasAddOnLoad, boolean hasEditOnLoad, String successMessage, String tableName, int formLinkId
				toReturn = new ZCForm(appOwner, appLinkName, componentName, componentLinkName, sequenceNumber,  hasAddOnLoad, hasEditOnLoad, successMessage, dateFormat, isStateLess);
				toReturn.addFields(fields);
				if(buttons.size()>0) {
					toReturn.addButtons(buttons);	
				}
			}
		}
		return toReturn;
	}

	static String parseErrorMessage(Node errorlistNode) {
		NodeList resultNodes = errorlistNode.getChildNodes();
		for(int k=0; k<resultNodes.getLength(); k++) {
			Node resultNodeChild = resultNodes.item(k);
			if(resultNodeChild.getNodeName().equals("error")) {
				NodeList resultNodeChildNodes = resultNodeChild.getChildNodes();
				for(int l=0; l<resultNodeChildNodes.getLength(); l++) {
					Node resultNodeChildNode = resultNodeChildNodes.item(l);
					if(resultNodeChildNode.getNodeName().equals("message")) {
						return getStringValue(resultNodeChildNode, "");
					}
				}
			}
		}
		return "";
	}

	private static ZCField parseField(Node resultNodeChild, String appOwner,boolean isParentSubForm) {
		//////System.out.println("inside zcparse field");
		NodeList fieldPropetyNodes = resultNodeChild.getChildNodes();

		String fieldName = null;
		FieldType fieldType = FieldType.SINGLE_LINE;
		String displayName = "";
		String delugeType = "";
		String textValue = "";



		String initialValue = "";
		List<String> initialValues = new ArrayList<String>(); 		

		boolean isUnique = false;
		boolean isRequired = false;

		int maxChar = 255;

		boolean urlTitleReq = false;
		boolean urlLinkNameReq = true;

		boolean fromZohoDoc = false;
		boolean fromGoogleDoc = false;
		boolean fromLocalComputer = true;

		boolean imgLinkReq = false;
		boolean imgTitleReq = false;
		boolean altTxtReq = false;

		boolean isLookup = false;
		boolean hasOnUserInput = false;
		boolean hasOnUserInputForFormula = false;
		boolean hasSubForm = false;

		String refFormLinkName = null;
		String refFormDisplayName = null;
		String refAppLinkName = null;
		String refFieldLinkName = null;
		String currencyType = "USD";//No I18N

		List<ZCChoice> choices  = new ArrayList<ZCChoice>(); 
		List<ZCField> subFormFields = new ArrayList<ZCField>();
		List<ZCField> subFormEditFields = new ArrayList<ZCField>();
		List<ZCRecord> subFormEntries = new ArrayList<ZCRecord>();


		for(int l=0; l<fieldPropetyNodes.getLength(); l++) {

			Node fieldPropetyNode = fieldPropetyNodes.item(l);
			if(fieldPropetyNode.getNodeName().equals("altTxtReq")) {
				altTxtReq = getBooleanValue(fieldPropetyNode, altTxtReq);
			} else if(fieldPropetyNode.getNodeName().equals("delugeType")) {
				delugeType = getStringValue(fieldPropetyNode, delugeType);
			} else if(fieldPropetyNode.getNodeName().equals("DisplayName")) {
				displayName = getStringValue(fieldPropetyNode, displayName);
				//				System.out.println("displayName " + displayName);
			} else if(fieldPropetyNode.getNodeName().equals("FieldName")) {
				fieldName = getStringValue(fieldPropetyNode, fieldName);
			} else if(fieldPropetyNode.getNodeName().equals("Text")) {
				textValue = getStringValue(fieldPropetyNode, textValue);
			} else if(fieldPropetyNode.getNodeName().equals("fromLocalComputer")) {
				fromLocalComputer = getBooleanValue(fieldPropetyNode, fromLocalComputer);
			} else if(fieldPropetyNode.getNodeName().equals("fromGoogleDoc")) {
				fromGoogleDoc = getBooleanValue(fieldPropetyNode, fromGoogleDoc);
			} else if(fieldPropetyNode.getNodeName().equals("fromZohoDoc")) {
				fromZohoDoc = getBooleanValue(fieldPropetyNode, fromZohoDoc);
			} else if(fieldPropetyNode.getNodeName().equals("imgLinkReq")) {
				imgLinkReq	= getBooleanValue(fieldPropetyNode, imgLinkReq);
			} else if(fieldPropetyNode.getNodeName().equals("imgTitleReq")) {
				imgTitleReq = getBooleanValue(fieldPropetyNode, imgTitleReq);
			} else if(fieldPropetyNode.getNodeName().equals("Initial")) {
				if(subFormFields.size()>0) {
					NodeList recordsList = fieldPropetyNode.getChildNodes();
					for(int k=0; k<recordsList.getLength(); k++) {
						Node recordNode = recordsList.item(k);
						if(recordNode.getNodeName().equals("record")) {
							parseAndSetRecord(null, recordNode, subFormEntries, subFormFields);
							//							////System.out.println("initial valueinside sub"+initialValue);
						}
					}
				} else {
//					System.out.println("initial value"+initialValue);
					initialValue = getStringValue(fieldPropetyNode, "");
	//				System.out.println("initial value"+initialValue);
				}
			} else if(fieldPropetyNode.getNodeName().equals("MaxChar")) {
				maxChar = getIntValue(fieldPropetyNode, maxChar);
			} else if(fieldPropetyNode.getNodeName().equals("ref_formdispname")) {
				refFormDisplayName = getStringValue(fieldPropetyNode, "");
			} else if(fieldPropetyNode.getNodeName().equals("ref_formname")) {
				refFormLinkName = getStringValue(fieldPropetyNode, "");
			} else if(fieldPropetyNode.getNodeName().equals("ref_appname")) {
				refAppLinkName = getStringValue(fieldPropetyNode, "");
			} else if(fieldPropetyNode.getNodeName().equals("ref_fieldname")) {
				refFieldLinkName = getStringValue(fieldPropetyNode, "");
			} else if(fieldPropetyNode.getNodeName().equals("Reqd")) {
				isRequired = getBooleanValue(fieldPropetyNode, isRequired);
			} else if(fieldPropetyNode.getNodeName().equals("Type")) {
				int type = getIntValue(fieldPropetyNode, 1);				
				fieldType = FieldType.getFieldType(type);
				//System.out.println("Type2 " + fieldType +"  type");
			} else if(fieldPropetyNode.getNodeName().equals("Unique")) {
				isUnique = getBooleanValue(fieldPropetyNode, isUnique);
			} else if(fieldPropetyNode.getNodeName().equals("urlTitleReq")) {

				urlTitleReq = getBooleanValue(fieldPropetyNode, urlTitleReq);
			} 
			else if(fieldPropetyNode.getNodeName().equals("CurrencyType")){
				currencyType = getStringValue(fieldPropetyNode, "");
			}
			else if(fieldPropetyNode.getNodeName().equals("urlLinkNameReq")) {
				urlLinkNameReq = getBooleanValue(fieldPropetyNode, urlLinkNameReq);
			} else if(fieldPropetyNode.getNodeName().equals("onChangeExists")) {
				hasOnUserInput = getBooleanValue(fieldPropetyNode, hasOnUserInput);
			} else if(fieldPropetyNode.getNodeName().equals("formulaExists")) {
				hasOnUserInputForFormula = getBooleanValue(fieldPropetyNode, hasOnUserInputForFormula);
			} else if(fieldPropetyNode.getNodeName().equals("SubFormFields")) {
				NodeList subFormFieldNodes = fieldPropetyNode.getChildNodes();
				hasSubForm = true;
				for(int m=0; m<subFormFieldNodes.getLength(); m++) {
					Node subFormFieldNode = subFormFieldNodes.item(m);
					if(subFormFieldNode.getNodeName().equals("Field")) {
						ZCField subFormField = parseField(subFormFieldNode, appOwner, true);
						if(subFormField != null)
						{
							subFormFields.add(subFormField);
							subFormEditFields.add(parseField(subFormFieldNode, appOwner, true));
						}
					}
				}
			} else if(fieldPropetyNode.getNodeName().equals("Choices")) {
				NodeList choiceNodes = fieldPropetyNode.getChildNodes();
				for(int m=0; m<choiceNodes.getLength(); m++) {
					Node choiceNode = choiceNodes.item(m);
					String key = choiceNode.getNodeName();
					String value = getStringValue(choiceNode, "");
					if(value.contains("\":\"")) {
						try {
							JSONObject jsonObj = new JSONObject(value);
							Iterator keys = jsonObj.keys();
							while(keys.hasNext()) {
								key = keys.next().toString();
								value = jsonObj.getString(key);
								isLookup = true;
								break;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					//////System.out.println(key + " : " + value);
					choices.add(new ZCChoice(key, value));
				}
			}
		}
		if(isParentSubForm) {
				if(fieldType == FieldType.SUB_FORM || fieldType == FieldType.UNKNOWN  || fieldType ==FieldType.NOTES || fieldType==FieldType.FORMULA) {
					return null;
				}
		}	

		ZCField zcField = new ZCField(fieldName, fieldType, displayName);
		if(FieldType.isMultiChoiceField(fieldType)) {
			// THis is used in Edit record for multichoice field. But in form builder there is no way to set initial value
			initialValues = parseMultiSelectValues(initialValue, choices);
			zcField.setRecordValue(new ZCRecordValue(zcField, initialValues)); 
			////System.out.println("initialvalues"+initialValue+zcField.getRecordValue().getValues());
		} else if(FieldType.isPhotoField(fieldType)) {
			File file = null;
			zcField.setRecordValue(new ZCRecordValue(zcField, file));
		} else {
			initialValue = getInitialValueForSingleChoiceField(initialValue, fieldType, choices);
			zcField.setRecordValue(new ZCRecordValue(zcField, initialValue));
		}
		
		zcField.setTextValue(textValue);
		zcField.addChoices(choices);
		zcField.setLookup(isLookup);
		zcField.setRequired(isRequired);
		zcField.setHasOnUserInput(hasOnUserInput);
		zcField.setHasOnUserInputForFormula(hasOnUserInputForFormula);
		zcField.setMaxChar(maxChar);
		zcField.setCurrencyType(currencyType);
		zcField.setUrlLinkNameReq(urlLinkNameReq);
		zcField.setUrlTitleReq(urlTitleReq);

		if(refFormLinkName != null && refFormDisplayName != null && refAppLinkName != null && refFieldLinkName != null) {
			zcField.setRefFormComponent(new ZCComponent(appOwner, refAppLinkName, ZCComponent.FORM, refFormDisplayName, refFormLinkName, -1));
			zcField.setRefFieldLinkName(refFieldLinkName);
			if(fieldType != FieldType.SUB_FORM) {
				zcField.setNewEntriesAllowed(true);
			}
		}


		// String appOwner, String appLinkName, String type, String componentName, String componentLinkName, int sequenceNumber, Long componentID, boolean hasAddOnLoad, boolean hasEditOnLoad, String successMessage, String tableName, int formLinkId
		if(hasSubForm) { // type/sequenceNumber/componentID/formLinkId -1, hasAddOnLoad/hasEditOnLoad false,  successMessage/tableName empty string, 
			ZCForm subForm = new ZCForm(appOwner, refAppLinkName, refFormDisplayName, refFormLinkName, -1, false, false, "", "", false);
			subForm.addFields(subFormFields);
			zcField.setSubForm(subForm);
			for(int i=0; i<subFormEntries.size(); i++) {
				zcField.addSubFormEntry(subFormEntries.get(i));
			}

			ZCForm editSubForm = new ZCForm(appOwner, refAppLinkName, refFormDisplayName, refFormLinkName, -1, false, false, "", "", false);
			editSubForm.addFields(subFormEditFields);
			zcField.setEditSubForm(editSubForm);
		}
		return zcField;
	}

	private static String getInitialValueForSingleChoiceField(String initialValue, FieldType ftype, List<ZCChoice> choices) {
		if(FieldType.isSingleChoiceField(ftype)) {
			for(int i=0; i<choices.size(); i++) {
				if(choices.get(i).getKey().equals(initialValue)) {
					initialValue = choices.get(i).getValue();
				}
			}
		}
		return initialValue;
	}
	private static List<String> parseMultiSelectValues(String value, List<ZCChoice> choices) {
		List<String> multSelVals = new ArrayList<String>();
		if(value.startsWith("[") && value.endsWith("]")) {
			value = value.substring(1, value.length()-1);
		}

		String[] tokens = value.split(",");
		for(int i=1;i<tokens.length;i++)
		{
			tokens[i]=tokens[i].substring(1);
		}

		for(int m = 0;m<tokens.length;m++)
		{
			if(!(tokens[m].equals("")))
			{
				String initialValue = tokens[m];
				for(int i=0; i<choices.size(); i++) {
					if(choices.get(i).getKey().equals(initialValue)) {
						initialValue = choices.get(i).getValue();
					}
				}
				multSelVals.add(initialValue);
			}

		}		
		return multSelVals;
	}

	static void parseAndAddRecords(Document rootDocument, ZCView zcView) {
		NodeList nl = rootDocument.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			Node responseNode = nl.item(i);
			//////System.out.println("here... " + responseNode.getNodeName());
			if(responseNode.getNodeName().equals("response")) {
				NodeList responseNodes = responseNode.getChildNodes();
				for(int j=0; j<responseNodes.getLength(); j++) {
					Node responseChildNode = responseNodes.item(j);
					if(responseChildNode.getNodeName().equals("records")) {
						parseAndSetRecords(zcView, responseChildNode);
					} else if(responseChildNode.getNodeName().equals("calendar")) {
						parseAndSetCalendarRecords(zcView, responseChildNode);
					}					
				}
			}
		}
	}

	private static void parseAndSetCalendarRecords(ZCView zcView, Node calendarNode) {

		zcView.setGrouped(true);
		NodeList eventsList = calendarNode.getChildNodes();


		for(int i=0; i<eventsList.getLength(); i++) {

			Node eventNode = eventsList.item(i);
			NamedNodeMap eventAttrMap = eventNode.getAttributes();
			long recordid = Long.parseLong(eventAttrMap.getNamedItem("id").getNodeValue()); //No I18N
			String title = getChildNodeValue(eventNode, "title"); //eventAttrMap.getNamedItem("title").getNodeValue(); //No I18N
			boolean isAllDay = Boolean.parseBoolean(eventAttrMap.getNamedItem("allDay").getNodeValue()); //No I18N
			// 07/31/2013 08:00:00
			String dateFormat = "MM/dd/yyyy HH:mm:ss"; //No I18N
			if(isAllDay) {
				dateFormat = "MM/dd/yyyy"; //No I18N
			}

			Date startTime = getDateValue(eventAttrMap.getNamedItem("start").getNodeValue(), dateFormat); //No I18N

			ZCRecord record = zcView.getRecord(recordid);

			record.setEventTitle(title);
			if(isAllDay) {
				zcView.setIsAllDay(isAllDay);
				zcView.setEvent(record, startTime);

				record.setEventDate(startTime);
			} else {
				// 07/31/2013 08:00:00
				Date endTime = getDateValue(eventAttrMap.getNamedItem("end").getNodeValue(), dateFormat); //No I18N
				record.setStartTime(startTime);
				record.setEndTime(endTime);

				Calendar startCalendar = new GregorianCalendar();
				startCalendar.setTime(startTime);
				startCalendar.set(Calendar.HOUR_OF_DAY, 0);
				startCalendar.set(Calendar.MINUTE, 0);
				startCalendar.set(Calendar.SECOND, 0);
				startCalendar.set(Calendar.MILLISECOND, 0);

				Calendar endCalendar = new GregorianCalendar();
				endCalendar.setTime(endTime);
				endCalendar.set(Calendar.HOUR_OF_DAY, 0);
				endCalendar.set(Calendar.MINUTE, 0);
				endCalendar.set(Calendar.SECOND, 0);
				endCalendar.set(Calendar.MILLISECOND, 0);

				Date eventDate = new Date(startCalendar.getTimeInMillis());
				zcView.setEvent(record, eventDate);
				while ((startCalendar.get(Calendar.YEAR) != endCalendar.get(Calendar.YEAR)) || (startCalendar.get(Calendar.MONTH) != endCalendar.get(Calendar.MONTH)) || (startCalendar.get(Calendar.DATE) != endCalendar.get(Calendar.DATE))) {
					startCalendar.add(Calendar.DATE, 1);
					eventDate = new Date(startCalendar.getTimeInMillis());
					zcView.setEvent(record, eventDate);
				}
			}
		}

		List<ZCGroup> zcGroups = zcView.getGroups();

		HashMap<Date, List<ZCRecord>> eventsMap = zcView.getEventRecordsMap();



		SortedSet<Date> keys = new TreeSet<Date>(eventsMap.keySet());
		for (Date eventDate : keys) { 
			List<ZCRecord> eventRecords = eventsMap.get(eventDate);
			List<String> groupHeaderValues  = new ArrayList<String>();
			SimpleDateFormat dateFormat = new SimpleDateFormat(zcView.getDateFormat());	//No I18N
			groupHeaderValues.add(dateFormat.format(eventDate));
			ZCGroup zcGroup = new ZCGroup(groupHeaderValues);
			zcGroups.add(zcGroup);
			for(int i=0; i<eventRecords.size(); i++) {
				ZCRecord eventRecord = eventRecords.get(i);
				zcGroup.addRecord(eventRecord);
			}
		}


		zcView.sortRecordsForCalendar();


	}

	private static void parseAndSetRecords(ZCView zcView, Node recordsNode) {
		NodeList recordsList = recordsNode.getChildNodes();
		//////System.out.println("recordsList.size(): " + recordsList.getLength());
		List<ZCRecord> records = new ArrayList<ZCRecord>();
		List<ZCGroup> zcGroups = zcView.getGroups();
		ZCGroup zcGroup = null;
		if(zcGroups.size()>0) { // setting the previous set's last group
			zcGroup = zcGroups.get(zcGroups.size()-1);
		}
		boolean isViewGrouped = false;
		boolean isFirstRecordInGroup = false;
		List<String> groupHeaderValues  = null;
		for(int k=0; k<recordsList.getLength(); k++) {
			Node recordNode = recordsList.item(k);
			if(recordNode.getNodeName().equals("group")) {
				isFirstRecordInGroup = true;
				if(!isViewGrouped) {
					isViewGrouped = true;
				}
				groupHeaderValues = new ArrayList<String>();
				NodeList columnList = recordNode.getChildNodes();
				for(int l=0; l<columnList.getLength(); l++) {
					Node columnNode = columnList.item(l);
					Node valueNode = columnNode.getFirstChild();
					groupHeaderValues.add(getStringValue(valueNode, ""));
				}
			} else if(recordNode.getNodeName().equals("record")) {
				ZCRecord record = parseAndSetRecord(zcView, recordNode, records, null);
				if(isViewGrouped) {
					if(isFirstRecordInGroup && (zcGroup == null || !zcGroup.getGroupHeaderValues().equals(groupHeaderValues))) { // To check if it's not the same group of the previous set's last group
						zcGroup = new ZCGroup(groupHeaderValues);
						zcGroups.add(zcGroup);
						isFirstRecordInGroup = false;
						groupHeaderValues = null;
					}
					zcGroup.addRecord(record);
				}
			}
		}
		zcView.addRecords(records);
		zcView.setGrouped(isViewGrouped);
	}

	private static ZCRecord parseAndSetRecord(ZCView zcView, Node recordNode, List<ZCRecord> records, List<ZCField> subFormFields) {
		NamedNodeMap recordAttrMap = recordNode.getAttributes();
		long recordid = Long.parseLong(recordAttrMap.getNamedItem("id").getNodeValue()); //No I18N
		List<ZCRecordValue> valueList  = new ArrayList<ZCRecordValue>();
		NodeList columnList = recordNode.getChildNodes();
		for(int l=0; l<columnList.getLength(); l++) {
			Node columnNode = columnList.item(l);
			NamedNodeMap columAttrMap = columnNode.getAttributes();
			String fieldName = columAttrMap.getNamedItem("name").getNodeValue(); //No I18N
			//////System.out.println("fieldName..... " + fieldName);
			//////System.out.println("Columns... "+ zcView.getColumns());
			ZCField zcField = null;
			if(zcView != null) {
				////System.out.println("dfh1");
				zcField = zcView.getColumn(fieldName);
				//				 if(zcField==null)
				//		           {
				//		           ////System.out.println("dfh2");
				//		           }
			} else {
				for(int m=0; m<subFormFields.size(); m++) {
					ZCField fieldToCheck = subFormFields.get(m);
					////System.out.println("dfh3"+fieldToCheck.getType());
					if(fieldToCheck.getFieldName().equals(fieldName)) {
						zcField = fieldToCheck;
						break;
					}
				}
			}
			Node valueNode = columnNode.getFirstChild();
			String value = getStringValue(valueNode, "");
			//			System.out.println("radioo"+value);
			ZCRecordValue zcValue = null;
			if(zcField!=null)
			{
				if(FieldType.isMultiChoiceField(zcField.getType())) {
					zcValue = new ZCRecordValue(zcField, parseMultiSelectValues(value, zcField.getChoices()));
				} else {
					value = getInitialValueForSingleChoiceField(value, zcField.getType(), zcField.getChoices());
					zcValue = new ZCRecordValue(zcField, value);
				}
				zcField.setRecordValue(zcValue);
				valueList.add(zcValue);
			}
		}
		ZCRecord record = new ZCRecord(recordid, valueList);
		records.add(record);
		return record;
	}

	static ZCView parseForView(Document rootDocument, String appLinkName, String appOwner, String componentType, int month, int year) {		
		NodeList nl = rootDocument.getChildNodes();
		ZCView toReturn = null;
		for(int i=0; i<nl.getLength(); i++) {
			Node responseNode = nl.item(i);
			//////System.out.println("here... " + responseNode.getNodeName());
			if(responseNode.getNodeName().equals("response")) {
				NodeList responseNodes = responseNode.getChildNodes();
				for(int j=0; j<responseNodes.getLength(); j++) {
					Node responseChildNode = responseNodes.item(j);
					//////System.out.println(responseChildNode.getNodeName());
					if(responseChildNode.getNodeName().equals("metadata")) {
						NodeList viewNodes = responseChildNode.getChildNodes();
						for(int k=0; k<viewNodes.getLength(); k++) {
							Node viewNode = viewNodes.item(k);
							if(viewNode.getNodeName().equals("View")) {
								NamedNodeMap attrMap = viewNode.getAttributes();
								String componentLinkName = attrMap.getNamedItem("LinkName").getNodeValue();//No I18N

								String componentName = getChildNodeValue(viewNode, "DisplayName"); //No I18N
								String dateFormat = attrMap.getNamedItem("DateFormat").getNodeValue();//No I18N
								//String appOwner, String appLinkName, String componentName, String componentLinkName
								toReturn = new ZCView(appOwner, appLinkName, componentType, componentName, componentLinkName);
								toReturn.setDateFormat(dateFormat);
								NodeList viewChildNodes = viewNode.getChildNodes();
								for(int l=0; l<viewChildNodes.getLength(); l++) {
									Node viewChildNode = viewChildNodes.item(l);
									if(viewChildNode.getNodeName().equals("BaseForm")) {
										NamedNodeMap baseFormAttrMap = viewChildNode.getAttributes();
										String baseFormLinkName = baseFormAttrMap.getNamedItem("linkname").getNodeValue(); //No I18N
										toReturn.setBaseFormLinkName(baseFormLinkName);
									} else if(viewChildNode.getNodeName().equals("customFilters")) {
										NodeList customFilterNodeList = viewChildNode.getChildNodes();
										List<ZCCustomFilter> customFilters = new ArrayList<ZCCustomFilter>();
										for(int m=0; m<customFilterNodeList.getLength(); m++) {
											Node customFilterNode = customFilterNodeList.item(m);
											NamedNodeMap customFilterAttrMap = customFilterNode.getAttributes();
											long id = Long.parseLong(customFilterAttrMap.getNamedItem("Id").getNodeValue()); //No I18N											
											String customFilterName = getChildNodeValue(customFilterNode, "DisplayName");//No I18N
											ZCCustomFilter customFilter = new ZCCustomFilter(customFilterName, id);
											customFilters.add(customFilter);
										}
										toReturn.addCustomFilters(customFilters);
									} else if(viewChildNode.getNodeName().equals("filters")) {
										NodeList filterNodeList = viewChildNode.getChildNodes();
										List<ZCFilter> filters = new ArrayList<ZCFilter>();
										for(int m=0; m<filterNodeList.getLength(); m++) {
											Node filterNode = filterNodeList.item(m);
											NamedNodeMap filterAttrMap = filterNode.getAttributes();
											String FilterLinkName = filterAttrMap.getNamedItem("name").getNodeValue(); //No I18N
											String filterName = getChildNodeValue(filterNode, "displayname"); //filterAttrMap.getNamedItem("displayname").getNodeValue(); //No I18N
											ZCFilter filter = new ZCFilter(FilterLinkName, filterName);
											NodeList filterValuesList = filterNode.getChildNodes();
											List<ZCFilterValue> filterValues = new ArrayList<ZCFilterValue>();
											for(int n=0; n<filterValuesList.getLength(); n++) {
												Node filterValueNode = filterValuesList.item(n);
												if(filterValueNode.getNodeName().equals("value")) {
													String filterValue = getStringValue(filterValueNode, ""); //No I18N
													filterValue = filterValue.substring(filterValue.indexOf(":") + 1);
													filterValues.add(new ZCFilterValue(filterValue));
												}
											}
											filter.addValues(filterValues);
											filters.add(filter);
										}
										toReturn.addFilters(filters);
										//									} else if(viewChildNode.getNodeName().equals("rec_filter")) {

										//									} else if(viewChildNode.getNodeName().equals("groupby")) {

										//									} else if(viewChildNode.getNodeName().equals("opensearch")) {

									} else if(viewChildNode.getNodeName().equals("permissions")) {
										NamedNodeMap permissionAttrMap = viewChildNode.getAttributes();
										boolean isAddAllowed = Boolean.parseBoolean(permissionAttrMap.getNamedItem("add").getNodeValue()); //No I18N
										boolean isEditAllowed = Boolean.parseBoolean(permissionAttrMap.getNamedItem("edit").getNodeValue()); //No I18N
										boolean isDeleteAllowed = Boolean.parseBoolean(permissionAttrMap.getNamedItem("delete").getNodeValue()); //No I18N
										boolean isDuplicateAllowed = Boolean.parseBoolean(permissionAttrMap.getNamedItem("duplicate").getNodeValue()); //No I18N
										boolean isBulkEditAllowed = Boolean.parseBoolean(permissionAttrMap.getNamedItem("bulkedit").getNodeValue()); //No I18N
										toReturn.setAddAllowed(isAddAllowed);
										toReturn.setEditAllowed(isEditAllowed);
										toReturn.setDeleteAllowed(isDeleteAllowed);
										toReturn.setDuplicateAllowed(isDuplicateAllowed);
										toReturn.setBulkEditAllowed(isBulkEditAllowed);
									} else if(viewChildNode.getNodeName().equals("Actions")) {
										NodeList actionList = viewChildNode.getChildNodes();
										List<ZCCustomAction> headerCustomActions = new ArrayList<ZCCustomAction>();
										List<ZCCustomAction> recordCustomActions = new ArrayList<ZCCustomAction>();
										for(int m=0; m<actionList.getLength(); m++) {
											Node actionNode = actionList.item(m);
											NamedNodeMap actionAttrMap = actionNode.getAttributes();
											String actionType = actionAttrMap.getNamedItem("type").getNodeValue(); //No I18N
											String actionName = getChildNodeValue(actionNode, "name"); //actionAttrMap.getNamedItem("name").getNodeValue(); //No I18N
											Long actionId = Long.parseLong(actionAttrMap.getNamedItem("id").getNodeValue()); //No I18N
											ZCCustomAction action = new ZCCustomAction(actionType, actionName, actionId);
											if(actionType.equals("row")) {
												recordCustomActions.add(action);
											} else if(actionType.equals("view")) {
												headerCustomActions.add(action);
											}
										}
										toReturn.addHeaderCustomActions(headerCustomActions);
										toReturn.addRecordCustomActions(recordCustomActions);
									} else if(viewChildNode.getNodeName().equals("Fields")) {
										// String fieldName, int type, String displayName, String delugeType, long compID
										List<ZCColumn> columns = new ArrayList<ZCColumn>();
										NodeList fieldList = viewChildNode.getChildNodes();
										for(int m=0; m<fieldList.getLength(); m++) {
											Node fieldNode = fieldList.item(m);
											NamedNodeMap fieldAttrMap = fieldNode.getAttributes();
											String displayName = getChildNodeValue(fieldNode, "DisplayName"); //fieldAttrMap.getNamedItem("DisplayName").getNodeValue(); //No I18N
											String fieldName = fieldAttrMap.getNamedItem("BaseLabelName").getNodeValue(); //No I18N
											String compType = fieldAttrMap.getNamedItem("ComponentType").getNodeValue(); //No I18N

											FieldType fieldType = FieldType.SINGLE_LINE;
											if(compType.equals("MULTI_SELECT")) {
												fieldType = FieldType.MULTISELECT;
											} else if(compType.equals("FILE_UPLOAD")) {
												fieldType = FieldType.FILE_UPLOAD;
											} else if(compType.equals("IMAGE")) {
												fieldType = FieldType.IMAGE;
											} else if(compType.equals("URL")){
												fieldType = FieldType.URL;
											}
											int seqNo  = Integer.parseInt(fieldAttrMap.getNamedItem("SequenceNumber").getNodeValue()); //No I18N
											ZCColumn column = new ZCColumn(fieldName, fieldType, displayName);
											column.setSequenceNumber(seqNo);
											columns.add(column);
										}
										Collections.sort(columns);
										toReturn.addColumns(columns);
									}
								}
							}
						}
					} else if(responseChildNode.getNodeName().equals("records")) {
						parseAndSetRecords(toReturn, responseChildNode);
					} else if(responseChildNode.getNodeName().equals("calendar")) {
						toReturn.setRecordsMonthYear(new ZCPair<Integer, Integer>(month, year));
						parseAndSetCalendarRecords(toReturn, responseChildNode);
					}
				}
			}
		}
		//printDocument(rootDocument);		
		return toReturn;		
	}

	private static String getChildNodeValue(Node parentNode, String nodeName) {
		NodeList parentNodeChildNodes = parentNode.getChildNodes();
//		if(nodeName.equals("title")) {
//			System.out.println("Parent node ---- " + getString(parentNode));
//		}
		for(int i=0; i<parentNodeChildNodes.getLength(); i++) {
			Node parentNodeChildNode = parentNodeChildNodes.item(i);
//			if(nodeName.equals("title")) {
//				System.out.println("node name " + parentNodeChildNode.getNodeName() + " ---- " + getString(parentNodeChildNode));
//			}
			if(parentNodeChildNode.getNodeName().equals(nodeName)) {
				Node firstItem = parentNodeChildNode.getChildNodes().item(0);
				if(firstItem != null) {
					return firstItem.getNodeValue();
				}
			}
		}
		return "";
	}


	static void parseAndCallFormEvents(Document rootDocument, ZCForm form) throws ZCException{
		List<String> alertMessages = new ArrayList<String>();
		NodeList nl = rootDocument.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			Node tasksNode = nl.item(i);
			if(tasksNode.getNodeName().equals("tasks")) {
				NodeList tasksNodes = tasksNode.getChildNodes();
				for(int j=0; j<tasksNodes.getLength(); j++) {
					Node taskNode = tasksNodes.item(j);
					if(taskNode.getNodeName().equals("task")) {
						NamedNodeMap attrMap = taskNode.getAttributes();
						Node typeNode = attrMap.getNamedItem("type");//No I18N
						String type = typeNode.getNodeValue();
						Node fieldNode = attrMap.getNamedItem("fieldname");//No I18N
						ZCField field = null;
						ZCRecordValue recordValue=null;
						if(fieldNode != null) {
							String fieldName = fieldNode.getNodeValue();
							field = form.getField(fieldName);
							field.setRebuildRequired(true);
							recordValue = field.getRecordValue();
						}
						Node formNode = attrMap.getNamedItem("formname");//No I18N
						String formName = null;
						if(formNode != null) {
							formName = formNode.getNodeValue();
						}
						NodeList taskNodes = taskNode.getChildNodes();
						String value = "";
						List<String> values = new ArrayList<String>();
						for(int k=0; k<taskNodes.getLength(); k++) {
							Node valueNode = taskNodes.item(k);
							if(valueNode.getNodeName().equals("value")) {
								value = getStringValue(valueNode, value);
								value = value.trim();
								if(value.contains("new makeList(\"")) {
									String[] valArr = value.split("\", \"");
									valArr[0] = valArr[0].substring("new makeList(\"".length());//No I18N
									valArr[(valArr.length)-1] = valArr[(valArr.length)-1].substring(0, valArr[(valArr.length)-1].length() - "\")".length());
									values = Arrays.asList(valArr);
								} else {
									List<ZCChoice> choices = field.getChoices();
									for(int l=0; l<choices.size(); l++) {
										if(choices.get(l).getKey().equals(value)) {
											value = choices.get(l).getValue();
										}
									}
									values.add(value);
								}
							}
						}
						if(type.equals("hide")) {
							//							System.out.println("inside hide");
							field.setHidden(true);
						} else if(type.equals("show")) {
							field.setHidden(false);
							//	System.out.println("inside h");
						} else if(type.equals("enable")) {
							field.setDisabled(false);
						} else if(type.equals("disable")) {
							field.setDisabled(true);
						} else if(type.equals("clear")) {
							field.clearChoices();
						} else if(type.equals("addvalue")) {
							List<ZCChoice> moreChoices = new ArrayList<ZCChoice>();
							for(int k=0; k<values.size(); k++) {
								ZCChoice choice = new ZCChoice(values.get(k), values.get(k));
								moreChoices.add(choice);
							}
							field.appendChoices(moreChoices);
						} else if(type.equals("select")) {
							//System.out.println("setva"+value+values);
							if(FieldType.isMultiChoiceField(field.getType())) {
								//System.out.println("seta"+value+values);
								recordValue.addToValues(values);
								//System.out.println("inside setvalues"+value+field.getRecordValue().getValues());
							} else {
								//System.out.println("sea"+value+values+" ff "+value+field.getRecordValue().getValue()+values.get(0));
								//value = getInitialValueForSingleChoiceField(value, zcField.getType(), zcField.getChoices());

								recordValue.setValue(values.get(0));
								//System.out.println("inside setvalues"+value+field.getRecordValue().getValue());
							}
						} 
						else if(type.equals("selectall")) {
							if(FieldType.isMultiChoiceField(field.getType())) {
								List<ZCChoice> choices = field.getChoices();
								values = new ArrayList<String>();
								for(int k=0; k<choices.size(); k++) {
									values.add(choices.get(k).getValue());
								}
								recordValue.setValues(values);
							} 
						} else if(type.equals("deselect")) {
							if(FieldType.isMultiChoiceField(field.getType())) {
								recordValue.removeFromValues(values);
							} else {
								recordValue.setValue(null);
							}
						} else if(type.equals("deselectall")) {
							if(FieldType.isMultiChoiceField(field.getType())) {
								recordValue.setValues(values);
							}
						} else if(type.equals("alert")) {
							alertMessages.add(value);
						} else if(type.equals("reloadform")) {
							form.setReLoadForm(true);
							break;
						} else if(type.equals("setvalue")) {
							//System.out.println("setva"+value+values);
							if(FieldType.isMultiChoiceField(field.getType())) {
								values = new ArrayList<String>();
								values.add(value);
								recordValue.addToValues(values);
								//System.out.println("seta"+value+values);
							} else {
								//System.out.println("sea"+value+values);
								recordValue.setValue(value);
							}
						}
						if(field != null && type.equals("setvalue") || type.equals("deselectall") || type.equals("deselect") || type.equals("selectall") || type.equals("select")) {
							if(field.isHasOnUserInputForFormula()) {
								field.onUserInputForFormula();
							}
							if(field.isHasOnUserInput()) {
								field.onUserInput();
							}
						}
					}
				}
			}
		}	
		if(alertMessages.size() >0) {
			form.setAlertMessages(alertMessages);
		}
	}

	static void parseForLicense(Document rootDocument) throws ZCException {
		NodeList nl = rootDocument.getChildNodes();
		for(int i=0; i<nl.getLength(); i++) {
			Node responseNode = nl.item(i);
			if(responseNode.getNodeName().equals("response")) { //No I18N
				NodeList responseNodes = responseNode.getChildNodes();
				for(int j=0; j<responseNodes.getLength(); j++) {
					Node resultNode = responseNodes.item(j);
					if(resultNode.getNodeName().equals("result")) { //No I18N
						NodeList resultNodes = resultNode.getChildNodes();
						for(int k=0; k<resultNodes.getLength(); k++) {
							Node resultNodeChild = resultNodes.item(k);
							if(resultNodeChild.getNodeName().equals("ENABLED")) { //No I18N
								if(!getBooleanValue(resultNodeChild, true)) {
									throw new ZCException("Mobile plan not enabled.", ZCException.LICENCE_ERROR); //No I18N
								}
							}
						}
					}
				}
			}
		}
	}

	private static void printDocument(Document doc) {
		try
		{
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			//System.out.println("Response " + writer.toString());
		}
		catch(TransformerException ex)
		{
			ex.printStackTrace();
		}

	}

	private static String getString(Node node) {
		try
		{
			DOMSource domSource = new DOMSource(node);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		}
		catch(TransformerException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}




}