package com.version_control_service.version_control_service.dto;


public class SopDto {
    private String id;
    private String title;
    private String description;
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
