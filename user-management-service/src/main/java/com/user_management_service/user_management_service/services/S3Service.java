// S3Service.java
package com.user_management_service.user_management_service.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        PutObjectRequest request = new PutObjectRequest(
                bucketName,
                key,
                file.getInputStream(),
                metadata
        );

        s3Client.putObject(request);
        return s3Client.getUrl(bucketName, key).toString();
    }

    public void deleteFile(String fileUrl) {
        String key = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
        s3Client.deleteObject(bucketName, key);
    }
}