package com.user_management_service.user_management_service.enums;

public enum ErrorCode {
    DEPARTMENT_NOT_FOUND("DEP_001", "Department not found"),
    DEPARTMENT_NAME_EXISTS("DEP_002", "Department name already exists"),
    DEPARTMENT_HAS_USERS("DEP_003", "Department cannot be deleted as it contains active users"),
    DEPARTMENT_INACTIVE("DEP_004", "Department is inactive"),
    DEPARTMENT_ALREADY_ACTIVE("DEP_005", "Department is already active"),
    VALIDATION_ERROR("VAL_001", "Validation error occurred"),
    DATA_INTEGRITY_ERROR("DAT_001", "Data integrity violation occurred"),
    SYSTEM_ERROR("SYS_001", "Internal system error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}