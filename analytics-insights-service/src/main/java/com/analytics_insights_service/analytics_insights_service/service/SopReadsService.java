package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.SOPDto;
import com.analytics_insights_service.analytics_insights_service.exception.NotFoundException;
import com.analytics_insights_service.analytics_insights_service.model.SopReads;
import com.analytics_insights_service.analytics_insights_service.repository.SopReadsRepository;
import com.analytics_insights_service.analytics_insights_service.util.DtoConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SopReadsService {

    private final SopReadsRepository sopReadsRepository;

    @Autowired
    public SopReadsService(SopReadsRepository sopReadsRepository) {
        this.sopReadsRepository = sopReadsRepository;
    }

    @Cacheable(value = "sop-reads", key = "#sopId")
    public SopReads getSopReads(String sopId) {
        log.info("Getting sop reads for sopId: {}", sopId);
        return sopReadsRepository.findById(sopId).orElse(null);
    }

    @KafkaListener(topics = "sop-read")
    @CacheEvict(value = "sop-reads", allEntries = true)
    public void sopViewedListener(String data) throws JsonProcessingException {
        log.info("Received sop read event: {}", data);

        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);
        SopReads sopReads = sopReadsRepository.findById(sopDto.getId()).orElse(null);

        if(sopReads == null) {
            SopReads newSopReads =SopReads.builder()
                    .sopId(sopDto.getId())
                    .reads(0)
                    .build();

            sopReadsRepository.save(newSopReads);
            sopReads = newSopReads;
        }

        sopReads.setReads(sopReads.getReads() + 1);

        sopReadsRepository.save(sopReads);
    }

    @KafkaListener(topics = "sop-created")
    @CacheEvict(value = "sop-reads", allEntries = true)
    public void sopCreatedListener(String data) throws JsonProcessingException {
        log.info("Received sop created event: {}", data);

        SOPDto sopDto = DtoConverter.sopDtoFromJson(data);

        SopReads sopReads =SopReads.builder()
                .sopId(sopDto.getId())
                .reads(0)
                .build();

        sopReadsRepository.save(sopReads);
    }
}
