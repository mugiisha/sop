package com.sop_content_service.sop_content_service.service;

import com.sop_content_service.sop_content_service.dto.SearchMetadata;
import com.sop_content_service.sop_content_service.dto.SopSearchRequest;
import com.sop_content_service.sop_content_service.dto.SopSearchResponse;
import com.sop_content_service.sop_content_service.exception.InvalidSearchParameterException;
import com.sop_content_service.sop_content_service.exception.SopNotFoundException;
import com.sop_content_service.sop_content_service.model.Sop;
import com.sop_content_service.sop_content_service.strategy.SearchContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SopSearchServiceTest {

    @Mock
    private SearchContext searchContext;

    private SopSearchService sopSearchService;

    @BeforeEach
    void setUp() {
        sopSearchService = new SopSearchService(searchContext);
    }

    @Test
    void searchSOPs_ValidRequest_ReturnsResults() throws Exception {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();
        request.setKeyword("test");
        request.setDepartment("IT");

        List<Sop> sops = new ArrayList<>();
        sops.add(new Sop()); // Add sample SOP data
        Page<Sop> sopPage = new PageImpl<>(sops);

        when(searchContext.executeSearch(any(SopSearchRequest.class), any(Pageable.class)))
                .thenReturn(sopPage);

        // Act
        ResponseEntity<SopSearchResponse<Page<Sop>>> response =
                sopSearchService.searchSOPs(request, 0, 10, "createdAt", "desc");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertFalse(response.getBody().getData().getContent().isEmpty());
    }

    @Test
    void searchSOPs_EmptyResults_ThrowsSopNotFoundException() throws Exception {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();
        request.setKeyword("nonexistent");

        Page<Sop> emptyPage = new PageImpl<>(new ArrayList<>());
        when(searchContext.executeSearch(any(SopSearchRequest.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act & Assert
        assertThrows(SopNotFoundException.class, () ->
                sopSearchService.searchSOPs(request, 0, 10, "createdAt", "desc"));
    }

    @Test
    void searchSOPs_InvalidPageSize_ThrowsException() {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();
        request.setKeyword("test");

        // Act & Assert
        assertThrows(InvalidSearchParameterException.class, () ->
                sopSearchService.searchSOPs(request, 0, 101, "createdAt", "desc"));
    }

    @Test
    void searchSOPs_NegativePageNumber_ThrowsException() {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();
        request.setKeyword("test");

        // Act & Assert
        assertThrows(InvalidSearchParameterException.class, () ->
                sopSearchService.searchSOPs(request, -1, 10, "createdAt", "desc"));
    }

    @Test
    void searchSOPs_InvalidSortDirection_ThrowsException() {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();
        request.setKeyword("test");

        // Act & Assert
        assertThrows(InvalidSearchParameterException.class, () ->
                sopSearchService.searchSOPs(request, 0, 10, "createdAt", "invalid"));
    }

    @Test
    void searchSOPs_NoSearchCriteria_ThrowsException() {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();

        // Act & Assert
        assertThrows(InvalidSearchParameterException.class, () ->
                sopSearchService.searchSOPs(request, 0, 10, "createdAt", "desc"));
    }

    @Test
    void searchSOPs_CustomSortField_Success() throws Exception {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();
        request.setKeyword("test");

        List<Sop> sops = new ArrayList<>();
        sops.add(new Sop());
        Page<Sop> sopPage = new PageImpl<>(sops);

        when(searchContext.executeSearch(any(SopSearchRequest.class), any(Pageable.class)))
                .thenReturn(sopPage);

        // Act
        ResponseEntity<SopSearchResponse<Page<Sop>>> response =
                sopSearchService.searchSOPs(request, 0, 10, "title", "asc");

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(searchContext).executeSearch(any(), argThat(pageable ->
                pageable.getSort().getOrderFor("title") != null &&
                        pageable.getSort().getOrderFor("title").getDirection() == Sort.Direction.ASC
        ));
    }

    @Test
    void searchSOPs_MetadataCorrect() throws Exception {
        // Arrange
        SopSearchRequest request = new SopSearchRequest();
        request.setKeyword("test");
        request.setDepartment("IT");

        List<Sop> sops = new ArrayList<>();
        sops.add(new Sop());
        Page<Sop> sopPage = new PageImpl<>(sops, PageRequest.of(0, 10), 1);

        when(searchContext.executeSearch(any(SopSearchRequest.class), any(Pageable.class)))
                .thenReturn(sopPage);

        // Act
        ResponseEntity<SopSearchResponse<Page<Sop>>> response =
                sopSearchService.searchSOPs(request, 0, 10, "createdAt", "desc");

        // Assert
        SearchMetadata metadata = response.getBody().getMetadata();
        assertNotNull(metadata);
        assertEquals(1, metadata.getTotalElements());
        assertEquals(1, metadata.getTotalPages());
        assertEquals(0, metadata.getCurrentPage());
        assertEquals(10, metadata.getPageSize());
        assertEquals("createdAt", metadata.getSortBy());
        assertEquals("desc", metadata.getSortOrder());
        assertTrue(metadata.getAppliedFilters().contains("keyword:test"));
        assertTrue(metadata.getAppliedFilters().contains("department:IT"));
    }

    @Test
    void clearSearchCache_Success() {
        // Act & Assert - no exception should be thrown
        assertDoesNotThrow(() -> sopSearchService.clearSearchCache());
    }
}