//package com.version_control_service.version_control_service.model;
//
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;
//import org.bson.types.ObjectId;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//import jakarta.validation.constraints.*;
//import org.springframework.data.mongodb.core.mapping.Field;
//import org.hibernate.validator.constraints.URL;
//
//import java.util.Date;
//
//@Document(collection = "sop_versions")
//@NoArgsConstructor
//@AllArgsConstructor
//public class SopVersionModel {
//    @Id
//    private String id;
//
//    @NotNull(message = "SOP ID cannot be null")
//    @Field("sopId")
//    private ObjectId sopId;
//
//    @NotBlank(message = "Title cannot be blank")
//    @Size(max = 100, message = "Title must not exceed 100 characters")
//    private String title;
//
//    @Size(max = 2000, message = "Description must not exceed 2000 characters")
//    private String description;
//
//    private String versionNumber;
//    private String createdBy;
//    private String code;
//    private String visibility;
//    private String category;
//    private String imageFile;
//    private String documentFile;
//
//    @CreatedDate
//    @Field("createdAt")
//    private Date createdAt;
//
//    private Date lastModifiedAt;
//    private String lastModifiedBy;
//    private String approvalStatus;
//    private String approvedBy;
//    private Date approvedAt;
//    private String comments;
//    private String department;
//    private String businessUnit;
//    private String processOwner;
//    private Date effectiveDate;
////    private Date expiryDate;
//    private String revisionHistory;
//    private String previousVersionId;
//    private String status;
//
//    // Getters
//    public String getId() { return id; }
//    public ObjectId getSopId() { return sopId; }
//    public String getTitle() { return title; }
//    public String getDescription() { return description; }
//    public String getVersionNumber() { return versionNumber; }
//    public String getCreatedBy() { return createdBy; }
//    public String getCode() { return code; }
//    public String getVisibility() { return visibility; }
//    public String getCategory() { return category; }
//    public String getImageFile() { return imageFile; }
//    public String getDocumentFile() { return documentFile; }
//    public Date getCreatedAt() { return createdAt; }
//    public Date getLastModifiedAt() { return lastModifiedAt; }
//    public String getLastModifiedBy() { return lastModifiedBy; }
//    public String getApprovalStatus() { return approvalStatus; }
//    public String getApprovedBy() { return approvedBy; }
//    public Date getApprovedAt() { return approvedAt; }
//    public String getComments() { return comments; }
//    public String getDepartment() { return department; }
//    public String getBusinessUnit() { return businessUnit; }
//    public String getProcessOwner() { return processOwner; }
//    public Date getEffectiveDate() { return effectiveDate; }
//    public Date getExpiryDate() { return expiryDate; }
//    public String getRevisionHistory() { return revisionHistory; }
//    public String getPreviousVersionId() { return previousVersionId; }
//    public String getStatus() { return status; }
//
//    // Setters
//    public void setId(String id) { this.id = id; }
//    public void setSopId(ObjectId sopId) { this.sopId = sopId; }
//    public void setTitle(String title) { this.title = title; }
//    public void setDescription(String description) { this.description = description; }
//    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }
//    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
//    public void setCode(String code) { this.code = code; }
//    public void setVisibility(String visibility) { this.visibility = visibility; }
//    public void setCategory(String category) { this.category = category; }
//    public void setImageFile(String imageFile) { this.imageFile = imageFile; }
//    public void setDocumentFile(String documentFile) { this.documentFile = documentFile; }
//    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
//    public void setLastModifiedAt(Date lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
//    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
//    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
//    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
//    public void setApprovedAt(Date approvedAt) { this.approvedAt = approvedAt; }
//    public void setComments(String comments) { this.comments = comments; }
//    public void setDepartment(String department) { this.department = department; }
//    public void setBusinessUnit(String businessUnit) { this.businessUnit = businessUnit; }
//    public void setProcessOwner(String processOwner) { this.processOwner = processOwner; }
//    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
//    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
//    public void setRevisionHistory(String revisionHistory) { this.revisionHistory = revisionHistory; }
//    public void setPreviousVersionId(String previousVersionId) { this.previousVersionId = previousVersionId; }
//    public void setStatus(String status) { this.status = status; }
//
//    // Utility methods
//    public void updateModificationInfo(String modifiedBy) {
//        this.lastModifiedAt = new Date();
//        this.lastModifiedBy = modifiedBy;
//    }
//
//    public void updateApprovalInfo(String approvedBy, String approvalStatus) {
//        this.approvedBy = approvedBy;
//        this.approvalStatus = approvalStatus;
//        this.approvedAt = new Date();
//    }
//
//    @AssertTrue(message = "Expiry date must be after effective date")
//    private boolean isExpiryDateValid() {
//        if (effectiveDate == null || expiryDate == null) {
//            return true;
//        }
//        return expiryDate.after(effectiveDate);
//    }
//
//    public boolean needsReview() {
//        if (expiryDate == null) {
//            return false;
//        }
//        Date now = new Date();
//        return now.after(new Date(expiryDate.getTime() - 30L * 24 * 60 * 60 * 1000));
//    }
//}