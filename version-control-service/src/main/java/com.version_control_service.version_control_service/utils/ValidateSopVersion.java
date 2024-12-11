//package com.version_control_service.version_control_service.utils;
//
//import com.version_control_service.version_control_service.model.SopVersionModel;
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.Validation;
//import jakarta.validation.Validator;
//import jakarta.validation.ValidatorFactory;
//
//import java.util.Set;
//
//public class ValidateSopVersion {
//
//    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//    private static final Validator validator = factory.getValidator();
//
//    public static void validateSopVersion(SopVersionModel sopVersion) {
//        // Validate the SopVersionModel
//        Set<ConstraintViolation<SopVersionModel>> violations = validator.validate(sopVersion);
//
//        // If there are violations, throw an exception
//        if (!violations.isEmpty()) {
//            StringBuilder errorMessages = new StringBuilder();
//            for (ConstraintViolation<SopVersionModel> violation : violations) {
//                errorMessages.append(violation.getPropertyPath())
//                        .append(": ")
//                        .append(violation.getMessage())
//                        .append("; ");
//            }
//            throw new IllegalArgumentException("Validation failed: " + errorMessages.toString());
//        }
//    }
//}
