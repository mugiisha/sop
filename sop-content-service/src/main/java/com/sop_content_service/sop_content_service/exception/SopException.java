package com.sop_content_service.sop_content_service.exception;

// Global custom exceptions for SOP-related operations
public class SopException {

    // Custom exception for SOP not found
    public static class SopNotFoundException extends RuntimeException {
        public SopNotFoundException(String message) {
            super(message); // Pass the message to the superclass
        }
    }

    // Custom exception for Image upload failure
    public static class ImageUploadException extends RuntimeException {
        public ImageUploadException(String message) {
            super(message); // Pass the message to the superclass
        }
    }

    // Custom exception for Document upload failure
    public static class DocumentUploadException extends RuntimeException {
        public DocumentUploadException(String message) {
            super(message); // Pass the message to the superclass
        }
    }
}
