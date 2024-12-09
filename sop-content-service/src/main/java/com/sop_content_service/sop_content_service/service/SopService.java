package com.sop_content_service.sop_content_service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sop_content_service.sop_content_service.dto.SopRequest;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Service
public class SopService {

    private static final Logger log = LoggerFactory.getLogger(SopService.class);

    private final AmazonS3 s3Client;
    private final SopRepository sopRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public SopService(AmazonS3 s3Client, SopRepository sopRepository) {
        this.s3Client = s3Client;
        this.sopRepository = sopRepository;

    }

    public SopModel uploadSop(
            List<MultipartFile> documents,
            MultipartFile coverImage,
            SopRequest sopRequest
    ) throws IOException {
        log.info("Starting SOP upload process.");

        // Validate input
        if (documents.isEmpty() || coverImage.isEmpty()) {
            log.error("Upload failed: Documents or cover image are missing.");
            throw new IllegalArgumentException("Documents and cover image are required.");
        }

        log.info("Uploading {} documents and 1 cover image.", documents.size());

        // Upload documents to S3 and retrieve their URLs
        List<String> documentUrls = documents.stream()
                .map(this::uploadFileToS3)
                .toList();

        log.info("Uploaded document URLs: {}", documentUrls);

        // Upload cover image to S3 and retrieve its URL
        String coverUrl = uploadFileToS3(coverImage);
        log.info("Uploaded cover image URL: {}", coverUrl);

        // Create and save SopModel
        SopModel sopModel = new SopModel();
        sopModel.setDocumentUrls(documentUrls);
        sopModel.setCoverUrl(coverUrl);
        sopModel.setTitle(sopRequest.getTitle());
        sopModel.setDescription(sopRequest.getDescription());
        sopModel.setBody(sopRequest.getBody());
        sopModel.setCategory(sopRequest.getCategory());
        sopModel.setVisibility(sopRequest.getVisibility());
        sopModel.setAuthors(sopRequest.getAuthors());
        sopModel.setReviewers(sopRequest.getReviewers());
        sopModel.setApprovers(sopRequest.getApprovers());
        sopModel.setCreatedAt(new Date());
        sopModel.setUpdatedAt(new Date());

        log.info("Final SopModel before save: {}", sopModel);

        SopModel savedSop = sopRepository.save(sopModel);
        log.info("SOP successfully saved to the database with ID: {}", savedSop.getId());

        return savedSop;
    }



    private String uploadFileToS3(MultipartFile file) {
        String fileName = "uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = new BufferedInputStream(file.getInputStream())) {
            log.info("Uploading file to S3: {}", fileName);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, null);
            putObjectRequest.getRequestClientOptions().setReadLimit(10 * 1024 * 1024); // 10 MB

            s3Client.putObject(putObjectRequest);
            String fileUrl = s3Client.getUrl(bucketName, fileName).toString();

            log.info("File successfully uploaded to S3. URL: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to S3: " + fileName, e);
        }
    }



}
