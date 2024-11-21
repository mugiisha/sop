package com.user_management_service.user_management_service.services;

import com.user_management_service.user_management_service.dtos.PasswordResetWithOtpDTO;
import com.user_management_service.user_management_service.validation.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, PasswordResetWithOtpDTO> {

    @Override
    public boolean isValid(PasswordResetWithOtpDTO dto, ConstraintValidatorContext context) {
        return dto.getNewPassword() != null && dto.getNewPassword().equals(dto.getConfirmPassword());
    }
}