package com.user_management_service.user_management_service.enums;

public enum Role {
    ADMIN,
    HOD,
    Reviewer,
    USER;
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}