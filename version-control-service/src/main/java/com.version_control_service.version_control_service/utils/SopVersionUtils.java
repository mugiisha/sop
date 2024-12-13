//package com.version_control_service.version_control_service.utils;
//
//import com.version_control_service.version_control_service.model.SopVersionModel;
//
//import java.util.Optional;
//
//public class SopVersionUtils {
//
//    public static void setSopId(SopVersionModel existingSop, SopVersionModel updatedSop) {
//        if (updatedSop.getSopId() != null) {
//            existingSop.setSopId(updatedSop.getSopId());
//        }
//    }
//
//    // Validate required fields
//    public static void validateRequiredFields(SopVersionModel newVersionDetails) {
//        if (newVersionDetails.getTitle() == null || newVersionDetails.getTitle().trim().isEmpty()) {
//            throw new IllegalArgumentException("Title cannot be blank");
//        }
//
//        if (newVersionDetails.getDescription() == null || newVersionDetails.getDescription().trim().isEmpty()) {
//            throw new IllegalArgumentException("Description cannot be blank");
//        }
//
//        if (newVersionDetails.getVersionNumber() == null || newVersionDetails.getVersionNumber().trim().isEmpty()) {
//            throw new IllegalArgumentException("Version number cannot be blank");
//        }
//    }
//
//    // Populate default values
//    public static void populateDefaultValues(SopVersionModel newVersionDetails) {
//        newVersionDetails.setCreatedBy(Optional.ofNullable(newVersionDetails.getCreatedBy()).orElse("HOD"));
//        newVersionDetails.setCode(Optional.ofNullable(newVersionDetails.getCode()).orElse("ABC123"));
//        newVersionDetails.setVisibility(Optional.ofNullable(newVersionDetails.getVisibility()).orElse("Public"));
//        newVersionDetails.setCategory(Optional.ofNullable(newVersionDetails.getCategory()).orElse("General"));
//    }
//
//}
