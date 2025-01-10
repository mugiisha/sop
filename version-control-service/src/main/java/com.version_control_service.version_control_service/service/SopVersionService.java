package com.version_control_service.version_control_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.version_control_service.version_control_service.dto.PublishedSopDto;
import com.version_control_service.version_control_service.dto.SopVersionDto;
import com.version_control_service.version_control_service.model.SOP;
import com.version_control_service.version_control_service.model.Version;
import com.version_control_service.version_control_service.model.VersionContent;
import com.version_control_service.version_control_service.repository.SopRepository;
import com.version_control_service.version_control_service.repository.VersionContentRepository;
import com.version_control_service.version_control_service.repository.VersionRepository;
import com.version_control_service.version_control_service.utils.DtoConverter;
import com.version_control_service.version_control_service.utils.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SopVersionService {

    private final SopRepository sopRepository;
    private final VersionContentRepository versionContentRepository;
    private final VersionRepository versionRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Autowired
    public SopVersionService(SopRepository sopRepository,
                             VersionContentRepository versionContentRepository,
                             VersionRepository versionRepository,
                             KafkaTemplate<String, Object> kafkaTemplate) {
        this.sopRepository = sopRepository;
        this.versionContentRepository = versionContentRepository;
        this.versionRepository = versionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "sop-published")
    @Transactional
    @CacheEvict(value = "sop-versions", allEntries = true)
    public void updateSopVersion(String data) throws JsonProcessingException {

        PublishedSopDto publishedSopDto = DtoConverter.publishedSopDtoFromJson(data);

        // Update the version of the SOP
        SOP existingVersionedSop = sopRepository.findById(publishedSopDto.getId()).orElse(null);

        //save first version if it's a newly published SOP
        if (existingVersionedSop != null){
            Version version = versionRepository.findFirstBySopIdAndCurrentVersion(publishedSopDto.getId(), true);
            version.setCurrentVersion(false);
            versionRepository.save(version);

            float versionNumber = versionRepository
                    .findTopBySopIdOrderByVersionNumberDesc(publishedSopDto.getId())
                    .getVersionNumber();

            recordSopVersionContent(existingVersionedSop, publishedSopDto, versionNumber);
        }else{
            recordSopVersionContent(null, publishedSopDto, 0.0f);
        }

    }

    @Transactional
    @CacheEvict(value = "sop-versions", allEntries = true)
    public Version revertSopVersion(String sopId, Float versionNumber){
        Version currentVersion = versionRepository.findFirstBySopIdAndCurrentVersion(sopId,true);
        Version version = versionRepository.findFirstBySopIdAndVersionNumber(sopId,versionNumber);

        if(version == null){
            throw new NotFoundException("Can't revert to a not found version "+ versionNumber);
        }

        if(currentVersion == null){
            throw new NotFoundException("SOP current version not found");
        }

        if(version.getVersionNumber() == currentVersion.getVersionNumber()){
            throw new NotFoundException("Can't revert to the current version");
        }

        currentVersion.setCurrentVersion(false);
        version.setCurrentVersion(true);

        versionRepository.save(currentVersion);
        versionRepository.save(version);

        PublishedSopDto revertedSopDto = PublishedSopDto
                .builder()
                .id(sopId)
                .documentUrls(version.getContent().getDocumentUrls())
                .coverUrl(version.getContent().getCoverUrl())
                .title(version.getContent().getTitle())
                .description(version.getContent().getDescription())
                .category(version.getContent().getCategory())
                .departmentId(version.getContent().getDepartmentId())
                .body(version.getContent().getBody())
                .visibility(version.getContent().getVisibility())
                .createdAt(version.getContent().getCreatedAt())
                .updatedAt(version.getContent().getUpdatedAt())
                .build();

        kafkaTemplate.send("sop-version-reverted",revertedSopDto);

        return version;
    }

    @Cacheable(value = "sop-versions", key ="#id")
    public List<SopVersionDto> getSopVersions(String id) {
        List<Version> versions = versionRepository.findAllBySopId(id);
        List<SopVersionDto> sopVersionDtos = new ArrayList<>();
        for (Version version : versions) {
            SopVersionDto sopVersionDto = SopVersionDto
                    .builder()
                    .versionNumber(version.getVersionNumber())
                    .currentVersion(version.isCurrentVersion())
                    .createdAt(version.getCreatedAt())
                    .updatedAt(version.getUpdatedAt())
                    .build();

            sopVersionDtos.add(sopVersionDto);
        }
        return sopVersionDtos;
    }

    public List<Version> compareSopVersions(String sopId, Float firstVersion, Float secondVersion) {
        List<Float> versionNumbers = new ArrayList<>();
        versionNumbers.add(firstVersion);
        versionNumbers.add(secondVersion);
        return versionRepository.findAllBySopIdAndVersionNumberIn(sopId,versionNumbers);
    }

    public void recordSopVersionContent(SOP sop,
                                        PublishedSopDto publishedSopDto,
                                        Float versionNumber) {

        //List of version to use if there are no previous versions
        List<Version> initialVersions = new ArrayList<>();

        VersionContent content =  VersionContent.builder()
                .title(publishedSopDto.getTitle())
                .description(publishedSopDto.getDescription())
                .body(publishedSopDto.getBody())
                .category(publishedSopDto.getCategory())
                .departmentId(publishedSopDto.getDepartmentId())
                .coverUrl(publishedSopDto.getCoverUrl())
                .visibility(publishedSopDto.getVisibility())
                .documentUrls(publishedSopDto.getDocumentUrls())
                .createdAt(publishedSopDto.getCreatedAt())
                .updatedAt(publishedSopDto.getUpdatedAt())
                .build();

        VersionContent savedContent = versionContentRepository.save(content);


        Version newVersion = Version.builder()
                .sopId(publishedSopDto.getId())
                .versionNumber(versionNumber + 1.0f)
                .currentVersion(true)
                .content(savedContent)
                .createdAt(publishedSopDto.getCreatedAt())
                .build();

        Version savedVersion = versionRepository.save(newVersion);

        if(sop == null){
            initialVersions.add(savedVersion);

            sop =  SOP.builder()
                    .id(publishedSopDto.getId())
                    .versions(initialVersions)
                    .build();

            sopRepository.save(sop);
            return;
        }

        List<Version> versions = sop.getVersions();
        versions.add(savedVersion);

        sopRepository.save(sop);
    }


}
