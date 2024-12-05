//package com.sop_content_service.sop_content_service.controller;
//
//import com.sop_content_service.sop_content_service.dto.SopSearchRequest;
//import com.sop_content_service.sop_content_service.dto.SopSearchResponse;
//import com.sop_content_service.sop_content_service.model.SopModel;
//import com.sop_content_service.sop_content_service.service.SopSearchService;
//import org.springframework.data.domain.Page;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/sops")
//public class SopSearchController {
//    private final SopSearchService searchService;
//
//    public SopSearchController(SopSearchService searchService) {
//        this.searchService = searchService;
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<SopSearchResponse<Page<SopModel>>> searchSOPs(
//            @ModelAttribute SopSearchRequest request,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdAt") String sortBy,
//            @RequestParam(defaultValue = "desc") String sortDir) throws Exception {
//        return searchService.searchSOPs(request, page, size, sortBy, sortDir);
//    }
//}