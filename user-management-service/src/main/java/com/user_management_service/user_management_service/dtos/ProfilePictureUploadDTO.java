package com.user_management_service.user_management_service.dtos;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfilePictureUploadDTO {
    private MultipartFile file;
}