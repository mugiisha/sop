package com.analytics_insights_service.analytics_insights_service.service;

import com.analytics_insights_service.analytics_insights_service.dto.SOPDto;
import com.analytics_insights_service.analytics_insights_service.model.SopReads;
import com.analytics_insights_service.analytics_insights_service.repository.SopReadsRepository;
import com.analytics_insights_service.analytics_insights_service.util.DtoConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SopReadsServiceTest {

    @Mock
    private SopReadsRepository sopReadsRepository;

    @Spy
    private CacheManager cacheManager = new ConcurrentMapCacheManager("sop-reads");

    @InjectMocks
    private SopReadsService sopReadsService;

    private static final String TEST_SOP_ID = "test-sop-id";
    private static final String TEST_JSON = "{\"id\":\"" + TEST_SOP_ID + "\",\"title\":\"Test SOP\"}";

    @BeforeEach
    void setUp() {
        ((ConcurrentMapCacheManager) cacheManager).setCacheNames(java.util.Collections.singletonList("sop-reads"));
    }

    @Test
    void getSopReads_WhenCached_ShouldReturnFromCache() {
        // Given
        SopReads sopReads = SopReads.builder()
                .sopId(TEST_SOP_ID)
                .reads(5)
                .build();

        // First call to actually fetch from repository
        when(sopReadsRepository.findById(TEST_SOP_ID)).thenReturn(Optional.of(sopReads));
        SopReads firstCall = sopReadsService.getSopReads(TEST_SOP_ID);

        // When - Second call should be from cache
        SopReads secondCall = sopReadsService.getSopReads(TEST_SOP_ID);

        // Then
        assertEquals(firstCall, secondCall);
        verify(sopReadsRepository, times(1)).findById(TEST_SOP_ID); // Repository should only be called once
    }

    @Test
    void getSopReads_WhenNotFound_ShouldReturnNull() {
        // Given
        when(sopReadsRepository.findById(TEST_SOP_ID)).thenReturn(Optional.empty());

        // When
        SopReads result = sopReadsService.getSopReads(TEST_SOP_ID);

        // Then
        assertNull(result);
        verify(sopReadsRepository).findById(TEST_SOP_ID);
    }

    @Test
    void sopViewedListener_WhenSopExists_ShouldIncrementReads() throws JsonProcessingException {
        // Given
        SopReads existingSopReads = SopReads.builder()
                .sopId(TEST_SOP_ID)
                .reads(5)
                .build();
        when(sopReadsRepository.findById(TEST_SOP_ID)).thenReturn(Optional.of(existingSopReads));
        when(sopReadsRepository.save(any(SopReads.class))).thenReturn(existingSopReads);

        // When
        sopReadsService.sopViewedListener(TEST_JSON);

        // Then
        verify(sopReadsRepository).save(argThat(sopReads ->
                sopReads.getSopId().equals(TEST_SOP_ID) &&
                        sopReads.getReads() == 6
        ));

        // Verify cache was evicted
        assertNull(cacheManager.getCache("sop-reads").get(TEST_SOP_ID));
    }

    @Test
    void sopViewedListener_WhenSopDoesNotExist_ShouldCreateNewEntry() throws JsonProcessingException {
        // Given
        when(sopReadsRepository.findById(TEST_SOP_ID)).thenReturn(Optional.empty());
        when(sopReadsRepository.save(any(SopReads.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        sopReadsService.sopViewedListener(TEST_JSON);

        // Then
        verify(sopReadsRepository).save(argThat(sopReads ->
                sopReads.getSopId().equals(TEST_SOP_ID) &&
                        sopReads.getReads() == 1
        ));
    }

    @Test
    void sopCreatedListener_ShouldCreateNewEntry() throws JsonProcessingException {
        // Given
        when(sopReadsRepository.save(any(SopReads.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        sopReadsService.sopCreatedListener(TEST_JSON);

        // Then
        verify(sopReadsRepository).save(argThat(sopReads ->
                sopReads.getSopId().equals(TEST_SOP_ID) &&
                        sopReads.getReads() == 0
        ));

        // Verify cache was evicted
        assertNull(cacheManager.getCache("sop-reads").get(TEST_SOP_ID));
    }

    @Test
    void sopViewedListener_WithInvalidJson_ShouldHandleException() {
        // Given
        String invalidJson = "invalid-json";

        // When/Then
        assertThrows(JsonProcessingException.class, () ->
                sopReadsService.sopViewedListener(invalidJson)
        );
    }

    @Test
    void sopCreatedListener_WithInvalidJson_ShouldHandleException() {
        // Given
        String invalidJson = "invalid-json";

        // When/Then
        assertThrows(JsonProcessingException.class, () ->
                sopReadsService.sopCreatedListener(invalidJson)
        );
    }

    @Test
    void cacheEviction_ShouldWork() {
        // Given
        SopReads sopReads = SopReads.builder()
                .sopId(TEST_SOP_ID)
                .reads(5)
                .build();
        when(sopReadsRepository.findById(TEST_SOP_ID)).thenReturn(Optional.of(sopReads));

        // When - First call to cache the value
        sopReadsService.getSopReads(TEST_SOP_ID);

        // Then - Verify value is in cache
        assertNotNull(cacheManager.getCache("sop-reads").get(TEST_SOP_ID));

        // When - Trigger cache eviction through Kafka listener
        try {
            sopReadsService.sopCreatedListener(TEST_JSON);
        } catch (JsonProcessingException e) {
            fail("Should not throw exception");
        }

        // Then - Verify cache was evicted
        assertNull(cacheManager.getCache("sop-reads").get(TEST_SOP_ID));
    }
}