package com.sop_content_service.sop_content_service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sop_content_service.sop_content_service.dto.SOPDto;
import com.sop_content_service.sop_content_service.dto.SopRequest;
import com.sop_content_service.sop_content_service.exception.SopNotFoundException;
import com.sop_content_service.sop_content_service.model.SopModel;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import com.sop_content_service.sop_content_service.util.DtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public SopModel createSop(String sopId, List<MultipartFile> documents, MultipartFile coverImage, SopRequest sopRequest) throws IOException {

        Optional<SopModel> optionalSop = sopRepository.findById(sopId);
        if (optionalSop.isEmpty()) {
            throw new SopNotFoundException("SOP with id " + sopId + " not found.");
        }


        SopModel existingSop = optionalSop.get();

        // Handle document upload first
        if (documents != null && !documents.isEmpty()) {
            log.info("Uploading {} documents.", documents.size());
            // Upload documents to S3 and retrieve their URLs
            List<String> documentUrls = documents.stream()
                    .map(this::uploadFileToS3)
                    .toList();
            existingSop.setDocumentUrls(documentUrls);
            log.info("Uploaded document URLs: {}", documentUrls);
        }

        // Handle cover image upload
        if (coverImage != null && !coverImage.isEmpty()) {
            log.info("Uploading cover image.");
            // Upload cover image to S3 and retrieve its URL
            String coverUrl = uploadFileToS3(coverImage);
            existingSop.setCoverUrl(coverUrl);
            log.info("Uploaded cover image URL: {}", coverUrl);
        }

        // Now update the SOP fields based on SopRequest, but only if they are not null and the current field is null
        if (sopRequest.getTitle() != null && existingSop.getTitle() == null) {
            existingSop.setTitle(sopRequest.getTitle());
        }
        if (sopRequest.getDescription() != null && existingSop.getDescription() == null) {
            existingSop.setDescription(sopRequest.getDescription());
        }
        if (sopRequest.getBody() != null && existingSop.getBody() == null) {
            existingSop.setBody(sopRequest.getBody());
        }
        if (sopRequest.getCategory() != null && existingSop.getCategoryId() == null) {
            existingSop.setCategoryId(sopRequest.getCategory());
        }
        if (sopRequest.getVisibility() != null && existingSop.getVisibility() == null) {
            existingSop.setVisibility(sopRequest.getVisibility());
        }
        if (sopRequest.getAuthors() != null && existingSop.getAuthors() == null) {
            existingSop.setAuthors(sopRequest.getAuthors());
        }
        if (sopRequest.getReviewers() != null && existingSop.getReviewers() == null) {
            existingSop.setReviewers(sopRequest.getReviewers());
        }
        if (sopRequest.getApprovers() != null && existingSop.getApprovers() == null) {
            existingSop.setApprovers(sopRequest.getApprovers());
        }
        // Update the timestamp
        existingSop.setUpdatedAt(new Date());
        // Save the updated SOP
        return sopRepository.save(existingSop);
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

    @KafkaListener(
            topics = "sop-created",
            groupId = "sop-content-service"
    )
    public void sopCreatedListener(String data) throws JsonProcessingException {
        log.info("Received sop created event: {}", data);

        // Convert JSON string to DTO
        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
        // Map DTO to entity
        SopModel sopModel = new SopModel();
        sopModel.setId(sopDto.getId());
        sopModel.setTitle(sopDto.getTitle());
        sopModel.setVisibility(sopDto.getVisibility());
        sopModel.setCategoryId(sopDto.getCategoryId());
        sopModel.setAuthors(List.of(String.valueOf(sopDto.getAuthorId()))); // Assuming a single author maps to authors list
        sopModel.setReviewers(List.of(String.valueOf(sopDto.getReviewers())));
        sopModel.setApprovers(List.of(String.valueOf(sopDto.getApproverId()))); // Assuming a single approver maps to approvers list

        // Save to repository
        sopRepository.save(sopModel);
        log.info("Saved SOP model: {}", sopModel);
    }

//      @return List of SOPs

    public List<SOPDto> getAllSops() {
        log.info("Fetching all SOPs.");
        List<SopModel> sopModels = sopRepository.findAll();
        return sopModels.stream()
                .map(DtoConverter::sopDtoFromEntity)
                .collect(Collectors.toList());
    }

//    @throws SopNotFoundException if the SOP is not found
    public SOPDto getSopById(String sopId) {
        log.info("Fetching SOP with ID: {}", sopId);
        return sopRepository.findById(sopId)
                .map(DtoConverter::sopDtoFromEntity)
                .orElseThrow(() -> new SopNotFoundException("SOP with id " + sopId + " not found."));
    }





}

