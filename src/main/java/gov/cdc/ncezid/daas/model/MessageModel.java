package gov.cdc.ncezid.daas.model;

public class MessageModel {
	
    private String messageGUID; //also used for MSH_ID
    private String transportGUID;
    private String bhsGUID;
    private String fhsGUID;
    private String batchPosition;
    private String status;
    private String sender; //MSH-4.2
    private String condition; //OBR-31.1
    private String messageCode;
    private String triggerEvent;
    private String messageStructure;
    private String profile;
    private String versionId;
    private String createdBy;
    private String createdTime;
    private String updatedBy;
    private String updatedTime;
    private String content;
    private String mD5;
	public String getMessageGUID() {
		return messageGUID;
	}
	public void setMessageGUID(String messageGUID) {
		this.messageGUID = messageGUID;
	}
	public String getTransportGUID() {
		return transportGUID;
	}
	public void setTransportGUID(String transportGUID) {
		this.transportGUID = transportGUID;
	}
	public String getBhsGUID() {
		return bhsGUID;
	}
	public void setBhsGUID(String bhsGUID) {
		this.bhsGUID = bhsGUID;
	}
	public String getFhsGUID() {
		return fhsGUID;
	}
	public void setFhsGUID(String fhsGUID) {
		this.fhsGUID = fhsGUID;
	}
	public String getBatchPosition() {
		return batchPosition;
	}
	public void setBatchPosition(String batchPosition) {
		this.batchPosition = batchPosition;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getMessageCode() {
		return messageCode;
	}
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}
	public String getTriggerEvent() {
		return triggerEvent;
	}
	public void setTriggerEvent(String triggerEvent) {
		this.triggerEvent = triggerEvent;
	}
	public String getMessageStructure() {
		return messageStructure;
	}
	public void setMessageStructure(String messageStructure) {
		this.messageStructure = messageStructure;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	public String getVersionId() {
		return versionId;
	}
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getUpdatedTime() {
		return updatedTime;
	}
	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getmD5() {
		return mD5;
	}
	public void setmD5(String mD5) {
		this.mD5 = mD5;
	}
    
    

}
