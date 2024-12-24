package com.sop_recommendation_service.sop_recommendation_service.controller;
import com.sop_recommendation_service.sop_recommendation_service.dtos.SOPGenerationRequestDTO;
import com.sop_recommendation_service.sop_recommendation_service.dtos.SOPGenerationResponseDTO;
import com.sop_recommendation_service.sop_recommendation_service.models.SOP;
import com.sop_recommendation_service.sop_recommendation_service.service.SOPGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sops")
@RequiredArgsConstructor
public class SOPController {

    private final SOPGenerationService sopGenerationService;

    @PostMapping("/generate")
    public Mono<SOPGenerationResponseDTO> generateSOP(@RequestBody SOPGenerationRequestDTO request) {
        return sopGenerationService.generateSOP(request);
    }

    @GetMapping("/{id}")
    public Mono<SOP> getSOP(@PathVariable String id) {
        return sopGenerationService.getSOP(id);
    }

    @GetMapping("/department/{department}")
    public Flux<SOP> getSOPsByDepartment(@PathVariable String department) {
        return sopGenerationService.getSOPsByDepartment(department);
    }
}
