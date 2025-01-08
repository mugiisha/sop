package com.sop_content_service.sop_content_service.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sop_content_service.sop_content_service.dto.*;
import com.sop_content_service.sop_content_service.enums.ApprovalStatus;
import com.sop_content_service.sop_content_service.enums.SOPStatus;
import com.sop_content_service.sop_content_service.enums.Visibility;
import com.sop_content_service.sop_content_service.exception.BadInputRequest;
import com.sop_content_service.sop_content_service.exception.BadRequestException;
import com.sop_content_service.sop_content_service.exception.SopNotFoundException;
import com.sop_content_service.sop_content_service.exception.WorkflowServerException;
import com.sop_content_service.sop_content_service.model.Sop;
import com.sop_content_service.sop_content_service.repository.SopRepository;
import com.sop_content_service.sop_content_service.util.DtoConverter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sopVersionService.GetSopVersionsResponse;
import sopWorkflowService.GetWorkflowStageInfoResponse;
import sopWorkflowService.IsSOPApprovedResponse;
import userService.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SopService {

    private static final Logger log = LoggerFactory.getLogger(SopService.class);

    private final AmazonS3 s3Client;
    private final SopRepository sopRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WorkflowClientService workflowClientService;
    private final UserInfoClientService userInfoClientService;
    private final VersionClientService versionClientService;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    public SopService(AmazonS3 s3Client, SopRepository sopRepository, KafkaTemplate<String, Object> kafkaTemplate, WorkflowClientService workflowClientService, UserInfoClientService userInfoClientService, VersionClientService versionClientService) {
        this.s3Client = s3Client;
        this.sopRepository = sopRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.workflowClientService = workflowClientService;
        this.userInfoClientService = userInfoClientService;
        this.versionClientService = versionClientService;
    }

    public Sop addSopContent(String sopId,
                             List<MultipartFile> documents,
                             MultipartFile coverImage,
                             @Valid SopContentDto sopContentDto,
                             UUID authorId) throws IOException {

        Optional<Sop> sop = sopRepository.findById(sopId);
        if (sop.isEmpty()) {
            throw new SopNotFoundException("SOP with id " + sopId + " not found.");
        }


        Sop existingSop = sop.get();

        if(!existingSop.getAuthor().equals(authorId)){
            throw new BadRequestException("You are not authorized to update this SOP.");
        }

        existingSop.setDescription(sopContentDto.getDescription());
        existingSop.setBody(sopContentDto.getBody());
        existingSop.setStatus(sopContentDto.getStatus());

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

        if(SOPStatus.UNDER_REVIEWAL.equals(updatedSop.getStatus())){
            kafkaTemplate.send("sop-reviewal-ready", sopDto);
        }else{
            kafkaTemplate.send("sop-drafted", sopDto);
        }

        return updatedSop;
    }


    // public get sops getting
    public List<SOPResponseDto>  getSops(UUID departmentId){
        log.info("Getting SOPs with departmentId: {}", departmentId);

        List<Sop> sops = sopRepository.findByDepartmentIdOrVisibilityOrderByCreatedAtDesc(departmentId, Visibility.PUBLIC);

        List<SOPResponseDto> formattedSops = new ArrayList<>();

        for(Sop sop: sops){
            formattedSops.add(mapSOPToSOPResponseDto(sop));
        }

        return formattedSops;
    }

    //      @return List of SOPs for admin
    public List<SOPResponseDto> getAllSops() {
        log.info("Fetching all SOPs");
        List<Sop> sops =  sopRepository.findAllByOrderByCreatedAtDesc();

        List<SOPResponseDto> formattedSops = new ArrayList<>();

        for(Sop sop: sops){
            formattedSops.add(mapSOPToSOPResponseDto(sop));
        }

        return formattedSops;
    }


    public SOPResponseDto getSopById(String sopId) {
        log.info("Fetching SOP with ID: {}", sopId);
        Sop sop = sopRepository.findById(sopId)
                .orElseThrow(() -> new SopNotFoundException("SOP with id " + sopId + " not found."));

        return mapSOPToSOPResponseDto(sop);
    }

    public SOPResponseDto publishSop(String sopId) {
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

        // kafka transfer object to notify concerned users and control versioning
        PublishedSopDto publishedSopDto = mapSopTopublishedSopDto(updatedSop);
        kafkaTemplate.send("sop-published", publishedSopDto);

        return mapSOPToSOPResponseDto(updatedSop);
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



    @KafkaListener(topics = "sop-created")
    public void sopCreatedListener(String data) throws JsonProcessingException {
        log.info("Received sop initiated event: {}", data);

        // Convert JSON string to DTO
        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);

        // save the newly initiated sop to our database
        Sop sop = new Sop();
        sop.setId(sopDto.getId());
        sop.setTitle(sopDto.getTitle());
        sop.setVisibility(Visibility.valueOf(sopDto.getVisibility().toUpperCase()));
        sop.setCategory(sopDto.getCategory());
        sop.setDepartmentId(sopDto.getDepartmentId());
        sop.setAuthor(sopDto.getAuthorId());
        sop.setReviewers(sopDto.getReviewers());
        sop.setApprover(sopDto.getApproverId());
        sop.setCreatedAt(sopDto.getCreatedAt());
        sop.setUpdatedAt(sop.getUpdatedAt());
        sop.setStatus(sopDto.getStatus());

        // Save to repository
        sopRepository.save(sop);
        log.info("Saved SOP model: {}", sop);
    }

    @KafkaListener(topics = "sop-version-reverted")
    public void sopVersionRevertedListener(String data) throws JsonProcessingException {
        log.info("Received sop version reverted event: {}", data);

        // Convert JSON string to DTO
        PublishedSopDto sopDto = DtoConverter.publishedSopDtoFromJson(data);

        // save the newly initiated sop to our database
        Sop sop = sopRepository.findById(sopDto.getId()).orElse(null);
        if(sop != null){
            sop.setDocumentUrls(sopDto.getDocumentUrls());
            sop.setCoverUrl(sopDto.getCoverUrl());
            sop.setTitle(sopDto.getTitle());
            sop.setDescription(sopDto.getDescription());
            sop.setBody(sopDto.getBody());
            sop.setVisibility(sopDto.getVisibility());
            sop.setCategory(sopDto.getCategory());
            sop.setDepartmentId(sopDto.getDepartmentId());
            sop.setCreatedAt(sopDto.getCreatedAt());
            sopDto.setUpdatedAt(sopDto.getUpdatedAt());

            // Save to repository
            sopRepository.save(sop);
            log.info("reverted SOP: {}", sop);
        }



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

    public PublishedSopDto mapSopTopublishedSopDto(Sop sop){
        return PublishedSopDto
                .builder()
                .id(sop.getId())
                .documentUrls(sop.getDocumentUrls())
                .coverUrl(sop.getCoverUrl())
                .title(sop.getTitle())
                .description(sop.getDescription())
                .body(sop.getBody())
                .category(sop.getCategory())
                .departmentId(sop.getDepartmentId())
                .visibility(sop.getVisibility())
                .author(sop.getAuthor())
                .reviewers(sop.getReviewers())
                .approver(sop.getApprover())
                .createdAt(sop.getCreatedAt())
                .updatedAt(sop.getUpdatedAt())
                .build();
    }


    private StageDto createStageDto(UUID userId, String sopId) {
        if (userId == null) {
            return null;
        }
        getUserInfoResponse userInfo = userInfoClientService.getUserInfo(userId.toString());
        GetWorkflowStageInfoResponse stageInfo = workflowClientService.getWorkflowStage(userId.toString(), sopId);

        if(!userInfo.getSuccess()){
            log.error("Error fetching user info: {}", userInfo.getErrorMessage());
        }

        if(!stageInfo.getSuccess()){
            log.error("Error fetching stage info: {}", stageInfo.getErrorMessage());
        }

        StageDto stageDto = new StageDto();
        stageDto.setUserId(userId);
        stageDto.setName(userInfo.getName());
        stageDto.setProfilePictureUrl(userInfo.getProfilePictureUrl());
        stageDto.setStatus(ApprovalStatus.valueOf(stageInfo.getStatus()));

        stageDto.setComments(stageInfo.getCommentsList().stream()
                    .map(comment -> new CommentDto(
                            comment.getCommentId(),
                            comment.getComment(),
                            LocalDateTime.parse(comment.getCreatedAt())))
                    .collect(Collectors.toList()));
        return stageDto;
    }



    // map sop to an object including assigned users profiles
    public SOPResponseDto mapSOPToSOPResponseDto(Sop sop) {

        GetSopVersionsResponse versionsResponse = versionClientService.GetSopVersions(sop.getId());
        getDepartmentNameResponse departmentNameResponse = userInfoClientService.getDepartmentName(sop.getDepartmentId().toString());

        if(!versionsResponse.getSuccess()){
            log.error("Error fetching versions: {}", versionsResponse.getErrorMessage());
        }

        if(!departmentNameResponse.getSuccess()){
            log.error("Error fetching department name: {}", departmentNameResponse.getErrorMessage());
        }

        SOPResponseDto response = new SOPResponseDto();
        response.setId(sop.getId());
        response.setTitle(sop.getTitle());
        response.setStatus(sop.getStatus());
        response.setCategory(sop.getCategory());
        response.setBody(sop.getBody());
        response.setDepartmentId(sop.getDepartmentId());
        response.setDepartmentName(departmentNameResponse.getDepartmentName());
        response.setDocumentUrls(sop.getDocumentUrls());
        response.setCoverUrl(sop.getCoverUrl());
        response.setVisibility(sop.getVisibility());
        response.setDescription(sop.getDescription());
        response.setCreatedAt(sop.getCreatedAt());
        response.setVersions(versionsResponse.getVersionsList().stream()
                .map(version -> SopVersionDto
                        .builder()
                        .versionNumber(version.getVersionNumber())
                        .currentVersion(version.getCurrentVersion())
                        .build())
                .collect(Collectors.toList()));
        response.setUpdatedAt(sop.getUpdatedAt());

        List<StageDto> reviewers = new ArrayList<>();


        for (UUID reviewerId : sop.getReviewers()) {
            StageDto stageDto = createStageDto(reviewerId, sop.getId());
            reviewers.add(stageDto);
        }

        response.setApprover(createStageDto(sop.getApprover(), sop.getId()));
        response.setAuthor(createStageDto(sop.getAuthor(), sop.getId()));

        response.setReviewers(reviewers);

        return response;
    }

}

