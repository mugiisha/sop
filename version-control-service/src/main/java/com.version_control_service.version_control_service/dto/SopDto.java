package com.version_control_service.version_control_service.dto;


public class SopDto {
    private String id;
    private String title;
    private String description;

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String status;
    private String version;
    private String imageUrl;
    private String documentUrl;

    public SopDto(String id, String title, String description, String status, String version, String imageUrl, String documentUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.version = version;
        this.imageUrl = imageUrl;
        this.documentUrl = documentUrl;
    }

}
