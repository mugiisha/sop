package com.sop_content_service.sop_content_service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sop_content_service.sop_content_service.dto.SOPDto;
import com.sop_content_service.sop_content_service.dto.SopContentDto;
import com.sop_content_service.sop_content_service.enums.SOPStatus;
import com.sop_content_service.sop_content_service.enums.Visibility;
import com.sop_content_service.sop_content_service.exception.BadInputRequest;
import com.sop_content_service.sop_content_service.exception.SopNotFoundException;
import com.sop_content_service.sop_content_service.exception.WorkflowServerException;
import com.sop_content_service.sop_content_service.model.Sop;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import com.sop_content_service.sop_content_service.util.DtoConverter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sopWorkflowService.IsSOPApprovedResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SopService {

    private static final Logger log = LoggerFactory.getLogger(SopService.class);

    private final AmazonS3 s3Client;
    private final SopRepository sopRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WorkflowClientService workflowClientService;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public SopService(AmazonS3 s3Client, SopRepository sopRepository, KafkaTemplate<String, Object> kafkaTemplate, WorkflowClientService workflowClientService) {
        this.s3Client = s3Client;
        this.sopRepository = sopRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.workflowClientService = workflowClientService;
    }

    public Sop addSopContent(String sopId,
                             List<MultipartFile> documents,
                             MultipartFile coverImage,
                             @Valid SopContentDto sopContentDto) throws IOException {

        Optional<Sop> sop = sopRepository.findById(sopId);
        if (sop.isEmpty()) {
            throw new SopNotFoundException("SOP with id " + sopId + " not found.");
        }


        Sop existingSop = sop.get();

        existingSop.setDescription(sopContentDto.getDescription());
        existingSop.setBody(sopContentDto.getBody());

        // Check cover image existence
        if (existingSop.getCoverUrl()== null && (coverImage == null || coverImage.isEmpty())) {
            throw new BadInputRequest("Cover image is required.");
        }

        if(coverImage != null && !coverImage.isEmpty()){
            String coverUrl = uploadFileToS3(coverImage);
            existingSop.setCoverUrl(coverUrl);
        }


        // Handle document upload
        if (documents != null && !documents.isEmpty()) {
            log.info("Uploading {} documents.", documents.size());
            // Upload documents to S3 and retrieve their URLs
            List<String> documentUrls = documents.stream()
                    .map(this::uploadFileToS3)
                    .toList();
            existingSop.setDocumentUrls(documentUrls);
            log.info("Uploaded document URLs: {}", documentUrls);
        }

        existingSop.setUpdatedAt(new Date());
        existingSop.setStatus(sopContentDto.getStatus());

        Sop updatedSop = sopRepository.save(existingSop);

        // prepare kafka transfer object to notify concerned users
        SOPDto sopDto = mapSOPToSOPDto(updatedSop);

        if(SOPStatus.REVIEWAL.equals(updatedSop.getStatus())){
            kafkaTemplate.send("sop-reviewal-ready", sopDto);
        }else{
            kafkaTemplate.send("sop-drafted", sopDto);
        }

        return updatedSop;
    }


    public Sop publishSop(String sopId) {
        Optional<Sop> sop = sopRepository.findById(sopId);
        if (sop.isEmpty()) {
            throw new SopNotFoundException("SOP with id " + sopId + " not found.");
        }

        Sop existingSop = sop.get();

        // Check if SOP is approved
        IsSOPApprovedResponse response = workflowClientService.isSOPApproved(sopId);
        if(!response.getSuccess()){
            log.info("Failed to check if SOP is approved. {}", response.getErrorMessage());
            throw new WorkflowServerException("Failed to check if SOP is approved. Please try again.");
        }

        if(!response.getSOPApproved()){
            throw new BadInputRequest("SOP is not approved yet.");
        }

        existingSop.setStatus(SOPStatus.PUBLISHED);
        existingSop.setUpdatedAt(new Date());

        Sop updatedSop = sopRepository.save(existingSop);

        // prepare kafka transfer object to notify concerned users
        SOPDto sopDto = mapSOPToSOPDto(updatedSop);
        kafkaTemplate.send("sop-published", sopDto);

        return updatedSop;
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


//      @return List of SOPs

    public List<Sop> getSops(UUID departmentId) {
        log.info("Fetching all SOPs.-USER");
        return sopRepository.findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(departmentId, Visibility.PUBLIC);
    }

    //      @return List of SOPs for admin

    public List<Sop> getAllSops() {
        log.info("Fetching all SOPs.-ADMIN");
        return sopRepository.findAllByOrderByCreatedAtDesc();
    }


    //    @throws SopNotFoundException if the SOP is not found
    public Sop getSopById(String sopId) {
        log.info("Fetching SOP with ID: {}", sopId);
        return sopRepository.findById(sopId)
                .orElseThrow(() -> new SopNotFoundException("SOP with id " + sopId + " not found."));
    }


    @KafkaListener(topics = "sop-created")
    public void sopCreatedListener(String data) throws JsonProcessingException {
        log.info("Received sop initiated event: {}", data);

        // Convert JSON string to DTO
        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);

        // save the newly initiated sop to our database
        Sop sop = new Sop();
        sop.setId(sopDto.getId());
        sop.setTitle(sopDto.getTitle());
        sop.setVisibility(sopDto.getVisibility());
        sop.setCategory(sopDto.getCategory());
        sop.setDepartmentId(sopDto.getDepartmentId());
        sop.setAuthor(sopDto.getAuthorId());
        sop.setReviewers(sopDto.getReviewers());
        sop.setApprover(sopDto.getApproverId());
        sop.setStatus(sopDto.getStatus());

        // Save to repository
        sopRepository.save(sop);
        log.info("Saved SOP model: {}", sop);
    }

    @KafkaListener(topics = "sop-deleted")
    public void sopDeletedListener(String data) throws JsonProcessingException{
        log.info("Received sop deleted event: {}", data);
        // Convert JSON string to DTO
        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
        // delete the sop from our database
        sopRepository.deleteById(sopDto.getId());
        log.info("Deleted SOP model: {}", sopDto);
    }


    // maps sop model to sop dto to prepare kafka communication objects
    public SOPDto mapSOPToSOPDto(Sop sop) {
        SOPDto sopDto = new SOPDto();
        sopDto.setId(sop.getId());
        sopDto.setTitle(sop.getTitle());
        sopDto.setCategory(sop.getCategory());
        sopDto.setDepartmentId(sop.getDepartmentId());
        sopDto.setAuthorId(sop.getAuthor());
        sopDto.setReviewers(sop.getReviewers());
        sopDto.setApproverId(sop.getApprover());
        sopDto.setStatus(sop.getStatus());
        return sopDto;
    }
}

